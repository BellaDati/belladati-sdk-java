package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.dashboard.Dashlet.Type;
import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to views.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class DashletsTest extends SDKTest {

	private final String dashboardsUri = "/api/dashboards";

	private final String dashboardId = "dashboardId";
	private final String dashboardName = "dashboard name";
	private final String viewId = "viewId";
	private final String viewName = "view name";

	/** Dashlet view JSON is loaded correctly. */
	@Test(dataProvider = "viewTypes")
	public void loadDashletView(String stringType, ViewType viewType) throws UnknownViewTypeException {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		dashletNode.put("canAccessViewReport", true).put("type", "viewReport")
			.put("viewReport", builder.buildViewNode(viewId, viewName, stringType));

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets().size(), 1);
		Dashlet dashlet = dashboard.getDashlets().get(0);

		assertEquals(dashlet.getType(), Type.VIEW);

		View view = (View) dashlet.getContent();
		assertEquals(view.getId(), viewId);
		assertEquals(view.getName(), viewName);
		assertEquals(view.getType(), viewType);

		server.assertRequestUris(dashboardsUri + "/" + dashboardId);
	}

	/** Dashlets pointing to inaccessible views are ignored. */
	public void inaccessibleView() {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		dashletNode.put("canAccessViewReport", false).put("type", "viewReport")
			.put("viewReport", builder.buildViewNode(viewId, viewName, "chart"));

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlets pointing to views with unknown accessibility are ignored. */
	public void missingAccessibilityView() {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		dashletNode.put("type", "viewReport").put("viewReport", builder.buildViewNode(viewId, viewName, "chart"));

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlets pointing to views with unknown accessibility are ignored. */
	public void notBooleanAccessibilityView() {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		dashletNode.put("canAccessViewReport", "not a boolean").put("type", "viewReport")
			.put("viewReport", builder.buildViewNode(viewId, viewName, "chart"));

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlets pointing to views with unknown type are ignored. */
	@Test(dataProvider = "unsupportedViewTypes")
	public void unknownTypeView(String stringType) {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		dashletNode.put("canAccessViewReport", true).put("type", "viewReport")
			.put("viewReport", builder.buildViewNode(viewId, viewName, stringType));

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlets pointing to views without type are ignored. */
	public void noTypeView() {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "");
		viewNode.remove("type");
		dashletNode.put("canAccessViewReport", true).put("type", "viewReport").put("viewReport", viewNode);

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlets pointing to views without a view element are ignored. */
	public void noViewElement() {
		registerDashboardWith(new ObjectMapper().createObjectNode().put("canAccessViewReport", true).put("type", "viewReport"));

		Dashboard dashboard = service.loadDashboard(dashboardId);
		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlet text content is loaded correctly. */
	public void loadDashletText() throws UnknownViewTypeException {
		String text = "some text here";
		ObjectNode dashletNode = new ObjectMapper().createObjectNode().put("type", "textContent").put("textContent", text);

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets().size(), 1);
		Dashlet dashlet = dashboard.getDashlets().get(0);

		assertEquals(dashlet.getType(), Type.TEXT);
		assertEquals(dashlet.getContent(), text);
	}

	/** Dashlet text content may be empty. */
	public void textDashletEmptyText() throws UnknownViewTypeException {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode().put("type", "textContent").put("textContent", "");

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets().size(), 1);
		Dashlet dashlet = dashboard.getDashlets().get(0);

		assertEquals(dashlet.getType(), Type.TEXT);
		assertEquals(dashlet.getContent(), "");
	}

	/** Text dashlets with null content are ignored. */
	public void textDashletNullText() throws UnknownViewTypeException {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode().put("type", "textContent")
			.put("textContent", (String) null);

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Text dashlets without content are ignored. */
	public void textDashletNoText() throws UnknownViewTypeException {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode().put("type", "textContent");

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashboard dashlets field may be null. */
	public void dashletsNull() {
		ObjectNode node = builder.buildDashboardNode(dashboardId, dashboardName, null).put("dashlets", (String) null);
		server.register(dashboardsUri + "/" + dashboardId, node.toString());
		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashboard dashlets field may be not an array. */
	public void dashletsNotArray() {
		ObjectNode node = builder.buildDashboardNode(dashboardId, dashboardName, null).put("dashlets", "not an array");
		server.register(dashboardsUri + "/" + dashboardId, node.toString());
		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Dashlets of unknown type are ignored. */
	public void loadDashletUnknownType() throws UnknownViewTypeException {
		ObjectNode dashletNode = new ObjectMapper().createObjectNode();
		dashletNode.put("canAccessViewReport", true).put("type", "not a dashlet type")
			.put("viewReport", builder.buildViewNode(viewId, viewName, "chart"));

		registerDashboardWith(dashletNode);

		Dashboard dashboard = service.loadDashboard(dashboardId);

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	private void registerDashboardWith(ObjectNode dashlet) {
		ObjectNode dashboardNode = builder.buildDashboardNode(dashboardId, dashboardName, null);
		dashboardNode.put("dashlets", new ObjectMapper().createArrayNode().add(dashlet));
		server.register(dashboardsUri + "/" + dashboardId, dashboardNode.toString());
	}
}
