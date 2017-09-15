package com.belladati.sdk.dataset.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.Test;

import com.belladati.sdk.dataset.data.OverwritePolicy;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
import com.belladati.sdk.dataset.source.ImportIntervalUnit;
import com.belladati.sdk.dataset.source.impl.DataSourceImpl;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class DataSourcePendingImportTest extends SDKTest {

	private final String id = "id";
	private final String requestUri = String.format("/api/dataSets/dataSources/%s/schedule", id);
	private final Date date = new Date(123);
	private DataSource dataSource;
	private DataSourcePendingImport pending;

	protected void setupSource() {
		dataSource = new DataSourceImpl(getService(), builder.buildDataSourceNode(id, "", ""));
		pending = dataSource.setupImport(date);
	}

	/** setting only a date from a source */
	public void dateOnlyFromSource() {
		setupSource();
		JsonNode expected = new ObjectMapper().createObjectNode().put("when",
			new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT).format(date));
		assertEquals(dataSource.setupImport(date).toJson(), expected);
	}

	/** setting only a date from service */
	public void dateOnlyFromService() {
		setupSource();
		Date date = new Date(123);

		JsonNode expected = new ObjectMapper().createObjectNode().put("when",
			new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT).format(date));
		assertEquals(getService().setupDataSourceImport(id, date).toJson(), expected);
	}

	/** posting to server sends JSON */
	public void postToServer() {
		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().get("params"), pending.toJson().toString());
			}
		});

		setupSource();
		pending.post();

		server.assertRequestUris(requestUri);
	}

	/** check initial field values */
	public void initialValues() {
		setupSource();
		assertNull(pending.getRepeatInterval());
		assertSame(pending.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertFalse(pending.isOverwriting());
	}

	/** overwrite policy can be set */
	public void setPolicy() {
		setupSource();
		pending.setOverwritePolicy(OverwritePolicy.byAllAttributes());

		assertSame(pending.getOverwritePolicy(), OverwritePolicy.byAllAttributes());
		assertTrue(pending.isOverwriting());
		assertEquals(pending.toJson().get("overwrite"), OverwritePolicy.byAllAttributes().toJson());
	}

	/** policy can be unset after setting */
	public void setUnsetPolicy() {
		setupSource();
		pending.setOverwritePolicy(OverwritePolicy.byAllAttributes());
		pending.setOverwritePolicy(OverwritePolicy.deleteNone());

		assertSame(pending.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertFalse(pending.isOverwriting());

		assertNull(pending.toJson().get("overwrite"));
	}

	/** null policy means delete none */
	public void nullPolicy() {
		setupSource();
		pending.setOverwritePolicy(OverwritePolicy.byAllAttributes());
		pending.setOverwritePolicy(null);

		assertSame(pending.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertFalse(pending.isOverwriting());

		assertNull(pending.toJson().get("overwrite"));
	}

	/** can set an interval */
	@Test(dataProvider = "intervalUnits")
	public void setInterval(String interval, ImportIntervalUnit unit, int factor, int minutes) {
		setupSource();
		pending.setRepeatInterval(unit, factor);

		assertEquals(pending.getRepeatInterval().getMinutes(), minutes);

		assertEquals(pending.toJson().get("repeateInterval").asText(), interval);
		assertNull(pending.toJson().get("customRepeateInterval"));
	}

	/** can set a custom interval */
	public void setMinuteInterval() {
		setupSource();
		pending.setRepeatInterval(ImportIntervalUnit.MINUTE, 30);

		assertEquals(pending.getRepeatInterval().getMinutes(), 30);

		assertEquals(pending.toJson().get("repeateInterval").asText(), "CUSTOM");
		assertEquals(pending.toJson().get("customRepeateInterval").asInt(), 30);
	}

	/** can set a new interval as custom */
	public void setNewInterval() {
		setupSource();
		pending.setRepeatInterval(ImportIntervalUnit.HOUR, 3);

		assertEquals(pending.getRepeatInterval().getMinutes(), 180);

		assertEquals(pending.toJson().get("repeateInterval").asText(), "CUSTOM");
		assertEquals(pending.toJson().get("customRepeateInterval").asInt(), 180);
	}

	/** unset interval by setting negative */
	public void negativeInterval() {
		setupSource();
		pending.setRepeatInterval(ImportIntervalUnit.MINUTE, 10).setRepeatInterval(ImportIntervalUnit.MINUTE, -10);

		assertNull(pending.getRepeatInterval());

		assertNull(pending.toJson().get("repeateInterval"));
		assertNull(pending.toJson().get("customRepeateInterval"));
	}

	/** unset interval by setting zero */
	public void zeroInterval() {
		setupSource();
		pending.setRepeatInterval(ImportIntervalUnit.MINUTE, 10).setRepeatInterval(ImportIntervalUnit.MINUTE, 0);

		assertNull(pending.getRepeatInterval());

		assertNull(pending.toJson().get("repeateInterval"));
		assertNull(pending.toJson().get("customRepeateInterval"));
	}

	/** unset interval by setting zero */
	public void nullUnitInterval() {
		setupSource();
		pending.setRepeatInterval(ImportIntervalUnit.MINUTE, 10).setRepeatInterval(null, 10);

		assertNull(pending.getRepeatInterval());

		assertNull(pending.toJson().get("repeateInterval"));
		assertNull(pending.toJson().get("customRepeateInterval"));
	}

	/** can't change policy after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void setPolicyAfterPost() {
		server.register(requestUri, "");
		setupSource();
		pending.post();
		pending.setOverwritePolicy(OverwritePolicy.deleteNone());
	}

	/** can't change interval after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void setIntervalAfterPost() {
		server.register(requestUri, "");
		setupSource();
		pending.post();
		pending.setRepeatInterval(ImportIntervalUnit.MINUTE, 10);
	}

	/** can't post again after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void postAfterPost() {
		server.register(requestUri, "");
		setupSource();
		pending.post();
		pending.post();
	}
}
