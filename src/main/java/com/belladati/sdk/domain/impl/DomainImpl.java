package com.belladati.sdk.domain.impl;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.domain.Domain;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserGroup;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

public class DomainImpl implements Domain {

	private final BellaDatiService service;

	private final String id;
	private final String name;
	private final String description;
	private final String dateFormat;
	private final String timeFormat;
	private final String timeZone;
	private final String locale;
	private final boolean active;

	public DomainImpl(BellaDatiService service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = getStringOrEmpty(json, "description");
		this.dateFormat = getStringOrEmpty(json, "dateFormat");
		this.timeFormat = getStringOrEmpty(json, "timeFormat");
		this.timeZone = getStringOrEmpty(json, "timeZone");
		this.locale = getStringOrEmpty(json, "locale");
		this.active = getBooleanOrDefault(json, "active", false);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getDateFormat() {
		return dateFormat;
	}

	@Override
	public String getTimeFormat() {
		return timeFormat;
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
	public CachedList<User> loadUsers(String userGroupId) {
		return service.getDomainUsers(id, userGroupId);
	}

	@Override
	public CachedList<UserGroup> loadUserGroups() {
		return service.getDomainUserGroups(id);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DomainImpl) {
			return id.equals(((DomainImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
