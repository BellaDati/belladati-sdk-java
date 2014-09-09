package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.view.JsonView;
import com.belladati.sdk.view.TableView;
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
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, stringType));

		ObjectNode viewNode = new ObjectMapper().createObjectNode().put("some field", "some value");
		server.register(viewsUri + id + "/" + stringType, viewNode.toString());

		assertEquals(view.toString(), name);
		assertEquals(view.loadContent(), viewNode);

		server.assertRequestUris(viewsUri + id + "/" + stringType);

		assertTrue(view instanceof JsonView, "Wrong view instance type, was " + view.getClass());
	}

	/** View JSON is loaded correctly via service. */
	@Test(dataProvider = "jsonViewTypes")
	public void loadViewJsonFromService(String stringType, ViewType viewType) throws UnknownViewTypeException {
		ObjectNode viewNode = new ObjectMapper().createObjectNode().put("some field", "some value");
		server.register(viewsUri + id + "/" + stringType, viewNode.toString());

		assertEquals(service.loadViewContent(id, viewType), viewNode);

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** View JSON is loaded correctly through a loader. */
	@Test(dataProvider = "jsonViewTypes")
	public void loadViewJsonFromLoader(String stringType, ViewType viewType) throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, stringType));

		ObjectNode viewNode = new ObjectMapper().createObjectNode().put("some field", "some value");
		server.register(viewsUri + id + "/" + stringType, viewNode.toString());

		assertEquals(view.createLoader().loadContent(), viewNode);

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** View JSON is loaded correctly via service through a loader. */
	@Test(dataProvider = "jsonViewTypes")
	public void loadViewJsonFromServiceLoader(String stringType, ViewType viewType) throws UnknownViewTypeException {
		ObjectNode viewNode = new ObjectMapper().createObjectNode().put("some field", "some value");
		server.register(viewsUri + id + "/" + stringType, viewNode.toString());

		assertEquals(service.createViewLoader(id, viewType).loadContent(), viewNode);

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
		View view = report.getViews().get(0);
		assertEquals(view.getId(), viewId);
		assertEquals(view.getName(), viewName);
		assertEquals(view.getType(), viewType);

		if (viewType == ViewType.TABLE) {
			assertTrue(view instanceof TableView, "Wrong view instance type, was " + view.getClass());
		} else {
			assertTrue(view instanceof JsonView, "Wrong view instance type, was " + view.getClass());
		}
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

	/** equals/hashcode for views (JSON and tables) */
	public void equality() throws UnknownViewTypeException {
		View v1 = ViewImpl.buildView(service, builder.buildViewNode(id, name, "chart"));
		View v2 = ViewImpl.buildView(service, builder.buildViewNode(id, "", "table"));
		View v3 = ViewImpl.buildView(service, builder.buildViewNode("otherId", name, "chart"));

		assertEquals(v1, v2);
		assertEquals(v1.hashCode(), v2.hashCode());

		assertNotEquals(v1, v3);
	}

	/** no date/time definition means neither is supported, nothing is set */
	public void noDateTimeDefinition() throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, "chart"));
		assertFalse(view.isDateIntervalSupported());
		assertFalse(view.isTimeIntervalSupported());
		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** neither is supported in the definition */
	public void neitherSupported() throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service,
			builder.insertViewDateTimeDefinition(false, false, builder.buildViewNode(id, name, "chart")));
		assertFalse(view.isDateIntervalSupported());
		assertFalse(view.isTimeIntervalSupported());
	}

	/** only date is supported in the definition */
	public void dateSupported() throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service,
			builder.insertViewDateTimeDefinition(true, false, builder.buildViewNode(id, name, "chart")));
		assertTrue(view.isDateIntervalSupported());
		assertFalse(view.isTimeIntervalSupported());
	}

	/** only time is supported in the definition */
	public void timeSupported() throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service,
			builder.insertViewDateTimeDefinition(false, true, builder.buildViewNode(id, name, "chart")));
		assertFalse(view.isDateIntervalSupported());
		assertTrue(view.isTimeIntervalSupported());
	}

	/** both are supported in the definition */
	public void bothSupported() throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service,
			builder.insertViewDateTimeDefinition(true, true, builder.buildViewNode(id, name, "chart")));
		assertTrue(view.isDateIntervalSupported());
		assertTrue(view.isTimeIntervalSupported());
	}

	/** system locale doesn't affect upper/lowercase conversion */
	@Test(dataProvider = "viewTypes", groups = "locale")
	public void localeCaseConversion(String stringType, ViewType viewType) throws UnknownViewTypeException {
		Locale.setDefault(new Locale("tr"));
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, stringType));
		assertEquals(view.getType(), viewType);
	}

	/** No locale means no parameter. */
	@Test(dataProvider = "jsonViewTypes")
	public void noLocale(String stringType, ViewType viewType) throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, stringType));

		server.register(viewsUri + id + "/" + stringType, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertFalse(holder.getUrlParameters().containsKey("lang"));
				holder.response.setEntity(new StringEntity("{}"));
			}
		});
		view.createLoader().loadContent();
		view.loadContent();

		server.assertRequestUris(viewsUri + id + "/" + stringType, viewsUri + id + "/" + stringType);
	}

	/** custom locale is passed as parameter. */
	@Test(dataProvider = "jsonViewTypes")
	public void customLocale(String stringType, ViewType viewType) throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, stringType));

		server.register(viewsUri + id + "/" + stringType, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters().get("lang"), "tr");
				holder.response.setEntity(new StringEntity("{}"));
			}
		});
		view.createLoader().setLocale(new Locale("tR")).loadContent();

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** predefined locale is passed as parameter. */
	@Test(dataProvider = "jsonViewTypes")
	public void builtInLocale(String stringType, ViewType viewType) throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, stringType));

		server.register(viewsUri + id + "/" + stringType, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters().get("lang"), "de");
				holder.response.setEntity(new StringEntity("{}"));
			}
		});
		view.createLoader().setLocale(Locale.GERMAN).loadContent();

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** no predefined filter set */
	public void noPredefinedFilter() throws UnknownViewTypeException {
		View view = ViewImpl.buildView(service, builder.buildViewNode(id, name, "chart"));
		assertFalse(view.hasPredefinedFilters());
		assertEquals(view.getPredefinedFilters(), Collections.emptyList());
	}
}
