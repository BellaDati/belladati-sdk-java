package com.belladati.sdk.test;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
		ObjectNode definition = new ObjectMapper().createObjectNode().put("timeSupported", timeSupported).put("dateSupported",
			dateSupported);
		viewNode.put("dateTimeDefinition", definition);
		return viewNode;
	}

	/** Builds a JSON node representing a report comment. */
	public ObjectNode buildCommentNode(String commentId, String authorId, String authorName, String text, String when) {
		return new ObjectMapper().createObjectNode().put("id", commentId).put("authorId", authorId).put("author", authorName)
			.put("text", text).put("when", when);
	}

	/** Builds a JSON node representing a report attribute. */
	public ObjectNode buildReportAttributeNode(String name, String code) {
		return new ObjectMapper().createObjectNode().put("name", name).put("code", code);
	}

	/** Builds a JSON node representing a report attribute value. */
	public ObjectNode buildAttributeValueNode(String label, String value) {
		return new ObjectMapper().createObjectNode().put("label", label).put("value", value);
	}

	/** Builds a JSON node representing a domain info. */
	public ObjectNode buildDomainInfoNode(String id, String name, String description, String active) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("description", description).put("active",
			active);
	}

	/** Builds a JSON node representing a domain. */
	public ObjectNode buildDomainNode(String id, String name, String description, String dateFormat, String timeFormat,
		String timeZone, String locale, String active) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("description", description)
			.put("dateFormat", dateFormat).put("timeFormat", timeFormat).put("timeZone", timeZone).put("locale", locale)
			.put("active", active);
	}

	/** Builds a JSON node representing a user group. */
	public ObjectNode buildUserGroupNode(String id, String name, String description) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("description", description);
	}

	/** Builds a JSON node representing a user. */
	public ObjectNode buildUserNode(String id, String username, String givenName, String familyName, String email,
		String firstLogin, String lastLogin, String locale) {
		return buildUserNode(id, username, givenName, familyName, email, firstLogin, lastLogin, locale, null, null, null, null,
			null);
	}

	/** Builds a JSON node representing a user with roles and user groups. */
	public ObjectNode buildUserNode(String id, String username, String givenName, String familyName, String email,
		String firstLogin, String lastLogin, String locale, String timeZone, String active, String domainId, String[] roles,
		String[][] groups) {
		ObjectMapper mapper = new ObjectMapper();

		// mandatory
		ObjectNode user = mapper.createObjectNode().put("id", id).put("username", username).put("name", givenName)
			.put("surname", familyName).put("email", email).put("firstLogin", firstLogin).put("lastLogin", lastLogin);

		// optional
		put(user, "locale", locale);
		put(user, "timeZone", timeZone);
		put(user, "active", active);
		put(user, "domain_id", domainId);
		if (roles != null) {
			ArrayNode array = mapper.createArrayNode();
			for (String role : roles) {
				array.add(mapper.createObjectNode().put("role", role));
			}
			user.put("roles", array);
		}
		if (groups != null) {
			ArrayNode array = mapper.createArrayNode();
			for (String[] group : groups) {
				array.add(mapper.createObjectNode().put("id", group[0]).put("name", group[1]));
			}
			user.put("groups", array);
		}

		return user;

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

	/** Builds a JSON node representing a data set or data set info item. */
	public ObjectNode buildDataSetDataRowNode(String uid1, String attName1, String attValue1, String indName1, String indValue1) {
		ObjectNode row1 = new ObjectMapper().createObjectNode();
		row1.put("id", uid1).put(attName1, attValue1).put(indName1, indValue1);
		return row1;
	}

	/** Builds a JSON node representing a data set or data set info item. */
	public ObjectNode buildDataSetDataNode(String id, String name, String description, String owner, String lastChange,
		String uid1, String attName1, String attValue1, String indName1, String indValue1) {
		ObjectNode json = new ObjectMapper().createObjectNode();
		ObjectNode dataSet = new ObjectMapper().createObjectNode();
		dataSet.put("id", id).put("name", name).put("description", description).put("owner", owner).put("lastChange", lastChange);
		json.put("dataSet", dataSet);

		ArrayNode data = new ObjectMapper().createArrayNode();
		ObjectNode row1 = new ObjectMapper().createObjectNode();
		row1.put("UID", uid1).put(attName1, attValue1).put(indName1, indValue1);
		data.add(row1);
		json.put("data", data);

		json.put("size", 1);
		json.put("offset", 0);

		return json;
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

	/** Builds a JSON node representing a data source. */
	public ObjectNode buildDataSourceNode(String id, String name, String type) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("type", type);
	}

	/** Builds a JSON node representing a data source import. */
	public ObjectNode buildSourceImportNode(String id, String caller, String lastImport, String overwritePolicy,
		String interval) {
		return new ObjectMapper().createObjectNode().put("id", id).put("createdBy", caller).put("when", lastImport)
			.put("overwritingPolicy", overwritePolicy).put("repeateInterval", interval);
	}

	/** Builds a JSON node representing a data source import with custom interval length. */
	public ObjectNode buildSourceImportNode(String id, String caller, String lastImport, String overwritePolicy, String interval,
		Integer customIntervalLength) {
		return buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval).put("repeateIntervalCustom",
			customIntervalLength);
	}

	/** Builds a JSON node representing a import form without any elements. */
	public ObjectNode buildFormNode(String id, String name, String recordTimestamp) {
		return new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("recordTimestamp", recordTimestamp);
	}

	/** Builds a JSON node representing a import form with given elements. */
	public ObjectNode buildFormNode(String id, String name, String recordTimestamp, List<JsonNode> elements) {
		ObjectNode formNode = buildFormNode(id, name, recordTimestamp);
		if (elements != null && !elements.isEmpty()) {
			ArrayNode arrayNode = formNode.putArray("elements");
			for (JsonNode elementNode : elements) {
				arrayNode.add(elementNode);
			}
		}
		return formNode;
	}

	/** Builds a JSON node representing a import form element. */
	public ObjectNode buildFormElementNode(String id, String name, String type, String mapToDateColumn, String... items) {
		ObjectNode elementNode = new ObjectMapper().createObjectNode().put("id", id).put("name", name).put("type", type);
		if (mapToDateColumn != null) {
			elementNode.put("mapToDateColumn", mapToDateColumn);
		}
		if (items != null && items.length > 0) {
			ArrayNode arrayNode = elementNode.putArray("items");
			for (String item : items) {
				ObjectNode itemNode = new ObjectMapper().createObjectNode().put("name", item);
				arrayNode.add(itemNode);
			}
		}
		return elementNode;
	}

	private void put(ObjectNode node, String field, String value) {
		if (value != null) {
			node.put(field, value);
		}
	}

}
