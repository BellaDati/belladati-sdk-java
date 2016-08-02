package com.belladati.sdk.domain.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.domain.DomainEditBuilder;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;

@Test
public class DomainEditBuilderTest extends SDKTest {

	private final String id = "666";
	private final String requestUri = "/api/domains/" + id;

	private final String description = "domain description";
	private final String dateFormat = "yyyy-MM-dd";
	private final String timeFormat = "hh:mm:ss";
	private final String timeZone = "Asia/Hong_Kong";
	private final String locale = "EN";
	private final String paramKey = "myKey";
	private final String paramValue = "myValue";

	private DomainEditBuilder builder;

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		builder = service.setupDomainEditBuilder(id);

		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().get("data"), builder.toJson().toString());
				holder.response.setEntity(new StringEntity(""));
			}
		});
	}

	public void postToServer_empty() {
		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, "");
	}

	public void postToServer_medium() {
		builder.setDescription(description);
		builder.addParameter(paramKey, paramValue);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, "");
	}

	public void postToServer_full() {
		builder.setDescription(description);
		builder.setDateFormat(dateFormat);
		builder.setTimeFormat(timeFormat);
		builder.setTimeZone(timeZone);
		builder.setLocale(locale);
		builder.addParameter(paramKey, paramValue);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, "");
	}

	/** can't post again after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void postAfterPost() {
		String result = null;

		result = builder.post();
		assertEquals(result, "");

		result = builder.post();
		fail("Previous request should fail");
	}

}
