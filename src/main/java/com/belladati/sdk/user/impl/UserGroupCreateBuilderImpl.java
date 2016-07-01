package com.belladati.sdk.user.impl;

import java.util.Collections;

import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.user.UserGroupCreateBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builder used to initiate user group object that should be created.
 * 
 * @author Lubomir Elko
 */
public class UserGroupCreateBuilderImpl implements UserGroupCreateBuilder {

	private final BellaDatiServiceImpl service;
	private final String domainId;
	private boolean posted = false;

	private String name;
	private String description;

	public UserGroupCreateBuilderImpl(BellaDatiServiceImpl service, String domainId) {
		this.service = service;
		this.domainId = domainId;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public JsonNode toJson() {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode object = mapper.createObjectNode();
		object.put("domain_id", domainId);
		object.put("name", name);

		if (description != null) {
			object.put("description", description);
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
