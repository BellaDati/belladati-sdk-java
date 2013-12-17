package com.belladati.sdk.impl;

import com.belladati.sdk.report.Attribute;
import com.belladati.sdk.report.AttributeValue;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

class AttributeImpl implements Attribute {

	private final BellaDatiServiceImpl service;
	private final String reportId;

	private final String name;
	private final String code;

	AttributeImpl(BellaDatiServiceImpl service, String reportId, JsonNode node) throws InvalidAttributeException {
		this.service = service;
		this.reportId = reportId;

		if (node.hasNonNull("name") && node.hasNonNull("code")) {
			this.name = node.get("name").asText();
			this.code = node.get("code").asText();
		} else {
			throw new InvalidAttributeException(node);
		}
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
	public CachedList<AttributeValue> getValues() {
		return service.getAttributeValues(reportId, code);
	}

	@Override
	public String toString() {
		return "Attribute(name: " + name + ", code: " + code + ")";
	}

	class InvalidAttributeException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -4920843734203654180L;

		public InvalidAttributeException(JsonNode node) {
			super("Invalid attribute JSON: " + node.toString());
		}
	}
}
