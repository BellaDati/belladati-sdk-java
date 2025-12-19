package com.belladati.sdk.domain.impl;

import com.belladati.sdk.domain.DomainCreateBuilder;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builder used to initiate domain object that should be created.
 * 
 * 
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
	private final Map<String, String> parameters;
	private String templateId;
	private String usernameSuffix;

	public DomainCreateBuilderImpl(BellaDatiServiceImpl service) {
		this.service = service;
		this.parameters = new HashMap<>();
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
	public void addParameter(String key, String value) {
		this.parameters.put(key, value);
	}

	@Override
	public void setTemplate(String id, String usernameSuffix) {
		this.templateId = id;
		this.usernameSuffix = usernameSuffix;
	}

	@Override
	public void setTemplateId(String id) {
		this.templateId = id;
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
		if (!parameters.isEmpty()) {
			ArrayNode array = mapper.createArrayNode();
			for (Entry<String, String> entry : parameters.entrySet()) {
				ObjectNode paramObject = mapper.createObjectNode();
				paramObject.put(entry.getKey(), entry.getValue());
				array.add(paramObject);
			}
			object.put("parameters", array);
		}
		if (templateId != null) {
			object.put("id", templateId);

			ObjectNode templateObject = mapper.createObjectNode();
			templateObject.put("id", templateId);
			if (usernameSuffix != null) {
				templateObject.put("usernameSuffix", usernameSuffix);
			}

			object.put("template", templateObject);
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
