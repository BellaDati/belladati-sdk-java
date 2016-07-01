package com.belladati.sdk.dashboard.impl;

import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.exception.impl.UnknownDashletTypeException;
import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.exception.impl.UnsupportedDashletContentException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.impl.ViewImpl;
import com.fasterxml.jackson.databind.JsonNode;

public class DashletImpl implements Dashlet {

	private final String name;
	private final Type type;
	private final Object content;

	public DashletImpl(BellaDatiServiceImpl service, JsonNode node)
		throws UnknownDashletTypeException, UnsupportedDashletContentException {

		if (node.hasNonNull("type")) {
			this.type = findType(node.get("type").asText());
			switch (type) {
			case VIEW:
				content = getContentView(service, node);
				name = ((View) content).getName();
				break;
			case TEXT:
				content = getContentText(node);
				name = node.hasNonNull("name") ? node.get("name").asText() : "";
				break;
			default:
				throw new UnknownDashletTypeException("Type not implemented: " + type);
			}
		} else {
			throw new UnknownDashletTypeException("missing type");
		}
	}

	/**
	 * Determines the dashlet type from the given type string.
	 * 
	 * @param typeString string to parse (from JSON)
	 * @return the dashlet type
	 * @throws UnknownDashletTypeException if no such type was found
	 */
	private Type findType(String typeString) throws UnknownDashletTypeException {
		if ("viewReport".equalsIgnoreCase(typeString)) {
			return Type.VIEW;
		} else if ("textContent".equalsIgnoreCase(typeString)) {
			return Type.TEXT;
		}
		throw new UnknownDashletTypeException(typeString);
	}

	/** Reads view content from a dashlet node. */
	private View getContentView(BellaDatiServiceImpl service, JsonNode node) throws UnsupportedDashletContentException {
		if (!node.hasNonNull("canAccessViewReport") || !node.get("canAccessViewReport").asBoolean()) {
			throw new UnsupportedDashletContentException("View not accessible");
		}
		if (!node.hasNonNull("viewReport")) {
			throw new UnsupportedDashletContentException("Missing view element");
		}
		try {
			return ViewImpl.buildView(service, node.get("viewReport"));
		} catch (UnknownViewTypeException e) {
			throw new UnsupportedDashletContentException(e);
		}
	}

	/** Reads text content from a dashlet node. */
	private String getContentText(JsonNode node) throws UnsupportedDashletContentException {
		if (node.hasNonNull("textContent")) {
			return node.get("textContent").asText();
		} else {
			throw new UnsupportedDashletContentException("Text content missing");
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "Dashlet containing " + type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DashletImpl) {
			return content.equals(((DashletImpl) obj).content);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

}
