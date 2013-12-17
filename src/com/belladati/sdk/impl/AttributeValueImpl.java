package com.belladati.sdk.impl;

import com.belladati.sdk.report.AttributeValue;
import com.fasterxml.jackson.databind.JsonNode;

class AttributeValueImpl implements AttributeValue {

	private final String label;
	private final String value;

	AttributeValueImpl(JsonNode node) throws InvalidAttributeValueException {
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

	class InvalidAttributeValueException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -4920843734203654180L;

		public InvalidAttributeValueException(JsonNode node) {
			super("Invalid attribute value JSON: " + node.toString());
		}
	}
}
