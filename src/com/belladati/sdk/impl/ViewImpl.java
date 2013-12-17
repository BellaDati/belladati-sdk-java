package com.belladati.sdk.impl;

import java.util.Collection;

import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class ViewImpl implements View {

	protected final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final ViewType type;

	ViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		this.service = service;

		this.id = node.get("id").asText();
		this.name = node.get("name").asText();
		if (node.hasNonNull("type")) {
			try {
				this.type = ViewType.valueOf(node.get("type").asText().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new UnknownViewTypeException(node.get("type").asText());
			}
		} else {
			throw new UnknownViewTypeException("missing type");
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

	class UnknownViewTypeException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -9179478821813868612L;

		public UnknownViewTypeException(String type) {
			super("Unknown view type: " + type);
		}
	}

}
