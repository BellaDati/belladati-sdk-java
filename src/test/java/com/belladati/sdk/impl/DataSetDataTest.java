package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.belladati.sdk.dataset.data.DataColumn;
import com.belladati.sdk.dataset.data.DataRow;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.dataset.data.OverwritePolicy;
import com.belladati.sdk.exception.dataset.data.NoColumnsException;
import com.belladati.sdk.exception.dataset.data.TooManyColumnsException;
import com.belladati.sdk.exception.dataset.data.UnknownColumnException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Test
public class DataSetDataTest extends SDKTest {

	private final String column = "column";

	/** table must have columns */
	@Test(expectedExceptions = NoColumnsException.class)
	public void noColumns() {
		DataTable.createBasicInstance(Collections.<String> emptyList());
	}

	/** create with single column */
	public void column() {
		DataTable table = DataTable.createBasicInstance(column);
		assertEquals(table.getColumns(), Arrays.asList(new DataColumn(column)));
		assertEquals(table.createRow().getColumns(), table.getColumns());
	}

	/** create with multiple columns passed as strings */
	public void columnsStrings() {
		String other = "other";
		DataTable table = DataTable.createBasicInstance(column, other);
		assertEquals(table.getColumns(), Arrays.asList(new DataColumn(column), new DataColumn(other)));
		assertEquals(table.createRow().getColumns(), table.getColumns());
	}

	/** create with multiple columns passed as list */
	public void columnsStringList() {
		String other = "other";
		DataTable table = DataTable.createBasicInstance(Arrays.asList(column, other));
		assertEquals(table.getColumns(), Arrays.asList(new DataColumn(column), new DataColumn(other)));
		assertEquals(table.createRow().getColumns(), table.getColumns());
	}

	/** create with multiple columns passed as strings */
	public void columnsObjects() {
		DataColumn col1 = new DataColumn(column);
		DataColumn col2 = new DataColumn("other");
		DataTable table = DataTable.createDetailedInstance(col1, col2);
		assertEquals(table.getColumns(), Arrays.asList(col1, col2));
		assertSame(table.getColumns().get(0), col1);
		assertSame(table.getColumns().get(1), col2);
		assertEquals(table.createRow().getColumns(), table.getColumns());
	}

	/** create with multiple columns passed as list */
	public void columnsObjectList() {
		DataColumn col1 = new DataColumn(column);
		DataColumn col2 = new DataColumn("other");
		DataTable table = DataTable.createDetailedInstance(Arrays.asList(col1, col2));
		assertEquals(table.getColumns(), Arrays.asList(col1, col2));
		assertSame(table.getColumns().get(0), col1);
		assertSame(table.getColumns().get(1), col2);
		assertEquals(table.createRow().getColumns(), table.getColumns());
	}

	/** columns with the same code are equal */
	public void columnsEqual() {
		assertEquals(new DataColumn(column), new DataColumn(column, "format"));
		assertEquals(new DataColumn(column).hashCode(), new DataColumn(column).hashCode());

		assertNotEquals(new DataColumn(column), new DataColumn("other"));
		assertNotEquals(new DataColumn(column).hashCode(), new DataColumn("other").hashCode());
	}

	/** create rows */
	public void createRows() {
		DataTable table = DataTable.createBasicInstance(column);
		assertEquals(table.getRows().size(), 0);
		table.createRow();
		assertEquals(table.getRows().size(), 1);
		table.createRow("abc");
		assertEquals(table.getRows().size(), 2);
	}

	/** returned row list cannot be modified */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void rowListImmutable() {
		DataTable table = DataTable.createBasicInstance(column);
		List<DataRow> rows = table.getRows();
		DataRow row = table.createRow();
		assertEquals(rows.size(), 0);
		rows.add(row);
	}

	/** returned column list cannot be modified */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void columnListImmutable() {
		DataTable.createBasicInstance(column).getColumns().add(null);
	}

	/** new rows are empty */
	public void rowEmpty() {
		DataTable table = DataTable.createBasicInstance(column);
		DataRow row = table.createRow();

		assertNull(row.get(column));
		assertEquals(row.getAll(), Arrays.asList((String) null));
	}

	/** can set row content through set method */
	public void rowContentSet() {
		String content = "content";
		DataTable table = DataTable.createBasicInstance(column);

		DataRow row = table.createRow();
		row.set(column, content);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can set row content through set method */
	public void rowContentSetAll() {
		String content = "content";
		DataTable table = DataTable.createBasicInstance(column);

		DataRow row = table.createRow();
		row.setAll(content);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can set row content through setAll method with offset */
	public void rowContentSetOffset() {
		String content = "content";
		DataTable table = DataTable.createBasicInstance(column);

		DataRow row = table.createRow();
		row.setAll(0, content);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can set row content through creation */
	public void rowContentCreate() {
		String content = "content";
		DataTable table = DataTable.createBasicInstance(column);

		DataRow row = table.createRow(content).getRows().get(0);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can overwrite row values */
	public void overwriteValue() {
		String content = "content";
		DataTable table = DataTable.createBasicInstance(column);

		DataRow row = table.createRow();
		row.set(column, "other").set(column, content);

		assertEquals(row.get(column), content);
	}

	/** setting more values than columns on the table */
	@Test(expectedExceptions = TooManyColumnsException.class)
	public void tooManyValuesTable() {
		DataTable table = DataTable.createBasicInstance(column);
		table.createRow("", "");
	}

	/** setting more values than columns on a row */
	@Test(expectedExceptions = TooManyColumnsException.class)
	public void tooManyValuesRow() {
		DataTable table = DataTable.createBasicInstance(column);
		table.createRow().setAll("", "");
	}

	/** setting values with too high offset */
	@Test(expectedExceptions = TooManyColumnsException.class)
	public void tooHighOffset() {
		DataTable table = DataTable.createBasicInstance(column);
		table.createRow().setAll(1, "");
	}

	/** not setting all values using setAll */
	public void tooFewValues() {
		DataTable table = DataTable.createBasicInstance(column);
		assertEquals(table.createRow().setAll().getAll(), Arrays.asList((String) null));
	}

	/** retrieve unknown column */
	@Test(expectedExceptions = UnknownColumnException.class)
	public void getUnknownColumn() {
		DataTable.createBasicInstance(column).createRow().get("unknown");
	}

	/** set unknown column */
	@Test(expectedExceptions = UnknownColumnException.class)
	public void setUnknownColumn() {
		DataTable.createBasicInstance(column).createRow().set("unknown", "abc");
	}

	/** convert column to JSON */
	public void columnJson() {
		JsonNode columnJson = new DataColumn(column).toJson();
		assertEquals(columnJson.get("code").asText(), column);
		assertFalse(columnJson.has("format"));
	}

	/** convert formatted column to JSON */
	public void columnFormatJson() {
		String format = "format";
		JsonNode columnJson = new DataColumn(column, format).toJson();
		assertEquals(columnJson.get("code").asText(), column);
		assertEquals(columnJson.get("format").asText(), format);
	}

	/** column format can be changed */
	public void columnChangeFormatJson() {
		String format = "format";
		DataColumn dataColumn = new DataColumn(column, "something");
		dataColumn.setFormat(format);
		assertEquals(dataColumn.getFormat(), format);

		JsonNode columnJson = dataColumn.toJson();
		assertEquals(columnJson.get("code").asText(), column);
		assertEquals(columnJson.get("format").asText(), format);
	}

	/** convert row to JSON */
	public void rowJson() {
		String column2 = "other";
		final String content1 = "content1";
		final String content2 = "content2";

		DataRow row = DataTable.createBasicInstance(column, column2).createRow().setAll(content1, content2);
		ArrayNode rowJson = (ArrayNode) row.toJson();

		assertEquals(rowJson.size(), 2);
		assertEquals(rowJson.get(0).asText(), content1);
		assertEquals(rowJson.get(1).asText(), content2);
	}

	/** convert row to JSON with reserved characters */
	public void rowJsonEscape() {
		String col2 = "col2";
		String col3 = "col3";
		final String val1 = "\"I'm a text with ; and , in it\"";
		final String val2 = "\"I'm more text with ; and , in it\"";
		final String val3 = "nothing special here";

		DataRow row = DataTable.createBasicInstance(column, col2, col3).createRow().setAll(val1, val2, val3);
		ArrayNode rowJson = (ArrayNode) row.toJson();

		assertEquals(rowJson.size(), 3);
		assertEquals(rowJson.get(0).asText(), val1);
		assertEquals(rowJson.get(1).asText(), val2);
		assertEquals(rowJson.get(2).asText(), val3);
	}

	/** convert table columns */
	public void tableColumns() {
		String column2 = "other";
		DataTable table = DataTable.createBasicInstance(column, column2).createRow("content");
		JsonNode tableJson = table.toJson();

		ArrayNode columns = (ArrayNode) tableJson.get("columns");
		assertEquals(columns.size(), 2);
		assertEquals(columns.get(0), new DataColumn(column).toJson());
		assertEquals(columns.get(1), new DataColumn(column2).toJson());
	}

	/** convert table with rows and columns */
	public void tableRows() {
		final String content1 = "content1";
		final String content2 = "content2";

		DataTable table = DataTable.createBasicInstance(column);
		DataRow row1 = table.createRow().setAll(content1);
		DataRow row2 = table.createRow().setAll(content2);

		JsonNode tableJson = table.toJson();
		ArrayNode rows = (ArrayNode) tableJson.get("data");
		assertEquals(rows.get(0), row1.toJson());
		assertEquals(rows.get(1), row2.toJson());
	}

	/** correct default overwrite policy */
	public void defaultOverwritePolicy() {
		DataTable table = DataTable.createBasicInstance(column);
		assertEquals(table.getOverwritePolicy(), OverwritePolicy.deleteNone());
	}

	/** convert table policy */
	public void tablePolicy() {
		DataTable table = DataTable.createBasicInstance(column);
		assertEquals(table.toJson().get("overwrite"), table.getOverwritePolicy().toJson());
	}

	/** overwrite policy can be changed */
	public void setOverwritePolicy() {
		DataTable table = DataTable.createBasicInstance(column);
		table.setOverwritePolicy(OverwritePolicy.deleteAll());
		assertNotEquals(table.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertEquals(table.getOverwritePolicy(), OverwritePolicy.deleteAll());
	}

	/** attribute policy uses unknown attribute */
	@Test(expectedExceptions = UnknownColumnException.class)
	public void attributePolicyMismatch() {
		DataTable.createBasicInstance(column).setOverwritePolicy(OverwritePolicy.byAttributes("other"));
	}

	/** attribute policy uses known attribute */
	public void attributePolicyMatch() {
		OverwritePolicy policy = OverwritePolicy.byAttributes(column);
		DataTable table = DataTable.createBasicInstance(column).setOverwritePolicy(policy);
		assertSame(table.getOverwritePolicy(), policy);
	}

	/** date policy uses unknown attribute */
	@Test(expectedExceptions = UnknownColumnException.class)
	public void datePolicyMismatch() {
		DataTable.createBasicInstance(column).setOverwritePolicy(OverwritePolicy.byDateFrom("other", Calendar.getInstance()));
	}

	/** date policy uses known attribute */
	public void datePolicyMatch() {
		OverwritePolicy policy = OverwritePolicy.byDateFrom(column, Calendar.getInstance());
		DataTable table = DataTable.createBasicInstance(column).setOverwritePolicy(policy);
		assertSame(table.getOverwritePolicy(), policy);
	}
}
