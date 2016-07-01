package com.belladati.sdk.view.impl;

import java.util.Collection;

import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.view.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonViewImpl extends ViewImpl implements JsonView {

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
