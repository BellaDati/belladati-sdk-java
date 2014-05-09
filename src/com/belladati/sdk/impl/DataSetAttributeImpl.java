package com.belladati.sdk.impl;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.AttributeType;
import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.belladati.sdk.report.AttributeValue;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

public class DataSetAttributeImpl implements Attribute {

	private final String id;
	private final String name;
	private final String code;
	private final AttributeType type;

	DataSetAttributeImpl(JsonNode node) throws InvalidAttributeException {
		if (node.hasNonNull("id") && node.hasNonNull("name") && node.hasNonNull("code") && node.hasNonNull("type")) {
			this.id = node.get("id").asText();
			this.name = node.get("name").asText();
			this.code = node.get("code").asText();
			this.type = AttributeType.valueOfJson(node.get("type").asText());
			if (this.type == null) {
				throw new InvalidAttributeException(node);
			}
		} else {
			throw new InvalidAttributeException(node);
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
	public String getCode() {
		return code;
	}

	@Override
	public AttributeType getType() {
		return type;
	}

	@Override
	public CachedList<AttributeValue> getValues() {
		throw new AttributeValueLoadException();
	}

	class InvalidAttributeException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -4920843734203654180L;

		public InvalidAttributeException(JsonNode node) {
			super("Invalid attribute JSON: " + node.toString());
		}
	}

	class AttributeValueLoadException extends BellaDatiRuntimeException {
		/** The serialVersionUID */
		private static final long serialVersionUID = 4392730653489014114L;

		public AttributeValueLoadException() {
			super("Value loading for data set attributes is currently unsupported.");
		}
	}
}
