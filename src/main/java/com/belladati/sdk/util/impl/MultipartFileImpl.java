package com.belladati.sdk.util.impl;

import java.io.File;

import org.apache.http.entity.ContentType;

import com.belladati.sdk.util.MultipartPiece;

public class MultipartFileImpl implements MultipartPiece<File> {

	private final String name;
	private final File file;

	public MultipartFileImpl(String name, File file) {
		if (name == null || file == null) {
			throw new IllegalArgumentException("All constructor arguments are mandatory");
		}
		this.name = name;
		this.file = file;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContentType() {
		return ContentType.APPLICATION_OCTET_STREAM.getMimeType();
	}

	@Override
	public File getValue() {
		return file;
	}

	public String getFilename() {
		return file.getName();
	}

}
