package com.belladati.sdk.util.impl;

import com.belladati.sdk.util.MultipartPiece;
import org.apache.hc.core5.http.ContentType;

public class MultipartTextImpl implements MultipartPiece<String> {

	private final String name;
	private final String value;

	public MultipartTextImpl(String name, String value) {
		if (name == null || value == null) {
			throw new IllegalArgumentException("All constructor arguments are mandatory");
		}
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContentType() {
		return ContentType.TEXT_PLAIN.getMimeType();
	}

	@Override
	public String getValue() {
		return value;
	}

}
