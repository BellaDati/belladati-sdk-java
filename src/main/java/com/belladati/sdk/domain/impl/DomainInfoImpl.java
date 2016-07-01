package com.belladati.sdk.domain.impl;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.domain.Domain;
import com.belladati.sdk.domain.DomainInfo;
import com.belladati.sdk.exception.impl.InvalidDomainException;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserGroup;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

public class DomainInfoImpl implements DomainInfo {

	private final BellaDatiService service;

	private final String id;
	private final String name;
	private final String description;
	private final boolean active;

	public DomainInfoImpl(BellaDatiService service, JsonNode json) {
		if (!json.hasNonNull("id") || !json.hasNonNull("name") || !json.hasNonNull("active")) {
			throw new InvalidDomainException(json);
		}
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = json.hasNonNull("description") ? json.get("description").asText() : "";
		this.active = json.get("active").asBoolean();
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
	public boolean getActive() {
		return active;
	}

	@Override
	public Domain loadDetails() {
		return service.loadDomain(id);
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
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DomainInfoImpl) {
			return id.equals(((DomainInfoImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
