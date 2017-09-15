package com.belladati.sdk.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.exception.server.InvalidStreamException;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserGroup;
import com.belladati.sdk.user.UserInfo;
import com.belladati.sdk.user.UserRequestType;
import com.belladati.sdk.user.UserRole;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to users.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class UserTest extends SDKTest {

	private final String usersUri = "/api/users";
	private final String statusUri = "/api/users/%s/status";
	private final String accessTokenUri = "/api/users/%s/accessToken";
	private final String userRequestUri = "/api/users/%s/requests";

	private final String id = "123";
	private final String username = "username";
	private final String givenName = "given name";
	private final String familyName = "family name";
	private final String email = "email@email.com";
	private final String firstLogin = "Mon, 16 Apr 2012 10:17:26 GMT";
	private final String lastLogin = "Tue, 17 Apr 2012 10:17:26 GMT";
	private final String locale = "locale";
	private final String timeZone = "GMT";
	private final String active = "true";
	private final String domainId = "456";
	private final Set<UserRole> userRoles = new HashSet<>(Arrays.asList(UserRole.values()));
	private final String userGroups0_id = "1";
	private final String userGroups0_name = "First Group";
	private final String userGroups1_id = "99";
	private final String userGroups1_name = "Last Group";

	private final String[] userRolesJson = { "ADMIN", "WORKSPACE_ADMIN", "DATASET_ADMIN", "REPORT_ADMIN" };
	private final String[][] userGroupsJson = { { userGroups0_id, userGroups0_name }, { userGroups1_id, userGroups1_name } };

	/** Individual user can be loaded by ID through getService(). */
	public void loadUser() {
		server.register(usersUri + "/" + id, builder.buildUserNode(id, username, givenName, familyName, email, firstLogin,
			lastLogin, locale, timeZone, active, domainId, userRolesJson, userGroupsJson).toString());

		User user = getService().loadUser(id);
		assertFullUserDetail(user);

		server.assertRequestUris(usersUri + "/" + id);

		assertEquals(user.toString(), givenName + " " + familyName);
	}

	public void loadUserByUsername() {
		server.register(usersUri + "/username/" + username, builder.buildUserNode(id, username, givenName, familyName, email,
			firstLogin, lastLogin, locale, timeZone, active, domainId, userRolesJson, userGroupsJson).toString());

		User user = getService().loadUserByUsername(username);
		assertFullUserDetail(user);

		server.assertRequestUris(usersUri + "/username/" + username);

		assertEquals(user.toString(), givenName + " " + familyName);
	}

	private void assertFullUserDetail(User user) {
		assertEquals(user.getId(), id);
		assertEquals(user.getUsername(), username);
		assertEquals(user.getName(), givenName + " " + familyName);
		assertEquals(user.getGivenName(), givenName);
		assertEquals(user.getFamilyName(), familyName);
		Calendar expectedFirst = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedFirst.set(2012, 3, 16, 10, 17, 26);
		expectedFirst.set(Calendar.MILLISECOND, 0);
		assertEquals(user.getFirstLogin(), expectedFirst.getTime());
		Calendar expectedLast = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedLast.set(2012, 3, 17, 10, 17, 26);
		expectedLast.set(Calendar.MILLISECOND, 0);
		assertEquals(user.getLastLogin(), expectedLast.getTime());
		assertEquals(user.getTimeZone(), timeZone);
		assertEquals(user.getLocale(), locale);
		assertEquals(user.getActive() + "", active);
		assertEquals(user.getDomainId(), domainId);
		assertEquals(user.getUserRoles(), userRoles);

		assertEquals(user.getUserGroups().size(), 2);
		UserGroup[] groups = user.getUserGroups().toArray(new UserGroup[2]);
		assertEquals(groups[0].getId(), userGroups1_id);
		assertEquals(groups[0].getName(), userGroups1_name);
		assertEquals(groups[0].getDescription(), "");
		assertEquals(groups[1].getId(), userGroups0_id);
		assertEquals(groups[1].getName(), userGroups0_name);
		assertEquals(groups[1].getDescription(), "");

		assertEquals(user.toString(), givenName + " " + familyName);
	}

	/** User can be loaded from a user info object. */
	public void loadUserFromInfo() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale).toString());

		UserInfo userInfo = new UserInfoImpl(getService(), id, "some other name");
		assertEquals(userInfo.toString(), userInfo.getName());

		User user = userInfo.loadDetails();

		assertEquals(user.getId(), id);
		assertEquals(user.getUsername(), username);
		assertEquals(user.getName(), givenName + " " + familyName);
		assertEquals(user.getGivenName(), givenName);
		assertEquals(user.getFamilyName(), familyName);
		Calendar expectedFirst = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedFirst.set(2012, 3, 16, 10, 17, 26);
		expectedFirst.set(Calendar.MILLISECOND, 0);
		assertEquals(user.getFirstLogin(), expectedFirst.getTime());
		Calendar expectedLast = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedLast.set(2012, 3, 17, 10, 17, 26);
		expectedLast.set(Calendar.MILLISECOND, 0);
		assertEquals(user.getLastLogin(), expectedLast.getTime());
		assertEquals(user.getLocale(), locale);

		server.assertRequestUris(usersUri + "/" + id);
	}

	/** Null fields are treated as empty. */
	@Test(dataProvider = "userOptionalFields")
	public void fieldNull(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.put(field, (String) null);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = getService().loadUser(id);

		assertEquals(User.class.getMethod(method).invoke(user), "");
	}

	/** Missing fields are treated as empty. */
	@Test(dataProvider = "userOptionalFields")
	public void fieldMissing(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.remove(field);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = getService().loadUser(id);

		assertEquals(User.class.getMethod(method).invoke(user), "");
	}

	/** Full name is correct when only given name exists. */
	public void onlyGivenName() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, givenName, null, email, firstLogin, lastLogin, locale).toString());

		assertEquals(getService().loadUser(id).getName(), givenName);
		assertEquals(getService().loadUser(id).toString(), givenName);
	}

	/** Full name is correct when only family name exists. */
	public void onlyFamilyName() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, null, familyName, email, firstLogin, lastLogin, locale).toString());

		assertEquals(getService().loadUser(id).getName(), familyName);
		assertEquals(getService().loadUser(id).toString(), familyName);
	}

	/** Full name is correct when no name exists. */
	public void noName() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, null, null, email, firstLogin, lastLogin, locale).toString());

		assertEquals(getService().loadUser(id).getName(), "");
		assertEquals(getService().loadUser(id).toString(), username);
	}

	/** Date fields not containing dates are treated as null. */
	@Test(dataProvider = "userOptionalDates")
	public void dateFieldNotDate(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.put(field, "not a date");
		server.register(usersUri + "/" + id, userNode.toString());

		User user = getService().loadUser(id);

		assertNull(User.class.getMethod(method).invoke(user));
	}

	/** Null date fields are treated as null. */
	@Test(dataProvider = "userOptionalDates")
	public void dateFieldNull(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.put(field, (String) null);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = getService().loadUser(id);

		assertNull(User.class.getMethod(method).invoke(user));
	}

	/** Missing date fields are treated as null. */
	@Test(dataProvider = "userOptionalDates")
	public void dateFieldMissing(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.remove(field);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = getService().loadUser(id);

		assertNull(User.class.getMethod(method).invoke(user));
	}

	/** Can load a user's image from getService(). */
	public void loadImageFromService() {
		server.register(usersUri + "/" + id + "/image", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});

		BufferedImage image = (BufferedImage) getService().loadUserImage(id);

		server.assertRequestUris(usersUri + "/" + id + "/image");

		assertEquals(image.getWidth(), 56);
		assertEquals(image.getHeight(), 46);
	}

	/** Can load a user's image from info. */
	public void loadImageFromReportInfo() {
		server.register(usersUri + "/" + id + "/image", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});

		UserInfo userInfo = new UserInfoImpl(getService(), id, "");
		BufferedImage image = (BufferedImage) userInfo.loadImage();

		server.assertRequestUris(usersUri + "/" + id + "/image");

		assertEquals(image.getWidth(), 56);
		assertEquals(image.getHeight(), 46);
	}

	/** Invalid image results in exception. */
	@Test(expectedExceptions = InvalidStreamException.class)
	public void loadInvalidImage() throws InvalidStreamException {
		server.register(usersUri + "/" + id + "/image", "not an image");

		UserInfo userInfo = new UserInfoImpl(getService(), id, "");
		userInfo.loadImage();
	}

	/** Missing image results in exception. */
	@Test(expectedExceptions = InvalidStreamException.class)
	public void loadMissingImage() throws InvalidStreamException {
		server.register(usersUri + "/" + id + "/image", "");

		UserInfo userInfo = new UserInfoImpl(getService(), id, "");
		userInfo.loadImage();
	}

	/** Empty content for image results in exception. */
	@Test(expectedExceptions = InvalidStreamException.class)
	public void loadEmptyContentImage() throws InvalidStreamException {
		server.register(usersUri + "/" + id + "/image", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setStatusCode(204);
				holder.response.setEntity(null);
			}
		});
		UserInfo userInfo = new UserInfoImpl(getService(), id, "");
		userInfo.loadImage();
	}

	/** equals/hashcode for UserInfo */
	public void userInfoEquality() {
		UserInfo u1 = new UserInfoImpl(getService(), id, givenName);
		UserInfo u2 = new UserInfoImpl(getService(), id, "");
		UserInfo u3 = new UserInfoImpl(getService(), "otherId", "");

		assertEquals(u1, u2);
		assertEquals(u1.hashCode(), u2.hashCode());

		assertFalse(u1.equals(new Object()));
		assertNotEquals(u1, u3);
	}

	/** equals/hashcode for User */
	public void userEquality() {
		User u1 = new UserImpl(getService(),
			builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale));
		User u2 = new UserImpl(getService(), builder.buildUserNode(id, "", "", "", "", "", "", ""));
		User u3 = new UserImpl(getService(),
			builder.buildUserNode("otherId", username, givenName, familyName, email, firstLogin, lastLogin, locale));

		assertEquals(u1, u2);
		assertEquals(u1.hashCode(), u2.hashCode());

		assertFalse(u1.equals(new Object()));
		assertNotEquals(u1, u3);
	}

	@DataProvider(name = "userOptionalFields")
	protected Object[][] provideFields() {
		return new Object[][] { { "name", "getGivenName" }, { "surname", "getFamilyName" }, { "email", "getEmail" },
			{ "locale", "getLocale" } };
	}

	@DataProvider(name = "userOptionalDates")
	protected Object[][] provideDates() {
		return new Object[][] { { "firstLogin", "getFirstLogin" }, { "lastLogin", "getLastLogin" } };
	}

	/** equals/hashcode for UserGroup */
	public void userGroupEquality() {
		UserGroup u1 = new UserGroupImpl(builder.buildUserGroupNode(id, username, null));
		UserGroup u2 = new UserGroupImpl(builder.buildUserGroupNode(id, "", ""));
		UserGroup u3 = new UserGroupImpl(builder.buildUserGroupNode("otherId", username, null));

		assertEquals(u1, u2);
		assertEquals(u1.hashCode(), u2.hashCode());

		assertFalse(u1.equals(new Object()));
		assertNotEquals(u1, u3);
	}

	public void loadUserStatus() {
		server.register(String.format(statusUri, id), "ACTIVE");
		User user = new UserImpl(getService(), builder.buildUserNode(id, "", "", "", "", "", "", ""));
		String status = user.loadStatus();
		assertEquals(status, "ACTIVE");
		server.assertRequestUris(String.format(statusUri, id));
	}

	public void loadUserStatusFromInfo() {
		server.register(String.format(statusUri, id), "INACTIVE");
		UserInfo userInfo = new UserInfoImpl(getService(), id, "some name");
		String status = userInfo.loadStatus();
		assertEquals(status, "INACTIVE");
		server.assertRequestUris(String.format(statusUri, id));
	}

	public void postUserStatus() {
		final String status = "INACTIVE";
		server.register(String.format(statusUri, id), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters(), Collections.singletonMap("status", status));
				holder.response.setEntity(new StringEntity(""));
			}
		});
		User user = new UserImpl(getService(), builder.buildUserNode(id, "", "", "", "", "", "", ""));
		user.postStatus(status);
		server.assertRequestUris(String.format(statusUri, id));
	}

	public void postUserStatusFromInfo() {
		final String status = "ACTIVE";
		server.register(String.format(statusUri, id), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters(), Collections.singletonMap("status", status));
				holder.response.setEntity(new StringEntity(""));
			}
		});
		UserInfo userInfo = new UserInfoImpl(getService(), id, "some name");
		userInfo.postStatus(status);
		server.assertRequestUris(String.format(statusUri, id));
	}

	public void createUserRequest_fromService() {
		server.register(String.format(userRequestUri, username), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters(), Collections.singletonMap("request_type", "PASSWORD_RESET"));
				holder.response.setEntity(new StringEntity("12345;AbCdEfGh"));
			}
		});
		String response = getService().createUserRequest(username, UserRequestType.PASSWORD_RESET);
		assertEquals(response, "12345;AbCdEfGh");
		server.assertRequestUris(String.format(userRequestUri, username));
	}

	public void createUserRequest_fromUser() {
		server.register(String.format(userRequestUri, username), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters(), Collections.singletonMap("request_type", "LOGIN_UNATTENDED"));
				holder.response.setEntity(new StringEntity("98765;RDQX1Qx"));
			}
		});
		User user = new UserImpl(getService(), builder.buildUserNode(id, username, "", "", "", "", "", ""));
		String response = user.createUserRequest(UserRequestType.LOGIN_UNATTENDED);
		assertEquals(response, "98765;RDQX1Qx");
		server.assertRequestUris(String.format(userRequestUri, username));
	}

	public void createAccessToken_fromService() throws Exception {
		server.register(String.format(accessTokenUri, username), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().size(), 0);
				holder.response.setEntity(new StringEntity("myToken1;myTokenSecret1"));
			}
		});
		String response = getService().createAccessToken(username, null, null);
		assertEquals(response, "myToken1;myTokenSecret1");
		server.assertRequestUris(String.format(accessTokenUri, username));
	}

	public void createAccessToken_fromUser() {
		server.register(String.format(accessTokenUri, username), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				Map<String, String> formParams = new HashMap<>();
				formParams.put("validity", "66");
				formParams.put("domain_id", domainId);

				assertEquals(holder.getFormParameters(), formParams);
				holder.response.setEntity(new StringEntity("myToken2;myTokenSecret2"));
			}
		});
		User user = new UserImpl(getService(), builder.buildUserNode(id, username, "", "", "", "", "", ""));
		String response = user.createAccessToken(66, domainId);
		assertEquals(response, "myToken2;myTokenSecret2");
		server.assertRequestUris(String.format(accessTokenUri, username));
	}

}
