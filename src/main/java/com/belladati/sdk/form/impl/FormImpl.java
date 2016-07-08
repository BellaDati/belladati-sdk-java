package com.belladati.sdk.form.impl;

import java.util.ArrayList;
import java.util.List;

import com.belladati.sdk.form.Form;
import com.belladati.sdk.form.FormElement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class FormImpl implements Form {

	private final String id;
	private final String name;
	private final boolean recordTimestamp;
	private final List<FormElement> elements;

	public FormImpl(JsonNode json) {
		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.recordTimestamp = getBooleanOrDefault(json, "recordTimestamp", false);

		this.elements = new ArrayList<FormElement>();
		if (json.hasNonNull("elements")) {
			ArrayNode nodes = (ArrayNode) json.get("elements");
			for (JsonNode node : nodes) {
				this.elements.add(new FormElementImpl(node));
			}
		}
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
	public boolean getRecordTimestamp() {
		return recordTimestamp;
	}

	@Override
	public List<FormElement> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormImpl) {
			return id.equals(((FormImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
