package com.belladati.sdk.impl;

import java.net.MalformedURLException;
import java.net.URL;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;

class OAuthRequestImpl implements OAuthRequest {

	private final BellaDatiClient client;

	private final TokenHolder tokenHolder;

	OAuthRequestImpl(BellaDatiClient client, TokenHolder tokenHolder) {
		this.client = client;
		this.tokenHolder = tokenHolder;
	}

	@Override
	public URL getAuthorizationUrl() {
		try {
			return new URL(client.getBaseUrl() + "authorizeRequestToken/" + tokenHolder.getToken() + "/"
				+ tokenHolder.getConsumerKey());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL", e);
		}
	}

	@Override
	public BellaDatiService requestAccess() {
		client.postToken("oauth/accessToken", tokenHolder);
		return new BellaDatiServiceImpl(client, tokenHolder);
	}

	@Override
	public String toString() {
		return "OAuth Authentication(server: " + client.getBaseUrl() + ", key: " + tokenHolder.getConsumerKey() + ")";
	}
}
