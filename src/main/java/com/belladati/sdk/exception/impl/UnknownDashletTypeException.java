package com.belladati.sdk.exception.impl;

public class UnknownDashletTypeException extends DashletException {

	/** The serialVersionUID */
	private static final long serialVersionUID = -9179478821813868612L;

	public UnknownDashletTypeException(String type) {
		super("Unknown view type: " + type);
	}

}
