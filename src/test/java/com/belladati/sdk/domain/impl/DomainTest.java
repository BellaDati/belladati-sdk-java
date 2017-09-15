package com.belladati.sdk.domain.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.domain.Domain;
import com.belladati.sdk.domain.DomainInfo;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserGroup;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to domains.
 * 
 * @author Lubomir Elko
 */
@Test
public class DomainTest extends SDKTest {

	private final String id = "123";

	private final String baseUri = "/api/domains";
	private final String apiUri = baseUri + "/";
	private final String usersUri = String.format("/api/domains/%s/users", id);
	private final String userGroupsUri = String.format("/api/domains/%s/userGroups", id);

	private final String name = "domainname";
	private final String description = "domain description";
	private final String dateFormat = "yyyy-MM-dd";
	private final String timeFormat = "hh:mm:ss";
	private final String timeZone = "Asia/Hong_Kong";
	private final String locale = "EN";
	private final String active = "true";

	private final String user_id = "234";
	private final String user_name = "User's Name";
	private final String userGroup_name = "First Group";
	private final String userGroup_id = "456";

	private final String[][] userGroupsJson = { { userGroup_id, userGroup_name } };

	/** Regular domain info data is loaded correctly. */
	public void loadDomainInfo() {
		server.registerPaginatedItem(baseUri, "domains", builder.buildDomainInfoNode(id, name, description, active));

		CachedList<DomainInfo> infos = getService().getDomainInfo();
		infos.load();
		server.assertRequestUris(baseUri);
		assertEquals(infos.get().size(), 1);

		DomainInfo info = infos.get().get(0);
		assertEquals(info.getId(), id);
		assertEquals(info.getName(), name);
		assertEquals(info.getDescription(), description);
		assertEquals(info.getActive() + "", active);
		assertEquals(info.toString(), name);
	}

	/** Individual Domain can be loaded by ID through getService(). */
	public void loadDomain() {
		server.register(apiUri + id,
			builder.buildDomainNode(id, name, description, dateFormat, timeFormat, timeZone, locale, active).toString());

		Domain domain = getService().loadDomain(id);

		assertEquals(domain.getId(), id);
		assertEquals(domain.getName(), name);
		assertEquals(domain.getDescription(), description);
		assertEquals(domain.getDateFormat(), dateFormat);
		assertEquals(domain.getTimeFormat(), timeFormat);
		assertEquals(domain.getTimeZone(), timeZone);
		assertEquals(domain.getLocale(), locale);
		assertEquals(domain.getActive() + "", active);

		server.assertRequestUris(apiUri + id);

		assertEquals(domain.toString(), name);
	}

	/** Domain can be loaded from a domain info object. */
	public void loadDomainFromInfo() {
		server.register(apiUri + id,
			builder.buildDomainNode(id, name, description, dateFormat, timeFormat, timeZone, locale, active).toString());

		ObjectNode infoJson = builder.buildDomainInfoNode(id, name, description, active);
		DomainInfo domainInfo = new DomainInfoImpl(getService(), infoJson);

		assertEquals(domainInfo.getId(), id);
		assertEquals(domainInfo.getName(), name);
		assertEquals(domainInfo.getDescription(), description);
		assertEquals(domainInfo.getActive() + "", active);
		assertEquals(domainInfo.toString(), domainInfo.getName());
		Domain domain = domainInfo.loadDetails();

		assertEquals(domain.getId(), id);
		assertEquals(domain.getName(), name);
		assertEquals(domain.getDescription(), description);
		assertEquals(domain.getDateFormat(), dateFormat);
		assertEquals(domain.getTimeFormat(), timeFormat);
		assertEquals(domain.getTimeZone(), timeZone);
		assertEquals(domain.getLocale(), locale);
		assertEquals(domain.getActive() + "", active);

		server.assertRequestUris(apiUri + id);
	}

	/** Null fields are treated as empty. */
	@Test(dataProvider = "provider_optionalFields")
	public void fieldNull(String field, String method) throws Exception {
		ObjectNode node = builder.buildDomainNode(id, name, description, dateFormat, timeFormat, timeZone, locale, active);
		node.put(field, (String) null);
		server.register(apiUri + id, node.toString());

		Domain resource = getService().loadDomain(id);

		assertEquals(Domain.class.getMethod(method).invoke(resource), "");
	}

	/** Missing fields are treated as empty. */
	@Test(dataProvider = "provider_optionalFields")
	public void fieldMissing(String field, String method) throws Exception {
		ObjectNode node = builder.buildDomainNode(id, name, description, dateFormat, timeFormat, timeZone, locale, active);
		node.remove(field);
		server.register(apiUri + id, node.toString());

		Domain resource = getService().loadDomain(id);

		assertEquals(Domain.class.getMethod(method).invoke(resource), "");
	}

	/** equals/hashcode for DomainInfo */
	public void domainInfoEquality() {
		DomainInfo u1 = new DomainInfoImpl(getService(), builder.buildDomainInfoNode(id, name, description, active));
		DomainInfo u2 = new DomainInfoImpl(getService(), builder.buildDomainInfoNode(id, "", null, ""));
		DomainInfo u3 = new DomainInfoImpl(getService(), builder.buildDomainInfoNode("otherId", name, description, active));

		assertEquals(u1, u2);
		assertEquals(u1.hashCode(), u2.hashCode());

		assertFalse(u1.equals(new Object()));
		assertNotEquals(u1, u3);
	}

	/** equals/hashcode for Domain */
	public void domainEquality() {
		Domain u1 = new DomainImpl(getService(),
			builder.buildDomainNode(id, name, description, dateFormat, timeFormat, timeZone, locale, active));
		Domain u2 = new DomainImpl(getService(), builder.buildDomainNode(id, "", "", "", "", "", "", ""));
		Domain u3 = new DomainImpl(getService(),
			builder.buildDomainNode("otherId", name, description, dateFormat, timeFormat, timeZone, locale, active));

		assertEquals(u1, u2);
		assertEquals(u1.hashCode(), u2.hashCode());

		assertFalse(u1.equals(new Object()));
		assertNotEquals(u1, u3);
	}

	@DataProvider(name = "provider_optionalFields")
	protected Object[][] provider_optionalFields() {
		return new Object[][] { { "description", "getDescription" }, { "dateFormat", "getDateFormat" },
			{ "timeFormat", "getTimeFormat" }, { "timeZone", "getTimeZone" }, { "locale", "getLocale" } };
	}

	/** Users (without group filter) are loaded correctly from getService(). */
	public void loadUsers_withoutGroupFilter() {
		registerSingleUser(builder.buildUserNode(user_id, user_name, "", "", "", "", "", "", null, null, id, null, null));

		ObjectNode node = builder.buildDomainNode(id, name, description, dateFormat, timeFormat, timeZone, locale, active);
		Domain domain = new DomainImpl(getService(), node);
		CachedList<User> list = domain.loadUsers(null);
		list.load();
		server.assertRequestUris(usersUri);
		assertEquals(list.toList().size(), 1);

		User item = list.toList().get(0);

		assertEquals(item.getId(), user_id);
		assertEquals(item.getUsername(), user_name);
		assertEquals(item.getDomainId(), id);
		assertEquals(item.getUserGroups().size(), 0);

		assertEquals(item.toString(), user_name);
	}

	/** Users (with group filter) are loaded correctly from getService(). */
	public void loadUsers_withGroupFilter() {
		registerSingleUser(
			builder.buildUserNode(user_id, user_name, "", "", "", "", "", "", null, null, id, null, userGroupsJson));

		ObjectNode infoJson = builder.buildDomainInfoNode(id, name, description, active);
		DomainInfo domainInfo = new DomainInfoImpl(getService(), infoJson);
		CachedList<User> list = domainInfo.loadUsers(userGroup_id);
		list.load();
		server.assertRequestUris(usersUri);
		assertEquals(list.toList().size(), 1);

		User item = list.toList().get(0);

		assertEquals(item.getId(), user_id);
		assertEquals(item.getUsername(), user_name);
		assertEquals(item.getDomainId(), id);
		assertEquals(item.getUserGroups().size(), 1);
		UserGroup[] groups = item.getUserGroups().toArray(new UserGroup[1]);
		assertEquals(groups[0].getId(), userGroup_id);
		assertEquals(groups[0].getName(), userGroup_name);
		assertEquals(groups[0].getDescription(), "");

		assertEquals(item.toString(), user_name);
	}

	private void registerSingleUser(JsonNode node) {
		server.registerPaginatedItem(usersUri, "users", node);
	}

	/** Loads UserGroups from Domain. */
	public void loadUserGroups_fromDomain() {
		ObjectNode infoJson = builder.buildDomainInfoNode(id, name, description, active);
		server.register(apiUri + id, infoJson.toString());
		registerSingleUserGroup(builder.buildUserGroupNode(userGroup_id, userGroup_name, description));

		DomainInfo domainInfo = new DomainInfoImpl(getService(), infoJson);
		assertEquals(domainInfo.getId(), id);
		Domain domain = domainInfo.loadDetails();
		CachedList<UserGroup> list = domain.loadUserGroups();

		list.load();
		assertEquals(list.toList().size(), 1);

		UserGroup item = list.toList().get(0);
		assertEquals(item.getId(), userGroup_id);
		assertEquals(item.getName(), userGroup_name);
		assertEquals(item.getDescription(), description);
		assertEquals(item.toString(), userGroup_name);
	}

	/** Loads UserGroups from DomainInfo. */
	public void loadUserGroups_fromDomainInfo() {
		registerSingleUserGroup(builder.buildUserGroupNode(userGroup_id, userGroup_name, description));

		ObjectNode infoJson = builder.buildDomainInfoNode(id, name, description, active);
		DomainInfo domainInfo = new DomainInfoImpl(getService(), infoJson);
		assertEquals(domainInfo.getId(), id);
		CachedList<UserGroup> list = domainInfo.loadUserGroups();

		list.load();
		assertEquals(list.toList().size(), 1);

		UserGroup item = list.toList().get(0);
		assertEquals(item.getId(), userGroup_id);
		assertEquals(item.getName(), userGroup_name);
		assertEquals(item.getDescription(), description);
		assertEquals(item.toString(), userGroup_name);
	}

	private void registerSingleUserGroup(JsonNode node) {
		server.registerPaginatedItem(userGroupsUri, "userGroups", node);
	}

}
