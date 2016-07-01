package com.belladati.sdk.exception.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class InvalidAttributeValueException extends Exception {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4920843734203654180L;

	public InvalidAttributeValueException(JsonNode node) {
		super("Invalid attribute value JSON: " + node.toString());
	}

}
