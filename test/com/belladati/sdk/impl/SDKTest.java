package com.belladati.sdk.impl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.belladati.sdk.test.JsonBuilder;
import com.belladati.sdk.test.RequestTrackingServer;
import com.belladati.sdk.view.ViewType;

/**
 * Superclass for all SDK test classes using a local test server.
 * 
 * @author Chris Hennigfeld
 */
public class SDKTest {

	/** the server; a new instance is created for every test method */
	protected RequestTrackingServer server;

	/** service instance to invoke test methods on */
	protected BellaDatiServiceImpl service;

	/** builds JSON objects */
	protected final JsonBuilder builder = new JsonBuilder();

	@BeforeMethod(alwaysRun = true)
	protected void setupServer() throws Exception {
		server = new RequestTrackingServer();
		server.start();

		BellaDatiClient client = new BellaDatiClient(server.getHttpURL(), false);
		service = new BellaDatiServiceImpl(client, new TokenHolder("key", "secret"));
	}

	@AfterMethod(alwaysRun = true)
	protected void tearDownServer() throws Exception {
		server.stop();
	}

	@DataProvider(name = "viewTypes")
	protected Object[][] viewTypeProvider() {
		return new Object[][] { { "chart", ViewType.CHART }, { "kpi", ViewType.KPI }, { "text", ViewType.TEXT },
			{ "table", ViewType.TABLE } };
	}

	@DataProvider(name = "jsonViewTypes")
	protected Object[][] jsonViewTypeProvider() {
		return new Object[][] { { "chart", ViewType.CHART }, { "kpi", ViewType.KPI }, { "text", ViewType.TEXT } };
	}

	@DataProvider(name = "unsupportedViewTypes")
	protected Object[][] unsupportedViewTypeProvider() {
		// we don't support tables yet
		return new Object[][] { { "unknown type" } };
	}
}
