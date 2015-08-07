package com.belladati.sdk.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

abstract class CachedListImpl<T> implements CachedList<T> {

	private final List<T> data = new ArrayList<T>();

	private final BellaDatiServiceImpl service;
	private final String uri;
	private final String field;

	private boolean isLoaded = false;

	public CachedListImpl(BellaDatiServiceImpl service, String uri, String field) {
		this.service = service;
		this.uri = uri;
		this.field = field;
	}

	@Override
	public List<T> get() {
		return Collections.unmodifiableList(data);
	}

	@Override
	public List<T> toList() {
		return get();
	}

	@Override
	public CachedList<T> load() {
		data.clear();
		JsonNode json = service.loadJson(uri);

		if (json.get(field) instanceof ArrayNode) {
			ArrayNode nodes = (ArrayNode) json.get(field);
			for (JsonNode node : nodes) {
				try {
					data.add(parse(service, node));
				} catch (ParseException e) {
					// nothing to do, just ignore
				}
			}
		}
		isLoaded = true;
		return this;
	}

	@Override
	public CachedList<T> loadFirstTime() {
		if (!isLoaded()) {
			load();
		}
		return this;
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	protected abstract T parse(BellaDatiServiceImpl service, JsonNode node) throws ParseException;

	protected static class ParseException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -3237010782121691680L;

		public ParseException(JsonNode node) {
			super("Failed to parse node " + node);
		}

		public ParseException(JsonNode node, Throwable cause) {
			super("Failed to parse node " + node, cause);
		}
	}
}
