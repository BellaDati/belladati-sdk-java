package com.belladati.sdk.user.impl;

import com.belladati.sdk.user.UserGroup;
import com.fasterxml.jackson.databind.JsonNode;

public class UserGroupImpl implements UserGroup {

	private final String id;
	private final String name;
	private final String description;

	public UserGroupImpl(JsonNode json) {
		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = getStringOrEmpty(json, "description");
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
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserGroupImpl) {
			return id.equals(((UserGroupImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
