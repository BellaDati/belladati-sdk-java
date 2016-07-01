package com.belladati.sdk.exception.impl;

import com.fasterxml.jackson.databind.JsonNode;

public class InvalidDataSourceImportException extends Exception {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4920843734203654180L;

	public InvalidDataSourceImportException(JsonNode node) {
		super("Invalid data source import JSON: " + node.toString());
	}

}
