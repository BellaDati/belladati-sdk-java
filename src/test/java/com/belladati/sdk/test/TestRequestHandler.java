package com.belladati.sdk.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * A request handler that provides more streamlined access to HTTP data and
 * handles some common processing.
 * 
 * @author Chris Hennigfeld
 */
public abstract class TestRequestHandler implements HttpRequestHandler {
	@Override
	public final void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
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
	protected abstract void handle(HttpHolder holder) throws IOException;

	/**
	 * Provides convenient access to HTTP data in commonly used ways.
	 * 
	 * @author Chris Hennigfeld
	 */
	protected class HttpHolder {
		/** the HTTP request received by the server */
		public final HttpRequest request;
		/** the HTTP response being prepared by the server */
		public final HttpResponse response;
		/** authentication header parameters extracted from the request */
		public final Map<String, String> authHeaders;

		private HttpHolder(HttpRequest request, HttpResponse response, Map<String, String> authHeaders) {
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
			if (request instanceof HttpEntityEnclosingRequest) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					((HttpEntityEnclosingRequest) request).getEntity().writeTo(baos);
					return baos.toByteArray();
				} finally {
					baos.close();
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
		public Map<String, String> getFormParameters() throws IOException {
			Map<String, String> map = new HashMap<String, String>();
			if (request instanceof HttpEntityEnclosingRequest) {
				for (NameValuePair pair : URLEncodedUtils.parse(((HttpEntityEnclosingRequest) request).getEntity())) {
					map.put(pair.getName(), pair.getValue());
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
			Map<String, String> map = new HashMap<String, String>();
			String uri = request.getRequestLine().getUri();
			if (uri.contains("?")) {
				String paramString = uri.substring(uri.indexOf("?") + 1);
				for (NameValuePair pair : URLEncodedUtils.parse(paramString, Charset.defaultCharset())) {
					map.put(pair.getName(), pair.getValue());
				}
			}
			return map;
		}

		public void assertGet() {
			assertEquals(request.getRequestLine().getMethod(), "GET");
		}

		public void assertPost() {
			assertEquals(request.getRequestLine().getMethod(), "POST");
		}
	}
}