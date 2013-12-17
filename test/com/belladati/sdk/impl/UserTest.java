package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserInfo;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to users.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class UserTest extends SDKTest {

	private final String usersUri = "/api/users";
	private final String id = "123";
	private final String username = "username";
	private final String givenName = "given name";
	private final String familyName = "family name";
	private final String email = "email@email.com";
	private final String firstLogin = "Mon, 16 Apr 2012 10:17:26 GMT";
	private final String lastLogin = "Tue, 17 Apr 2012 10:17:26 GMT";
	private final String locale = "locale";

	/** Individual user can be loaded by ID through service. */
	public void loadUser() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale).toString());

		User user = service.loadUser(id);

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

	/** User can be loaded from a user info object. */
	public void loadUserFromInfo() {
		UserInfo userInfo = new UserInfoImpl(service, id, "some other name");

		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale).toString());

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

		User user = service.loadUser(id);

		assertEquals(User.class.getMethod(method).invoke(user), "");
	}

	/** Missing fields are treated as empty. */
	@Test(dataProvider = "userOptionalFields")
	public void fieldMissing(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.remove(field);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = service.loadUser(id);

		assertEquals(User.class.getMethod(method).invoke(user), "");
	}

	/** Full name is correct when only given name exists. */
	public void onlyGivenName() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, givenName, null, email, firstLogin, lastLogin, locale).toString());

		assertEquals(service.loadUser(id).getName(), givenName);
	}

	/** Full name is correct when only family name exists. */
	public void onlyFamilyName() {
		server.register(usersUri + "/" + id,
			builder.buildUserNode(id, username, null, familyName, email, firstLogin, lastLogin, locale).toString());

		assertEquals(service.loadUser(id).getName(), familyName);
	}

	/** Full name is correct when no name exists. */
	public void noName() {
		server.register(usersUri + "/" + id, builder
			.buildUserNode(id, username, null, null, email, firstLogin, lastLogin, locale).toString());

		assertEquals(service.loadUser(id).getName(), "");
	}

	/** Date fields not containing dates are treated as null. */
	@Test(dataProvider = "userOptionalDates")
	public void dateFieldNotDate(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.put(field, "not a date");
		server.register(usersUri + "/" + id, userNode.toString());

		User user = service.loadUser(id);

		assertNull(User.class.getMethod(method).invoke(user));
	}

	/** Null date fields are treated as null. */
	@Test(dataProvider = "userOptionalDates")
	public void dateFieldNull(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.put(field, (String) null);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = service.loadUser(id);

		assertNull(User.class.getMethod(method).invoke(user));
	}

	/** Missing date fields are treated as null. */
	@Test(dataProvider = "userOptionalDates")
	public void dateFieldMissing(String field, String method) throws Exception {
		ObjectNode userNode = builder.buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale);
		userNode.remove(field);
		server.register(usersUri + "/" + id, userNode.toString());

		User user = service.loadUser(id);

		assertNull(User.class.getMethod(method).invoke(user));
	}

	/** Can load a user's image from service. */
	public void loadImageFromService() throws IOException {
		server.register(usersUri + "/" + id + "/image", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
			}
		});

		BufferedImage image = (BufferedImage) service.loadUserImage(id);

		server.assertRequestUris(usersUri + "/" + id + "/image");

		assertEquals(image.getWidth(), 56);
		assertEquals(image.getHeight(), 46);
	}

	/** Can load a user's image from info. */
	public void loadImageFromReportInfo() throws IOException {
		UserInfo userInfo = new UserInfoImpl(service, id, "");

		server.register(usersUri + "/" + id + "/image", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
			}
		});

		BufferedImage image = (BufferedImage) userInfo.loadImage();

		server.assertRequestUris(usersUri + "/" + id + "/image");

		assertEquals(image.getWidth(), 56);
		assertEquals(image.getHeight(), 46);
	}

	/** Invalid image results in exception. */
	@Test(expectedExceptions = IOException.class)
	public void loadInvalidImage() throws IOException {
		UserInfo userInfo = new UserInfoImpl(service, id, "");

		server.register(usersUri + "/" + id + "/image", "not an image");

		userInfo.loadImage();
	}

	/** Missing image results in exception. */
	@Test(expectedExceptions = IOException.class)
	public void loadMissingImage() throws IOException {
		UserInfo userInfo = new UserInfoImpl(service, id, "");

		server.register(usersUri + "/" + id + "/image", "");

		userInfo.loadImage();
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
}
