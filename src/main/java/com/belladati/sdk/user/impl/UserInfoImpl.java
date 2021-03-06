package com.belladati.sdk.user.impl;

import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserInfo;

public class UserInfoImpl implements UserInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;

	public UserInfoImpl(BellaDatiServiceImpl service, String id, String name) {
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
	public Object loadImage() {
		return service.loadUserImage(id);
	}

	@Override
	public String loadStatus() {
		return service.loadUserStatus(id);
	}

	@Override
	public void postStatus(String status) {
		service.postUserStatus(id, status);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserInfoImpl) {
			return id.equals(((UserInfoImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
