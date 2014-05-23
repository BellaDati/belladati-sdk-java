package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.belladati.sdk.dataset.data.DataRow;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.exception.dataset.data.NoColumnsException;
import com.belladati.sdk.exception.dataset.data.TooManyColumnsException;
import com.belladati.sdk.exception.dataset.data.UnknownColumnException;

@Test
public class DataSetDataTest extends SDKTest {

	private final String column = "column";

	/** table must have columns */
	@Test(expectedExceptions = NoColumnsException.class)
	public void noColumns() {
		new DataTable(Collections.<String> emptyList());
	}

	/** create with single column */
	public void column() {
		DataTable table = new DataTable(column);
		assertEquals(table.getColumns(), Arrays.asList(column));
		assertEquals(table.createRow().getColumns(), Arrays.asList(column));
	}

	/** create with multiple columns passed as strings */
	public void columnsStrings() {
		String other = "other";
		DataTable table = new DataTable(column, other);
		assertEquals(table.getColumns(), Arrays.asList(column, other));
		assertEquals(table.createRow().getColumns(), Arrays.asList(column, other));
	}

	/** create with multiple columns passed as list */
	public void columnsList() {
		String other = "other";
		DataTable table = new DataTable(Arrays.asList(column, other));
		assertEquals(table.getColumns(), Arrays.asList(column, other));
		assertEquals(table.createRow().getColumns(), Arrays.asList(column, other));
	}

	/** create rows */
	public void createRows() {
		DataTable table = new DataTable(column);
		assertEquals(table.getRows().size(), 0);
		table.createRow();
		assertEquals(table.getRows().size(), 1);
		table.createRow("abc");
		assertEquals(table.getRows().size(), 2);
	}

	/** returned row list cannot be modified */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void rowListImmutable() {
		DataTable table = new DataTable(column);
		List<DataRow> rows = table.getRows();
		DataRow row = table.createRow();
		assertEquals(rows.size(), 0);
		rows.add(row);
	}

	/** returned column list cannot be modified */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void columnListImmutable() {
		new DataTable(column).getColumns().add("abc");
	}

	/** new rows are empty */
	public void rowEmpty() {
		DataTable table = new DataTable(column);
		DataRow row = table.createRow();

		assertNull(row.get(column));
		assertEquals(row.getAll(), Arrays.asList((String) null));
	}

	/** can set row content through set method */
	public void rowContentSet() {
		String content = "content";
		DataTable table = new DataTable(column);

		DataRow row = table.createRow();
		row.set(column, content);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can set row content through set method */
	public void rowContentSetAll() {
		String content = "content";
		DataTable table = new DataTable(column);

		DataRow row = table.createRow();
		row.setAll(content);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can set row content through setAll method with offset */
	public void rowContentSetOffset() {
		String content = "content";
		DataTable table = new DataTable(column);

		DataRow row = table.createRow();
		row.setAll(0, content);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can set row content through creation */
	public void rowContentCreate() {
		String content = "content";
		DataTable table = new DataTable(column);

		DataRow row = table.createRow(content).getRows().get(0);

		assertEquals(row.get(column), content);
		assertEquals(row.getAll(), Arrays.asList(content));
	}

	/** can overwrite row values */
	public void overwriteValue() {
		String content = "content";
		DataTable table = new DataTable(column);

		DataRow row = table.createRow();
		row.set(column, "other").set(column, content);

		assertEquals(row.get(column), content);
	}

	/** setting more values than columns on the table */
	@Test(expectedExceptions = TooManyColumnsException.class)
	public void tooManyValuesTable() {
		DataTable table = new DataTable(column);
		table.createRow("", "");
	}

	/** setting more values than columns on a row */
	@Test(expectedExceptions = TooManyColumnsException.class)
	public void tooManyValuesRow() {
		DataTable table = new DataTable(column);
		table.createRow().setAll("", "");
	}

	/** setting values with too high offset */
	@Test(expectedExceptions = TooManyColumnsException.class)
	public void tooHighOffset() {
		DataTable table = new DataTable(column);
		table.createRow().setAll(1, "");
	}

	/** not setting all values using setAll */
	public void tooFewValues() {
		DataTable table = new DataTable(column);
		assertEquals(table.createRow().setAll().getAll(), Arrays.asList((String) null));
	}

	/** retrieve unknown column */
	@Test(expectedExceptions = UnknownColumnException.class)
	public void getUnknownColumn() {
		new DataTable(column).createRow().get("unknown");
	}

	/** set unknown column */
	@Test(expectedExceptions = UnknownColumnException.class)
	public void setUnknownColumn() {
		new DataTable(column).createRow().set("unknown", "abc");
	}
}
