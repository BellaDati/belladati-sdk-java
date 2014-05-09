package com.belladati.sdk.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds JSON objects as returned by the BellaDati server.
 * 
 * @author Chris Hennigfeld
 */
public class JsonBuilder {

	/** Builds a JSON node representing a dashboard or dashboard info item. */
	public ObjectNode buildDashboardNode(String id, String name, String lastChange) {
		ObjectNode dashboard = new ObjectMapper().createObjectNode();
		dashboard.put("id", id).put("name", name).put("lastChange", lastChange);
		return dashboard;
	}

	/** Builds a JSON node representing a report or report info item. */
	public ObjectNode buildReportNode(String id, String name, String description, String owner, String lastChange) {
		ObjectNode report = new ObjectMapper().createObjectNode();
		report.put("id", id).put("name", name).put("description", description).put("owner", owner).put("lastChange", lastChange);
		ObjectNode dataSet = new ObjectMapper().createObjectNode();
		dataSet.put("drilldownAttributes", new ObjectMapper().createArrayNode());
		report.put("dataSet", dataSet);
		return report;
	}

	/** Builds a JSON node representing a view in a report. */
	public ObjectNode buildViewNode(String id, String name, String type) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("type", type);
	}

	public ObjectNode insertViewDateTimeDefinition(boolean dateSupported, boolean timeSupported, ObjectNode viewNode) {
		ObjectNode definition = new ObjectMapper().createObjectNode().put("timeSupported", timeSupported)
			.put("dateSupported", dateSupported);
		viewNode.put("dateTimeDefinition", definition);
		return viewNode;
	}

	/** Builds a JSON node representing a report comment. */
	public ObjectNode buildCommentNode(String authorId, String authorName, String text, String when) {
		return new ObjectMapper().createObjectNode().put("authorId", authorId).put("author", authorName).put("text", text)
			.put("when", when);
	}

	/** Builds a JSON node representing a report attribute. */
	public ObjectNode buildReportAttributeNode(String name, String code) {
		return new ObjectMapper().createObjectNode().put("name", name).put("code", code);
	}

	/** Builds a JSON node representing a report attribute value. */
	public ObjectNode buildAttributeValueNode(String label, String value) {
		return new ObjectMapper().createObjectNode().put("label", label).put("value", value);
	}

	/** Builds a JSON node representing a user. */
	public ObjectNode buildUserNode(String id, String username, String givenName, String familyName, String email,
		String firstLogin, String lastLogin, String locale) {
		return new ObjectMapper().createObjectNode().put("id", id).put("username", username).put("name", givenName)
			.put("surname", familyName).put("email", email).put("firstLogin", firstLogin).put("lastLogin", lastLogin)
			.put("locale", locale);
	}

	/** Builds a JSON node representing table bounds. */
	public ObjectNode buildTableNode(int rows, int columns) {
		return buildTableNode(rows, columns, 0, 0);
	}

	/** Builds a JSON node representing table bounds. */
	public ObjectNode buildTableNode(int rows, int columns, int leftHeaderColumns, int topHeaderRows) {
		return new ObjectMapper().createObjectNode().put("rowsCount", rows).put("columnsCount", columns)
			.put("topHeaderRowsCount", topHeaderRows).put("leftHeaderColumnsCount", leftHeaderColumns);
	}

	/** Builds a JSON node representing a data set or data set info item. */
	public ObjectNode buildDataSetNode(String id, String name, String description, String owner, String lastChange) {
		ObjectNode dataSet = new ObjectMapper().createObjectNode();
		dataSet.put("id", id).put("name", name).put("description", description).put("owner", owner).put("lastChange", lastChange);
		return dataSet;
	}

	/** Builds a JSON node representing a data set attribute. */
	public ObjectNode buildAttributeNode(String id, String name, String code, String type) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("code", code).put("type", type);
	}

	/** Builds a JSON node representing a data set indicator. */
	public ObjectNode buildIndicatorNode(String id, String name, String code, String formula, String type) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("code", code).put("formula", formula)
			.put("type", type);
	}
}
