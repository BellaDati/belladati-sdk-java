package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to reports.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class ReportsTest extends SDKTest {

	private final String reportsUri = "/api/reports";

	private final String id = "123";
	private final String name = "report name";
	private final String description = "report description";
	private final String owner = "report owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";

	/** Regular report info data is loaded correctly. */
	public void loadReportInfo() {
		PaginatedList<ReportInfo> reportInfos = service.getReportInfo();

		registerSingleReport(builder.buildReportNode(id, name, description, owner, lastChange));

		reportInfos.load();
		server.assertRequestUris(reportsUri);
		assertEquals(reportInfos.size(), 1);

		ReportInfo info = reportInfos.get(0);
		assertEquals(info.getId(), id);
		assertEquals(info.getName(), name);
		assertEquals(info.getDescription(), description);
		assertEquals(info.getOwnerName(), owner);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(info.getLastChange(), expectedChange.getTime());

		assertEquals(info.toString(), name);
	}

	/** Report description may be null. */
	public void infoNullDescription() {
		registerSingleReport(builder.buildReportNode(id, name, null, owner, lastChange));

		assertEquals(service.getReportInfo().load().get(0).getDescription(), "");
	}

	/** Report description may be missing. */
	public void infoWithoutDescription() {
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.remove("description");
		registerSingleReport(node);

		assertEquals(service.getReportInfo().load().get(0).getDescription(), "");
	}

	/** Report last change may be null. */
	public void infoNullLastChange() {
		registerSingleReport(builder.buildReportNode(id, name, description, owner, null));

		assertNull(service.getReportInfo().load().get(0).getLastChange());
	}

	/** Report last change may be missing. */
	public void infoWithoutLastChange() {
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.remove("lastChange");
		registerSingleReport(node);

		assertNull(service.getReportInfo().load().get(0).getLastChange());
	}

	/** Report last change may be invalid format. */
	public void infoInvalidLastChange() {
		registerSingleReport(builder.buildReportNode(id, name, description, owner, "something invalid"));

		assertNull(service.getReportInfo().load().get(0).getLastChange());
	}

	/** Getting a report info list multiple times returns the same list. */
	public void reportInfoListSame() {
		assertSame(service.getReportInfo(), service.getReportInfo());
	}

	/** Individual report can be loaded by ID through service. */
	public void loadReport() {
		server.register(reportsUri + "/" + id, builder.buildReportNode(id, name, description, owner, lastChange).toString());

		Report report = service.loadReport(id);
		server.assertRequestUris(reportsUri + "/" + id);

		assertEquals(report.getId(), id);
		assertEquals(report.getName(), name);
		assertEquals(report.getDescription(), description);
		assertEquals(report.getOwnerName(), owner);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(report.getLastChange(), expectedChange.getTime());

		assertEquals(report.getAttributes(), Collections.emptyList());
		assertEquals(report.getViews(), Collections.emptyList());

		assertEquals(report.toString(), name);
	}

	/** Report description may be null. */
	public void reportNullDescription() {
		server.register(reportsUri + "/" + id, builder.buildReportNode(id, name, null, owner, lastChange).toString());
		Report report = service.loadReport(id);

		assertEquals(report.getDescription(), "");
	}

	/** Report description may be missing. */
	public void reportWithoutDescription() {
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.remove("description");
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertEquals(report.getDescription(), "");
	}

	/** Report last change may be null. */
	public void reportNullLastChange() {
		server.register(reportsUri + "/" + id, builder.buildReportNode(id, name, description, owner, null).toString());
		Report report = service.loadReport(id);

		assertNull(report.getLastChange());
	}

	/** Report last change may be missing. */
	public void reportWithoutLastChange() {
		ObjectNode node = builder.buildReportNode(id, name, description, owner, lastChange);
		node.remove("lastChange");
		server.register(reportsUri + "/" + id, node.toString());
		Report report = service.loadReport(id);

		assertNull(report.getLastChange());
	}

	/** Report last change may be invalid format. */
	public void reportInvalidLastChange() {
		server.register(reportsUri + "/" + id, builder.buildReportNode(id, name, description, owner, "something invalid")
			.toString());
		Report report = service.loadReport(id);

		assertNull(report.getLastChange());
	}

	/** Report can be loaded from a report info object. */
	public void loadReportFromInfo() {
		String idRep = "id2";
		String nameRep = "name2";
		String descRep = "desc2";
		String ownerRep = "owner2";
		String lastChangeRep = "Tue, 17 Apr 2012 11:18:27 GMT";

		registerSingleReport(builder.buildReportNode(id, name, description, owner, lastChange));
		server.register(reportsUri + "/" + id, builder.buildReportNode(idRep, nameRep, descRep, ownerRep, lastChangeRep)
			.toString());

		Report report = service.getReportInfo().load().get(0).loadDetails();

		assertEquals(report.getId(), idRep);
		assertEquals(report.getName(), nameRep);
		assertEquals(report.getDescription(), descRep);
		assertEquals(report.getOwnerName(), ownerRep);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 17, 11, 18, 27);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(report.getLastChange(), expectedChange.getTime());

		assertEquals(report.getViews(), Collections.emptyList());
	}

	/** Can load a report's thumbnail from service. */
	public void loadThumbnailFromService() throws IOException {
		server.register(reportsUri + "/" + id + "/thumbnail", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
			}
		});

		BufferedImage thumbnail = (BufferedImage) service.loadReportThumbnail(id);

		server.assertRequestUris(reportsUri + "/" + id + "/thumbnail");

		assertEquals(thumbnail.getWidth(), 56);
		assertEquals(thumbnail.getHeight(), 46);
	}

	/** Can load a report's thumbnail from info. */
	public void loadThumbnailFromReportInfo() throws IOException {
		ReportInfo reportInfo = new ReportInfoImpl(service, builder.buildReportNode(id, name, description, owner, lastChange));

		server.register(reportsUri + "/" + id + "/thumbnail", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("belladati.png")));
			}
		});

		BufferedImage thumbnail = (BufferedImage) reportInfo.loadThumbnail();

		server.assertRequestUris(reportsUri + "/" + id + "/thumbnail");

		assertEquals(thumbnail.getWidth(), 56);
		assertEquals(thumbnail.getHeight(), 46);
	}

	/** Invalid thumbnail results in exception. */
	@Test(expectedExceptions = IOException.class)
	public void loadInvalidThumbnail() throws IOException {
		ReportInfo reportInfo = new ReportInfoImpl(service, builder.buildReportNode(id, name, description, owner, lastChange));

		server.register(reportsUri + "/" + id + "/thumbnail", "not a thumbnail image");

		reportInfo.loadThumbnail();
	}

	/** equals/hashcode for report info */
	public void reportInfoEquality() {
		ReportInfo r1 = new ReportInfoImpl(service, builder.buildReportNode(id, name, description, owner, lastChange));
		ReportInfo r2 = new ReportInfoImpl(service, builder.buildReportNode(id, "", "", "", null));
		ReportInfo r3 = new ReportInfoImpl(service, builder.buildReportNode("otherId", name, description, owner, lastChange));

		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());

		assertNotEquals(r1, r3);
	}

	/** equals/hashcode for report */
	public void reportEquality() {
		Report r1 = new ReportImpl(service, builder.buildReportNode(id, name, description, owner, lastChange));
		Report r2 = new ReportImpl(service, builder.buildReportNode(id, "", "", "", null));
		Report r3 = new ReportImpl(service, builder.buildReportNode("otherId", name, description, owner, lastChange));

		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());

		assertNotEquals(r1, r3);
	}

	/** underlying data sets are loaded for reports */
	public void reportDataSet() {
		String idDS = "id2";
		String nameDS = "name2";
		String descDS = "desc2";
		String ownerDS = "owner2";
		String lastChangeDS = "Tue, 17 Apr 2012 11:18:27 GMT";
		ObjectNode reportNode = builder.buildReportNode(id, name, description, owner, lastChange);
		ObjectNode dataSetNode = builder.buildDataSetNode(idDS, nameDS, descDS, ownerDS, lastChangeDS);
		((ObjectNode) reportNode.get("dataSet")).putAll(dataSetNode);

		server.register(reportsUri + "/" + id, reportNode.toString());
		Report report = service.loadReport(id);

		DataSetInfo dataSet = report.getDataSet();

		assertEquals(dataSet.getId(), idDS);
		assertEquals(dataSet.getName(), nameDS);
		assertEquals(dataSet.getDescription(), descDS);
		assertEquals(dataSet.getOwnerName(), ownerDS);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 17, 11, 18, 27);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(dataSet.getLastChange(), expectedChange.getTime());
	}

	/** incomplete data set information is ignored */
	public void incompleteDataSet() {
		server.register(reportsUri + "/" + id, builder.buildReportNode(id, name, description, owner, lastChange).toString());
		assertNull(service.loadReport(id).getDataSet());
	}

	/** missing data set information is ignored */
	public void missingDataSet() {
		ObjectNode reportNode = builder.buildReportNode(id, name, description, owner, lastChange);
		reportNode.remove("dataSet");
		server.register(reportsUri + "/" + id, reportNode.toString());
		assertNull(service.loadReport(id).getDataSet());
	}

	/** data set details can be loaded from a report's data set info */
	public void reportDataSetLoadDetails() {
		ObjectNode reportNode = builder.buildReportNode(id, name, description, owner, lastChange);
		ObjectNode dataSetNode = builder.buildDataSetNode(id, name, description, owner, lastChange);
		((ObjectNode) reportNode.get("dataSet")).putAll(dataSetNode);

		server.register(reportsUri + "/" + id, reportNode.toString());
		server.register("/api/dataSets/" + id, dataSetNode.toString());
		Report report = service.loadReport(id);

		DataSetInfo dataSet = report.getDataSet();
		assertNotNull(dataSet.loadDetails());

		server.assertRequestUris(reportsUri + "/" + id, "/api/dataSets/" + id);
	}

	/**
	 * Tells the server to return the specified node as the only report node in
	 * a <tt>reports</tt> array.
	 * 
	 * @param node the node to return
	 */
	private void registerSingleReport(JsonNode node) {
		server.registerPaginatedItem(reportsUri, "reports", node);
	}
}
