package com.belladati.sdk.impl;

import java.util.Collection;

import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.view.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

class JsonViewImpl extends ViewImpl implements JsonView {

	public JsonViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		super(service, node);
	}

	@Override
	public JsonNode loadContent(Filter<?>... filters) {
		return (JsonNode) super.loadContent(filters);
	}

	@Override
	public JsonNode loadContent(Collection<Filter<?>> filters) {
		return (JsonNode) super.loadContent(filters);
	}
}
