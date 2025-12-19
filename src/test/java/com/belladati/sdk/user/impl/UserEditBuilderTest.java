package com.belladati.sdk.user.impl;

import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.user.UserEditBuilder;
import com.belladati.sdk.user.UserRole;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Test
public class UserEditBuilderTest extends SDKTest {

	private final String userId = "123";
	private final String requestUri = "/api/users/" + userId;

	private final String username = "username";
	private final String firstName = "first name";
	private final String lastName = "last name";
	private final String email = "email@email.com";
	private final String locale = "locale";
	private final String timeZone = "GMT";

	private UserEditBuilder builder;

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getFormParameters().get("data"), builder.toJson().toString());
				holder.response.setEntity(new StringEntity(""));
			}
		});
		builder = getService().setupUserEditBuilder(userId);
	}

	public void postToServer_empty() {
		String result = builder.post();
		server.assertRequestUris(requestUri);
		assertEquals(result, "");
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
		assertEquals(result, "");
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
