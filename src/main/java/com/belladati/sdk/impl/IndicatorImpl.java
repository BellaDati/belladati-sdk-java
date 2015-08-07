package com.belladati.sdk.impl;

import com.belladati.sdk.dataset.Indicator;
import com.belladati.sdk.dataset.IndicatorType;
import com.fasterxml.jackson.databind.JsonNode;

public class IndicatorImpl implements Indicator {

	private final String id;
	private final String name;
	private final String code;
	private final String formula;
	private final IndicatorType type;

	IndicatorImpl(JsonNode node) throws InvalidIndicatorException {
		if (node.hasNonNull("id") && node.hasNonNull("name") && node.hasNonNull("type")) {
			this.id = node.get("id").asText();
			this.name = node.get("name").asText();
			this.type = IndicatorType.valueOfJson(node.get("type").asText());
			if (this.type == null) {
				throw new InvalidIndicatorException(node);
			} else if (this.type == IndicatorType.DATA) {
				if (!node.hasNonNull("code")) {
					throw new InvalidIndicatorException(node);
				}
				this.code = node.get("code").asText();
				this.formula = null;
			} else if (this.type == IndicatorType.FORMULA) {
				this.code = null;
				if (!node.hasNonNull("formula")) {
					throw new InvalidIndicatorException(node);
				}
				this.formula = node.get("formula").asText();
			} else {
				this.code = null;
				this.formula = null;
			}
		} else {
			throw new InvalidIndicatorException(node);
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
	public IndicatorType getType() {
		return type;
	}

	@Override
	public String getFormula() {
		return formula;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IndicatorImpl) {
			return id.equals(((IndicatorImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	class InvalidIndicatorException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -4920843734203654180L;

		public InvalidIndicatorException(JsonNode node) {
			super("Invalid indicator JSON: " + node.toString());
		}
	}
}
