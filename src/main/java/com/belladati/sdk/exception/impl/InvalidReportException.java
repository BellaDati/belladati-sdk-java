package com.belladati.sdk.exception.impl;

import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.fasterxml.jackson.databind.JsonNode;

public class InvalidReportException extends BellaDatiRuntimeException {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4920843734203654180L;

	public InvalidReportException(JsonNode node) {
		super("Invalid report JSON: " + node.toString());
	}

}