package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourceImport;
import com.belladati.sdk.dataset.source.ImportInterval;
import com.belladati.sdk.impl.DataSourceImportImpl.InvalidDataSourceImportException;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class DataSourceImportTest extends SDKTest {

	private final String dsId = "ds-id";
	private final String importUri = String.format("/api/dataSets/dataSources/%s/executions", dsId);

	private final String id = "123";
	private final String caller = "import caller";
	private final String lastImport = "Mon, 16 Apr 2012 10:17:26 GMT";
	private final String overwritePolicy = "DELETE_ALL";
	private final String interval = "CUSTOM";
	private final int customIntervalLength = 30;

	/** Import is loaded correctly from service. */
	public void loadDataSource() {
		CachedList<DataSourceImport> imports = service.getDataSourceImports(dsId);

		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval,
			customIntervalLength));

		imports.load();
		server.assertRequestUris(importUri);
		assertEquals(imports.toList().size(), 1);

		DataSourceImport exec = imports.toList().get(0);

		assertEquals(exec.getId(), id);
		assertEquals(exec.getCallerName(), caller);
		Calendar expectedImport = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedImport.set(2012, 3, 16, 10, 17, 26);
		expectedImport.set(Calendar.MILLISECOND, 0);
		assertEquals(exec.getLastExecutionDate(), expectedImport.getTime());
		assertEquals(exec.getImportInterval().getMinutes(), customIntervalLength);
		expectedImport.add(Calendar.MINUTE, customIntervalLength);
		assertEquals(exec.getImportInterval().getNextExecution(), expectedImport.getTime());
		assertTrue(exec.isOverwriting());
	}

	/** Import is loaded correctly from data source. */
	public void loadFromDataSet() {
		DataSource source = new DataSourceImpl(service, builder.buildDataSourceNode(dsId, "", ""));
		CachedList<DataSourceImport> imports = source.getImports();

		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval,
			customIntervalLength));

		imports.load();
		server.assertRequestUris(importUri);
		assertEquals(imports.toList().size(), 1);

		DataSourceImport exec = imports.toList().get(0);

		assertEquals(exec.getId(), id);
		assertEquals(exec.getCallerName(), caller);
		Calendar expectedImport = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedImport.set(2012, 3, 16, 10, 17, 26);
		expectedImport.set(Calendar.MILLISECOND, 0);
		assertEquals(exec.getLastExecutionDate(), expectedImport.getTime());
		assertEquals(exec.getImportInterval().getMinutes(), customIntervalLength);
		expectedImport.add(Calendar.MINUTE, customIntervalLength);
		assertEquals(exec.getImportInterval().getNextExecution(), expectedImport.getTime());
		assertTrue(exec.isOverwriting());
	}

	/** given the same ID, the same collection is returned */
	public void sameCollection() {
		DataSource source = new DataSourceImpl(service, builder.buildDataSourceNode(dsId, "", ""));

		assertSame(source.getImports(), service.getDataSourceImports(dsId));
	}

	/** equals/hashcode for imports */
	public void dataSourceEquality() throws InvalidDataSourceImportException {
		DataSourceImport d1 = new DataSourceImportImpl(builder.buildSourceImportNode(dsId, caller, lastImport, overwritePolicy,
			interval, customIntervalLength));
		DataSourceImport d2 = new DataSourceImportImpl(builder.buildSourceImportNode(dsId, null, lastImport, null, null));
		DataSourceImport d3 = new DataSourceImportImpl(builder.buildSourceImportNode("other id", caller, lastImport,
			overwritePolicy, interval, customIntervalLength));

		assertEquals(d1, d2);
		assertEquals(d1.hashCode(), d2.hashCode());

		assertNotEquals(d1, d3);
	}

	/** missing last import source is ignored */
	public void missingLastImport() {
		ObjectNode node = builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, customIntervalLength);
		node.remove("when");
		registerSingleImport(node);

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** null last import source is ignored */
	public void nullLastImport() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, null, overwritePolicy, interval, customIntervalLength));

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** invalid last import source is ignored */
	public void invalidLastImport() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, "not a date", overwritePolicy, interval,
			customIntervalLength));

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** missing interval means no scheduling */
	public void missingInterval() {
		ObjectNode node = builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, customIntervalLength);
		node.remove("repeateInterval");
		registerSingleImport(node);

		assertNull(service.getDataSourceImports(dsId).load().toList().get(0).getImportInterval());
	}

	/** null interval means no scheduling */
	public void nullInterval() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, null, customIntervalLength));

		assertNull(service.getDataSourceImports(dsId).load().toList().get(0).getImportInterval());
	}

	/** sources with invalid intervals are ignored */
	public void invalidInterval() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, "not an interval type",
			customIntervalLength));

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** custom interval with missing length is invalid */
	public void customIntervalMissingLength() {
		ObjectNode node = builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, customIntervalLength);
		node.remove("repeateIntervalCustom");
		registerSingleImport(node);

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** custom interval with null length is invalid */
	public void customIntervalNullLength() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, null));

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** custom interval with invalid length is invalid */
	public void customIntervalInvalidLength() {
		ObjectNode node = builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, customIntervalLength);
		node.put("repeateIntervalCustom", "not a number");
		registerSingleImport(node);

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** custom interval with negative length is invalid */
	public void customIntervalNegativeLength() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, -11));

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** custom interval with zero length is invalid */
	public void customIntervalZeroLength() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, 0));

		assertEquals(service.getDataSourceImports(dsId).load().toList(), Collections.emptyList());
	}

	/** missing policy means no overwrite */
	public void missingOverwritePolicy() {
		ObjectNode node = builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval, customIntervalLength);
		node.remove("overwritingPolicy");
		registerSingleImport(node);

		assertFalse(service.getDataSourceImports(dsId).load().toList().get(0).isOverwriting());
	}

	/** null policy means no overwrite */
	public void nullOverwritePolicy() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, null, interval, customIntervalLength));

		assertFalse(service.getDataSourceImports(dsId).load().toList().get(0).isOverwriting());
	}

	/** empty string policy means no overwrite */
	public void emptyOverwritePolicy() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, "", interval, customIntervalLength));

		assertFalse(service.getDataSourceImports(dsId).load().toList().get(0).isOverwriting());
	}

	/** any other policy means overwrite */
	public void otherOverwritePolicy() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, "some policy", interval, customIntervalLength));

		assertTrue(service.getDataSourceImports(dsId).load().toList().get(0).isOverwriting());
	}

	/** interval units are parsed correctly */
	@Test(dataProvider = "intervalUnits")
	public void predefinedInterval(String interval, int minutes) {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, interval));

		ImportInterval result = service.getDataSourceImports(dsId).load().toList().get(0).getImportInterval();
		Calendar expectedImport = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedImport.set(2012, 3, 16, 10, 17, 26);
		expectedImport.set(Calendar.MILLISECOND, 0);
		expectedImport.add(Calendar.MINUTE, minutes);

		assertEquals(result.getMinutes(), minutes);
		assertEquals(result.getNextExecution(), expectedImport.getTime());
	}

	/** custom numbers are ignored with non-CUSTOM interval */
	public void predefinedWithNumber() {
		registerSingleImport(builder.buildSourceImportNode(id, caller, lastImport, overwritePolicy, "HOUR", 123));

		assertEquals(service.getDataSourceImports(dsId).load().toList().get(0).getImportInterval().getMinutes(), 60);
	}

	private void registerSingleImport(JsonNode node) {
		server.registerPaginatedItem(importUri, "executions", node);
	}

	@DataProvider(name = "intervalUnits")
	protected Object[][] provideIntervalUnits() {
		return new Object[][] { new Object[] { "HOUR", 60 }, new Object[] { "HOUR2", 120 }, new Object[] { "HOUR4", 240 },
			new Object[] { "HOUR8", 480 }, new Object[] { "DAY", 1440 }, new Object[] { "DAY2", 2880 },
			new Object[] { "WEEK", 10080 }, new Object[] { "WEEK2", 20160 }, new Object[] { "MONTH", 44640 },
			new Object[] { "QUARTER", 133920 }, new Object[] { "YEAR", 525600 } };
	}
}
