package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

@Test
public class DataSourcesTest extends SDKTest {

	private final String dsId = "ds-id";
	private final String dataSourcesUri = String.format("/api/dataSets/%s/dataSources", dsId);

	private final String id = "123";
	private final String name = "data source name";
	private final String type = "some type";

	/** Data source is loaded correctly from service. */
	public void loadDataSource() {
		CachedList<DataSource> dataSources = service.getDataSources(dsId);

		registerSingleDataSource(builder.buildDataSourceNode(id, name, type));

		dataSources.load();
		server.assertRequestUris(dataSourcesUri);
		assertEquals(dataSources.toList().size(), 1);

		DataSource source = dataSources.toList().get(0);

		assertEquals(source.getId(), id);
		assertEquals(source.getName(), name);
		assertEquals(source.getType(), type);

		assertEquals(source.toString(), name);
	}

	/** Data source is loaded correctly from data set. */
	public void loadFromDataSet() {
		DataSet dataSet = new DataSetImpl(service, builder.buildDataSetNode(dsId, "", null, null, null));
		CachedList<DataSource> dataSources = dataSet.getDataSources();

		registerSingleDataSource(builder.buildDataSourceNode(id, name, type));

		dataSources.load();
		server.assertRequestUris(dataSourcesUri);
		assertEquals(dataSources.toList().size(), 1);

		DataSource source = dataSources.toList().get(0);

		assertEquals(source.getId(), id);
		assertEquals(source.getName(), name);
		assertEquals(source.getType(), type);

		assertEquals(source.toString(), name);
	}

	/** given the same ID, the same collection is returned */
	public void sameCollection() {
		DataSet dataSet = new DataSetImpl(service, builder.buildDataSetNode(dsId, "", null, null, null));

		assertSame(dataSet.getDataSources(), service.getDataSources(dsId));
	}

	/** equals/hashcode for data source */
	public void dataSourceEquality() {
		DataSource d1 = new DataSourceImpl(service, builder.buildDataSourceNode(id, name, type));
		DataSource d2 = new DataSourceImpl(service, builder.buildDataSourceNode(id, "", ""));
		DataSource d3 = new DataSourceImpl(service, builder.buildDataSourceNode("otherId", name, type));

		assertEquals(d1, d2);
		assertEquals(d1.hashCode(), d2.hashCode());

		assertNotEquals(d1, d3);
	}

	/** trailing 'ImportTable' is removed from type */
	public void importTableType() {
		assertEquals(new DataSourceImpl(service, builder.buildDataSourceNode(id, name, type + "ImportTable")).getType(), type);
	}

	private void registerSingleDataSource(JsonNode node) {
		server.registerPaginatedItem(dataSourcesUri, "dataSources", node);
	}
}
