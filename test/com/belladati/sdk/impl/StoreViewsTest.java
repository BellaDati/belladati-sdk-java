package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.export.ViewStorage;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.view.JsonView;
import com.belladati.sdk.view.TableView;
import com.belladati.sdk.view.TableView.Table;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests storing and serializing content objects.
 * 
 * @author Chris
 */
@Test
public class StoreViewsTest extends SDKTest {

	private final String viewsUri = "/api/reports/views/";
	private final String id = "id";
	private final String name = "name";

	/** store a JsonView */
	@Test(dataProvider = "jsonViewTypes")
	public void storeJsonView(String stringType, ViewType viewType) throws UnknownViewTypeException {
		String locName = "english name";
		ObjectNode viewJson = builder.buildViewNode(id, name, stringType);
		viewJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		JsonView view = new JsonViewImpl(service, viewJson);

		ObjectNode contentJson = new ObjectMapper().createObjectNode().put("key", "value");
		server.register(viewsUri + id + "/" + stringType, contentJson.toString());

		JsonView stored = new ViewStorage().storeView(view);

		assertEquals(stored.getId(), id);
		assertEquals(stored.getName(), name);
		assertEquals(stored.getType(), viewType);
		assertEquals(stored.getName(Locale.ENGLISH), locName);
		assertEquals(stored.loadContent(), contentJson);
		assertEquals(stored.loadContent(Collections.<Filter<?>> emptyList()), contentJson);
		assertEquals(stored.createLoader().loadContent(), contentJson);

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** store a JsonView while overriding the read methods */
	public void storeJsonViewOverwrite() throws UnknownViewTypeException {
		ObjectNode viewJson = builder.buildViewNode("other id", "other name", "chart");
		JsonView view = new JsonViewImpl(service, viewJson);

		final ObjectNode contentJson = new ObjectMapper().createObjectNode().put("key", "value");

		JsonView stored = new ViewStorage() {
			protected String readId(View view) {
				return id;
			};

			protected String readName(View view) {
				return name;
			};

			protected ViewType readType(View view) {
				return ViewType.KPI;
			};

			protected JsonNode readJsonContent(JsonView view) {
				return contentJson;
			};
		}.storeView(view);

		assertEquals(stored.getId(), id);
		assertEquals(stored.getName(), name);
		assertEquals(stored.getType(), ViewType.KPI);
		assertEquals(stored.loadContent(), contentJson);
		assertEquals(stored.loadContent(Collections.<Filter<?>> emptyList()), contentJson);
		assertEquals(stored.createLoader().loadContent(), contentJson);

		server.assertRequestUris();
	}

	/** serialize/deserialize a stored JsonView */
	@Test(dataProvider = "jsonViewTypes")
	public void serializeJsonView(String stringType, ViewType viewType) throws UnknownViewTypeException, IOException,
		ClassNotFoundException {
		String locName = "english name";
		ObjectNode viewJson = builder.buildViewNode(id, name, stringType);
		viewJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		JsonView view = new JsonViewImpl(service, viewJson);

		ObjectNode contentJson = new ObjectMapper().createObjectNode().put("key", "value");
		server.register(viewsUri + id + "/" + stringType, contentJson.toString());

		JsonView stored = new ViewStorage().storeView(view);
		JsonView newStored = (JsonView) serializeDeserialize(stored);

		assertEquals(newStored.getId(), id);
		assertEquals(newStored.getName(), name);
		assertEquals(newStored.getType(), viewType);
		assertEquals(newStored.getName(Locale.ENGLISH), locName);
		assertEquals(newStored.loadContent(), contentJson);
		assertEquals(newStored.loadContent(Collections.<Filter<?>> emptyList()), contentJson);
		assertEquals(newStored.createLoader().loadContent(), contentJson);

		server.assertRequestUris(viewsUri + id + "/" + stringType);
	}

	/** store a table */
	public void storeTable() {
		String[][] left = new String[][] { { "<th>1</th>" }, { "<th>2</th>" } };
		String[][] top = new String[][] { { "<th>A</th>", "<th>B</th>" } };
		String[][] data = new String[][] { { "<td>A1</td>", "<td>B1</td>" }, { "<td>A2</td>", "<td>B2</td>" } };

		Table table = new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 1, 1));

		final JsonNode leftResult = new ObjectMapper().createObjectNode().put("content",
			"<tr>" + left[0][0] + "</tr><tr>" + left[1][0] + "</tr>");
		final JsonNode topResult = new ObjectMapper().createObjectNode().put("content", "<tr>" + top[0][0] + top[0][1] + "</tr>");
		final JsonNode dataResult = new ObjectMapper().createObjectNode().put("content",
			"<tr>" + data[0][0] + data[0][1] + "</tr>" + "<tr>" + data[1][0] + data[1][1] + "</tr>");

		server.register(viewsUri + id + "/table/leftHeader", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				Map<String, String> expectedParams = new HashMap<String, String>();
				expectedParams.put("rowsFrom", "0");
				expectedParams.put("rowsTo", "1");
				assertEquals(holder.getUrlParameters(), expectedParams);
				holder.response.setEntity(new StringEntity(leftResult.toString()));
			}
		});
		server.register(viewsUri + id + "/table/topHeader", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				Map<String, String> expectedParams = new HashMap<String, String>();
				expectedParams.put("columnsFrom", "0");
				expectedParams.put("columnsTo", "1");
				assertEquals(holder.getUrlParameters(), expectedParams);
				holder.response.setEntity(new StringEntity(topResult.toString()));
			}
		});
		server.register(viewsUri + id + "/table/data", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				Map<String, String> expectedParams = new HashMap<String, String>();
				expectedParams.put("rowsFrom", "0");
				expectedParams.put("rowsTo", "1");
				expectedParams.put("columnsFrom", "0");
				expectedParams.put("columnsTo", "1");
				assertEquals(holder.getUrlParameters(), expectedParams);
				holder.response.setEntity(new StringEntity(dataResult.toString()));
			}
		});

		Table stored = new ViewStorage().storeTable(table);

		assertTrue(stored.hasLeftHeader());
		assertTrue(stored.hasTopHeader());

		assertEquals(stored.getRowCount(), 2);
		assertEquals(stored.getColumnCount(), 2);

		assertEquals(stored.loadLeftHeader(0, 1).get("content").asText(), "<tr>" + left[0][0] + "</tr>");
		assertEquals(stored.loadLeftHeader(0, 2).get("content").asText(), "<tr>" + left[0][0] + "</tr><tr>" + left[1][0]
			+ "</tr>");
		assertEquals(stored.loadLeftHeader(1, 2).get("content").asText(), "<tr>" + left[1][0] + "</tr>");

		assertEquals(stored.loadTopHeader(0, 1).get("content").asText(), "<tr>" + top[0][0] + "</tr>");
		assertEquals(stored.loadTopHeader(0, 2).get("content").asText(), "<tr>" + top[0][0] + top[0][1] + "</tr>");
		assertEquals(stored.loadTopHeader(1, 2).get("content").asText(), "<tr>" + top[0][1] + "</tr>");

		assertEquals(stored.loadData(0, 1, 0, 1).get("content").asText(), "<tr>" + data[0][0] + "</tr>");
		assertEquals(stored.loadData(0, 2, 0, 2).get("content").asText(), "<tr>" + data[0][0] + data[0][1] + "</tr><tr>"
			+ data[1][0] + data[1][1] + "</tr>");
		assertEquals(stored.loadData(1, 2, 1, 2).get("content").asText(), "<tr>" + data[1][1] + "</tr>");

		server.assertRequestUris(viewsUri + id + "/table/leftHeader", viewsUri + id + "/table/topHeader", viewsUri + id
			+ "/table/data");
	}

	/** store a table without left header */
	public void storeTableNoLeftHeader() {
		Table table = new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 0, 1));

		String top = "<tr><th>A</th></tr>";
		String data = "<tr><td>A1</td></tr>";
		JsonNode topResult = new ObjectMapper().createObjectNode().put("content", top);
		JsonNode dataResult = new ObjectMapper().createObjectNode().put("content", data);

		server.register(viewsUri + id + "/table/topHeader", topResult.toString());
		server.register(viewsUri + id + "/table/data", dataResult.toString());

		Table stored = new ViewStorage().storeTable(table);

		assertFalse(stored.hasLeftHeader());
		assertTrue(stored.hasTopHeader());

		assertEquals(stored.loadTopHeader(0, 1).get("content").asText(), top);
		assertEquals(stored.loadData(0, 1, 0, 1).get("content").asText(), data);
	}

	/** store a table without top header */
	public void storeTableNoTopHeader() {
		Table table = new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 1, 0));

		String left = "<tr><th>1</th></tr>";
		String data = "<tr><td>A1</td></tr>";
		JsonNode leftResult = new ObjectMapper().createObjectNode().put("content", left);
		JsonNode dataResult = new ObjectMapper().createObjectNode().put("content", data);

		server.register(viewsUri + id + "/table/leftHeader", leftResult.toString());
		server.register(viewsUri + id + "/table/data", dataResult.toString());

		Table stored = new ViewStorage().storeTable(table);

		assertTrue(stored.hasLeftHeader());
		assertFalse(stored.hasTopHeader());

		assertEquals(stored.loadLeftHeader(0, 1).get("content").asText(), left);
		assertEquals(stored.loadData(0, 1, 0, 1).get("content").asText(), data);
	}

	/** store a table with two columns in the left header */
	public void doubleLeftHeader() {
		Table table = new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 1, 0));

		String left = "<tr><th>1-1</th><th>1-2</th></tr>";
		String data = "<tr><td>A1</td></tr>";
		JsonNode leftResult = new ObjectMapper().createObjectNode().put("content", left);
		JsonNode dataResult = new ObjectMapper().createObjectNode().put("content", data);

		server.register(viewsUri + id + "/table/leftHeader", leftResult.toString());
		server.register(viewsUri + id + "/table/data", dataResult.toString());

		Table stored = new ViewStorage().storeTable(table);

		assertEquals(stored.loadLeftHeader(0, 1).get("content").asText(), left);
	}

	/** store a table with two rows in the top header */
	public void doubleTopHeader() {
		Table table = new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 0, 1));

		String top = "<tr><th>A-A</th></tr><tr><th>A-B</th></tr>";
		String data = "<tr><td>A1</td></tr>";
		JsonNode topResult = new ObjectMapper().createObjectNode().put("content", top);
		JsonNode dataResult = new ObjectMapper().createObjectNode().put("content", data);

		server.register(viewsUri + id + "/table/topHeader", topResult.toString());
		server.register(viewsUri + id + "/table/data", dataResult.toString());

		Table stored = new ViewStorage().storeTable(table);

		assertEquals(stored.loadTopHeader(0, 1).get("content").asText(), top);
	}

	/** store a TableView */
	public void storeTableView() throws UnknownViewTypeException {
		String locName = "english name";
		ObjectNode viewJson = builder.buildViewNode(id, name, "table");
		viewJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		TableView view = new TableViewImpl(service, viewJson);

		ObjectNode tableJson = builder.buildTableNode(1, 1);
		String data = "<tr><td>A1</td></tr>";
		JsonNode dataJson = new ObjectMapper().createObjectNode().put("content", data);

		server.register(viewsUri + id + "/table/bounds", tableJson.toString());
		server.register(viewsUri + id + "/table/data", dataJson.toString());

		TableView stored = new ViewStorage().storeView(view);

		assertEquals(stored.getId(), id);
		assertEquals(stored.getName(), name);
		assertEquals(stored.getType(), ViewType.TABLE);
		assertEquals(stored.getName(Locale.ENGLISH), locName);
		assertSame(stored.loadContent(), stored.loadContent(Collections.<Filter<?>> emptyList()));
		assertSame(stored.createLoader().loadContent(), stored.loadContent());

		Table storedTable = stored.loadContent();
		assertEquals(storedTable.loadData(0, 1, 0, 1).get("content").asText(), data);

		server.assertRequestUris(viewsUri + id + "/table/bounds", viewsUri + id + "/table/data");
	}

	/** store a TableView while overriding each individual read method */
	public void storeTableViewOverwriteEach() throws UnknownViewTypeException {
		TableView view = new TableViewImpl(service, builder.buildViewNode(id, name, "table"));

		final String left = "<tr><th>1</th></tr>";
		final String top = "<tr><th>A</th></tr>";
		final String data = "<tr><td>A1</td></tr>";
		TableView stored = new ViewStorage() {
			protected Table readTableContent(TableView view) {
				return new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 1, 1));
			};

			protected String[][] readLeftHeaderContent(Table table) {
				return parseRowsColumns(left);
			};

			protected String[][] readTopHeaderContent(Table table) {
				return parseRowsColumns(top);
			};

			protected String[][] readDataContent(Table table) {
				return parseRowsColumns(data);
			};
		}.storeView(view);

		assertEquals(stored.loadContent().loadLeftHeader(0, 1).get("content").asText(), left);
		assertEquals(stored.loadContent().loadTopHeader(0, 1).get("content").asText(), top);
		assertEquals(stored.loadContent().loadData(0, 1, 0, 1).get("content").asText(), data);

		server.assertRequestUris();
	}

	/** store a TableView while overriding the readAllContent method */
	public void storeTableViewOverwriteOnce() throws UnknownViewTypeException {
		TableView view = new TableViewImpl(service, builder.buildViewNode(id, name, "table"));

		final String left = "<tr><th>1</th></tr>";
		final String top = "<tr><th>A</th></tr>";
		final String data = "<tr><td>A1</td></tr>";
		TableView stored = new ViewStorage() {
			protected Table readTableContent(TableView view) {
				return new TableViewImpl.TableImpl(service, id, builder.buildTableNode(1, 1, 1, 1));
			};

			protected TableData readAllContent(Table table) {
				return new TableData(parseRowsColumns(left), parseRowsColumns(top), parseRowsColumns(data));
			};
		}.storeView(view);

		assertEquals(stored.loadContent().loadLeftHeader(0, 1).get("content").asText(), left);
		assertEquals(stored.loadContent().loadTopHeader(0, 1).get("content").asText(), top);
		assertEquals(stored.loadContent().loadData(0, 1, 0, 1).get("content").asText(), data);

		server.assertRequestUris();
	}

	/** serialize/deserialize a stored TableView */
	public void serializeTableView() throws UnknownViewTypeException, IOException, ClassNotFoundException {
		String locName = "english name";
		ObjectNode viewJson = builder.buildViewNode(id, name, "table");
		viewJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		TableView view = new TableViewImpl(service, viewJson);

		ObjectNode tableJson = builder.buildTableNode(1, 1);
		String data = "<tr><td>A1</td></tr>";
		JsonNode dataJson = new ObjectMapper().createObjectNode().put("content", data);

		server.register(viewsUri + id + "/table/bounds", tableJson.toString());
		server.register(viewsUri + id + "/table/data", dataJson.toString());

		TableView stored = new ViewStorage().storeView(view);
		TableView newStored = (TableView) serializeDeserialize(stored);

		assertEquals(newStored.getId(), id);
		assertEquals(newStored.getName(), name);
		assertEquals(newStored.getType(), ViewType.TABLE);
		assertEquals(newStored.getName(Locale.ENGLISH), locName);
		assertSame(newStored.loadContent(), newStored.loadContent(Collections.<Filter<?>> emptyList()));
		assertSame(newStored.createLoader().loadContent(), newStored.loadContent());

		Table storedTable = newStored.loadContent();
		assertEquals(storedTable.loadData(0, 1, 0, 1).get("content").asText(), data);

		server.assertRequestUris(viewsUri + id + "/table/bounds", viewsUri + id + "/table/data");
	}
}
