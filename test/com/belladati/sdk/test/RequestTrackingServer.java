package com.belladati.sdk.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A test server that keeps track of any requests received.
 * 
 * @author Chris Hennigfeld
 */
public class RequestTrackingServer extends LocalTestServer {

	/** contains all requests received, in order */
	private List<String> requestUris = Collections.synchronizedList(new ArrayList<String>());

	public RequestTrackingServer() {
		super(null);
		register("/*", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
				// do nothing, this is just a dummy to track the URI
			}
		});
	}

	@Override
	public void register(String pattern, final HttpRequestHandler handler) {
		super.register(pattern, new HttpRequestHandler() {

			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
				// wrap the original handler to track the request
				String uri = request.getRequestLine().getUri();
				requestUris.add(uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri);
				// never cache during tests
				response.addHeader("Cache-Control", "no-cache");
				handler.handle(request, response, context);
			}
		});
	}

	public void register(String pattern, final String responseContent) {
		register(pattern, new HttpRequestHandler() {

			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
				response.setEntity(new StringEntity(responseContent));
			}
		});
	}

	public void register(String pattern, final HttpEntity entity) {
		register(pattern, new HttpRequestHandler() {

			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
				response.setEntity(entity);
			}
		});
	}

	/**
	 * Tells the server to return the specified node as the only item node in a
	 * paginated collection using the given field name.
	 * 
	 * @param pattern URI pattern to register
	 * @param field JSON field name containing the array of items
	 * @param item the node to return
	 */
	public void registerPaginatedItem(String pattern, String field, JsonNode item) {
		ObjectMapper mapper = new ObjectMapper();
		final ObjectNode root = mapper.createObjectNode().put("size", 1).put("offset", 0);
		ArrayNode elements = mapper.createArrayNode();
		elements.add(item);
		root.put(field, elements);

		register(pattern, root.toString());
	}

	/**
	 * Tells the server to return the specified error code and content.
	 * 
	 * @param pattern URI pattern to register
	 * @param code HTTP status code to return
	 * @param content the content to return
	 */
	public void registerError(String pattern, final int code, final String content) {
		register(pattern, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setStatusCode(code);
				holder.response.setEntity(new StringEntity(content));
			}
		});
	}

	/**
	 * Returns all request URIs queried.
	 * 
	 * @return all request URIs queried
	 */
	public List<String> getRequestUris() {
		return Collections.unmodifiableList(requestUris);
	}

	/**
	 * Asserts that the queried request URIs match the expected URIs.
	 * 
	 * @param expectedURIs the expected query URIs, in order
	 */
	public void assertRequestUris(String... expectedURIs) {
		assertEquals(requestUris, Arrays.asList(expectedURIs), "Unexpected request URIs");
	}

	/**
	 * Resets the list of queried request URIs.
	 */
	public void resetRequestUris() {
		requestUris.clear();
	}

	/**
	 * Convenience method to get the server's HTTP URL.
	 * 
	 * @return the server's HTTP URL
	 */
	public String getHttpURL() {
		return "http:/" + getServiceAddress();
	}
}