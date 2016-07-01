package com.belladati.sdk.exception.impl;

import com.belladati.sdk.exception.BellaDatiRuntimeException;

public class AttributeValueLoadException extends BellaDatiRuntimeException {

	/** The serialVersionUID */
	private static final long serialVersionUID = 4392730653489014114L;

	public AttributeValueLoadException() {
		super("Value loading for data set attributes is currently unsupported.");
	}

}
