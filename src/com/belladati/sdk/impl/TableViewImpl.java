package com.belladati.sdk.impl;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.view.TableView;
import com.fasterxml.jackson.databind.JsonNode;

class TableViewImpl extends ViewImpl implements TableView {

	TableViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		super(service, node);
	}

	@Override
	public Table loadContent(Filter<?>... filters) {
		return (Table) super.loadContent(filters);
	}

	@Override
	public Table loadContent(Collection<Filter<?>> filters) {
		return (Table) super.loadContent(filters);
	}

	static class TableImpl implements Table {

		private final BellaDatiServiceImpl service;
		private final String id;
		private final int rowCount;
		private final int columnCount;
		private final int leftHeaderColumnCount;
		private final int topHeaderRowCount;
		private final Collection<Filter<?>> filters;

		TableImpl(BellaDatiServiceImpl service, String id, JsonNode node) {
			this(service, id, node, Collections.<Filter<?>> emptyList());
		}

		TableImpl(BellaDatiServiceImpl service, String id, JsonNode node, Collection<Filter<?>> filters) {
			this.service = service;
			this.id = id;
			this.filters = filters;

			this.rowCount = node.hasNonNull("rowsCount") ? node.get("rowsCount").asInt() : 0;
			this.columnCount = node.hasNonNull("columnsCount") ? node.get("columnsCount").asInt() : 0;

			this.leftHeaderColumnCount = node.hasNonNull("leftHeaderColumnsCount") ? node.get("leftHeaderColumnsCount").asInt()
				: 0;
			this.topHeaderRowCount = node.hasNonNull("topHeaderRowsCount") ? node.get("topHeaderRowsCount").asInt() : 0;
		}

		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public int getColumnCount() {
			return columnCount;
		}

		@Override
		public boolean hasLeftHeader() {
			return leftHeaderColumnCount > 0;
		}

		@Override
		public boolean hasTopHeader() {
			return topHeaderRowCount > 0;
		}

		@Override
		public JsonNode loadLeftHeader(int firstRow, int lastRow) throws IllegalArgumentException {
			if (firstRow < 0) {
				throw new IllegalArgumentException("First row must be > 0");
			}
			if (lastRow >= getRowCount()) {
				throw new IllegalArgumentException("Last row must be < row count");
			}
			if (firstRow > lastRow) {
				throw new IllegalArgumentException("First row must be <= last row");
			}
			try {
				URIBuilder builder = new URIBuilder("api/reports/views/" + id + "/table/leftHeader");
				builder.addParameter("rowsFrom", "" + firstRow);
				builder.addParameter("rowsTo", "" + lastRow);
				service.appendFilter(builder, filters);
				return service.loadJson(builder.build().toString());
			} catch (URISyntaxException e) {
				throw new InternalConfigurationException(e);
			}
		}

		@Override
		public JsonNode loadTopHeader(int firstColumn, int lastColumn) throws IllegalArgumentException {
			if (firstColumn < 0) {
				throw new IllegalArgumentException("First column must be > 0");
			}
			if (lastColumn >= getColumnCount()) {
				throw new IllegalArgumentException("Last column must be < column count");
			}
			if (firstColumn > lastColumn) {
				throw new IllegalArgumentException("First column must be <= last column");
			}
			try {
				URIBuilder builder = new URIBuilder("api/reports/views/" + id + "/table/topHeader");
				builder.addParameter("columnsFrom", "" + firstColumn);
				builder.addParameter("columnsTo", "" + lastColumn);
				service.appendFilter(builder, filters);
				return service.loadJson(builder.build().toString());
			} catch (URISyntaxException e) {
				throw new InternalConfigurationException(e);
			}
		}

		@Override
		public JsonNode loadData(int firstRow, int lastRow, int firstColumn, int lastColumn) throws IllegalArgumentException {
			if (firstRow < 0) {
				throw new IllegalArgumentException("First row must be > 0");
			}
			if (lastRow >= getRowCount()) {
				throw new IllegalArgumentException("Last row must be < row count");
			}
			if (firstRow > lastRow) {
				throw new IllegalArgumentException("First row must be <= last row");
			}
			if (firstColumn < 0) {
				throw new IllegalArgumentException("First column must be > 0");
			}
			if (lastColumn >= getRowCount()) {
				throw new IllegalArgumentException("Last column must be < column count");
			}
			if (firstColumn > lastColumn) {
				throw new IllegalArgumentException("First column must be <= last column");
			}
			try {
				URIBuilder builder = new URIBuilder("api/reports/views/" + id + "/table/data");
				builder.addParameter("rowsFrom", "" + firstRow);
				builder.addParameter("rowsTo", "" + lastRow);
				builder.addParameter("columnsFrom", "" + firstColumn);
				builder.addParameter("columnsTo", "" + lastColumn);
				service.appendFilter(builder, filters);
				return service.loadJson(builder.build().toString());
			} catch (URISyntaxException e) {
				throw new InternalConfigurationException(e);
			}
		}
	}
}
