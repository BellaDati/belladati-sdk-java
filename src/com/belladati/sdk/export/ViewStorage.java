package com.belladati.sdk.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.belladati.sdk.impl.LocalizationImpl;
import com.belladati.sdk.impl.ViewImpl;
import com.belladati.sdk.view.JsonView;
import com.belladati.sdk.view.TableView;
import com.belladati.sdk.view.TableView.Table;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Stores views as self-contained objects.
 * <p />
 * All requests to the server are made while storing and the view's content is
 * kept inside the view object. Calling load*() methods on the returned object
 * will return the stored data and doesn't communicate with the server.
 * <p />
 * All returned objects are {@link Serializable}.
 * 
 * @author Chris Hennigfeld
 */
public class ViewStorage {

	/**
	 * Stores a JsonView. Makes server requests as needed to fetch the view's
	 * content.
	 * 
	 * @param view the view to store
	 * @return a {@link Serializable} view object holding all the view's data
	 */
	public JsonView storeView(JsonView view) {
		return new StoredJsonView(readId(view), readName(view), readType(view), readLocalization(view), readJsonContent(view));
	}

	/**
	 * Stores a TableView. Makes server requests as needed to fetch the view's
	 * content.
	 * 
	 * @param view the view to store
	 * @return a {@link Serializable} view object holding all the view's data
	 */
	public TableView storeView(TableView view) {
		return new StoredTableView(readId(view), readName(view), readType(view), readLocalization(view),
			storeTable(readTableContent(view)));
	}

	/**
	 * Stores a view. Makes server requests as needed to fetch the view's
	 * content.
	 * 
	 * @param view the view to store
	 * @return a {@link Serializable} view object holding all the view's data,
	 *         or <tt>null</tt> if the view isn't either a {@link JsonView} or
	 *         {@link TableView}.
	 */
	public View storeView(View view) {
		if (view instanceof JsonView) {
			return storeView((JsonView) view);
		} else if (view instanceof TableView) {
			return storeView((TableView) view);
		}
		return null;
	}

	/**
	 * Stores a {@link TableView}'s {@link Table} object. Makes server requests
	 * as needed to fetch the table's content.
	 * 
	 * @param table the table to store
	 * @return a {@link Serializable} table object holding all the table's data
	 */
	public Table storeTable(Table table) {
		TableData data = readAllContent(table);
		return new StoredTable(data.leftHeader, data.topHeader, data.data, readLocale(table));
	}

	/**
	 * Reads a view's ID. Override to customize ID reading.
	 * 
	 * @param view view to read from
	 * @return the view's ID
	 */
	protected String readId(View view) {
		return view.getId();
	}

	/**
	 * Reads a view's name. Override to customize name reading.
	 * 
	 * @param view view to read from
	 * @return the view's name
	 */
	protected String readName(View view) {
		return view.getName();
	}

	/**
	 * Reads a view's type. Override to customize type reading.
	 * 
	 * @param view view to read from
	 * @return the view's type
	 */
	protected ViewType readType(View view) {
		return view.getType();
	}

	/**
	 * Reads a view's localization. Override to customize localization reading.
	 * 
	 * @param view view to read from
	 * @return the view's localization
	 */
	protected LocalizationImpl readLocalization(View view) {
		return (view instanceof ViewImpl) ? ((ViewImpl) view).getLocalization() : null;
	}

	/**
	 * Reads a view's JSON content. Override to customize JSON loading.
	 * 
	 * @param view view to read from
	 * @return the view's JSON content
	 */
	protected JsonNode readJsonContent(JsonView view) {
		return view.loadContent();
	}

	/**
	 * Reads a view's table content. Override to customize table loading.
	 * 
	 * @param view view to read from
	 * @return the view's table content
	 */
	protected Table readTableContent(TableView view) {
		return view.loadContent();
	}

	/**
	 * Reads a table's entire content. Override to customize table loading, e.g.
	 * to make server calls in parallel.
	 * 
	 * @param table table to read from
	 * @return the table's header and data content
	 */
	protected TableData readAllContent(Table table) {
		return new TableData(readLeftHeaderContent(table), readTopHeaderContent(table), readDataContent(table));
	}

	/**
	 * Reads a table's left header. Override to customize header loading.
	 * 
	 * @param table table to read from
	 * @return the table's left header, broken down into rows and columns each
	 *         containing a <tt>&lt;td&gt;</tt> or <tt>&lt;th&gt;</tt> element.
	 */
	protected String[][] readLeftHeaderContent(Table table) {
		if (!table.hasLeftHeader()) {
			return new String[table.getRowCount()][0];
		}
		return parseRowsColumns(table.loadLeftHeader(0, table.getRowCount()).get("content").asText());
	}

	/**
	 * Reads a table's top header. Override to customize header loading.
	 * 
	 * @param table table to read from
	 * @return the table's top header, broken down into rows and columns each
	 *         containing a <tt>&lt;td&gt;</tt> or <tt>&lt;th&gt;</tt> element.
	 */
	protected String[][] readTopHeaderContent(Table table) {
		if (!table.hasTopHeader()) {
			return new String[0][table.getColumnCount()];
		}
		return parseRowsColumns(table.loadTopHeader(0, table.getColumnCount()).get("content").asText());
	}

	/**
	 * Reads a table's data. Override to customize data loading.
	 * 
	 * @param table table to read from
	 * @return the table's data content, broken down into rows and columns each
	 *         containing a <tt>&lt;td&gt;</tt> or <tt>&lt;th&gt;</tt> element.
	 */
	protected String[][] readDataContent(Table table) {
		return parseRowsColumns(table.loadData(0, table.getRowCount(), 0, table.getColumnCount()).get("content").asText());
	}

	/**
	 * Parses the given HTML string into rows and columns. The string is
	 * expected to contain a number of <tt>&lt;tr&gt;</tt> elements each
	 * containing a number of <tt>&lt;th&gt;</tt> or <tt>&lt;td&gt;</tt>
	 * elements.
	 * 
	 * @param html HTML string to parse
	 * @return content of the HTML
	 */
	protected String[][] parseRowsColumns(String html) {
		List<String> rows = new ArrayList<String>();
		Pattern rowPattern = Pattern.compile("<tr>(.*?)</tr>");
		Pattern colPattern = Pattern.compile("<th>(.*?)</th>|<td>(.*?)</td>");
		Matcher rowMatcher = rowPattern.matcher(html);
		while (rowMatcher.find()) {
			rows.add(rowMatcher.group().replaceAll("<tr>(.*?)</tr>", "$1"));
		}
		String[][] result = null;
		for (int i = 0; i < rows.size(); i++) {
			String row = rows.get(i);
			Matcher colMatcher = colPattern.matcher(row);
			List<String> cols = new ArrayList<String>();
			while (colMatcher.find()) {
				cols.add(colMatcher.group());
			}
			if (result == null) {
				result = new String[rows.size()][cols.size()];
			}
			result[i] = cols.toArray(new String[cols.size()]);
		}
		return result;
	}

	/**
	 * Reads a view's locale. Override to customize locale reading.
	 * 
	 * @param view view to read from
	 * @return the view's locale
	 */
	protected Locale readLocale(Table table) {
		return table.getLocale();
	}

	/**
	 * Holds a table's header and data content broken down into rows and
	 * columns.
	 * 
	 * @author Chris Hennigfeld
	 */
	protected class TableData {
		/** left header, by rows and columns */
		public String[][] leftHeader;
		/** top header, by rows and columns */
		public String[][] topHeader;
		/** data, by rows and columns */
		public String[][] data;

		/**
		 * Creates a new instance with the given contents.
		 * 
		 * @param leftHeader left header, by rows and columns
		 * @param topHeader top header, by rows and columns
		 * @param data data, by rows and columns
		 */
		public TableData(String[][] leftHeader, String[][] topHeader, String[][] data) {
			this.leftHeader = leftHeader;
			this.topHeader = topHeader;
			this.data = data;
		}
	}
}
