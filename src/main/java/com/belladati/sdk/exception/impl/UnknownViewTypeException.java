package com.belladati.sdk.exception.impl;

public class UnknownViewTypeException extends Exception {

	/** The serialVersionUID */
	private static final long serialVersionUID = -9179478821813868612L;

	public UnknownViewTypeException(String type) {
		super("Unknown view type: " + type);
	}

}
