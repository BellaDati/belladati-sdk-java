package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.export.PageStorage;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.view.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests storing reports and their contents.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class StoreReportsTest extends SDKTest {

	private final String reportsUri = "/api/reports/";
	private final String viewsUri = "/api/reports/views/";

	private final String id = "123";
	private final String locName = "english name";
	private final String name = "report name";
	private final String description = "report description";
	private final String owner = "report owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";

	private final String viewId = "viewId";
	private final String viewName = "viewName";

	/** store a report */
	public void storeReport() throws IOException {
		ObjectNode reportJson = builder.buildReportNode(id, name, description, owner, lastChange);
		reportJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		Report report = new ReportImpl(service, reportJson);

		server.register(reportsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		assertReport(new PageStorage().storeReport(report));

		server.assertRequestUris(reportsUri + id + "/thumbnail");
	}

	/** store a report info */
	public void storeReportInfo() throws IOException {
		ObjectNode reportJson = builder.buildReportNode(id, name, description, owner, lastChange);
		reportJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		ReportInfo reportInfo = new ReportInfoImpl(service, reportJson);

		server.register(reportsUri + id, reportJson.toString());
		server.register(reportsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		assertReport(new PageStorage().storeReport(reportInfo));

		server.assertRequestUris(reportsUri + id, reportsUri + id + "/thumbnail");
	}

	/** serialize/deserialize a stored report */
	public void serializeReport() throws IOException, ClassNotFoundException {
		ObjectNode reportJson = builder.buildReportNode(id, name, description, owner, lastChange);
		reportJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		Report report = new ReportImpl(service, reportJson);

		server.register(reportsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));

		ReportInfo stored = new PageStorage().storeReport(report);
		ReportInfo newStored = (ReportInfo) serializeDeserialize(stored);

		assertReport(newStored);

		server.assertRequestUris(reportsUri + id + "/thumbnail");
	}

	/** serialize/deserialize a stored report */
	public void serializeReportNoThumbnail() throws IOException, ClassNotFoundException {
		ObjectNode reportJson = builder.buildReportNode(id, name, description, owner, lastChange);
		reportJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		Report report = new ReportImpl(service, reportJson);

		ReportInfo stored = new PageStorage().storeReport(report);
		ReportInfo newStored = (ReportInfo) serializeDeserialize(stored);

		assertReport(newStored, false);

		server.assertRequestUris(reportsUri + id + "/thumbnail");
	}

	private void assertReport(ReportInfo stored) throws IOException {
		assertReport(stored, true);
	}

	private void assertReport(ReportInfo stored, boolean hasThumbnail) throws IOException {
		assertEquals(stored.getId(), id);
		assertEquals(stored.getName(), name);
		assertEquals(stored.getName(Locale.ENGLISH), locName);
		assertEquals(stored.getDescription(), description);
		assertEquals(stored.getOwnerName(), owner);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(stored.getLastChange(), expectedChange.getTime());
		assertTrue(stored.hasLocalization(Locale.ENGLISH));

		assertTrue(stored.getComments().isEmpty());
		stored.postComment("text"); // shouldn't crash

		BufferedImage thumbnail;
		if (hasThumbnail) {
			thumbnail = (BufferedImage) stored.loadThumbnail();
			assertEquals(thumbnail.getWidth(), 56);
			assertEquals(thumbnail.getHeight(), 46);
		} else {
			assertNull(stored.loadThumbnail());
		}

		assertEquals(stored.loadDetails().getId(), id);
		assertEquals(stored.loadDetails().getName(), name);
		assertEquals(stored.loadDetails().getName(Locale.ENGLISH), locName);
		assertEquals(stored.loadDetails().getDescription(), description);
		assertEquals(stored.loadDetails().getOwnerName(), owner);
		assertEquals(stored.loadDetails().getLastChange(), expectedChange.getTime());
		assertTrue(stored.loadDetails().hasLocalization(Locale.ENGLISH));
		assertTrue(stored.loadDetails().getAttributes().isEmpty());
		assertNull(stored.loadDetails().getDataSet());

		assertTrue(stored.loadDetails().getComments().isEmpty());
		stored.loadDetails().postComment("text"); // shouldn't crash

		if (hasThumbnail) {
			thumbnail = (BufferedImage) stored.loadDetails().loadThumbnail();
			assertEquals(thumbnail.getWidth(), 56);
			assertEquals(thumbnail.getHeight(), 46);
		} else {
			assertNull(stored.loadDetails().loadThumbnail());
		}
	}

	/** store a report containing views */
	public void storeReportView() throws IOException, ClassNotFoundException {
		ObjectNode reportJson = builder.buildReportNode(id, name, description, owner, lastChange);
		ObjectNode viewJson = new ObjectMapper().createObjectNode().put("key", "value");
		reportJson.put("localization", new ObjectMapper().createObjectNode().put("en", locName));
		ReportInfo reportInfo = new ReportInfoImpl(service, reportJson);

		ArrayNode views = new ObjectMapper().createArrayNode().add(builder.buildViewNode(viewId, viewName, "chart"));
		reportJson.put("views", views);

		server.register(reportsUri + id, reportJson.toString());
		server.register(reportsUri + id + "/thumbnail", new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
		server.register(viewsUri + viewId + "/chart", viewJson.toString());

		ReportInfo stored = new PageStorage().storeReport(reportInfo);
		ReportInfo newStored = (ReportInfo) serializeDeserialize(stored);

		server.assertRequestUris(reportsUri + id, reportsUri + id + "/thumbnail", viewsUri + viewId + "/chart");
		server.resetRequestUris();

		assertEquals(newStored.loadDetails().getViews().size(), 1);
		View view = newStored.loadDetails().getViews().get(0);
		assertEquals(view.getId(), viewId);
		assertEquals(view.loadContent(), viewJson);

		server.assertRequestUris();
	}
}
