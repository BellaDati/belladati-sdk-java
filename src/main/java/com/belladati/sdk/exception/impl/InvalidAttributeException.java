package com.belladati.sdk.exception.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class InvalidAttributeException extends Exception {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4920843734203654180L;

	public InvalidAttributeException(JsonNode node) {
		super("Invalid attribute JSON: " + node.toString());
	}

}
