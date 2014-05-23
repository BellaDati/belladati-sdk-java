package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.AttributeType;
import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.Indicator;
import com.belladati.sdk.dataset.IndicatorType;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.belladati.sdk.exception.dataset.data.NoColumnsException;
import com.belladati.sdk.report.AttributeValue;
import com.belladati.sdk.test.TestRequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class DataSetStructureTest extends SDKTest {

	private final String dataSetsUri = "/api/dataSets";

	private final String dataSetId = "123";
	private final String name = "name";
	private final String description = "data set description";
	private final String owner = "data set owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";
	private final String id = "id";
	private final String code = "code";
	private final String formula = "some formula";

	/** No attributes field means no attributes. */
	public void attributesMissing() {
		server.register(dataSetsUri + "/" + dataSetId, builder.buildDataSetNode(dataSetId, name, description, owner, lastChange)
			.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getAttributes(), Collections.emptyList());
	}

	/** Empty attributes array means no attributes. */
	public void attributesEmpty() {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("attributes", new ObjectMapper().createArrayNode());
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getAttributes(), Collections.emptyList());
	}

	/** Invalid attributes are ignored. */
	@Test(dataProvider = "invalidAttributes")
	public void attributeInvalid(JsonNode attribute) {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("attributes", new ObjectMapper().createArrayNode().add(attribute));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getAttributes(), Collections.emptyList());
	}

	/** Valid attribute types are parsed correctly. */
	@Test(dataProvider = "attributeTypes")
	public void attributeTypes(String jsonType, AttributeType type) {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("attributes", new ObjectMapper().createArrayNode().add(builder.buildAttributeNode(id, name, code, jsonType)));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getAttributes().size(), 1);

		Attribute attribute = dataSet.getAttributes().get(0);
		assertEquals(attribute.getId(), id);
		assertEquals(attribute.getName(), name);
		assertEquals(attribute.getCode(), code);
		assertEquals(attribute.getType(), type);
	}

	/** data set attributes can load values just like report attributes */
	@Test(expectedExceptions = BellaDatiRuntimeException.class)
	public void loadAttributeValues() {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("attributes", new ObjectMapper().createArrayNode().add(builder.buildAttributeNode(id, name, code, "string")));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);
		Attribute attribute = dataSet.getAttributes().get(0);

		String label = "label";
		String value = "value";
		final ObjectNode valueNode = new ObjectMapper().createObjectNode();
		valueNode.put("values", new ObjectMapper().createArrayNode().add(builder.buildAttributeValueNode(label, value)));

		server.register("VALUES_URL", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				if (code.equals(holder.getUrlParameters().get("code"))) {
					holder.response.setEntity(new StringEntity(valueNode.toString()));
				}
			}
		});

		List<AttributeValue> values = attribute.getValues().load().get();
		assertEquals(values.size(), 1);

		assertEquals(values.get(0).getLabel(), label);
		assertEquals(values.get(0).getValue(), value);

		assertEquals(attribute.toString(), name);
		assertEquals(values.get(0).toString(), label);
	}

	/** No indicators field means no indicators. */
	public void indicatorsMissing() {
		server.register(dataSetsUri + "/" + dataSetId, builder.buildDataSetNode(dataSetId, name, description, owner, lastChange)
			.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getIndicators(), Collections.emptyList());
	}

	/** Empty indicators array means no indicators. */
	public void indicatorsEmpty() {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("indicators", new ObjectMapper().createArrayNode());
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getIndicators(), Collections.emptyList());
	}

	/** Invalid indicators are ignored. */
	@Test(dataProvider = "invalidIndicators")
	public void indicatorInvalid(JsonNode indicator) {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("indicators", new ObjectMapper().createArrayNode().add(indicator));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getIndicators(), Collections.emptyList());
	}

	/** Data indicators are parsed correctly. */
	public void dataIndicator() {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("indicators",
			new ObjectMapper().createArrayNode().add(builder.buildIndicatorNode(id, name, code, formula, "data_indicator")));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getIndicators().size(), 1);

		Indicator indicator = dataSet.getIndicators().get(0);
		assertEquals(indicator.getId(), id);
		assertEquals(indicator.getName(), name);
		assertEquals(indicator.getCode(), code);
		assertEquals(indicator.getFormula(), null);
		assertEquals(indicator.getType(), IndicatorType.DATA);
	}

	/** Formula indicators are parsed correctly. */
	public void formulaIndicator() {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("indicators",
			new ObjectMapper().createArrayNode().add(builder.buildIndicatorNode(id, name, code, formula, "formula_indicator")));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getIndicators().size(), 1);

		Indicator indicator = dataSet.getIndicators().get(0);
		assertEquals(indicator.getId(), id);
		assertEquals(indicator.getName(), name);
		assertEquals(indicator.getCode(), null);
		assertEquals(indicator.getFormula(), formula);
		assertEquals(indicator.getType(), IndicatorType.FORMULA);
	}

	/** Indicator groups are parsed correctly. */
	public void groupIndicator() {
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("indicators",
			new ObjectMapper().createArrayNode().add(builder.buildIndicatorNode(id, name, code, formula, "indicator_group")));
		server.register(dataSetsUri + "/" + dataSetId, node.toString());
		DataSet dataSet = service.loadDataSet(dataSetId);

		assertEquals(dataSet.getIndicators().size(), 1);

		Indicator indicator = dataSet.getIndicators().get(0);
		assertEquals(indicator.getId(), id);
		assertEquals(indicator.getName(), name);
		assertEquals(indicator.getCode(), null);
		assertEquals(indicator.getFormula(), null);
		assertEquals(indicator.getType(), IndicatorType.GROUP);
	}

	/** data set without columns can't create import table */
	@Test(expectedExceptions = NoColumnsException.class)
	public void emptyDataSetTable() {
		DataSet dataSet = new DataSetImpl(service, builder.buildDataSetNode(id, name, description, owner, lastChange));
		dataSet.createDataTable();
	}

	/** data set can create import table */
	public void dataSetTable() {
		String attributeCode = "A";
		String indicatorCode = "I";
		ObjectNode node = builder.buildDataSetNode(dataSetId, name, description, owner, lastChange);
		node.put("attributes",
			new ObjectMapper().createArrayNode().add(builder.buildAttributeNode(id, name, attributeCode, "String")));
		node.put(
			"indicators",
			new ObjectMapper().createArrayNode().add(
				builder.buildIndicatorNode(id, name, indicatorCode, formula, "data_indicator")));
		DataTable table = new DataSetImpl(service, node).createDataTable();
		assertEquals(table.getColumns(), Arrays.asList(attributeCode, indicatorCode));
	}

	/** Provides attribute JSON that's invalid in some way. */
	@DataProvider(name = "invalidAttributes")
	protected Object[][] invalidAttributeProvider() {
		String id = "id";
		String name = "name";
		String code = "code";
		String type = "String";
		// invalid is if code or name is null or doesn't exist
		return new Object[][] { { builder.buildAttributeNode(null, name, code, type) },
			{ builder.buildAttributeNode(id, null, code, type) }, { builder.buildAttributeNode(id, name, null, type) },
			{ builder.buildAttributeNode(id, name, code, null) },
			{ builder.buildAttributeNode(id, name, code, type).retain("name", "code", "type") },
			{ builder.buildAttributeNode(id, name, code, type).retain("id", "code", "type") },
			{ builder.buildAttributeNode(id, name, code, type).retain("id", "name", "type") },
			{ builder.buildAttributeNode(id, name, code, type).retain("id", "name", "code") },
			{ builder.buildAttributeNode(id, name, code, "other_type") } };
	}

	/** Provides attribute types and corresponding enum items. */
	@DataProvider(name = "attributeTypes")
	protected Object[][] attributeTypeProvider() {
		return new Object[][] { { "String", AttributeType.TEXT }, { "Date", AttributeType.DATE }, { "Time", AttributeType.TIME },
			{ "Point", AttributeType.GEO_POINT }, { "sTRING", AttributeType.TEXT } };
	}

	/** Provides indicator JSON that's invalid in some way. */
	@DataProvider(name = "invalidIndicators")
	protected Object[][] invalidIndicatorProvider() {
		String id = "id";
		String name = "name";
		String code = "code";
		String formula = "formula";
		String type = "indicator_group";
		// invalid is if code or name is null or doesn't exist
		return new Object[][] { { builder.buildIndicatorNode(null, name, code, formula, type) },
			{ builder.buildIndicatorNode(id, null, code, formula, type) },
			{ builder.buildIndicatorNode(id, name, null, formula, "data_indicator") },
			{ builder.buildIndicatorNode(id, name, code, null, "formula_indicator") },
			{ builder.buildIndicatorNode(id, name, code, formula, null) },
			{ builder.buildIndicatorNode(id, name, code, formula, type).retain("name", "type") },
			{ builder.buildIndicatorNode(id, name, code, formula, type).retain("id", "type") },
			{ builder.buildIndicatorNode(id, name, code, formula, type).retain("id", "name") },
			{ builder.buildIndicatorNode(id, name, code, formula, "data_indicator").retain("id", "name", "type") },
			{ builder.buildIndicatorNode(id, name, code, formula, "formula_indicator").retain("id", "name", "type") },
			{ builder.buildIndicatorNode(id, name, code, formula, "other_type") } };
	}
}
