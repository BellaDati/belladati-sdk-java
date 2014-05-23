package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.exception.dataset.data.UnknownServerColumnException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.belladati.sdk.test.TestRequestHandler;

@Test
public class DataImportTest extends SDKTest {

	private final String id = "id";
	private final String column = "column";

	/** the correct URL is used for uploading */
	public void uploadURL() {
		String url = "/api/import/csv/" + id + "/" + column;
		server.register(url, "");

		service.uploadData(id, new DataTable(column).createRow("content"));

		server.assertRequestUris(url);
	}

	/** the correct URL is used for uploading multiple columns */
	public void uploadURLMultipleColumns() {
		String column2 = "other";
		String url = "/api/import/csv/" + id + "/" + column + ";" + column2;
		server.register(url, "");

		service.uploadData(id, new DataTable(column, column2).createRow("content"));

		server.assertRequestUris(url);
	}

	/** nothing happens when uploading empty data */
	public void uploadNoData() {
		service.uploadData(id, new DataTable(column));

		server.assertRequestUris();
	}

	/** actual data is sent */
	public void uploadRow() {
		String content = "content";
		String url = "/api/import/csv/" + id + "/" + column;
		registerRequestCheck(url, "\"" + content + "\"");

		service.uploadData(id, new DataTable(column).createRow(content));

		server.assertRequestUris(url);
	}

	/** multiple rows are separated by line breaks */
	public void uploadRows() {
		String content = "content";
		String content2 = "content2";
		String url = "/api/import/csv/" + id + "/" + column;
		registerRequestCheck(url, "\"" + content + "\"\n\"" + content2 + "\"");

		service.uploadData(id, new DataTable(column).createRow(content).createRow(content2));

		server.assertRequestUris(url);
	}

	/** data is sent in CSV */
	public void csvEscape() {
		String col2 = "col2";
		String col3 = "col3";
		String val1 = "\"I'm a text with ; and , in it\"";
		String val2 = "\"I'm more text with ; and , in it\"";
		String val3 = "nothing special here";
		String url = "/api/import/csv/" + id + "/" + column + ";" + col2 + ";" + col3;

		registerRequestCheck(url,
			"\"" + val1.replace("\"", "\"\"") + "\";\"" + val2.replace("\"", "\"\"") + "\";\"" + val3.replace("\"", "\"\"")
				+ "\"");

		service.uploadData(id, new DataTable(column, col2, col3).createRow(val1, val2, val3));

		server.assertRequestUris(url);
	}

	/** non-existing column server error */
	public void nonExistingColumn() {
		String url = "/api/import/csv/" + id + "/" + column;
		server.registerError(url, 400, "Indicator/attribute '" + column + "' doesn't exist");

		try {
			service.uploadData(id, new DataTable(column).createRow("content"));
			fail("No exception thrown");
		} catch (UnknownServerColumnException e) {
			assertEquals(e.getId(), id);
			assertEquals(e.getColumn(), column);
		}

		server.assertRequestUris(url);
	}

	/** unrelated server error */
	@Test(expectedExceptions = UnexpectedResponseException.class)
	public void otherError() {
		String url = "/api/import/csv/" + id + "/" + column;
		server.registerError(url, 400, "something else");

		service.uploadData(id, new DataTable(column).createRow("content"));
	}

	private void registerRequestCheck(final String url, final String content) {
		server.register(url, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				HttpEntity entity = ((BasicHttpEntityEnclosingRequest) holder.request).getEntity();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				entity.writeTo(baos);
				byte[] bytes = baos.toByteArray();
				baos.close();
				assertEquals(new String(bytes), content);
			}
		});
	}

}
