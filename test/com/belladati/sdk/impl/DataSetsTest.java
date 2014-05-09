package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import org.testng.annotations.Test;

import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior related to data sets.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class DataSetsTest extends SDKTest {

	private final String dataSetsUri = "/api/dataSets";

	private final String id = "123";
	private final String name = "data set name";
	private final String description = "data set description";
	private final String owner = "data set owner";
	private final String lastChange = "Mon, 16 Apr 2012 10:17:26 GMT";

	/** Regular data set info data is loaded correctly. */
	public void loadDataSetInfo() {
		PaginatedList<DataSetInfo> dataSetInfos = service.getDataSetInfo();

		registerSingleDataSet(builder.buildDataSetNode(id, name, description, owner, lastChange));

		dataSetInfos.load();
		server.assertRequestUris(dataSetsUri);
		assertEquals(dataSetInfos.size(), 1);

		DataSetInfo info = dataSetInfos.get(0);
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

	/** Data set description may be null. */
	public void infoNullDescription() {
		registerSingleDataSet(builder.buildDataSetNode(id, name, null, owner, lastChange));

		assertEquals(service.getDataSetInfo().load().get(0).getDescription(), "");
	}

	/** Data set description may be missing. */
	public void infoWithoutDescription() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		node.remove("description");
		registerSingleDataSet(node);

		assertEquals(service.getDataSetInfo().load().get(0).getDescription(), "");
	}

	/** Data set last change may be null. */
	public void infoNullLastChange() {
		registerSingleDataSet(builder.buildDataSetNode(id, name, description, owner, null));

		assertNull(service.getDataSetInfo().load().get(0).getLastChange());
	}

	/** Data set last change may be missing. */
	public void infoWithoutLastChange() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		node.remove("lastChange");
		registerSingleDataSet(node);

		assertNull(service.getDataSetInfo().load().get(0).getLastChange());
	}

	/** Data set last change may be invalid format. */
	public void infoInvalidLastChange() {
		registerSingleDataSet(builder.buildDataSetNode(id, name, description, owner, "something invalid"));

		assertNull(service.getDataSetInfo().load().get(0).getLastChange());
	}

	/** Getting a data set info list multiple times returns the same list. */
	public void dataSetInfoListSame() {
		assertSame(service.getDataSetInfo(), service.getDataSetInfo());
	}

	/** Individual data set can be loaded by ID through service. */
	public void loadDataSet() {
		server.register(dataSetsUri + "/" + id, builder.buildDataSetNode(id, name, description, owner, lastChange).toString());

		DataSet dataSet = service.loadDataSet(id);
		server.assertRequestUris(dataSetsUri + "/" + id);

		assertEquals(dataSet.getId(), id);
		assertEquals(dataSet.getName(), name);
		assertEquals(dataSet.getDescription(), description);
		assertEquals(dataSet.getOwnerName(), owner);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 16, 10, 17, 26);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(dataSet.getLastChange(), expectedChange.getTime());

		assertEquals(dataSet.getAttributes(), Collections.emptyList());
		assertEquals(dataSet.getIndicators(), Collections.emptyList());
		assertEquals(dataSet.getReports(), Collections.emptyList());

		assertEquals(dataSet.toString(), name);
	}

	/** Data set description may be null. */
	public void dataSetNullDescription() {
		server.register(dataSetsUri + "/" + id, builder.buildDataSetNode(id, name, null, owner, lastChange).toString());
		DataSet dataSet = service.loadDataSet(id);

		assertEquals(dataSet.getDescription(), "");
	}

	/** Data set description may be missing. */
	public void dataSetWithoutDescription() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		node.remove("description");
		server.register(dataSetsUri + "/" + id, node.toString());
		DataSet dataSet = service.loadDataSet(id);

		assertEquals(dataSet.getDescription(), "");
	}

	/** Data set last change may be null. */
	public void dataSetNullLastChange() {
		server.register(dataSetsUri + "/" + id, builder.buildDataSetNode(id, name, description, owner, null).toString());
		DataSet dataSet = service.loadDataSet(id);

		assertNull(dataSet.getLastChange());
	}

	/** Data set last change may be missing. */
	public void dataSetWithoutLastChange() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		node.remove("lastChange");
		server.register(dataSetsUri + "/" + id, node.toString());
		DataSet dataSet = service.loadDataSet(id);

		assertNull(dataSet.getLastChange());
	}

	/** Data set last change may be invalid format. */
	public void dataSetInvalidLastChange() {
		server.register(dataSetsUri + "/" + id, builder.buildDataSetNode(id, name, description, owner, "something invalid")
			.toString());
		DataSet dataSet = service.loadDataSet(id);

		assertNull(dataSet.getLastChange());
	}

	/** Data set can be loaded from a data set info object. */
	public void loadDataSetFromInfo() {
		String idDS = "id2";
		String nameDS = "name2";
		String descDS = "desc2";
		String ownerDS = "owner2";
		String lastChangeDS = "Tue, 17 Apr 2012 11:18:27 GMT";

		registerSingleDataSet(builder.buildDataSetNode(id, name, description, owner, lastChange));
		server.register(dataSetsUri + "/" + id, builder.buildDataSetNode(idDS, nameDS, descDS, ownerDS, lastChangeDS).toString());

		DataSet dataSet = service.getDataSetInfo().load().get(0).loadDetails();

		assertEquals(dataSet.getId(), idDS);
		assertEquals(dataSet.getName(), nameDS);
		assertEquals(dataSet.getDescription(), descDS);
		assertEquals(dataSet.getOwnerName(), ownerDS);
		Calendar expectedChange = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedChange.set(2012, 3, 17, 11, 18, 27);
		expectedChange.set(Calendar.MILLISECOND, 0);
		assertEquals(dataSet.getLastChange(), expectedChange.getTime());

		assertEquals(dataSet.getAttributes(), Collections.emptyList());
		assertEquals(dataSet.getIndicators(), Collections.emptyList());
		assertEquals(dataSet.getReports(), Collections.emptyList());
	}

	/** equals/hashcode for data set info */
	public void dataSetInfoEquality() {
		DataSetInfo d1 = new DataSetInfoImpl(service, builder.buildDataSetNode(id, name, description, owner, lastChange));
		DataSetInfo d2 = new DataSetInfoImpl(service, builder.buildDataSetNode(id, "", "", "", null));
		DataSetInfo d3 = new DataSetInfoImpl(service, builder.buildDataSetNode("otherId", name, description, owner, lastChange));

		assertEquals(d1, d2);
		assertEquals(d1.hashCode(), d2.hashCode());

		assertNotEquals(d1, d3);
	}

	/** equals/hashcode for data set */
	public void dataSetEquality() {
		DataSet d1 = new DataSetImpl(service, builder.buildDataSetNode(id, name, description, owner, lastChange));
		DataSet d2 = new DataSetImpl(service, builder.buildDataSetNode(id, "", "", "", null));
		DataSet d3 = new DataSetImpl(service, builder.buildDataSetNode("otherId", name, description, owner, lastChange));

		assertEquals(d1, d2);
		assertEquals(d1.hashCode(), d2.hashCode());

		assertNotEquals(d1, d3);
	}

	/** No reports field means no reports. */
	public void reportsMissing() {
		server.register(dataSetsUri + "/" + id, builder.buildDataSetNode(id, name, description, owner, lastChange).toString());
		DataSet dataSet = service.loadDataSet(id);

		assertEquals(dataSet.getReports(), Collections.emptyList());
	}

	/** Empty reports array means no reports. */
	public void reportsEmpty() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		node.put("reports", new ObjectMapper().createArrayNode());
		server.register(dataSetsUri + "/" + id, node.toString());
		DataSet dataSet = service.loadDataSet(id);

		assertEquals(dataSet.getReports(), Collections.emptyList());
	}

	/** Invalid reports are ignored. */
	public void reportsInvalid() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		node.put("reports",
			new ObjectMapper().createArrayNode().add(builder.buildReportNode(null, name, description, owner, lastChange)));
		server.register(dataSetsUri + "/" + id, node.toString());
		DataSet dataSet = service.loadDataSet(id);

		assertEquals(dataSet.getReports(), Collections.emptyList());
	}

	/** valid reports are loaded */
	public void reportValid() {
		ObjectNode node = builder.buildDataSetNode(id, name, description, owner, lastChange);
		String reportId = "reportId";
		node.put("reports",
			new ObjectMapper().createArrayNode().add(builder.buildReportNode(reportId, name, description, owner, lastChange)));
		server.register(dataSetsUri + "/" + id, node.toString());
		DataSet dataSet = service.loadDataSet(id);

		assertEquals(dataSet.getReports().size(), 1);
		ReportInfo reportInfo = dataSet.getReports().get(0);
		assertEquals(reportInfo.getId(), reportId);
		assertEquals(reportInfo.getName(), name);
		assertEquals(reportInfo.getDescription(), description);
		assertEquals(reportInfo.getOwnerName(), owner);

		server.register("/api/reports/" + reportId, builder.buildReportNode(reportId, name, description, owner, lastChange)
			.toString());

		reportInfo.loadDetails();
		server.assertRequestUris(dataSetsUri + "/" + id, "/api/reports/" + reportId);
	}

	/**
	 * Tells the server to return the specified node as the only data set node
	 * in a <tt>data sets</tt> array.
	 * 
	 * @param node the node to return
	 */
	private void registerSingleDataSet(JsonNode node) {
		server.registerPaginatedItem(dataSetsUri, "dataSets", node);
	}
}
