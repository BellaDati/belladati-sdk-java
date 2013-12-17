package com.belladati.sdk.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.belladati.sdk.user.User;
import com.fasterxml.jackson.databind.JsonNode;

class UserImpl implements User {

	private final String id;
	private final String username;
	private final String givenName;
	private final String familyName;
	private final String email;
	private final Date firstLogin;
	private final Date lastLogin;
	private final String locale;

	UserImpl(JsonNode json) {
		this.id = json.get("id").asText();
		this.username = json.get("username").asText();
		this.givenName = getStringOrEmpty(json, "name");
		this.familyName = getStringOrEmpty(json, "surname");
		this.email = getStringOrEmpty(json, "email");
		this.firstLogin = parseDate(json, "firstLogin");
		this.lastLogin = parseDate(json, "lastLogin");
		this.locale = getStringOrEmpty(json, "locale");
	}

	private String getStringOrEmpty(JsonNode json, String field) {
		return json.hasNonNull(field) ? json.get(field).asText() : "";
	}

	private Date parseDate(JsonNode json, String field) {
		if (json.hasNonNull(field)) {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			try {
				return format.parse(json.get(field).asText());
			} catch (ParseException e) {}
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
	public String getLocale() {
		return locale;
	}

	@Override
	public String toString() {
		String name = getName();
		if (name == null || name.isEmpty()) {
			return username;
		}
		return name;
	}
}
