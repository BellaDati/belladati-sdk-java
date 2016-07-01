package com.belladati.sdk.dataset.impl;

import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.exception.impl.InvalidAttributeValueException;
import com.fasterxml.jackson.databind.JsonNode;

public class AttributeValueImpl implements AttributeValue {

	private final String label;
	private final String value;

	public AttributeValueImpl(JsonNode node) throws InvalidAttributeValueException {
		if (node.hasNonNull("label") && node.hasNonNull("value")) {
			this.label = node.get("label").asText();
			this.value = node.get("value").asText();
		} else {
			throw new InvalidAttributeValueException(node);
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AttributeValue) {
			return value.equals(((AttributeValue) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

}
