package com.belladati.sdk.domain.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.domain.DomainEditBuilder;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builder used to initiate domain object that should be edited.
 * 
 * @author Lubomir Elko
 */
public class DomainEditBuilderImpl implements DomainEditBuilder {

	private final BellaDatiServiceImpl service;
	private final String id;
	private boolean posted = false;

	private String description;
	private String dateFormat;
	private String timeFormat;
	private String timeZone;
	private String locale;
	private final Map<String, String> parameters;

	public DomainEditBuilderImpl(BellaDatiServiceImpl service, String id) {
		this.service = service;
		this.id = id;
		this.parameters = new HashMap<>();
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
	public void addParameter(String key, String value) {
		this.parameters.put(key, value);
	}

	@Override
	public JsonNode toJson() {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode object = mapper.createObjectNode();

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
		if (!parameters.isEmpty()) {
			ArrayNode array = mapper.createArrayNode();
			for (Entry<String, String> entry : parameters.entrySet()) {
				ObjectNode paramObject = mapper.createObjectNode();
				paramObject.put(entry.getKey(), entry.getValue());
				array.add(paramObject);
			}
			object.put("parameters", array);
		}

		return object;
	}

	@Override
	public String post() {
		if (posted) {
			throw new IllegalStateException("Request already submitted to server.");
		}
		byte[] response = service.getClient().post("api/domains/" + id, service.getTokenHolder(),
			Collections.singletonList(new BasicNameValuePair("data", toJson().toString())));
		posted = true;
		return new String(response);
	}

}
