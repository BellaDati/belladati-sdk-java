package com.belladati.sdk.util.impl;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import org.apache.http.entity.ByteArrayEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;

@Test(dataProvider = "uris")
public class CustomRequestTest extends SDKTest {

	private final String serverUri = "/some/uri";
	private final byte[] body = new byte[] { 'b', 'o', 'd', 'y' };
	private final byte[] returnValue = new byte[] { 'r', 'e', 't', 'u', 'r', 'n' };
	private final Map<String, String> params = Collections.singletonMap("key", "value");

	private final String brokenUri = "not a uri";

	public void post(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				assertEquals(holder.getFormParameters(), Collections.emptyMap());
				holder.assertPost();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().post(uri), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void postUriParams(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), params);
				assertEquals(holder.getFormParameters(), Collections.emptyMap());
				holder.assertPost();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().post(uri, params), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void postBody(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				assertEquals(holder.getRequestBodyBytes(), body);
				assertEquals(holder.getFormParameters(), Collections.emptyMap());
				holder.assertPost();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().post(uri, body), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void postUriParamsBody(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), params);
				assertEquals(holder.getRequestBodyBytes(), body);
				assertEquals(holder.getFormParameters(), Collections.emptyMap());
				holder.assertPost();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().post(uri, params, body), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void postFormParams(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				assertEquals(holder.getFormParameters(), params);
				holder.assertPost();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().postForm(uri, params), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void postUriFormParams(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), params);
				assertEquals(holder.getFormParameters(), params);
				holder.assertPost();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().postForm(uri, params, params), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void get(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.assertGet();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().get(uri), returnValue);

		server.assertRequestUris(serverUri);
	}

	public void getUriParams(String uri) throws URISyntaxException {
		server.register(serverUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), params);
				assertEquals(holder.getFormParameters(), Collections.emptyMap());
				holder.assertGet();
				holder.response.setEntity(new ByteArrayEntity(returnValue));
			}
		});

		assertEquals(getService().get(uri, params), returnValue);

		server.assertRequestUris(serverUri);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenPost() throws URISyntaxException {
		getService().post(brokenUri);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenPostUriParams() throws URISyntaxException {
		getService().post(brokenUri, params);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenPostBody() throws URISyntaxException {
		getService().post(brokenUri, body);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenPostUriParamsBody() throws URISyntaxException {
		getService().post(brokenUri, params, body);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenPostFormParams() throws URISyntaxException {
		getService().postForm(brokenUri, params);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenPostUriFormParams() throws URISyntaxException {
		getService().postForm(brokenUri, params, params);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenGet() throws URISyntaxException {
		getService().get(brokenUri);
	}

	@Test(expectedExceptions = URISyntaxException.class)
	public void brokenGetUriParams() throws URISyntaxException {
		getService().get(brokenUri, params);
	}

	@DataProvider(name = "uris")
	protected Object[][] uriProvider() {
		return new Object[][] { { "/some/uri" }, { "some/uri" } };
	}
}
