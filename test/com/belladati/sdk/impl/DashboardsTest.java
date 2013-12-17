package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to dashboards.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class DashboardsTest extends SDKTest {

	private final String dashboardsUri = "/api/dashboards";

	private final String id = "123";
	private final String name = "dashboard name";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";

	/** Regular dashboard info data is loaded correctly. */
	public void loadDashboardInfo() {
		PaginatedList<DashboardInfo> dashboardInfos = service.getDashboardInfo();

		registerSingleDashboard(builder.buildDashboardNode(id, name, lastChange));

		dashboardInfos.load();
		server.assertRequestUris(dashboardsUri);
		assertEquals(dashboardInfos.size(), 1);

		DashboardInfo info = dashboardInfos.get(0);
		assertEquals(info.getId(), id);
		assertEquals(info.getName(), name);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(info.getLastChange(), expectedChange.getTime());

		assertEquals(info.toString(), name);
	}

	/** Dashboard last change may be null. */
	public void infoNullLastChange() {
		registerSingleDashboard(builder.buildDashboardNode(id, name, null));

		assertNull(service.getDashboardInfo().load().get(0).getLastChange());
	}

	/** Dashboard last change may be missing. */
	public void infoWithoutLastChange() {
		ObjectNode node = builder.buildDashboardNode(id, name, lastChange);
		node.remove("lastChange");
		registerSingleDashboard(node);

		assertNull(service.getDashboardInfo().load().get(0).getLastChange());
	}

	/** Dashboard last change may be invalid format. */
	public void infoInvalidLastChange() {
		registerSingleDashboard(builder.buildDashboardNode(id, name, "something invalid"));

		assertNull(service.getDashboardInfo().load().get(0).getLastChange());
	}

	/** Getting a dashboard info list multiple times returns the same list. */
	public void dashboardInfoListSame() {
		assertSame(service.getDashboardInfo(), service.getDashboardInfo());
	}

	/** Individual dashboard can be loaded by ID through service. */
	public void loadDashboard() {
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(id, name, lastChange).toString());

		Dashboard dashboard = service.loadDashboard(id);
		server.assertRequestUris(dashboardsUri + "/" + id);

		assertEquals(dashboard.getId(), id);
		assertEquals(dashboard.getName(), name);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(dashboard.getLastChange(), expectedChange.getTime());

		assertEquals(dashboard.getDashlets(), Collections.emptyList());

		assertEquals(dashboard.toString(), name);
	}

	/** Dashboard last change may be null. */
	public void dashboardNullLastChange() {
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(id, name, null).toString());
		Dashboard dashboard = service.loadDashboard(id);

		assertNull(dashboard.getLastChange());
	}

	/** Dashboard last change may be missing. */
	public void dashboardWithoutLastChange() {
		ObjectNode node = builder.buildDashboardNode(id, name, lastChange);
		node.remove("lastChange");
		server.register(dashboardsUri + "/" + id, node.toString());
		Dashboard dashboard = service.loadDashboard(id);

		assertNull(dashboard.getLastChange());
	}

	/** Dashboard last change may be invalid format. */
	public void dashboardInvalidLastChange() {
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(id, name, "something invalid").toString());
		Dashboard dashboard = service.loadDashboard(id);

		assertNull(dashboard.getLastChange());
	}

	/** Dashboard can be loaded from a dashboard info object. */
	public void loadDashboardFromInfo() {
		String idDash = "id2";
		String nameDash = "name2";
		String lastChangeDash = "Tue, 17 Apr 2012 11:18:27 GMT";

		registerSingleDashboard(builder.buildDashboardNode(id, name, lastChange));
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(idDash, nameDash, lastChangeDash).toString());

		Dashboard dashboard = service.getDashboardInfo().load().get(0).loadDetails();

		assertEquals(dashboard.getId(), idDash);
		assertEquals(dashboard.getName(), nameDash);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 17, 11, 18, 27);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(dashboard.getLastChange(), expectedChange.getTime());

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Can load a dashboard thumbnail from service. */
	public void loadThumbnailFromService() throws IOException {
		server.register(dashboardsUri + "/" + id + "/thumbnail", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
			}
		});

		BufferedImage thumbnail = (BufferedImage) service.loadDashboardThumbnail(id);

		server.assertRequestUris(dashboardsUri + "/" + id + "/thumbnail");

		assertEquals(thumbnail.getWidth(), 56);
		assertEquals(thumbnail.getHeight(), 46);
	}

	/** Can load a dashboard thumbnail from info. */
	public void loadThumbnailFromDashboardInfo() throws IOException {
		DashboardInfo dashboardInfo = new DashboardInfoImpl(service, builder.buildDashboardNode(id, name, lastChange));

		server.register(dashboardsUri + "/" + id + "/thumbnail", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
			}
		});

		BufferedImage thumbnail = (BufferedImage) dashboardInfo.loadThumbnail();

		server.assertRequestUris(dashboardsUri + "/" + id + "/thumbnail");

		assertEquals(thumbnail.getWidth(), 56);
		assertEquals(thumbnail.getHeight(), 46);
	}

	/** Invalid thumbnail results in exception. */
	@Test(expectedExceptions = IOException.class)
	public void loadInvalidThumbnail() throws IOException {
		DashboardInfo dashboardInfo = new DashboardInfoImpl(service, builder.buildDashboardNode(id, name, lastChange));

		server.register(dashboardsUri + "/" + id + "/thumbnail", "not a thumbnail image");

		dashboardInfo.loadThumbnail();
	}

	/**
	 * Tells the server to return the specified node as the only dashboard node
	 * in a <tt>dashboards</tt> array.
	 * 
	 * @param node the node to return
	 */
	private void registerSingleDashboard(JsonNode node) {
		server.registerPaginatedItem(dashboardsUri, "dashboards", node);
	}
}
