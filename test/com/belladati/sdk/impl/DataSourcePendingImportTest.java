package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.data.OverwritePolicy;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
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

	@BeforeMethod(alwaysRun = true)
	protected void setupSource() throws Exception {
		dataSource = new DataSourceImpl(service, builder.buildDataSourceNode(id, "", ""));
		pending = dataSource.setupImport(date);
	}

	/** setting only a date from a source */
	public void dateOnlyFromSource() {
		JsonNode expected = new ObjectMapper().createObjectNode().put("when",
			new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT).format(date));
		assertEquals(dataSource.setupImport(date).toJson(), expected);
	}

	/** setting only a date from service */
	public void dateOnlyFromService() {
		Date date = new Date(123);

		JsonNode expected = new ObjectMapper().createObjectNode().put("when",
			new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT).format(date));
		assertEquals(service.setupDataSourceImport(id, date).toJson(), expected);
	}

	/** posting to server sends JSON */
	public void postToServer() {
		server.register(requestUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters().get("params"), pending.toJson().toString());
			}
		});
		pending.post();

		server.assertRequestUris(requestUri);
	}

	/** check initial field values */
	public void initialValues() {
		assertNull(pending.getRepeatInterval());
		assertSame(pending.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertFalse(pending.isOverwriting());
	}

	/** overwrite policy can be set */
	public void setPolicy() {
		pending.setOverwritePolicy(OverwritePolicy.byAllAttributes());

		assertSame(pending.getOverwritePolicy(), OverwritePolicy.byAllAttributes());
		assertTrue(pending.isOverwriting());
		assertEquals(pending.toJson().get("overwrite"), OverwritePolicy.byAllAttributes().toJson());
	}

	/** policy can be unset after setting */
	public void setUnsetPolicy() {
		pending.setOverwritePolicy(OverwritePolicy.byAllAttributes());
		pending.setOverwritePolicy(OverwritePolicy.deleteNone());

		assertSame(pending.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertFalse(pending.isOverwriting());

		assertNull(pending.toJson().get("overwrite"));
	}

	/** null policy means delete none */
	public void nullPolicy() {
		pending.setOverwritePolicy(OverwritePolicy.byAllAttributes());
		pending.setOverwritePolicy(null);

		assertSame(pending.getOverwritePolicy(), OverwritePolicy.deleteNone());
		assertFalse(pending.isOverwriting());

		assertNull(pending.toJson().get("overwrite"));
	}

	/** can set an interval */
	public void setInterval() {
		int minutes = 180;
		pending.setRepeatInterval(minutes);

		assertEquals(pending.getRepeatInterval().getMinutes(), minutes);

		assertEquals(pending.toJson().get("repeateInterval").asText(), "CUSTOM");
		assertEquals(pending.toJson().get("customRepeateInterval").asInt(), 180);
	}

	/** unset interval by setting negative */
	public void negativeInterval() {
		pending.setRepeatInterval(10).setRepeatInterval(-10);

		assertNull(pending.getRepeatInterval());

		assertNull(pending.toJson().get("repeateInterval"));
		assertNull(pending.toJson().get("customRepeateInterval"));
	}

	/** unset interval by setting zero */
	public void zeroInterval() {
		pending.setRepeatInterval(10).setRepeatInterval(0);

		assertNull(pending.getRepeatInterval());

		assertNull(pending.toJson().get("repeateInterval"));
		assertNull(pending.toJson().get("customRepeateInterval"));
	}

	/** can't change policy after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void setPolicyAfterPost() {
		server.register(requestUri, "");
		pending.post();
		pending.setOverwritePolicy(OverwritePolicy.deleteNone());
	}

	/** can't change interval after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void setIntervalAfterPost() {
		server.register(requestUri, "");
		pending.post();
		pending.setRepeatInterval(10);
	}

	/** can't post again after posting */
	@Test(expectedExceptions = IllegalStateException.class)
	public void postAfterPost() {
		server.register(requestUri, "");
		pending.post();
		pending.post();
	}
}
