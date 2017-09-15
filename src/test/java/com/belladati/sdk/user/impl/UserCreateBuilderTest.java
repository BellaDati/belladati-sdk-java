package com.belladati.sdk.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.user.UserCreateBuilder;
import com.belladati.sdk.user.UserRole;

@Test
public class UserCreateBuilderTest extends SDKTest {

	private final String domainId = "123";
	private final String requestUri = "/api/users/create";

	private final String id = "666";
	private final String username = "username";
	private final String firstName = "first name";
	private final String lastName = "last name";
	private final String email = "email@email.com";
	private final String locale = "locale";
	private final String timeZone = "GMT";

	private UserCreateBuilder builder;

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().get("data"), builder.toJson().toString());
				holder.response.setEntity(new StringEntity(id));
			}
		});
		builder = getService().setupUserCreateBuilder(domainId);
	}

	public void postToServer_empty() {
		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, id);
	}

	public void postToServer_medium() {
		builder.setUsername(username);
		builder.setFirstName(firstName);
		builder.setLastName(lastName);
		builder.setEmail(email);
		builder.setLocale(locale);
		builder.setTimeZone(timeZone);
		builder.setRoles(null);
		builder.setUserGroups(null);

		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, id);
	}

	public void postToServer_full() {
		builder.setUsername(username);
		builder.setFirstName(firstName);
		builder.setLastName(lastName);
		builder.setEmail(email);
		builder.setLocale(locale);
		builder.setTimeZone(timeZone);
		builder.setRoles(UserRole.values());
		builder.setUserGroups("1", "99");

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
