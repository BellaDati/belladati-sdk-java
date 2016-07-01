package com.belladati.sdk.exception.impl;

import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.fasterxml.jackson.databind.JsonNode;

public class InvalidDomainException extends BellaDatiRuntimeException {

	/** The serialVersionUID */
	private static final long serialVersionUID = -8545539519152240039L;

	public InvalidDomainException(JsonNode node) {
		super("Invalid domain JSON: " + node.toString());
	}

}