package com.belladati.sdk.user.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserGroup;
import com.belladati.sdk.user.UserRequestType;
import com.belladati.sdk.user.UserRole;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class UserImpl implements User {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String username;
	private final String givenName;
	private final String familyName;
	private final String email;
	private final Date firstLogin;
	private final Date lastLogin;
	private final String timeZone;
	private final String locale;
	private final boolean active;
	private final String domainId;
	private final Set<UserRole> userRoles;
	private final Set<UserGroup> userGroups;

	public UserImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.username = json.get("username").asText();
		this.givenName = getStringOrEmpty(json, "name");
		this.familyName = getStringOrEmpty(json, "surname");
		this.email = getStringOrEmpty(json, "email");
		this.firstLogin = parseDate(json, "firstLogin");
		this.lastLogin = parseDate(json, "lastLogin");
		this.timeZone = getStringOrEmpty(json, "timeZone");
		this.locale = getStringOrEmpty(json, "locale");
		this.active = getBooleanOrDefault(json, "active", false);
		this.domainId = getStringOrEmpty(json, "domain_id");

		this.userRoles = new HashSet<UserRole>();
		if (json.hasNonNull("roles")) {
			ArrayNode nodes = (ArrayNode) json.get("roles");
			for (JsonNode node : nodes) {
				this.userRoles.add(UserRole.valueOfJson(node.get("role").asText()));
			}
		}

		this.userGroups = new HashSet<UserGroup>();
		if (json.hasNonNull("groups")) {
			ArrayNode nodes = (ArrayNode) json.get("groups");
			for (JsonNode node : nodes) {
				this.userGroups.add(new UserGroupImpl(node));
			}
		}
	}

	private Date parseDate(JsonNode json, String field) {
		if (json.hasNonNull(field)) {
			return BellaDatiSdkUtils.parseJavaUtilDate(json.get(field).asText());
		}
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		if (givenName.isEmpty() || familyName.isEmpty()) {
			// no need for separating space since at least one of them is empty
			return givenName + familyName;
		}
		return givenName + " " + familyName;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getGivenName() {
		return givenName;
	}

	@Override
	public String getFamilyName() {
		return familyName;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public Date getFirstLogin() {
		return firstLogin != null ? (Date) firstLogin.clone() : null;
	}

	@Override
	public Date getLastLogin() {
		return lastLogin != null ? (Date) lastLogin.clone() : null;
	}

	@Override
	public String getTimeZone() {
		return timeZone;
	}

	@Override
	public String getLocale() {
		return locale;
	}

	@Override
	public boolean getActive() {
		return active;
	}

	@Override
	public String getDomainId() {
		return domainId;
	}

	@Override
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	@Override
	public Set<UserGroup> getUserGroups() {
		return userGroups;
	}

	@Override
	public String toString() {
		String name = getName();
		if (name == null || name.isEmpty()) {
			return username;
		}
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserImpl) {
			return id.equals(((UserImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String loadStatus() {
		return service.loadUserStatus(id);
	}

	@Override
	public void postStatus(String status) {
		service.postUserStatus(id, status);
	}

	@Override
	public String createUserRequest(UserRequestType requestType) {
		return service.createUserRequest(username, requestType);
	}

	@Override
	public String createAccessToken(Integer validity, String domainId) {
		return service.createAccessToken(username, validity, domainId);
	}

}
