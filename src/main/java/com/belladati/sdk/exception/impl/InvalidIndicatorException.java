package com.belladati.sdk.exception.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class InvalidIndicatorException extends Exception {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4920843734203654180L;

	public InvalidIndicatorException(JsonNode node) {
		super("Invalid indicator JSON: " + node.toString());
	}

}
