package com.belladati.sdk.domain.impl;

import java.util.Collections;

import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.domain.DomainCreateBuilder;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builder used to initiate domain object that should be created.
 * 
 * @author Lubomir Elko
 */
public class DomainCreateBuilderImpl implements DomainCreateBuilder {

	private final BellaDatiServiceImpl service;
	private boolean posted = false;

	private String name;
	private String description;
	private String dateFormat;
	private String timeFormat;
	private String timeZone;
	private String locale;

	public DomainCreateBuilderImpl(BellaDatiServiceImpl service) {
		this.service = service;
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
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
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
	public JsonNode toJson() {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode object = mapper.createObjectNode();
		object.put("name", name);

		if (description != null) {
			object.put("description", description);
		}
		if (dateFormat != null) {
			object.put("dateFormat", dateFormat);
		}
		if (timeFormat != null) {
			object.put("timeFormat", timeFormat);
		}
		if (timeZone != null) {
			object.put("timeZone", timeZone);
		}
		if (locale != null) {
			object.put("locale", locale);
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
