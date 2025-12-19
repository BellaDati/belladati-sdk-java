package com.belladati.sdk.test;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A request handler that provides more streamlined access to HTTP data and
 * handles some common processing.
 * 
 * 
 */
public abstract class TestRequestHandler implements HttpRequestHandler {
	@Override
	public final void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
		String authHeader = request.getFirstHeader("Authorization").getValue();
		assertTrue(authHeader.startsWith("OAuth"), "Unexpected auth header, was " + authHeader);
		Map<String, String> authItems = new HashMap<String, String>();
		for (String item : authHeader.substring(6).split(", ")) {
			String key = item.split("=")[0];
			String value = item.split("=")[1];
			value = value.substring(1, value.length() - 1);
			authItems.put(key, value);
		}
		handle(new HttpHolder(request, response, authItems));
	}

	/**
	 * Implement your handler code here.
	 * 
	 * @param holder provides access to HTTP data.
	 */
	protected abstract void handle(HttpHolder holder) throws IOException, ParseException;

	/**
	 * Provides convenient access to HTTP data in commonly used ways.
	 * 
	 * 
	 */
	protected class HttpHolder {
		/** the HTTP request received by the server */
		public final ClassicHttpRequest request;
		/** the HTTP response being prepared by the server */
		public final ClassicHttpResponse response;
		/** authentication header parameters extracted from the request */
		public final Map<String, String> authHeaders;

		private HttpHolder(ClassicHttpRequest request, ClassicHttpResponse response, Map<String, String> authHeaders) {
			this.request = request;
			this.response = response;
			this.authHeaders = Collections.unmodifiableMap(authHeaders);
		}

		/**
		 * Asserts that the request is sending a valid authorization header
		 * using the given OAuth key and token.
		 * 
		 * @param key expected OAuth consumer key
		 * @param token expected OAuth token
		 */
		public void assertAuth(String key, String token) {
			assertEquals(authHeaders.get("oauth_consumer_key"), key, "Unexpected consumer key");
			assertEquals(authHeaders.get("oauth_token"), token, "Unexpected token");
			assertNotNull(authHeaders.get("oauth_nonce"), "Missing nonce");
			String timestampString = authHeaders.get("oauth_timestamp");
			assertNotNull(timestampString, "Missing timestamp");
			long timestamp = Long.parseLong(timestampString);
			long minTime = new Date().getTime() / 1000 - 1;
			long maxTime = new Date().getTime() / 1000 + 1;
			assertTrue(timestamp >= minTime, "Timestamp too old, was " + timestamp + " but needed at least " + minTime);
			assertTrue(timestamp <= maxTime, "Timestamp too new, was " + timestamp + " but needed at most " + maxTime);
		}

		/**
		 * Returns the body of the request.
		 * 
		 * @return the body of the request
		 * @throws IOException
		 */
		public String getRequestBody() throws IOException {
			return new String(getRequestBodyBytes());
		}

		/**
		 * Returns the body of the request.
		 * 
		 * @return the body of the request
		 * @throws IOException
		 */
		public byte[] getRequestBodyBytes() throws IOException {
			if (request instanceof ClassicHttpRequest) {
				HttpEntity entity = ((ClassicHttpRequest) request).getEntity();
				if (entity != null) {
					// EntityUtils has a direct method to get the bytes
					return EntityUtils.toByteArray(entity);
				}
			}
			return new byte[0];
		}

		/**
		 * Returns form parameters sent in the request body as a map. Call this
		 * only when dealing with a POST request containing form parameters.
		 * 
		 * @return form parameters sent in the request body
		 * @throws IOException
		 */
		public Map<String, String> getFormParameters() throws IOException, ParseException {
			Map<String, String> map = new HashMap<String, String>();
			if (request instanceof ClassicHttpRequest) {
				HttpEntity entity = ((ClassicHttpRequest) request).getEntity();
				if (entity != null) {
					// Read the entity body as a string
					String body = EntityUtils.toString(entity);

					// Parse it as URL-encoded form data
					List<NameValuePair> params = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);

					for (NameValuePair pair : params) {
						map.put(pair.getName(), pair.getValue());
					}
				}
			}
			return map;
		}

		/**
		 * Returns URL parameters sent in the request as a map. The map is empty
		 * if there are no URL parameters.
		 * 
		 * @return URL parameters sent in the request
		 */
		public Map<String, String> getUrlParameters() {
			Map<String, String> map = new HashMap<>();
			try {
				URI uri = request.getUri();
				List<NameValuePair> params = new URIBuilder(uri).getQueryParams();
				for (NameValuePair pair : params) {
					map.put(pair.getName(), pair.getValue());
				}
			} catch (URISyntaxException ignored) {};
			return map;
		}

		public void assertGet() {
			assertEquals(request.getMethod(), "GET");
		}

		public void assertPost() {
			assertEquals(request.getMethod(), "POST");
		}
	}
}