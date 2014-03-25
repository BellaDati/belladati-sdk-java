package com.belladati.sdk.impl;

import java.util.Collection;

import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;

abstract class ViewImpl implements View {

	/**
	 * Builds an instance based on the given node. Will select an appropriate
	 * class to instantiate based on the view's type.
	 */
	static ViewImpl buildView(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		switch (parseType(node)) {
		case TABLE:
			return new TableViewImpl(service, node);
		default:
			return new JsonViewImpl(service, node);
		}
	}

	/**
	 * Parses the view type from the given JSON node.
	 * 
	 * @param node the node to examine
	 * @return the view type from the node
	 * @throws UnknownViewTypeException if no view type was found or it couldn't
	 *             be parsed
	 */
	private static ViewType parseType(JsonNode node) throws UnknownViewTypeException {
		if (node.hasNonNull("type")) {
			try {
				return ViewType.valueOf(node.get("type").asText().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new UnknownViewTypeException(node.get("type").asText());
			}
		} else {
			throw new UnknownViewTypeException("missing type");
		}
	}

	protected final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final ViewType type;
	private final boolean dateIntervalSupported;
	private final boolean timeIntervalSupported;

	ViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		this.service = service;

		this.id = node.get("id").asText();
		this.name = node.get("name").asText();
		this.type = parseType(node);

		if (node.hasNonNull("dateTimeDefinition") && this.type != ViewType.TABLE) {
			// we have a date/time definition and are not dealing with a table
			JsonNode definition = node.get("dateTimeDefinition");
			if (definition.hasNonNull("dateSupported")) {
				dateIntervalSupported = definition.get("dateSupported").asBoolean();
			} else {
				dateIntervalSupported = false;
			}
			if (definition.hasNonNull("timeSupported")) {
				timeIntervalSupported = definition.get("timeSupported").asBoolean();
			} else {
				timeIntervalSupported = false;
			}
		} else {
			dateIntervalSupported = false;
			timeIntervalSupported = false;
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ViewType getType() {
		return type;
	}

	@Override
	public Object loadContent(Filter<?>... filters) {
		return service.loadViewContent(id, type, filters);
	}

	@Override
	public Object loadContent(Collection<Filter<?>> filters) {
		return service.loadViewContent(id, type, filters);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViewImpl) {
			return id.equals(((ViewImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean isDateIntervalSupported() {
		return dateIntervalSupported;
	}

	@Override
	public boolean isTimeIntervalSupported() {
		return timeIntervalSupported;
	}

	@Override
	public ViewLoader createLoader() {
		return new ViewLoaderImpl(service, id, type);
	}

	static class UnknownViewTypeException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -9179478821813868612L;

		public UnknownViewTypeException(String type) {
			super("Unknown view type: " + type);
		}
	}

}
