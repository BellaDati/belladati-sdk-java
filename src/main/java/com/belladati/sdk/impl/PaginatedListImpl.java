package com.belladati.sdk.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

abstract class PaginatedListImpl<T> implements PaginatedList<T> {

	protected final List<T> currentData = new ArrayList<T>();
	private final BellaDatiServiceImpl service;
	private final String relativeUrl;
	private final String field;

	/** The first page loaded during the most recent call to a load() method */
	private int firstPage = -1;
	/** The most recent page loaded */
	private int page = -1;
	/** The page size currently used by this instance */
	private int size = -1;

	PaginatedListImpl(BellaDatiServiceImpl service, String relativeUrl, String field) {
		this.service = service;
		this.relativeUrl = relativeUrl;
		this.field = field;
	}

	@Override
	public Iterator<T> iterator() {
		return currentData.iterator();
	}

	@Override
	public PaginatedList<T> load() {
		return loadFrom(relativeUrl);
	}

	@Override
	public PaginatedList<T> load(int size) throws IllegalArgumentException {
		return load(0, size);
	}

	@Override
	public PaginatedList<T> load(int page, int size) throws IllegalArgumentException {
		// argument checking
		if (page < 0) {
			throw new IllegalArgumentException("Page must be >= 0, was " + page);
		}
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be > 0, was " + size);
		}

		// query parameterized URL
		return loadFrom(buildUri(page, size).toString());
	}

	private URI buildUri(int page, int size) {
		try {
			return new URIBuilder(relativeUrl).addParameter("offset", "" + page * size).addParameter("size", "" + size).build();
		} catch (URISyntaxException e) {
			throw new InternalConfigurationException("Invalid URI", e);
		}
	}

	private PaginatedList<T> loadFrom(String parameterizedUri) {
		currentData.clear();
		addFrom(parameterizedUri);
		firstPage = page;
		return this;
	}

	private PaginatedList<T> addFrom(String parameterizedUri) {
		JsonNode json = service.loadJson(parameterizedUri);

		size = json.get("size").asInt();
		page = json.get("offset").asInt() / size;

		ArrayNode nodes = (ArrayNode) json.get(field);
		for (JsonNode node : nodes) {
			currentData.add(parse(service, node));
		}
		return this;
	}

	@Override
	public PaginatedList<T> loadNext() {
		if (!isLoaded()) {
			// if we haven't loaded this list yet, load the first page
			return load();
		}
		if (!hasNextPage()) {
			// there are no more pages to load
			return this;
		}
		return addFrom(buildUri(page + 1, size).toString());
	}

	@Override
	public boolean isLoaded() {
		return size > 0 && page >= 0;
	}

	@Override
	public boolean hasNextPage() {
		if (!isLoaded()) {
			return true;
		}
		// if all pages until now were full, we have more items
		return size * (page - firstPage + 1) == currentData.size();
	}

	@Override
	public int getFirstLoadedPage() {
		if (!isLoaded()) {
			return -1;
		}
		return firstPage;
	}

	@Override
	public int getLastLoadedPage() {
		if (!isLoaded()) {
			return -1;
		}
		return page;
	}

	@Override
	public int getFirstLoadedIndex() {
		if (!isLoaded() || currentData.isEmpty()) {
			return -1;
		}
		return firstPage * size;
	}

	@Override
	public int getLastLoadedIndex() {
		if (!isLoaded() || currentData.isEmpty()) {
			return -1;
		}
		return getFirstLoadedIndex() + currentData.size() - 1;
	}

	@Override
	public int getPageSize() {
		return size;
	}

	@Override
	public boolean contains(T element) {
		return currentData.contains(element);
	}

	@Override
	public T get(int index) throws IndexOutOfBoundsException {
		return currentData.get(index - getFirstLoadedIndex());
	}

	@Override
	public int indexOf(T element) {
		int dataIndex = currentData.indexOf(element);
		if (dataIndex < 0) {
			return -1;
		}
		return getFirstLoadedIndex() + dataIndex;
	}

	@Override
	public boolean isEmpty() {
		return currentData.isEmpty();
	}

	@Override
	public int size() {
		return currentData.size();
	}

	@Override
	public List<T> toList() {
		return Collections.unmodifiableList(currentData);
	}

	@Override
	public String toString() {
		return currentData.toString();
	}

	protected abstract T parse(BellaDatiServiceImpl service, JsonNode node);
}
