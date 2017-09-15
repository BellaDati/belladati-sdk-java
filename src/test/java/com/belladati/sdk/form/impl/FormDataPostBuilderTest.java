package com.belladati.sdk.form.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.form.FormDataPostBuilder;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;

@Test
public class FormDataPostBuilderTest extends SDKTest {

	private final String formId = "123";
	private final String requestUri = "/api/import/forms/" + formId;
	private final String result = "";

	private final String textId = "abcd1001";
	private final String textValue = "first name";
	private final String numberId = "abcd1002";
	private final BigDecimal numberValue = new BigDecimal(1234567890);
	private final String booleanId = "abcd1003";
	private final boolean booleanValue = true;

	private FormDataPostBuilder builder;

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().get("data"), builder.toJson().toString());
				holder.response.setEntity(new StringEntity(result));
			}
		});
		builder = getService().setupFormDataPostBuilder(formId);
	}

	public void postToServer_empty() {
		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, result);
	}

	public void postToServer_null() {
		builder.addTextValue(textId, null);
		builder.addNumberValue(numberId, null);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, result);
	}

	public void postToServer_full() {
		builder.addTextValue(textId, textValue);
		builder.addNumberValue(numberId, numberValue);
		builder.addBooleanValue(booleanId, booleanValue);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, result);
	}

	/** can't post again after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void postAfterPost() {
		String result = null;

		result = builder.post();
		assertEquals(result, result);

		result = builder.post();
		fail("Previous request should fail");
	}

}
