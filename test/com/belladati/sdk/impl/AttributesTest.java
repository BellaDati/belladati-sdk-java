package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.impl.AttributeImpl.InvalidAttributeException;
import com.belladati.sdk.impl.AttributeValueImpl.InvalidAttributeValueException;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.test.TestRequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class AttributesTest extends SDKTest {

	private final String reportsUri = "/api/reports";
	private final String valuesUri = "/api/dataSets/%s/attributes/%s/values";

	private final String reportId = "123";
	private final String dataSetId = "456";
	private final String id = "id";
	private final String name = "name";
	private final String description = "report description";
	private final String owner = "report owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";
	private final String code = "code";
	private final String type = "string";

	/** No data set field means no attributes. */
	public void reportWithoutDataSet() {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		node.remove("dataSet");
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = service.loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** No drilldownAttributes field means no attributes. */
	public void reportWithoutDrilldownAttributes() {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).remove("drilldownAttributes");
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = service.loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** drilldownAttributes field not an array means no attributes. */
	public void reportWithNonArrayAttributes() {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).put("drilldownAttributes", "not an array");
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = service.loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** Report attributes are loaded. */
	public void reportWithAttribute() {
		String attId = "attribute id";
		String attName = "attribute name";
		String attCode = "attribute code";
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).putAll(builder.buildDataSetNode(dataSetId, name, description, owner, lastChange));
		((ArrayNode) node.get("dataSet").get("drilldownAttributes")).add(builder
			.buildAttributeNode(attId, attName, attCode, type));
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = service.loadReport(reportId);

		assertEquals(report.getAttributes().size(), 1);
		assertEquals(report.getAttributes().get(0).getName(), attName);
		assertEquals(report.getAttributes().get(0).getCode(), attCode);
	}

	/** Invalid attributes are ignored. */
	@Test(dataProvider = "invalidAttributes")
	public void reportWithInvalidAttribute(JsonNode attribute) {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).putAll(builder.buildDataSetNode(dataSetId, name, description, owner, lastChange));
		((ArrayNode) node.get("dataSet").get("drilldownAttributes")).add(attribute);
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = service.loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** Missing values attribute means no attribute values. */
	public void valuesWithoutValues() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));
		registerValues(new ObjectMapper().createObjectNode());

		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Null values attribute means no attribute values. */
	public void valuesNull() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));
		registerValues(new ObjectMapper().createObjectNode().put("values", (String) null));

		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Values attribute not an array means no attribute values. */
	public void valuesNotArray() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));
		registerValues(new ObjectMapper().createObjectNode().put("values", "not an array"));

		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Values attribute empty means no attribute values. */
	public void valuesEmpty() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("values", new ObjectMapper().createArrayNode());
		registerValues(node);

		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Valid values are loaded from values attribute. */
	public void valueValid() throws InvalidAttributeException {
		String label = "label";
		String value = "value";
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("values", new ObjectMapper().createArrayNode().add(builder.buildAttributeValueNode(label, value)));
		registerValues(node);

		List<AttributeValue> values = attribute.getValues().load().get();
		assertEquals(values.size(), 1);

		assertEquals(values.get(0).getLabel(), label);
		assertEquals(values.get(0).getValue(), value);

		assertEquals(attribute.toString(), name);
		assertEquals(values.get(0).toString(), label);
	}

	/** Invalid values in values attribute are ignored. */
	@Test(dataProvider = "invalidValues")
	public void valueInvalid(JsonNode value) throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("values", new ObjectMapper().createArrayNode().add(value));
		registerValues(node);

		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** getValues() always returns the same object. */
	public void cacheSame() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));

		assertSame(attribute.getValues(), attribute.getValues());
	}

	/**
	 * getAttributeValues() from service returns the same object as getValues()
	 * from attribute.
	 */
	public void cacheFromServiceSame() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, name, code, type));

		assertSame(service.getAttributeValues(dataSetId, code), attribute.getValues());
	}

	/** equals and hashcode work as expected for attributes */
	public void attributeEquality() throws InvalidAttributeException {
		Attribute att1 = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, "1", "1", type));
		Attribute att2 = new AttributeImpl(service, dataSetId, builder.buildAttributeNode(id, "2", "2", type));
		Attribute att3 = new AttributeImpl(service, dataSetId, builder.buildAttributeNode("other", "3", "3", type));
		Attribute att4 = new AttributeImpl(service, "4", builder.buildAttributeNode(id, name, code, type));

		assertEquals(att1, att2);
		assertEquals(att1.hashCode(), att2.hashCode());

		assertNotEquals(att1, att3);
		assertEquals(att1, att4);
	}

	/** equals and hashcode work as expected for attribute values */
	public void attributeValueEquality() throws InvalidAttributeValueException {
		String value = "value";
		AttributeValue v1 = new AttributeValueImpl(builder.buildAttributeValueNode("1", value));
		AttributeValue v2 = new AttributeValueImpl(builder.buildAttributeValueNode("2", value));
		AttributeValue v3 = new AttributeValueImpl(builder.buildAttributeValueNode("3", "3"));

		assertEquals(v1, v2);
		assertEquals(v1.hashCode(), v2.hashCode());

		assertNotEquals(v1, v3);
	}

	/** Provides attribute JSON that's invalid in some way. */
	@DataProvider(name = "invalidAttributes")
	protected Object[][] invalidAttributeProvider() {
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

	/** Provides attribute value JSON that's invalid in some way. */
	@DataProvider(name = "invalidValues")
	protected Object[][] invalidValueProvider() {
		// invalid is if label or value is null or doesn't exist
		return new Object[][] { { builder.buildAttributeValueNode(null, "value") },
			{ builder.buildAttributeValueNode("label", null) },
			{ builder.buildAttributeValueNode("label", "value").retain("label") },
			{ builder.buildAttributeValueNode("label", "value").retain("value") } };
	}

	/**
	 * Tells the server to respond to an attribute value query with the given
	 * JSON.
	 */
	private void registerValues(final JsonNode node) {
		server.register(String.format(valuesUri, dataSetId, code), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new StringEntity(node.toString()));
			}
		});
	}
}
