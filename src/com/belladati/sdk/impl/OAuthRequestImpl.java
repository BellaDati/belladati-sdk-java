package com.belladati.sdk.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;

class OAuthRequestImpl implements OAuthRequest {

	/** The serialVersionUID */
	private static final long serialVersionUID = -8823818244254801967L;

	private final BellaDatiClient client;

	private final TokenHolder tokenHolder;

	OAuthRequestImpl(BellaDatiClient client, TokenHolder tokenHolder) {
		this.client = client;
		this.tokenHolder = tokenHolder;
	}

	@Override
	public URL getAuthorizationUrl() {
		return getAuthorizationUrl(null);
	}

	@Override
	public URL getAuthorizationUrl(String redirectUrl) {
		if (redirectUrl != null) {
			// check if the redirect URL is valid
			try {
				new URL(redirectUrl);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Invalid redirect URL", e);
			}
		}
		try {
			URIBuilder builder = new URIBuilder(client.getBaseUrl() + "authorizeRequestToken/" + tokenHolder.getToken() + "/"
				+ tokenHolder.getConsumerKey());
			if (redirectUrl != null) {
				builder.addParameter("callbackUrl", redirectUrl);
			}
			return builder.build().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL", e);
		} catch (URISyntaxException e) {
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
