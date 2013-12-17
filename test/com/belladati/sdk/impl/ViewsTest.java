package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to views.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class ViewsTest extends SDKTest {

	private final String viewsUri = "/api/reports/views/";
	private final String reportsUri = "/api/reports";

	private final String id = "id";
	private final String name = "name";
	private final String description = "report description";
	private final String owner = "report owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";

	/** View JSON is loaded correctly. */
	@Test(dataProvider = "jsonViewTypes")
	public void loadViewJson(String stringType, ViewType viewType) throws UnknownViewTypeException {
		View view = new JsonViewImpl(service, builder.buildViewNode(id, name, stringType));

		ObjectNode viewNode = new ObjectMapper().createObjectNode().put("some field", "some value");
		server.register(viewsUri + id + "/" + stringType, viewNode.toString());

		assertEquals(view.loadContent(), viewNode);

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** View JSON is loaded correctly via service. */
	@Test(dataProvider = "jsonViewTypes")
	public void loadViewJsonFromService(String stringType, ViewType viewType) throws UnknownViewTypeException {
		ObjectNode viewNode = new ObjectMapper().createObjectNode().put("some field", "some value");
		server.register(viewsUri + id + "/" + stringType, viewNode.toString());

		assertEquals(service.loadViewContent(id, viewType), viewNode);

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** Report views field may be null. */
	public void reportViewsNull() {
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange).put("views", (String) null);
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertEquals(report.getViews(), Collections.emptyList());
	}

	/** Report views field may be not an array. */
	public void reportViewsNotArray() {
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange).put("views", "not an array");
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertEquals(report.getViews(), Collections.emptyList());
	}

	/** Report views are read correctly for all known view types. */
	@Test(dataProvider = "viewTypes")
	public void reportViewInfo(String stringType, ViewType viewType) {
		String viewId = "viewId";
		String viewName = "viewName";

		ArrayNode views = new ObjectMapper().createArrayNode();
		views.add(builder.buildViewNode(viewId, viewName, stringType));
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.put("views", views);
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertEquals(report.getViews().size(), 1);
		View viewInfo = report.getViews().get(0);
		assertEquals(viewInfo.getId(), viewId);
		assertEquals(viewInfo.getName(), viewName);
		assertEquals(viewInfo.getType(), viewType);
	}

	/** Report views of unsupported type are ignored. */
	@Test(dataProvider = "unsupportedViewTypes")
	public void reportViewInfoUnknownType(String stringType) {
		String viewId = "viewId";
		String viewName = "viewName";

		ArrayNode views = new ObjectMapper().createArrayNode();
		views.add(builder.buildViewNode(viewId, viewName, stringType));
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.put("views", views);
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertEquals(report.getViews(), Collections.emptyList());
	}

	/** Report views without type are ignored. */
	public void reportViewInfoWithoutType() {
		String viewId = "viewId";
		String viewName = "viewName";

		ArrayNode views = new ObjectMapper().createArrayNode();
		ObjectNode viewInfoNode = builder.buildViewNode(viewId, viewName, "");
		viewInfoNode.remove("type");
		views.add(viewInfoNode);
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.put("views", views);
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertEquals(report.getViews(), Collections.emptyList());
	}
}
