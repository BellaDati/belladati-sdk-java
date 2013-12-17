package com.belladati.sdk.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;

class BellaDatiConnectionImpl implements BellaDatiConnection {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4137207765985769374L;

	private final BellaDatiClient client;

	BellaDatiConnectionImpl(String baseUrl, boolean trustSelfSigned) {
		client = new BellaDatiClient(baseUrl, trustSelfSigned);
	}

	@Override
	public OAuthRequest oAuth(String consumerKey, String consumerSecret) {
		TokenHolder tokenHolder = new TokenHolder(consumerKey, consumerSecret);
		client.postToken("oauth/requestToken", tokenHolder);
		return new OAuthRequestImpl(client, tokenHolder);
	}

	@Override
	public BellaDatiService xAuth(String consumerKey, String consumerSecret, String username, String password) {
		TokenHolder tokenHolder = new TokenHolder(consumerKey, consumerSecret);
		List<? extends NameValuePair> parameters = Arrays.asList(new BasicNameValuePair("x_auth_username", username),
			new BasicNameValuePair("x_auth_password", password));

		client.postToken("oauth/accessToken", tokenHolder, parameters);
		return new BellaDatiServiceImpl(client, tokenHolder);
	}
}
