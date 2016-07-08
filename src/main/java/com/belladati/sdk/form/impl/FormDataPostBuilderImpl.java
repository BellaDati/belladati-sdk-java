package com.belladati.sdk.form.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.form.FormDataPostBuilder;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builder used to initiate domain object that should be created.
 * 
 * @author Lubomir Elko
 */
public class FormDataPostBuilderImpl implements FormDataPostBuilder {

	private final BellaDatiServiceImpl service;
	private final String formId;
	private boolean posted = false;

	private final Map<String, String> textValues = new HashMap<>();
	private final Map<String, BigDecimal> numberValues = new HashMap<>();
	private final Map<String, Boolean> booleanValues = new HashMap<>();

	public FormDataPostBuilderImpl(BellaDatiServiceImpl service, String formId) {
		this.service = service;
		this.formId = formId;
	}

	@Override
	public void addTextValue(String elementId, String value) {
		if (value != null) {
			textValues.put(elementId, value);
		}
	}

	@Override
	public void addNumberValue(String elementId, BigDecimal value) {
		if (value != null) {
			numberValues.put(elementId, value);
		}
	}

	@Override
	public void addBooleanValue(String elementId, boolean value) {
		booleanValues.put(elementId, value);
	}

	@Override
	public JsonNode toJson() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode object = mapper.createObjectNode();

		for (Entry<String, String> entry : textValues.entrySet()) {
			object.put(entry.getKey(), entry.getValue());
		}
		for (Entry<String, BigDecimal> entry : numberValues.entrySet()) {
			object.put(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Boolean> entry : booleanValues.entrySet()) {
			object.put(entry.getKey(), entry.getValue().booleanValue());
		}

		return object;
	}

	@Override
	public String post() {
		if (posted) {
			throw new IllegalStateException("Request already submitted to server.");
		}
		byte[] response = service.getClient().post("api/import/forms/" + formId, service.getTokenHolder(),
			Collections.singletonList(new BasicNameValuePair("data", toJson().toString())));
		posted = true;
		return new String(response);
	}

}
