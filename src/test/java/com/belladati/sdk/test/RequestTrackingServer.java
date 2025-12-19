package com.belladati.sdk.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * A test server that keeps track of any requests received.
 * 
 *
 */
public class RequestTrackingServer {

	private static final Logger log = LoggerFactory.getLogger(RequestTrackingServer.class);
	/** contains all requests received, in order */
	private List<String> requestUris = Collections.synchronizedList(new ArrayList<>());
	private final ServerBootstrap serverBootstrap;
	private HttpServer server;

	public RequestTrackingServer() throws Exception {
		this.serverBootstrap = ServerBootstrap.bootstrap();


		register("/*", (request, response, context) -> {
			System.err.println(request.getRequestUri());
			response.setCode(200);
		});

		this.server = serverBootstrap.create();
		start();
	}

	public void start() throws IOException {
		this.server.start();
	}

	public void stop() {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	public void register(String pattern, final HttpRequestHandler handler) {
		if (server != null) {
			stop();
			//throw new RuntimeException("Cannot register hander when server is running");
		}
		serverBootstrap.register(pattern, (ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) -> {
			String uri = request.getRequestUri();
			requestUris.add(uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri);
			response.addHeader("Cache-Control", "no-cache");
			handler.handle(request, response, context);
		});
		try {
			this.server = serverBootstrap.create();
			start();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void register(String pattern, final String responseContent) {
		register(pattern, (ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) -> {
			response.setCode(200);
			response.setEntity(new StringEntity(responseContent));
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
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				holder.response.setCode(code);
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
		return "http://localhost:" + server.getLocalPort();
	}
}