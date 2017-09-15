package com.belladati.sdk.dashboard.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
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
import com.belladati.sdk.exception.server.InvalidStreamException;
import com.belladati.sdk.test.SDKTest;
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
		registerSingleDashboard(builder.buildDashboardNode(id, name, lastChange));

		PaginatedList<DashboardInfo> dashboardInfos = getService().getDashboardInfo();
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

		assertNull(getService().getDashboardInfo().load().get(0).getLastChange());
	}

	/** Dashboard last change may be missing. */
	public void infoWithoutLastChange() {
		ObjectNode node = builder.buildDashboardNode(id, name, lastChange);
		node.remove("lastChange");
		registerSingleDashboard(node);

		assertNull(getService().getDashboardInfo().load().get(0).getLastChange());
	}

	/** Dashboard last change may be invalid format. */
	public void infoInvalidLastChange() {
		registerSingleDashboard(builder.buildDashboardNode(id, name, "something invalid"));

		assertNull(getService().getDashboardInfo().load().get(0).getLastChange());
	}

	/** Getting a dashboard info list multiple times returns the same list. */
	public void dashboardInfoListSame() {
		assertSame(getService().getDashboardInfo(), getService().getDashboardInfo());
	}

	/** Individual dashboard can be loaded by ID through getService(). */
	public void loadDashboard() {
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(id, name, lastChange).toString());

		Dashboard dashboard = getService().loadDashboard(id);
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
		Dashboard dashboard = getService().loadDashboard(id);

		assertNull(dashboard.getLastChange());
	}

	/** Dashboard last change may be missing. */
	public void dashboardWithoutLastChange() {
		ObjectNode node = builder.buildDashboardNode(id, name, lastChange);
		node.remove("lastChange");
		server.register(dashboardsUri + "/" + id, node.toString());
		Dashboard dashboard = getService().loadDashboard(id);

		assertNull(dashboard.getLastChange());
	}

	/** Dashboard last change may be invalid format. */
	public void dashboardInvalidLastChange() {
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(id, name, "something invalid").toString());
		Dashboard dashboard = getService().loadDashboard(id);

		assertNull(dashboard.getLastChange());
	}

	/** Dashboard can be loaded from a dashboard info object. */
	public void loadDashboardFromInfo() {
		String idDash = "id2";
		String nameDash = "name2";
		String lastChangeDash = "Tue, 17 Apr 2012 11:18:27 GMT";

		registerSingleDashboard(builder.buildDashboardNode(id, name, lastChange));
		server.register(dashboardsUri + "/" + id, builder.buildDashboardNode(idDash, nameDash, lastChangeDash).toString());

		Dashboard dashboard = getService().getDashboardInfo().load().get(0).loadDetails();

		assertEquals(dashboard.getId(), idDash);
		assertEquals(dashboard.getName(), nameDash);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 17, 11, 18, 27);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(dashboard.getLastChange(), expectedChange.getTime());

		assertEquals(dashboard.getDashlets(), Collections.emptyList());
	}

	/** Can load a dashboard thumbnail from getService(). */
	public void loadThumbnailFromService() {
		server.register(dashboardsUri + "/" + id + "/thumbnail", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});

		BufferedImage thumbnail = (BufferedImage) getService().loadDashboardThumbnail(id);

		server.assertRequestUris(dashboardsUri + "/" + id + "/thumbnail");

		assertEquals(thumbnail.getWidth(), 56);
		assertEquals(thumbnail.getHeight(), 46);
	}

	/** Can load a dashboard thumbnail from info. */
	public void loadThumbnailFromDashboardInfo() {
		server.register(dashboardsUri + "/" + id + "/thumbnail", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});

		DashboardInfo dashboardInfo = new DashboardInfoImpl(getService(), builder.buildDashboardNode(id, name, lastChange));
		BufferedImage thumbnail = (BufferedImage) dashboardInfo.loadThumbnail();

		server.assertRequestUris(dashboardsUri + "/" + id + "/thumbnail");

		assertEquals(thumbnail.getWidth(), 56);
		assertEquals(thumbnail.getHeight(), 46);
	}

	/** Invalid thumbnail results in exception. */
	@Test(expectedExceptions = InvalidStreamException.class)
	public void loadInvalidThumbnail() throws InvalidStreamException {
		server.register(dashboardsUri + "/" + id + "/thumbnail", "not a thumbnail image");

		DashboardInfo dashboardInfo = new DashboardInfoImpl(getService(), builder.buildDashboardNode(id, name, lastChange));
		dashboardInfo.loadThumbnail();
	}

	/** equals/hashcode for dashboard info */
	public void dashboardInfoEquality() {
		DashboardInfo d1 = new DashboardInfoImpl(getService(), builder.buildDashboardNode(id, name, lastChange));
		DashboardInfo d2 = new DashboardInfoImpl(getService(), builder.buildDashboardNode(id, "", null));
		DashboardInfo d3 = new DashboardInfoImpl(getService(), builder.buildDashboardNode("otherId", name, lastChange));

		assertEquals(d1, d2);
		assertEquals(d1.hashCode(), d2.hashCode());

		assertFalse(d1.equals(new Object()));
		assertNotEquals(d1, d3);
	}

	/** equals/hashcode for dashboard */
	public void dashboardEquality() {
		Dashboard d1 = new DashboardImpl(getService(), builder.buildDashboardNode(id, name, lastChange));
		Dashboard d2 = new DashboardImpl(getService(), builder.buildDashboardNode(id, "", null));
		Dashboard d3 = new DashboardImpl(getService(), builder.buildDashboardNode("otherId", name, lastChange));

		assertEquals(d1, d2);
		assertEquals(d1.hashCode(), d2.hashCode());

		assertFalse(d1.equals(new Object()));
		assertNotEquals(d1, d3);
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
