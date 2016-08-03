package com.belladati.sdk.view.impl;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.view.TableView;
import com.fasterxml.jackson.databind.JsonNode;

public class TableViewImpl extends ViewImpl implements TableView {

	public TableViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
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

	public static class TableImpl implements Table {

		private final BellaDatiServiceImpl service;
		private final String id;
		private final int rowCount;
		private final int columnCount;
		private final int leftHeaderColumnCount;
		private final int topHeaderRowCount;
		private final Collection<Filter<?>> filters;
		private Locale locale;

		public TableImpl(BellaDatiServiceImpl service, String id, JsonNode node) {
			this(service, id, node, Collections.<Filter<?>> emptyList());
		}

		public TableImpl(BellaDatiServiceImpl service, String id, JsonNode node, Collection<Filter<?>> filters) {
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
		public JsonNode loadLeftHeader(int startRow, int endRow) throws IllegalArgumentException {
			if (startRow < 0) {
				throw new IllegalArgumentException("First row must be > 0");
			}
			if (endRow > getRowCount()) {
				throw new IllegalArgumentException("Last row must be <= row count");
			}
			if (startRow > endRow) {
				throw new IllegalArgumentException("First row must be <= last row");
			}
			try {
				URIBuilder builder = new URIBuilder("api/reports/views/" + id + "/table/leftHeader");
				builder.addParameter("rowsFrom", "" + startRow);
				builder.addParameter("rowsTo", "" + endRow);
				service.appendFilter(builder, filters);
				service.appendLocale(builder, locale);
				return service.getAsJson(builder.build().toString());
			} catch (URISyntaxException e) {
				throw new InternalConfigurationException(e);
			}
		}

		@Override
		public JsonNode loadTopHeader(int startColumn, int endColumn) throws IllegalArgumentException {
			if (startColumn < 0) {
				throw new IllegalArgumentException("First column must be > 0");
			}
			if (endColumn > getColumnCount()) {
				throw new IllegalArgumentException("Last column must be <= column count");
			}
			if (startColumn > endColumn) {
				throw new IllegalArgumentException("First column must be <= last column");
			}
			try {
				URIBuilder builder = new URIBuilder("api/reports/views/" + id + "/table/topHeader");
				builder.addParameter("columnsFrom", "" + startColumn);
				builder.addParameter("columnsTo", "" + endColumn);
				service.appendFilter(builder, filters);
				service.appendLocale(builder, locale);
				return service.getAsJson(builder.build().toString());
			} catch (URISyntaxException e) {
				throw new InternalConfigurationException(e);
			}
		}

		@Override
		public JsonNode loadData(int startRow, int endRow, int startColumn, int endColumn) throws IllegalArgumentException {
			if (startRow < 0) {
				throw new IllegalArgumentException("First row must be > 0");
			}
			if (endRow > getRowCount()) {
				throw new IllegalArgumentException("Last row must be <= row count");
			}
			if (startRow > endRow) {
				throw new IllegalArgumentException("First row must be <= last row");
			}
			if (startColumn < 0) {
				throw new IllegalArgumentException("First column must be > 0");
			}
			if (endColumn > getColumnCount()) {
				throw new IllegalArgumentException("Last column must be <= column count");
			}
			if (startColumn > endColumn) {
				throw new IllegalArgumentException("First column must be <= last column");
			}
			try {
				URIBuilder builder = new URIBuilder("api/reports/views/" + id + "/table/data");
				builder.addParameter("rowsFrom", "" + startRow);
				builder.addParameter("rowsTo", "" + endRow);
				builder.addParameter("columnsFrom", "" + startColumn);
				builder.addParameter("columnsTo", "" + endColumn);
				service.appendFilter(builder, filters);
				service.appendLocale(builder, locale);
				return service.getAsJson(builder.build().toString());
			} catch (URISyntaxException e) {
				throw new InternalConfigurationException(e);
			}
		}

		@Override
		public Locale getLocale() {
			return locale;
		}

		@Override
		public Table setLocale(Locale locale) {
			this.locale = locale;
			return this;
		}

		@Override
		public String toString() {
			return "Table(id: " + id + ")";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TableImpl) {
				return id.equals(((TableImpl) obj).id) && filters.equals(((TableImpl) obj).filters);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

}
