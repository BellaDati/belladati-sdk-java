package com.belladati.sdk.dataset.impl;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.exception.impl.InvalidAttributeException;
import com.belladati.sdk.exception.impl.InvalidAttributeValueException;
import com.belladati.sdk.filter.FilterValue;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test
public class AttributesTest extends SDKTest {

	private final String reportsUri = "/api/reports";
	private final String valuesUri = "/api/dataSets/%s/attributes/%s/values";
	private final String imageUri = "/api/dataSets/%s/attributes/%s/%s/image";

	private final String reportId = "123";
	private final String dataSetId = "456";
	private final String id = "id";
	private final String name = "name";
	private final String description = "report description";
	private final String owner = "report owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";
	private final String code = "code";
	private final String type = "string";

	private final String value1 = "Simple";
	private final String value2 = "Some Spaces";
	private final String value3 = "Test Characters /.,:;!@#$%^&*()_\\' End";
	private final String value4 = "Chinese 把百度 And Latin ľščťžýáíéúäňô End";

	/** No data set field means no attributes. */
	public void reportWithoutDataSet() {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		node.remove("dataSet");
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = getService().loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** No drilldownAttributes field means no attributes. */
	public void reportWithoutDrilldownAttributes() {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).remove("drilldownAttributes");
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = getService().loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** drilldownAttributes field not an array means no attributes. */
	public void reportWithNonArrayAttributes() {
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).put("drilldownAttributes", "not an array");
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = getService().loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** Report attributes are loaded. */
	public void reportWithAttribute() {
		String attId = "attribute id";
		String attName = "attribute name";
		String attCode = "attribute code";
		ObjectNode node = builder.buildReportNode(reportId, name, description, owner, lastChange);
		((ObjectNode) node.get("dataSet")).putAll(builder.buildDataSetNode(dataSetId, name, description, owner, lastChange));
		((ArrayNode) node.get("dataSet").get("drilldownAttributes"))
			.add(builder.buildAttributeNode(attId, attName, attCode, type));
		server.register(reportsUri + "/" + reportId, node.toString());
		Report report = getService().loadReport(reportId);

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
		Report report = getService().loadReport(reportId);

		assertEquals(report.getAttributes(), Collections.emptyList());
	}

	/** Missing values attribute means no attribute values. */
	public void valuesWithoutValues() throws InvalidAttributeException {
		registerValues(new ObjectMapper().createObjectNode());

		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));
		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Null values attribute means no attribute values. */
	public void valuesNull() throws InvalidAttributeException {
		registerValues(new ObjectMapper().createObjectNode().put("values", (String) null));

		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));
		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Values attribute not an array means no attribute values. */
	public void valuesNotArray() throws InvalidAttributeException {
		registerValues(new ObjectMapper().createObjectNode().put("values", "not an array"));

		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));
		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Values attribute empty means no attribute values. */
	public void valuesEmpty() throws InvalidAttributeException {
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("values", new ObjectMapper().createArrayNode());
		registerValues(node);

		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));
		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** Valid values are loaded from values attribute. */
	public void valueValid() throws InvalidAttributeException {
		String label = "label";
		String value = "value";
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("values", new ObjectMapper().createArrayNode().add(builder.buildAttributeValueNode(label, value)));
		registerValues(node);

		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));
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
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("values", new ObjectMapper().createArrayNode().add(value));
		registerValues(node);

		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));

		assertEquals(attribute.getValues().load().get(), Collections.emptyList());
	}

	/** getValues() always returns the same object. */
	public void cacheSame() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));

		assertSame(attribute.getValues(), attribute.getValues());
	}

	/**
	 * getAttributeValues() from service returns the same object as getValues()
	 * from attribute.
	 */
	public void cacheFromServiceSame() throws InvalidAttributeException {
		Attribute attribute = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, name, code, type));

		assertSame(getService().getAttributeValues(dataSetId, code), attribute.getValues());
	}

	/** equals and hashcode work as expected for attributes */
	public void attributeEquality() throws InvalidAttributeException {
		Attribute att1 = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, "1", "1", type));
		Attribute att2 = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode(id, "2", "2", type));
		Attribute att3 = new AttributeImpl(getService(), dataSetId, builder.buildAttributeNode("other", "3", "3", type));
		Attribute att4 = new AttributeImpl(getService(), "4", builder.buildAttributeNode(id, name, code, type));

		assertEquals(att1, att2);
		assertEquals(att1.hashCode(), att2.hashCode());

		assertNotEquals(att1, att3);
		assertEquals(att1, att4);
		assertFalse(att1.equals(new Object()));
	}

	/** equals and hashcode work as expected for attribute values */
	public void attributeValueEquality() throws InvalidAttributeValueException {
		String value = "value";
		AttributeValue v1 = new AttributeValueImpl(builder.buildAttributeValueNode("1", value));
		AttributeValue v2 = new AttributeValueImpl(builder.buildAttributeValueNode("2", value));
		AttributeValue v3 = new AttributeValueImpl(builder.buildAttributeValueNode("3", "3"));
		AttributeValue v4 = new FilterValue("4", value);

		assertEquals(v1, v2);
		assertEquals(v1, v4);
		assertEquals(v4, v1);
		assertEquals(v1.hashCode(), v2.hashCode());
		assertEquals(v1.hashCode(), v4.hashCode());

		assertFalse(v1.equals(new Object()));
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
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				holder.response.setEntity(new StringEntity(node.toString()));
			}
		});
	}

	@DataProvider(name = "postAttributeValueImage_provider")
	private Object[][] postAttributeValueImage_provider() {
		return new Object[][] { { value1 }, { value2 }, { value3 }, { value4 } };
	}

	@Test(dataProvider = "postAttributeValueImage_provider")
	public void postAttributeValueImage(String value) throws URISyntaxException, UnsupportedEncodingException {
		String url = String.format(imageUri, dataSetId, code, URLEncoder.encode(value,"UTF-8"));

		final boolean[] executed = new boolean[1];
		server.register(url, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity(""));
				executed[0] = true;
			}
		});

		getService().postAttributeValueImage(dataSetId, code, value, getTestImageFile());

		assertTrue(executed[0]);
	}

}
