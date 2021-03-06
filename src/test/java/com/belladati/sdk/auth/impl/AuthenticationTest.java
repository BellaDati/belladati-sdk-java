package com.belladati.sdk.auth.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.BellaDati;
import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;
import com.belladati.sdk.test.SDKTest;
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
	public void oAuth() throws Exception {
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
		String reportsURI = registerQueryReports(key, accessToken);
		server.start();

		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		OAuthRequest oAuth = connection.oAuth(key, secret);

		assertTrue(connection.toString().contains(server.getHttpURL()));
		assertTrue(oAuth.toString().contains(server.getHttpURL()));
		assertTrue(oAuth.toString().contains(key));
		assertFalse(oAuth.toString().contains(secret));

		BellaDatiService service = oAuth.requestAccess();

		service.getReportInfo().load();
		server.assertRequestUris(requestTokenURI, accessTokenURI, reportsURI);

		assertTrue(service.toString().contains(server.getHttpURL()));
		assertTrue(service.toString().contains(key));
		assertTrue(service.toString().contains(accessToken));
		assertFalse(service.toString().contains(accessSecret));
	}

	/**
	 * Auth URL is correct when there's no redirect.
	 */
	public void authUrlNoRedirect() throws Exception {
		// set up connnection and server
		final String key = "key";
		final String requestToken = "abc123";
		server.register("/oauth/requestToken", "oauth_token=" + requestToken + "&oauth_token_secret=123abc");
		server.start();

		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		OAuthRequest request = connection.oAuth(key, "secret");

		assertEquals(request.getAuthorizationUrl().toString(),
			server.getHttpURL() + "/authorizeRequestToken/" + requestToken + "/" + key, "Unexpected authorization URL");
	}

	/**
	 * Valid redirect URL is appended.
	 */
	public void authUrlValidRedirect() throws Exception {
		// set up connnection and server
		final String key = "key";
		final String requestToken = "abc123";
		final String redirectUrl = "http://www.example.com";

		server.register("/oauth/requestToken", new TestRequestHandler() {

			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.authHeaders.get("oauth_callback"), URLEncoder.encode(redirectUrl, "UTF-8"), "Wrong callback");
				assertEquals(holder.getRequestBody(), "");
				holder.response.setEntity(new StringEntity("oauth_token=" + requestToken + "&oauth_token_secret=123abc"));
			}
		});
		server.start();
		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		OAuthRequest request = connection.oAuth(key, "secret", redirectUrl);

		assertEquals(request.getAuthorizationUrl().toString(),
			server.getHttpURL() + "/authorizeRequestToken/" + requestToken + "/" + key, "Unexpected authorization URL");
	}

	/**
	 * Invalid redirect URL leads to exception.
	 * @throws Exception 
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void authUrlInvalidRedirect() throws Exception {
		// set up connnection and server
		final String requestToken = "abc123";
		server.register("/oauth/requestToken", "oauth_token=" + requestToken + "&oauth_token_secret=123abc");
		server.start();
		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		final String key = "key";
		String redirectUrl = "not a URL";
		connection.oAuth(key, "secret", redirectUrl);
	}

	/** Tests xAuth authentication. */
	public void xAuth() throws Exception {
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
		String reportsURI = registerQueryReports(key, accessToken);
		server.start();

		BellaDatiService service = BellaDati.connectInsecure(server.getHttpURL()).xAuth(key, secret, username, password);
		service.getReportInfo().load();
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
	private String registerQueryReports(final String key, final String token) throws Exception {
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
		return reportsURI;
	}
}
