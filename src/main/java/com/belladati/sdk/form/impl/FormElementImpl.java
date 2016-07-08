package com.belladati.sdk.form.impl;

import java.util.ArrayList;
import java.util.List;

import com.belladati.sdk.form.FormElement;
import com.belladati.sdk.form.FormElementType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class FormElementImpl implements FormElement {

	private final String id;
	private final String name;
	private final FormElementType type;
	private final Boolean mapToDateColumn;
	private final List<String> items;

	public FormElementImpl(JsonNode json) {
		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.type = FormElementType.valueOfJson(json.get("type").asText());
		this.mapToDateColumn = json.hasNonNull("mapToDateColumn") ? json.get("mapToDateColumn").asBoolean() : null;

		this.items = new ArrayList<String>();
		if (json.hasNonNull("items")) {
			ArrayNode nodes = (ArrayNode) json.get("items");
			for (JsonNode node : nodes) {
				this.items.add(node.get("name").asText());
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
	public FormElementType getType() {
		return type;
	}

	@Override
	public Boolean getMapToDateColumn() {
		return mapToDateColumn;
	}

	@Override
	public List<String> getItems() {
		return items;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormElementImpl) {
			return id.equals(((FormElementImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
