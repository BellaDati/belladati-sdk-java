package com.belladati.sdk.dataset.impl;

import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.exception.dataset.data.UnknownServerColumnException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Test
public class DataImportTest extends SDKTest {

	private final String id = "id";
	private final String column = "column";
	private final String url = "/api/import/" + id;

	/** JSON data is sent correctly */
	public void uploadJson() {
		final DataTable table = DataTable.createBasicInstance(column).createRow("content");

		server.register(url, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				HttpEntity entity = holder.request.getEntity();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				entity.writeTo(baos);
				JsonNode json = new ObjectMapper().readTree(baos.toByteArray());
				baos.close();

				assertEquals(json, table.toJson());
			}
		});

		getService().uploadData(id, table);

		server.assertRequestUris(url);
	}

	/** can import data from a data set */
	public void uploadFromDataSet() {
		final DataTable table = DataTable.createBasicInstance(column).createRow("content");

		server.register(url, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				HttpEntity entity = holder.request.getEntity();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				entity.writeTo(baos);
				JsonNode json = new ObjectMapper().readTree(baos.toByteArray());
				baos.close();

				assertEquals(json, table.toJson());
			}
		});

		DataSet dataSet = new DataSetImpl(getService(), builder.buildDataSetNode(id, "", "", "", ""));
		dataSet.uploadData(table);

		server.assertRequestUris(url);
	}

	/** nothing happens when uploading empty data */
	public void uploadNoData() {
		getService().uploadData(id, DataTable.createBasicInstance(column));

		server.assertRequestUris();
	}

	/** non-existing column server error */
	public void nonExistingColumn() {
		server.registerError(url, 400, "Indicator/attribute '" + column + "' doesn't exist");

		try {
			getService().uploadData(id, DataTable.createBasicInstance(column).createRow("content"));
			fail("No exception thrown");
		} catch (UnknownServerColumnException e) {
			assertEquals(e.getId(), id);
			assertEquals(e.getColumn(), column);
		}
	}

	/** unrelated server error */
	@Test(expectedExceptions = UnexpectedResponseException.class)
	public void otherError() {
		server.registerError(url, 400, "something else");

		getService().uploadData(id, DataTable.createBasicInstance(column).createRow("content"));
	}
}
