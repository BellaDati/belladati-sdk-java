package com.belladati.sdk.export;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import com.belladati.sdk.view.TableView.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class StoredTable implements Table, Serializable {

	private static final long serialVersionUID = -2404496926925214029L;

	private final String[][] leftHeader;
	private final String[][] topHeader;
	private final String[][] data;

	private final Locale locale;

	StoredTable(String[][] leftHeader, String[][] topHeader, String[][] data, Locale locale) {
		this.leftHeader = deepCopy(leftHeader);
		this.topHeader = deepCopy(topHeader);
		this.data = deepCopy(data);

		this.locale = locale;
	}

	private String[][] deepCopy(String[][] array) {
		if (array == null || array.length == 0) {
			return new String[0][0];
		}
		if (array[0] == null || array[0].length == 0) {
			return new String[array.length][0];
		}
		String[][] result = new String[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			result[i] = Arrays.copyOf(array[i], array[i].length);
		}
		return result;
	}

	@Override
	public int getRowCount() {
		return Math.min(leftHeader.length, data.length);
	}

	@Override
	public int getColumnCount() {
		if (topHeader.length > 0 && data.length > 0) {
			return Math.min(topHeader[0].length, data[0].length);
		}
		if (topHeader.length > 0) {
			return topHeader[0].length;
		}
		if (data.length > 0) {
			return data[0].length;
		}
		return 0;
	}

	@Override
	public boolean hasLeftHeader() {
		return leftHeader.length > 0 && leftHeader[0].length > 0;
	}

	@Override
	public boolean hasTopHeader() {
		return topHeader.length > 0;
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
		StringBuilder builder = new StringBuilder();
		for (int row = startRow; row < endRow; row++) {
			builder.append("<tr>");
			for (int col = 0; col < leftHeader[row].length; col++) {
				builder.append(leftHeader[row][col]);
			}
			builder.append("</tr>");
		}
		return new ObjectMapper().createObjectNode().put("content", builder.toString());
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
		StringBuilder builder = new StringBuilder();
		for (int row = 0; row < topHeader.length; row++) {
			builder.append("<tr>");
			for (int col = startColumn; col < endColumn; col++) {
				builder.append(topHeader[row][col]);
			}
			builder.append("</tr>");
		}
		return new ObjectMapper().createObjectNode().put("content", builder.toString());
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
		StringBuilder builder = new StringBuilder();
		for (int row = startRow; row < endRow; row++) {
			builder.append("<tr>");
			for (int col = startColumn; col < endColumn; col++) {
				builder.append(data[row][col]);
			}
			builder.append("</tr>");
		}
		return new ObjectMapper().createObjectNode().put("content", builder.toString());
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public Table setLocale(Locale locale) {
		return this;
	}

}
