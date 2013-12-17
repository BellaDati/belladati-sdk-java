package com.belladati.sdk;

import com.belladati.sdk.auth.OAuthRequest;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.util.PaginatedList;

public class Test {

	public static void main(String[] args) throws Exception {
		BellaDatiService service = xAuth();

		System.out.println("Loading reports...");
		PaginatedList<ReportInfo> infos = service.getReportInfo().load();
		Report report = infos.get(0).loadDetails();
		System.out.println(report.getAttributes());
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
}
