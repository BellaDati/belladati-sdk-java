package com.belladati.sdk.user.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.user.UserCreateBuilder;
import com.belladati.sdk.user.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builder used to initiate user object that should be created.
 * 
 * @author Lubomir Elko
 */
public class UserCreateBuilderImpl implements UserCreateBuilder {

	private final BellaDatiServiceImpl service;
	private final String domainId;
	private boolean posted = false;

	private String username;
	private String email;
	private String firstName;
	private String lastName;
	private String timeZone;
	private String locale;
	private Set<UserRole> roles;
	private Set<String> userGroupIds;

	public UserCreateBuilderImpl(BellaDatiServiceImpl service, String domainId) {
		this.service = service;
		this.domainId = domainId;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@Override
	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public void setRoles(UserRole... roles) {
		this.roles = new HashSet<>();
		if (roles != null) {
			this.roles.addAll(Arrays.asList(roles));
		}
	}

	@Override
	public void setUserGroups(String... userGroupIds) {
		this.userGroupIds = new HashSet<>();
		if (userGroupIds != null) {
			this.userGroupIds.addAll(Arrays.asList(userGroupIds));
		}
	}

	@Override
	public JsonNode toJson() {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode object = mapper.createObjectNode();
		object.put("domain_id", domainId);
		object.put("username", username);
		object.put("email", email);
		object.put("firstName", firstName);
		object.put("lastName", lastName);

		if (timeZone != null) {
			object.put("timeZone", timeZone);
		}
		if (locale != null) {
			object.put("locale", locale);
		}
		if (roles != null && !roles.isEmpty()) {
			ArrayNode array = mapper.createArrayNode();
			for (UserRole role : roles) {
				array.add(role.getJsonRole());
			}
			object.put("roles", array);
		}
		if (userGroupIds != null && !userGroupIds.isEmpty()) {
			ArrayNode array = mapper.createArrayNode();
			for (String group : userGroupIds) {
				array.add(group);
			}
			object.put("groups", array);
		}

		return object;
	}

	@Override
	public String post() {
		if (posted) {
			throw new IllegalStateException("Request already submitted to server.");
		}
		byte[] response = service.getClient().post("api/domains/create", service.getTokenHolder(),
			Collections.singletonList(new BasicNameValuePair("data", toJson().toString())));
		posted = true;
		return new String(response);
	}

}
