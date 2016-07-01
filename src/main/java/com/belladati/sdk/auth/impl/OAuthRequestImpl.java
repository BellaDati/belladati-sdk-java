package com.belladati.sdk.auth.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;
import com.belladati.sdk.impl.BellaDatiClient;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.impl.TokenHolder;

public class OAuthRequestImpl implements OAuthRequest {

	/** The serialVersionUID */
	private static final long serialVersionUID = -8823818244254801967L;

	private final BellaDatiClient client;

	private final TokenHolder tokenHolder;

	public OAuthRequestImpl(BellaDatiClient client, TokenHolder tokenHolder) {
		this.client = client;
		this.tokenHolder = tokenHolder;
	}

	@Override
	public URL getAuthorizationUrl() {
		try {
			return new URIBuilder(
				client.getBaseUrl() + "authorizeRequestToken/" + tokenHolder.getToken() + "/" + tokenHolder.getConsumerKey())
					.build().toURL();
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
