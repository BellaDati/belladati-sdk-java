package com.belladati.sdk.impl;

import java.io.IOException;

import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserInfo;

class UserInfoImpl implements UserInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;

	UserInfoImpl(BellaDatiServiceImpl service, String id, String name) {
		this.service = service;
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public User loadDetails() {
		return service.loadUser(id);
	}

	@Override
	public Object loadImage() throws IOException {
		return service.loadUserImage(id);
	}

	@Override
	public String toString() {
		return name;
	}
}
