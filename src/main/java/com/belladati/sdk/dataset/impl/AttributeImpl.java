package com.belladati.sdk.dataset.impl;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.AttributeType;
import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.exception.impl.AttributeValueLoadException;
import com.belladati.sdk.exception.impl.InvalidAttributeException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

public class AttributeImpl implements Attribute {

	private final BellaDatiServiceImpl service;
	private final String dataSetId;

	private final String id;
	private final String name;
	private final String code;
	private final AttributeType type;

	public AttributeImpl(BellaDatiServiceImpl service, String dataSetId, JsonNode node) throws InvalidAttributeException {
		this.service = service;
		this.dataSetId = dataSetId;

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
		if (dataSetId == null) {
			throw new AttributeValueLoadException();
		}
		return service.getAttributeValues(dataSetId, code);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AttributeImpl) {
			return id.equals(((AttributeImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
