package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.dashboard.Dashlet.Type;
import com.belladati.sdk.export.PageStorage;
import com.belladati.sdk.view.View;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests storing dashboards and their contents.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class StoreDashboardsTest extends SDKTest {

	private final String dashboardsUri = "/api/dashboards/";
	private final String viewsUri = "/api/reports/views/";

	private final String id = "123";
	private final String name = "dashboard name";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";

	private final String viewId = "viewId";
	private final String viewName = "viewName";

	/** store a dashboard */
	public void storeDashboard() throws IOException {
		ObjectNode dashboardJson = builder.buildDashboardNode(id, name, lastChange);
		Dashboard dashboard = new DashboardImpl(service, dashboardJson);

		server
			.register(dashboardsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		assertDashboard(new PageStorage().storeDashboard(dashboard));

		server.assertRequestUris(dashboardsUri + id + "/thumbnail");
	}

	/** store a dashboard info */
	public void storeDashboardInfo() throws IOException {
		ObjectNode dashboardJson = builder.buildDashboardNode(id, name, lastChange);
		DashboardInfo dashboardInfo = new DashboardInfoImpl(service, dashboardJson);

		server.register(dashboardsUri + id, dashboardJson.toString());
		server
			.register(dashboardsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		assertDashboard(new PageStorage().storeDashboard(dashboardInfo));

		server.assertRequestUris(dashboardsUri + id, dashboardsUri + id + "/thumbnail");
	}

	/** serialize/deserialize a stored dashboard */
	public void serializeDashboard() throws IOException, ClassNotFoundException {
		ObjectNode dashboardJson = builder.buildDashboardNode(id, name, lastChange);
		Dashboard dashboard = new DashboardImpl(service, dashboardJson);

		server
			.register(dashboardsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		DashboardInfo stored = new PageStorage().storeDashboard(dashboard);
		DashboardInfo newStored = (DashboardInfo) serializeDeserialize(stored);

		assertDashboard(newStored);

		server.assertRequestUris(dashboardsUri + id + "/thumbnail");
	}

	/** serialize/deserialize a stored dashboard */
	public void serializeDashboardNoThumbnail() throws IOException, ClassNotFoundException {
		ObjectNode dashboardJson = builder.buildDashboardNode(id, name, lastChange);
		Dashboard dashboard = new DashboardImpl(service, dashboardJson);

		DashboardInfo stored = new PageStorage().storeDashboard(dashboard);
		DashboardInfo newStored = (DashboardInfo) serializeDeserialize(stored);

		assertDashboard(newStored, false);

		server.assertRequestUris(dashboardsUri + id + "/thumbnail");
	}

	private void assertDashboard(DashboardInfo stored) throws IOException {
		assertDashboard(stored, true);
	}

	private void assertDashboard(DashboardInfo stored, boolean hasThumbnail) throws IOException {
		assertEquals(stored.getId(), id);
		assertEquals(stored.getName(), name);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(stored.getLastChange(), expectedChange.getTime());

		BufferedImage thumbnail;
		if (hasThumbnail) {
			thumbnail = (BufferedImage) stored.loadThumbnail();
			assertEquals(thumbnail.getWidth(), 56);
			assertEquals(thumbnail.getHeight(), 46);
		} else {
			assertNull(stored.loadThumbnail());
		}

		assertEquals(stored.loadDetails().getId(), id);
		assertEquals(stored.loadDetails().getName(), name);
		assertEquals(stored.loadDetails().getLastChange(), expectedChange.getTime());

		if (hasThumbnail) {
			thumbnail = (BufferedImage) stored.loadDetails().loadThumbnail();
			assertEquals(thumbnail.getWidth(), 56);
			assertEquals(thumbnail.getHeight(), 46);
		} else {
			assertNull(stored.loadDetails().loadThumbnail());
		}
	}

	/** store a dashboard containing a text dashlet */
	public void storeDashboardText() throws IOException, ClassNotFoundException {
		ObjectNode dashboardJson = builder.buildDashboardNode(id, name, lastChange);
		String dashletText = "some text";
		JsonNode dashletJson = new ObjectMapper().createObjectNode().put("type", "textContent").put("textContent", dashletText);
		dashboardJson.put("dashlets", new ObjectMapper().createArrayNode().add(dashletJson));
		DashboardInfo dashboardInfo = new DashboardInfoImpl(service, dashboardJson);

		server.register(dashboardsUri + id, dashboardJson.toString());
		server
			.register(dashboardsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		DashboardInfo stored = new PageStorage().storeDashboard(dashboardInfo);
		DashboardInfo newStored = (DashboardInfo) serializeDeserialize(stored);

		server.assertRequestUris(dashboardsUri + id, dashboardsUri + id + "/thumbnail");
		server.resetRequestUris();

		assertEquals(newStored.loadDetails().getDashlets().size(), 1);
		Dashlet dashlet = newStored.loadDetails().getDashlets().get(0);
		assertEquals(dashlet.getType(), Type.TEXT);
		assertEquals(dashlet.getContent(), dashletText);

		server.assertRequestUris();
	}

	/** store a dashboard containing a view dashlet */
	public void storeDashboardView() throws IOException, ClassNotFoundException {
		ObjectNode dashboardJson = builder.buildDashboardNode(id, name, lastChange);
		ObjectNode viewJson = new ObjectMapper().createObjectNode().put("key", "value");

		ObjectNode dashletJson = new ObjectMapper().createObjectNode();
		dashletJson.put("canAccessViewReport", true).put("type", "viewReport")
			.put("viewReport", builder.buildViewNode(viewId, viewName, "chart"));
		dashboardJson.put("dashlets", new ObjectMapper().createArrayNode().add(dashletJson));
		DashboardInfo dashboardInfo = new DashboardInfoImpl(service, dashboardJson);

		server.register(dashboardsUri + id, dashboardJson.toString());
		server
			.register(dashboardsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
		server.register(viewsUri + viewId + "/chart", viewJson.toString());

		DashboardInfo stored = new PageStorage().storeDashboard(dashboardInfo);
		DashboardInfo newStored = (DashboardInfo) serializeDeserialize(stored);

		server.assertRequestUris(dashboardsUri + id, dashboardsUri + id + "/thumbnail", viewsUri + viewId + "/chart");
		server.resetRequestUris();

		assertEquals(newStored.loadDetails().getDashlets().size(), 1);
		Dashlet dashlet = newStored.loadDetails().getDashlets().get(0);
		assertEquals(dashlet.getType(), Type.VIEW);
		View view = (View) dashlet.getContent();
		assertEquals(view.getId(), viewId);
		assertEquals(view.loadContent(), viewJson);

		server.assertRequestUris();
	}
}
