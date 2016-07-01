package com.belladati.sdk.exception.impl;

public abstract class DashletException extends Exception {

	/** The serialVersionUID */
	private static final long serialVersionUID = -2686607915455923775L;

	public DashletException(String message) {
		super(message);
	}

	public DashletException(Throwable cause) {
		super(cause);
	}

}
