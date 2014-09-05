package com.belladati.sdk.export;

import java.io.Serializable;

import com.belladati.sdk.dashboard.Dashlet;

class StoredDashlet implements Dashlet, Serializable {

	private static final long serialVersionUID = 5054248260881472354L;

	private final Type type;
	private final Object content;

	StoredDashlet(Type type, Object content) {
		this.type = type;
		this.content = content;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Object getContent() {
		return content;
	}

}
