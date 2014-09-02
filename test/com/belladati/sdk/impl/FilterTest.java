package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.Filter.MultiValueFilter;
import com.belladati.sdk.filter.Filter.NoValueFilter;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.belladati.sdk.impl.AttributeImpl.InvalidAttributeException;
import com.belladati.sdk.impl.AttributeValueImpl.InvalidAttributeValueException;
import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.view.TableView.Table;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests filters and using filters in views.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class FilterTest extends SDKTest {

	private final String name = "attribute name";
	private final String code = "attribute code";
	private final String label = "attribute value label";
	private final String valueString = "attribute value";

	private final String reportId = "reportId";
	private final String viewId = "viewId";
	private final String viewName = "view name";

	private final String viewsUri = String.format("/api/reports/views/%s/chart", viewId);

	private final ObjectMapper mapper = new ObjectMapper();
	private Attribute attribute;
	private AttributeValue value;

	/** Null filter is correctly turned into JSON. */
	public void filterNullToJson() throws InvalidAttributeException {
		NoValueFilter filter = FilterOperation.NULL.createFilter(attribute);
		JsonNode filterJson = filter.toJson();

		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode operation = mapper.createObjectNode().put("op", "NULL");
		expectedJson.put(code, operation);

		assertEquals(filterJson, expectedJson);

		assertEquals(filter.toString(), expectedJson.toString());
	}

	/** Not null filter is correctly turned into JSON. */
	public void filterNotNullToJson() {
		JsonNode filterJson = FilterOperation.NOT_NULL.createFilter(attribute).toJson();

		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode operation = mapper.createObjectNode().put("op", "NOT_NULL");
		expectedJson.put(code, operation);

		assertEquals(filterJson, expectedJson);
	}

	/** In filter with empty value list is correctly turned into JSON. */
	public void filterInEmptyValuesToJson() {
		JsonNode filterJson = FilterOperation.IN.createFilter(attribute).toJson();

		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode operation = mapper.createObjectNode();
		operation.put("op", "IN").put("values", mapper.createArrayNode());
		expectedJson.put(code, operation);

		assertEquals(filterJson, expectedJson);
	}

	/** Not in filter with empty value list is correctly turned into JSON. */
	public void filterNotInEmptyValuesToJson() {
		JsonNode filterJson = FilterOperation.NOT_IN.createFilter(attribute).toJson();

		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode operation = mapper.createObjectNode();
		operation.put("op", "NOT_IN").put("values", mapper.createArrayNode());
		expectedJson.put(code, operation);

		assertEquals(filterJson, expectedJson);
	}

	/** Filters with values are correctly turned into JSON. */
	public void filterValuesToJson() throws InvalidAttributeValueException {
		MultiValueFilter filter = FilterOperation.IN.createFilter(attribute).addValue(value);
		JsonNode filterJson = filter.toJson();

		assertEquals(filterJson, buildInFilterNode());
		assertEquals(filter.toString(), filterJson.toString());
	}

	/** Filters with temporary values are correctly turned into JSON. */
	public void filterTempValueToJson() throws InvalidAttributeValueException {
		MultiValueFilter filter = FilterOperation.IN.createFilter(attribute).addValue(new FilterValue(valueString));

		assertEquals(filter.getValues().iterator().next().getLabel(), valueString);
		assertEquals(filter.getValues().iterator().next().getValue(), valueString);
		assertEquals(filter.toJson(), buildInFilterNode());
	}

	/** Filters with temporary values and label are correctly turned into JSON. */
	public void filterTempValueLabelToJson() throws InvalidAttributeValueException {
		MultiValueFilter filter = FilterOperation.IN.createFilter(attribute).addValue(new FilterValue(label, valueString));

		assertEquals(filter.getValues().iterator().next().getLabel(), label);
		assertEquals(filter.getValues().iterator().next().getValue(), valueString);
		assertEquals(filter.toJson(), buildInFilterNode());
	}

	/**
	 * Filters can be created using only an attribute code instead of the full
	 * attribute. The filter's attribute acts as a normal attribute but uses the
	 * code for name.
	 */
	public void filterCodeOnly() {
		MultiValueFilter filter = FilterOperation.IN.createFilter(service, reportId, code);

		assertEquals(filter.getAttribute().getCode(), code);
		assertSame(filter.getAttribute().getValues(), service.getAttributeValues(reportId, code));
		assertEquals(filter.getAttribute().getName(), code);
	}

	/** Query parameters without filter are correct. */
	public void queryStringNoFilter() throws UnknownViewTypeException {
		View view = new JsonViewImpl(service, builder.buildViewNode(viewId, viewName, "chart"));

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		view.loadContent();
		service.createViewLoader(viewId, ViewType.CHART).loadContent();

		server.assertRequestUris(viewsUri, viewsUri);
	}

	/** Query parameters with a single filter are correct. */
	public void queryStringSingleFilter() throws UnknownViewTypeException {
		View view = new JsonViewImpl(service, builder.buildViewNode(viewId, viewName, "chart"));

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", buildInFilterNode());
				assertEquals(mapper.readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		view.loadContent(FilterOperation.IN.createFilter(attribute).addValue(value));
		service.createViewLoader(viewId, ViewType.CHART).addFilters(FilterOperation.IN.createFilter(attribute).addValue(value))
			.loadContent();

		server.assertRequestUris(viewsUri, viewsUri);
	}

	/** Query parameters with multiple filters are correct. */
	public void queryStringMultipleFilters() throws UnknownViewTypeException, InvalidAttributeException,
		InvalidAttributeValueException {
		View view = new JsonViewImpl(service, builder.buildViewNode(viewId, viewName, "chart"));

		final String code2 = "another code";
		final String valueString2 = "another value";

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode expected = mapper.createObjectNode();
				expected.setAll(buildInFilterNode());
				expected.setAll(buildInFilterNode(code2, valueString2));
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", expected);
				assertEquals(new ObjectMapper().readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		Attribute attribute2 = new AttributeImpl(service, "", builder.buildAttributeNode("another ID", "another name",
			code2, "string"));
		AttributeValue value2 = new AttributeValueImpl(builder.buildAttributeValueNode("another label", valueString2));
		view.loadContent(FilterOperation.IN.createFilter(attribute).addValue(value), FilterOperation.IN.createFilter(attribute2)
			.addValue(value2));
		service
			.createViewLoader(viewId, ViewType.CHART)
			.addFilters(FilterOperation.IN.createFilter(attribute).addValue(value),
				FilterOperation.IN.createFilter(attribute2).addValue(value2)).loadContent();

		server.assertRequestUris(viewsUri, viewsUri);
	}

	/** Query parameters with multiple filters are correct. */
	public void queryStringMultipleConsecutiveFilters() throws UnknownViewTypeException, InvalidAttributeException,
		InvalidAttributeValueException {
		View view = new JsonViewImpl(service, builder.buildViewNode(viewId, viewName, "chart"));

		final String code2 = "another code";
		final String valueString2 = "another value";

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode expected = mapper.createObjectNode();
				expected.setAll(buildInFilterNode());
				expected.setAll(buildInFilterNode(code2, valueString2));
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", expected);
				assertEquals(new ObjectMapper().readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		Attribute attribute2 = new AttributeImpl(service, "", builder.buildAttributeNode("another id", "another name",
			code2, "string"));
		AttributeValue value2 = new AttributeValueImpl(builder.buildAttributeValueNode("another label", valueString2));
		view.loadContent(FilterOperation.IN.createFilter(attribute).addValue(value), FilterOperation.IN.createFilter(attribute2)
			.addValue(value2));
		service.createViewLoader(viewId, ViewType.CHART).addFilters(FilterOperation.IN.createFilter(attribute).addValue(value))
			.addFilters(FilterOperation.IN.createFilter(attribute2).addValue(value2)).loadContent();

		server.assertRequestUris(viewsUri, viewsUri);
	}

	/** Table bounds and contents are loaded using a filter. */
	public void tableWithFilter() throws UnknownViewTypeException {
		View view = new TableViewImpl(service, builder.buildViewNode(viewId, viewName, "table"));

		String boundsUri = "/api/reports/views/" + viewId + "/table/bounds";
		String leftUri = "/api/reports/views/" + viewId + "/table/leftHeader";
		String topUri = "/api/reports/views/" + viewId + "/table/topHeader";
		String dataUri = "/api/reports/views/" + viewId + "/table/data";
		server.register(boundsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", buildInFilterNode());
				assertEquals(mapper.readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity(builder.buildTableNode(10, 10).toString()));
			}
		});
		server.register(leftUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", buildInFilterNode());
				assertEquals(mapper.readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});
		server.register(topUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", buildInFilterNode());
				assertEquals(mapper.readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});
		server.register(dataUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String filterString = holder.getUrlParameters().get("filter");
				assertNotNull(filterString);
				ObjectNode drilldownNode = mapper.createObjectNode();
				drilldownNode.put("drilldown", buildInFilterNode());
				assertEquals(mapper.readTree(filterString), drilldownNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		MultiValueFilter filter = FilterOperation.IN.createFilter(attribute).addValue(value);
		Table viewTable = (Table) view.loadContent(filter);
		Table serviceTable = (Table) service.loadViewContent(viewId, ViewType.TABLE, filter);

		viewTable.loadLeftHeader(1, 1);
		serviceTable.loadLeftHeader(1, 1);

		viewTable.loadTopHeader(1, 1);
		serviceTable.loadTopHeader(1, 1);

		viewTable.loadData(1, 1, 1, 1);
		serviceTable.loadData(1, 1, 1, 1);

		server.assertRequestUris(boundsUri, boundsUri, leftUri, leftUri, topUri, topUri, dataUri, dataUri);
	}

	/** equals/hashcode for no value filters */
	public void noValuesEquality() throws InvalidAttributeException {
		Filter<?> f1 = FilterOperation.NULL.createFilter(attribute);
		Filter<?> f2 = FilterOperation.NULL.createFilter(attribute);
		Filter<?> f3 = FilterOperation.NOT_NULL.createFilter(attribute);
		Filter<?> f4 = FilterOperation.NULL.createFilter(new AttributeImpl(service, reportId, builder.buildAttributeNode(
			"otherId", name, code, "string")));

		assertEquals(f1, f2);
		assertEquals(f1.hashCode(), f2.hashCode());

		assertNotEquals(f1, f3);
		assertNotEquals(f1, f4);
	}

	/** equals/hashcode for value filters */
	public void valuesEquality() throws InvalidAttributeException, InvalidAttributeValueException {
		Filter<?> f1 = FilterOperation.IN.createFilter(attribute).addValue(value);
		Filter<?> f2 = FilterOperation.IN.createFilter(attribute).addValue(value);
		Filter<?> f3 = FilterOperation.NOT_IN.createFilter(attribute).addValue(value);
		Filter<?> f4 = FilterOperation.IN.createFilter(
			new AttributeImpl(service, reportId, builder.buildAttributeNode("other id", name, code, "string"))).addValue(
			value);
		Filter<?> f5 = FilterOperation.IN.createFilter(attribute).addValue(
			new AttributeValueImpl(builder.buildAttributeValueNode(label, "other value")));

		assertEquals(f1, f2);
		assertEquals(f1.hashCode(), f2.hashCode());

		assertNotEquals(f1, f3);
		assertNotEquals(f1, f4);
		assertNotEquals(f1, f5);
	}

	private ObjectNode buildInFilterNode() {
		return buildInFilterNode(code, valueString);
	}

	private ObjectNode buildInFilterNode(String code, String value) {
		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode operation = mapper.createObjectNode();
		ArrayNode values = mapper.createArrayNode();
		values.add(value);
		operation.put("op", "IN").put("values", values);
		expectedJson.put(code, operation);
		return expectedJson;
	}

	@BeforeMethod
	protected void setupAttribute() throws InvalidAttributeException, InvalidAttributeValueException {
		attribute = new AttributeImpl(service, reportId, builder.buildAttributeNode("id", name, code, "string"));
		value = new AttributeValueImpl(builder.buildAttributeValueNode(label, valueString));
	}
}
