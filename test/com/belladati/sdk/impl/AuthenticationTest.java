package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.BellaDati;
import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;
import com.belladati.sdk.test.TestRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests authentication using the SDK. Currently doesn't check signatures or
 * SSL, but ensures the correct keys and tokens are sent.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class AuthenticationTest extends SDKTest {

	/** Tests OAuth authentication. */
	public void oAuth() {
		final String key = "key";
		final String secret = "secret";

		final String requestToken = "requestToken";
		final String requestSecret = "requestSecret";

		String requestTokenURI = "/oauth/requestToken";
		server.register(requestTokenURI, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.assertAuth(key, null);
				holder.response
					.setEntity(new StringEntity("oauth_token=" + requestToken + "&oauth_token_secret=" + requestSecret));
			}
		});

		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		OAuthRequest oAuth = connection.oAuth(key, secret);

		assertTrue(connection.toString().contains(server.getHttpURL()));
		assertTrue(oAuth.toString().contains(server.getHttpURL()));
		assertTrue(oAuth.toString().contains(key));
		assertFalse(oAuth.toString().contains(secret));

		final String accessToken = "accessToken";
		final String accessSecret = "accessSecret";

		String accessTokenURI = "/oauth/accessToken";
		server.register(accessTokenURI, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.assertAuth(key, requestToken);
				holder.response.setEntity(new StringEntity("oauth_token=" + accessToken + "&oauth_token_secret=" + accessSecret));
			}
		});

		BellaDatiService service = oAuth.requestAccess();
		String reportsURI = queryReports(service, key, accessToken);

		server.assertRequestUris(requestTokenURI, accessTokenURI, reportsURI);

		assertTrue(service.toString().contains(server.getHttpURL()));
		assertTrue(service.toString().contains(key));
		assertTrue(service.toString().contains(accessToken));
		assertFalse(service.toString().contains(accessSecret));
	}

	/**
	 * Auth URL is correct when there's no redirect.
	 */
	public void authUrlNoRedirect() {
		// set up connnection and server
		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		final String key = "key";
		final String requestToken = "abc123";
		server.register("/oauth/requestToken", "oauth_token=" + requestToken + "&oauth_token_secret=123abc");
		OAuthRequest request = connection.oAuth(key, "secret");

		assertEquals(request.getAuthorizationUrl().toString(), server.getHttpURL() + "/authorizeRequestToken/" + requestToken
			+ "/" + key, "Unexpected authorization URL");
	}

	/**
	 * Valid redirect URL is appended.
	 */
	public void authUrlValidRedirect() throws UnsupportedEncodingException {
		// set up connnection and server
		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		final String key = "key";
		final String requestToken = "abc123";
		server.register("/oauth/requestToken", "oauth_token=" + requestToken + "&oauth_token_secret=123abc");
		OAuthRequest request = connection.oAuth(key, "secret");

		String redirectUrl = "http://www.example.com";
		assertEquals(request.getAuthorizationUrl(redirectUrl).toString(), server.getHttpURL() + "/authorizeRequestToken/"
			+ requestToken + "/" + key + "?callbackUrl=" + URLEncoder.encode(redirectUrl, "UTF-8"),
			"Unexpected authorization URL");
	}

	/**
	 * Invalid redirect URL leads to exception.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void authUrlInvalidRedirect() {
		// set up connnection and server
		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		final String key = "key";
		final String requestToken = "abc123";
		server.register("/oauth/requestToken", "oauth_token=" + requestToken + "&oauth_token_secret=123abc");
		OAuthRequest request = connection.oAuth(key, "secret");

		String redirectUrl = "not a URL";
		assertEquals(request.getAuthorizationUrl(redirectUrl).toString(), server.getHttpURL() + "/authorizeRequestToken/"
			+ requestToken + "/" + key + "?callbackUrl=" + redirectUrl, "Unexpected authorization URL");
	}

	/** Tests xAuth authentication. */
	public void xAuth() {
		final String key = "key";
		final String secret = "secret";
		final String username = "username";
		final String password = "password";

		final String accessToken = "accessToken";
		final String accessSecret = "accessSecret";

		String accessTokenURI = "/oauth/accessToken";
		server.register(accessTokenURI, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.assertAuth(key, null);
				Map<String, String> formParams = holder.getFormParameters();
				assertEquals(formParams.get("x_auth_username"), username, "Unexpected username");
				assertEquals(formParams.get("x_auth_password"), password, "Unexpected password");
				holder.response.setEntity(new StringEntity("oauth_token=" + accessToken + "&oauth_token_secret=" + accessSecret));
			}
		});

		BellaDatiService service = BellaDati.connect(server.getHttpURL()).xAuth(key, secret, username, password);
		String reportsURI = queryReports(service, key, accessToken);

		server.assertRequestUris(accessTokenURI, reportsURI);

		assertTrue(service.toString().contains(server.getHttpURL()));
		assertTrue(service.toString().contains(key));
		assertTrue(service.toString().contains(accessToken));
		assertFalse(service.toString().contains(accessSecret));
	}

	/**
	 * Sends a query to the reports API, verifying the specified key and token
	 * are used.
	 * 
	 * @param service service to use for the query
	 * @param key consumer key
	 * @param token access token
	 * @return the server URI queried
	 */
	private String queryReports(BellaDatiService service, final String key, final String token) {
		String reportsURI = "/api/reports";
		server.register(reportsURI, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.assertAuth(key, token);
				ObjectNode node = new ObjectMapper().createObjectNode();
				node.put("size", 1).put("offset", 0).put("reports", new ObjectMapper().createArrayNode());
				holder.response.setEntity(new StringEntity(node.toString()));
			}
		});
		service.getReportInfo().load();
		return reportsURI;
	}
}
