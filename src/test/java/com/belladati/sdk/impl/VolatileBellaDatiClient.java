package com.belladati.sdk.impl;

import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.exception.InvalidImplementationException;
import com.belladati.sdk.exception.auth.AuthorizationException;
import com.belladati.sdk.exception.auth.AuthorizationException.Reason;
import com.belladati.sdk.exception.auth.InvalidTimestampException;
import com.belladati.sdk.exception.server.InternalErrorException;
import com.belladati.sdk.exception.server.InvalidJsonException;
import com.belladati.sdk.exception.server.InvalidStreamException;
import com.belladati.sdk.exception.server.MethodNotAllowedException;
import com.belladati.sdk.exception.server.NotFoundException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.belladati.sdk.util.MultipartPiece;
import com.belladati.sdk.util.impl.MultipartFileImpl;
import com.belladati.sdk.util.impl.MultipartTextImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CachingHttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * 
 */
public class VolatileBellaDatiClient extends BellaDatiClient {
	private static final long serialVersionUID = 1483144025894079717L;

	private String baseUrl;
	private final boolean trustSelfSigned;

	private transient CloseableHttpClient client;

	public VolatileBellaDatiClient(String baseUrl, boolean trustSelfSigned) {
		super(baseUrl, trustSelfSigned);
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
	private CloseableHttpClient buildClient(boolean trustSelfSigned) {
		try {
			// if required, define custom SSL context allowing self-signed certs
			SSLContext sslContext = !trustSelfSigned ? SSLContexts.createSystemDefault()
				: SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();

			// set timeouts for the HTTP client
			int globalTimeout = readFromProperty("bdTimeout", 100000);
			int connectTimeout = readFromProperty("bdConnectTimeout", globalTimeout);
			int connectionRequestTimeout = readFromProperty("bdConnectionRequestTimeout", globalTimeout);
			int socketTimeout = readFromProperty("bdSocketTimeout", globalTimeout);

			RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
					.setResponseTimeout(Timeout.ofMilliseconds(socketTimeout))
					.setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeout)).build();


			// configure caching
			CacheConfig cacheConfig = CacheConfig.custom()
					.setSharedCache(false)
					.setMaxCacheEntries(1000)
					.setMaxObjectSize(2 * 1024 * 1024L) // note: long in Client5
					.build();


			ConnectionConfig connectionCofig = ConnectionConfig.custom()
					.setSocketTimeout(Timeout.ofMilliseconds(socketTimeout)) // read timeout
					.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout)) // connection establishment timeout
					.build();

			// configure connection pooling with modern builder
			PoolingHttpClientConnectionManager connManager =
					PoolingHttpClientConnectionManagerBuilder.create()
							.setDefaultConnectionConfig(connectionCofig)
							.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext))
							.build();

			int connectionLimit = readFromProperty("bdMaxConnections", 40);
			connManager.setMaxTotal(connectionLimit);
			connManager.setDefaultMaxPerRoute(connectionLimit);

// optional: socket config
			SocketConfig socketConfig = SocketConfig.custom()
					.setSoTimeout(Timeout.ofSeconds(30))
					.build();
			connManager.setDefaultSocketConfig(socketConfig);

// create the HTTP client
			return CachingHttpClients.custom()
					.setCacheConfig(cacheConfig)
					.setDefaultRequestConfig(requestConfig)
					.setConnectionManager(connManager)
					.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(2)))
					.build();
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

	public byte[] delete(String relativeUrl, TokenHolder tokenHolder) {
		return delete(relativeUrl, tokenHolder, null);
	}

	public byte[] delete(String relativeUrl, TokenHolder tokenHolder, HttpParameters oauthParams) {
		HttpDelete delete = new HttpDelete(baseUrl + relativeUrl);
		return doRequest(delete, tokenHolder, oauthParams);
	}

	public byte[] post(String relativeUrl, TokenHolder tokenHolder) {
		return post(relativeUrl, tokenHolder, null, null);
	}

	public byte[] post(String relativeUrl, TokenHolder tokenHolder, HttpParameters oauthParams) {
		return post(relativeUrl, tokenHolder, oauthParams, null);
	}

	public byte[] post(String relativeUrl, TokenHolder tokenHolder, List<? extends NameValuePair> parameters) {
		return post(relativeUrl, tokenHolder, null, parameters);
	}

	public byte[] post(String relativeUrl, TokenHolder tokenHolder, HttpParameters oauthParams,
		List<? extends NameValuePair> parameters) {
		HttpPost post = new HttpPost(baseUrl + relativeUrl);
		if (parameters != null) {
			post.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
		}
		return doRequest(post, tokenHolder, oauthParams);
	}

	public byte[] postMultipart(String relativeUrl, TokenHolder tokenHolder, List<? extends MultipartPiece<?>> multipart) {
		return postMultipart(relativeUrl, tokenHolder, null, multipart);
	}

	public byte[] postMultipart(String relativeUrl, TokenHolder tokenHolder, HttpParameters oauthParams,
		List<? extends MultipartPiece<?>> multipart) {
		HttpPost post = new HttpPost(baseUrl + relativeUrl);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.LEGACY);

		for (MultipartPiece<?> part : multipart) {
			ContentType contentType = ContentType.create(part.getContentType());

			ContentBody contentBody = null;
			if (part instanceof MultipartFileImpl) {
				MultipartFileImpl filePart = (MultipartFileImpl) part;
				contentBody = new FileBody(filePart.getValue(), contentType, filePart.getFilename());
			} else if (part instanceof MultipartTextImpl) {
				MultipartTextImpl textPart = (MultipartTextImpl) part;
				contentBody = new StringBody(textPart.getValue(), contentType);
			} else {
				throw new InvalidImplementationException(
					"Unknown type " + contentType + " and class " + part.getClass().getSimpleName());
			}
			builder.addPart(part.getName(), contentBody);
		}
		post.setEntity(builder.build());

		return doRequest(post, tokenHolder, oauthParams);
	}

	public byte[] postUpload(String relativeUrl, TokenHolder tokenHolder, String content) {
		HttpPost post = new HttpPost(baseUrl + relativeUrl);
		StringEntity entity = new StringEntity(content, ContentType.APPLICATION_OCTET_STREAM);
		post.setEntity(entity);
		return doRequest(post, tokenHolder);
	}

	public byte[] postData(String relativeUrl, TokenHolder tokenHolder, byte[] content) {
		HttpPost post = new HttpPost(baseUrl + relativeUrl);
		ByteArrayEntity entity = new ByteArrayEntity(content, ContentType.APPLICATION_OCTET_STREAM);
		post.setEntity(entity);
		return doRequest(post, tokenHolder);
	}

	public TokenHolder postToken(String relativeUrl, TokenHolder tokenHolder) {
		return postToken(relativeUrl, tokenHolder, null, null);
	}

	public TokenHolder postToken(String relativeUrl, TokenHolder tokenHolder, HttpParameters oauthParams) {
		return postToken(relativeUrl, tokenHolder, oauthParams, null);
	}

	public TokenHolder postToken(String relativeUrl, TokenHolder tokenHolder, List<? extends NameValuePair> parameters) {
		return postToken(relativeUrl, tokenHolder, null, parameters);
	}

	public TokenHolder postToken(String relativeUrl, TokenHolder tokenHolder, HttpParameters oauthParams,
		List<? extends NameValuePair> parameters) {
		byte[] response = post(relativeUrl, tokenHolder, oauthParams, parameters);
		try {
			HttpParameters responseParams = OAuth.decodeForm(new ByteArrayInputStream(response));
			String token = responseParams.getFirst(OAuth.OAUTH_TOKEN);
			String tokenSecret = responseParams.getFirst(OAuth.OAUTH_TOKEN_SECRET);
			tokenHolder.setToken(token, tokenSecret);
			return tokenHolder;
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to load OAuth token from response", e);
		}
	}

	public byte[] get(String relativeUrl, TokenHolder tokenHolder) {
		return doRequest(new HttpGet(baseUrl + relativeUrl), tokenHolder);
	}

	public JsonNode getAsJson(String relativeUrl, TokenHolder tokenHolder) throws InvalidJsonException {
		byte[] response = get(relativeUrl, tokenHolder);
		try {
			return new ObjectMapper().readTree(response);
		} catch (IOException e) {
			throw new InvalidJsonException("Could not parse JSON response, was " + new String(response), e);
		}
	}

	public ByteArrayInputStream getAsStream(String relativeUrl, TokenHolder tokenHolder) {
		byte[] response = get(relativeUrl, tokenHolder);
		return new ByteArrayInputStream(response);
	}

	public BufferedImage getAsImage(String relativeUrl, TokenHolder tokenHolder) throws InvalidStreamException {
		ByteArrayInputStream bais = getAsStream(relativeUrl, tokenHolder);
		try {
			BufferedImage image = ImageIO.read(bais);
			if (image == null) {
				throw new IOException("Loaded image is null");
			}
			return image;
		} catch (IOException e) {
			throw new InvalidStreamException("Could not parse image response", e);
		} finally {
			closeQuietly(bais);
		}
	}

	private void closeQuietly(Closeable c) {
		if (c == null) {
			return;
		} else {
			try {
				c.close();
			} catch (IOException e) {}
		}
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		client = buildClient(trustSelfSigned);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	private byte[] doRequest(HttpUriRequestBase request, TokenHolder tokenHolder) {
		return doRequest(request, tokenHolder, null);
	}

	private byte[] doRequest(HttpUriRequestBase request, TokenHolder tokenHolder, HttpParameters oauthParams) {
		CloseableHttpResponse response = null;
		try {
			request.setHeader("Connection", "close");
			OAuthConsumer consumer = tokenHolder.createConsumer();
			consumer.setAdditionalParameters(oauthParams);
			consumer.sign(request);
			response = client.execute(request);
			int statusCode = response.getCode();
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
				throw buildException(statusCode, content, tokenHolder.hasToken());
			case 404:
				throw new NotFoundException(request.getRequestUri());
			case 405:
				throw new MethodNotAllowedException(request.getRequestUri());
			case 500:
				throw new InternalErrorException();
			default:
				throw new UnexpectedResponseException(statusCode, new String(content));
			}
		} catch (OAuthException e) {
			throw new InternalConfigurationException("Failed to create OAuth signature", e);
		} catch (IOException e) {
			throw new ConnectionException("Failed to connect to BellaDati", e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				throw new ConnectionException("Failed to connect to BellaDati", e);
			}
		}
	}

	/**
	 * Builds an exception based on the given content, assuming that it has been
	 * returned as an error from the server.
	 * 
	 * @param code response code returned by the server
	 * @param content content returned by the server
	 * @param hasToken <tt>true</tt> if the request was made using a request or
	 *            access token
	 * @return an exception to throw for the given content
	 */
	private BellaDatiRuntimeException buildException(int code, byte[] content, boolean hasToken) {
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
						return new InvalidTimestampException(Long.parseLong(acceptable.split("-")[0]),
							Long.parseLong(acceptable.split("-")[1]));
					}
				}
				return new AuthorizationException(Reason.OTHER, problem);
			}
			return new UnexpectedResponseException(code, new String(content));
		} catch (IOException e) {
			throw new UnexpectedResponseException(code, new String(content), e);
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
