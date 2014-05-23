package com.belladati.sdk;

import com.belladati.sdk.auth.OAuthRequest;

public class Test {

	public static void main(String[] args) throws Exception {
		BellaDatiService service = xAuthLocal();

		service.uploadData("81", null);
	}

	public static BellaDatiService oAuth() throws Exception {
		// Login credentials: sunview@belladati.com//SunView1
		OAuthRequest oAuthRequest = BellaDati.connect().oAuth("sunviewkey", "sunviewsecret");
		System.out.println("Open the following URL in your browser and log in:");
		System.out.println(oAuthRequest.getAuthorizationUrl());

		System.out.println("Press enter when you're done...");
		System.in.read();

		return oAuthRequest.requestAccess();
	}

	public static BellaDatiService xAuth() throws Exception {
		return BellaDati.connectInsecure("https://service-test.belladati.com").xAuth("chkey", "chsecret",
			"christian.hennigfeld@belladati.com", "Support01");
	}

	public static BellaDatiService xAuthLocal() throws Exception {
		return BellaDati.connectInsecure("http://127.0.0.1:8080/belladati").xAuth("chkey", "chsecret", "ch", "Support01");
	}
}
