package com.belladati.sdk.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;

import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.exception.auth.AuthorizationException;
import com.belladati.sdk.exception.auth.AuthorizationException.Reason;
import com.belladati.sdk.exception.auth.InvalidTimestampException;
import com.belladati.sdk.exception.server.InternalErrorException;
import com.belladati.sdk.exception.server.InvalidJsonException;
import com.belladati.sdk.exception.server.NotFoundException;
import com.belladati.sdk.exception.server.ServerResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class BellaDatiClient implements Serializable {

	/** The serialVersionUID */
	private static final long serialVersionUID = 9138881190417975299L;

	private final String baseUrl;
	private final boolean trustSelfSigned;

	private final transient HttpClient client;

	BellaDatiClient(String baseUrl, boolean trustSelfSigned) {
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : (baseUrl + "/");
		this.trustSelfSigned = trustSelfSigned;
		this.client = buildClient(trustSelfSigned);
	}

	/**
	 * Builds the HTTP client to connect to the server.
	 * 
	 * @param trustSelfSigned <tt>true</tt> if the client should accept
	 *            self-signed certificates
	 * @return a new client instance
	 */
	private HttpClient buildClient(boolean trustSelfSigned) {
		try {
			// if required, define custom SSL context allowing self-signed certs
			SSLContext sslContext = !trustSelfSigned ? SSLContexts.createSystemDefault() : SSLContexts.custom()
				.loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();

			// set timeouts for the HTTP client
			int globalTimeout = readFromProperty("bdTimeout", 10000);
			int connectTimeout = readFromProperty("bdConnectTimeout", globalTimeout);
			int connectionRequestTimeout = readFromProperty("bdConnectionRequestTimeout", globalTimeout);
			int socketTimeout = readFromProperty("bdSocketTimeout", globalTimeout);
			RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT).setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();

			// configure caching
			CacheConfig cacheConfig = CacheConfig.copy(CacheConfig.DEFAULT).setSharedCache(false).setMaxCacheEntries(1000)
				.setMaxObjectSize(2 * 1024 * 1024).build();

			// create the HTTP client
			return CachingHttpClientBuilder.create().setCacheConfig(cacheConfig).setSslcontext(sslContext)
				.setDefaultRequestConfig(requestConfig).build();
		} catch (GeneralSecurityException e) {
			throw new InternalConfigurationException("Failed to set up SSL context", e);
		}
	}

	private int readFromProperty(String property, int defaultValue) {
		try {
			return Integer.parseInt(System.getProperty(property));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public byte[] post(String relativeUrl, TokenHolder tokenHolder) {
		return post(relativeUrl, tokenHolder, Collections.<NameValuePair> emptyList());
	}

	public byte[] post(String relativeUrl, TokenHolder tokenHolder, List<? extends NameValuePair> parameters) {
		HttpPost post = new HttpPost(baseUrl + relativeUrl);
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Invalid URL encoding", e);
		}
		return doRequest(post, tokenHolder);
	}

	public TokenHolder postToken(String relativeUrl, TokenHolder tokenHolder) {
		return postToken(relativeUrl, tokenHolder, Collections.<NameValuePair> emptyList());
	}

	public TokenHolder postToken(String relativeUrl, TokenHolder tokenHolder, List<? extends NameValuePair> parameters) {
		byte[] response = post(relativeUrl, tokenHolder, parameters);
		try {
			HttpParameters oauthParams = OAuth.decodeForm(new ByteArrayInputStream(response));
			String token = oauthParams.getFirst(OAuth.OAUTH_TOKEN);
			String tokenSecret = oauthParams.getFirst(OAuth.OAUTH_TOKEN_SECRET);
			tokenHolder.setToken(token, tokenSecret);
			return tokenHolder;
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to load OAuth token from response", e);
		}
	}

	public byte[] get(String relativeUrl, TokenHolder tokenHolder) {
		return doRequest(new HttpGet(baseUrl + relativeUrl), tokenHolder);
	}

	public JsonNode getJson(String relativeUrl, TokenHolder tokenHolder) {
		byte[] response = get(relativeUrl, tokenHolder);
		try {
			return new ObjectMapper().readTree(response);
		} catch (IOException e) {
			throw new InvalidJsonException("Could not parse JSON response, was " + new String(response), e);
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	private byte[] doRequest(HttpRequestBase request, TokenHolder tokenHolder) {
		try {
			tokenHolder.createConsumer().sign(request);
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			byte[] content = entity != null ? readBytes(entity.getContent()) : new byte[0];
			switch (statusCode) {
			case 200:
			case 204:
				// all is well, return
				return content;
				// there was some sort of error - throw the right exception
			case 400:
			case 401:
			case 403:
				throw buildException(content, tokenHolder.hasToken());
			case 404:
				throw new NotFoundException(request.getRequestLine().getUri());
			case 500:
				throw new InternalErrorException();
			default:
				throw new ServerResponseException("Unknown error, status: " + statusCode + ", content: " + new String(content));
			}
		} catch (OAuthException e) {
			throw new InternalConfigurationException("Failed to create OAuth signature", e);
		} catch (IOException e) {
			throw new ConnectionException("Failed to connect to BellaDati", e);
		} finally {
			request.releaseConnection();
		}
	}

	/**
	 * Builds an exception based on the given content, assuming that it has been
	 * returned as an error from the server.
	 * 
	 * @param content content returned by the server
	 * @param hasToken <tt>true</tt> if the request was made using a request or
	 *            access token
	 * @return an exception to throw for the given content
	 */
	private BellaDatiRuntimeException buildException(byte[] content, boolean hasToken) {
		try {
			HttpParameters oauthParams = OAuth.decodeForm(new ByteArrayInputStream(content));
			if (oauthParams.containsKey("oauth_problem")) {
				String problem = oauthParams.getFirst("oauth_problem");
				if ("missing_consumer".equals(problem) || "invalid_consumer".equals(problem)) {
					return new AuthorizationException(Reason.CONSUMER_KEY_UNKNOWN);
				} else if ("invalid_signature".equals(problem) || "signature_invalid".equals(problem)) {
					return new AuthorizationException(hasToken ? Reason.TOKEN_INVALID : Reason.CONSUMER_SECRET_INVALID);
				} else if ("domain_expired".equals(problem)) {
					return new AuthorizationException(Reason.DOMAIN_EXPIRED);
				} else if ("missing_token".equals(problem) || "invalid_token".equals(problem)) {
					return new AuthorizationException(Reason.TOKEN_INVALID);
				} else if ("unauthorized_token".equals(problem)) {
					return new AuthorizationException(Reason.TOKEN_UNAUTHORIZED);
				} else if ("token_expired".equals(problem)) {
					return new AuthorizationException(Reason.TOKEN_EXPIRED);
				} else if ("x_auth_disabled".equals(problem)) {
					return new AuthorizationException(Reason.X_AUTH_DISABLED);
				} else if ("piccolo_not_enabled".equals(problem)) {
					return new AuthorizationException(Reason.BD_MOBILE_DISABLED);
				} else if ("missing_username".equals(problem) || "missing_password".equals(problem)
					|| "invalid_credentials".equals(problem) || "permission_denied".equals(problem)) {
					return new AuthorizationException(Reason.USER_CREDENTIALS_INVALID);
				} else if ("account_locked".equals(problem) || "user_not_active".equals(problem)) {
					return new AuthorizationException(Reason.USER_ACCOUNT_LOCKED);
				} else if ("domain_restricted".equals(problem)) {
					return new AuthorizationException(Reason.USER_DOMAIN_MISMATCH);
				} else if ("timestamp_refused".equals(problem)) {
					String acceptable = oauthParams.getFirst("oauth_acceptable_timestamps");
					if (acceptable != null && acceptable.contains("-")) {
						return new InvalidTimestampException(Long.parseLong(acceptable.split("-")[0]), Long.parseLong(acceptable
							.split("-")[1]));
					}
				}
				return new AuthorizationException(Reason.OTHER, problem);
			}
			return new ServerResponseException("Unexpected server response: " + new String(content));
		} catch (IOException e) {
			throw new ServerResponseException("Response didn't contain valid error content, was " + new String(content), e);
		}

	}

	private static byte[] readBytes(InputStream in) throws IOException {
		int len;
		byte[] buffer = new byte[128];
		ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
		while ((len = in.read(buffer, 0, buffer.length)) != -1) {
			buf.write(buffer, 0, len);
		}
		buf.flush();
		return buf.toByteArray();
	}

	/** Deserialization. Sets up an HTTP client instance. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			Field client = getClass().getDeclaredField("client");
			client.setAccessible(true);
			client.set(this, buildClient(trustSelfSigned));
		} catch (NoSuchFieldException e) {
			throw new InternalConfigurationException("Failed to set client fields", e);
		} catch (IllegalAccessException e) {
			throw new InternalConfigurationException("Failed to set client fields", e);
		} catch (SecurityException e) {
			throw new InternalConfigurationException("Failed to set client fields", e);
		} catch (IllegalArgumentException e) {
			throw new InternalConfigurationException("Failed to set client fields", e);
		}
	}
}
