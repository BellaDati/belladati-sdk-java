package com.belladati.sdk.domain.impl;

import com.belladati.sdk.domain.DomainCreateBuilder;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Test
public class DomainCreateBuilderTest extends SDKTest {

	private final String requestUri = "/api/domains/create";

	private final String id = "666";
	private final String name = "domainname";
	private final String description = "domain description";
	private final String dateFormat = "yyyy-MM-dd";
	private final String timeFormat = "hh:mm:ss";
	private final String timeZone = "Asia/Hong_Kong";
	private final String locale = "EN";
	private final String paramKey = "myKey";
	private final String paramValue = "myValue";
	private final String templateId = "templateId";
	private final String usernameSuffix = "_MySuffix";

	private DomainCreateBuilder builder;

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getFormParameters().get("data"), builder.toJson().toString());
				holder.response.setEntity(new StringEntity(id));
			}
		});
		builder = getService().setupDomainCreateBuilder();
	}

	public void postToServer_empty() {
		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, id);
	}

	public void postToServer_medium() {
		builder.setName(name);
		builder.setDescription(description);
		builder.addParameter(paramKey, paramValue);
		builder.setTemplate(id, null);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, id);
	}

	public void postToServer_full() {
		builder.setName(name);
		builder.setDescription(description);
		builder.setDateFormat(dateFormat);
		builder.setTimeFormat(timeFormat);
		builder.setTimeZone(timeZone);
		builder.setLocale(locale);
		builder.addParameter(paramKey, paramValue);
		builder.setTemplate(id, usernameSuffix);
		builder.setTemplateId(templateId);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, id);
	}

	/** can't post again after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void postAfterPost() {
		String result = null;

		result = builder.post();
		assertEquals(result, id);

		result = builder.post();
		fail("Previous request should fail");
	}

}
