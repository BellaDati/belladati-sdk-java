package com.belladati.sdk.impl;

import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;
import com.belladati.sdk.auth.impl.OAuthRequestImpl;
import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.auth.AuthorizationException;
import oauth.signpost.OAuth;
import oauth.signpost.http.HttpParameters;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

class BellaDatiConnectionImpl implements BellaDatiConnection {

	/** The serialVersionUID */
	private static final long serialVersionUID = -4137207765985769374L;

	private final BellaDatiClient client;

	BellaDatiConnectionImpl(String baseUrl, boolean trustSelfSigned) {
		client = new BellaDatiClient(baseUrl, trustSelfSigned);
	}

	@Override
	public OAuthRequest oAuth(String consumerKey, String consumerSecret) {
		return oAuth(consumerKey, consumerSecret, null);
	}

	@Override
	public OAuthRequest oAuth(String consumerKey, String consumerSecret, String redirectUrl) throws ConnectionException,
		AuthorizationException {
		HttpParameters params = new HttpParameters();
		if (redirectUrl != null) {
			// check if the redirect URL is valid
			try {
				new URL(redirectUrl);
				params.put(OAuth.OAUTH_CALLBACK, OAuth.percentEncode(redirectUrl));
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Invalid redirect URL", e);
			}
		}
		TokenHolder tokenHolder = new TokenHolder(consumerKey, consumerSecret);

		client.postToken("oauth/requestToken", tokenHolder, params);
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

	@Override
	public String toString() {
		return "Connection(server: " + client.getBaseUrl() + ")";
	}
}
