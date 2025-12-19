package com.belladati.sdk.impl;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp5.CommonsHttpOAuthConsumer;

import java.io.Serializable;
import java.util.Random;

public class TokenHolder implements Serializable {

	/** The serialVersionUID */
	private static final long serialVersionUID = 8122702080702303615L;

	private static final Random RANDOM = new Random();

	private final String consumerKey;
	private final String consumerSecret;

	private String token;
	private String tokenSecret;

	public TokenHolder(String consumerKey, String consumerSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
	}

	public OAuthConsumer createConsumer() {
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret) {
			private static final long serialVersionUID = 2077439267247908434L;

                    @Override
			protected String generateNonce() {
				// thread-safe nonce generation
				// http://code.google.com/p/oauth-signpost/issues/detail?id=41
				return Long.toString(RANDOM.nextLong());
                    }
		};
		consumer.setTokenWithSecret(token, tokenSecret);
		return consumer;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public boolean hasToken() {
		return token != null;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token, String tokenSecret) {
		this.token = token;
		this.tokenSecret = tokenSecret;
	}
}
