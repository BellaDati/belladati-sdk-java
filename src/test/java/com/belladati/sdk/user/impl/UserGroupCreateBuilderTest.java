package com.belladati.sdk.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.user.UserGroupCreateBuilder;

@Test
public class UserGroupCreateBuilderTest extends SDKTest {

	private final String domainId = "123";
	private final String requestUri = "/api/users/groups/create";

	private final String id = "666";
	private final String name = "User Group Name";
	private final String description = "My description";

	private UserGroupCreateBuilder builder;

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		builder = service.setupUserGroupCreateBuilder(domainId);

		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().get("data"), builder.toJson().toString());
				holder.response.setEntity(new StringEntity(id));
			}
		});
	}

	public void postToServer_empty() {
		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, id);
	}

	public void postToServer_full() {
		builder.setName(name);
		builder.setDescription(description);

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
