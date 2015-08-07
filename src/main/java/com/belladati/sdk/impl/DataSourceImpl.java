package com.belladati.sdk.impl;

import java.util.Date;

import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourceImport;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

class DataSourceImpl implements DataSource {

	private static final String TYPE_SUFFIX = "ImportTable";

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String type;

	DataSourceImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();

		String type = json.get("type").asText();
		if (type != null && type.endsWith(TYPE_SUFFIX)) {
			type = type.substring(0, type.length() - TYPE_SUFFIX.length());
		}
		this.type = type;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public CachedList<DataSourceImport> getImports() {
		return service.getDataSourceImports(id);
	}

	@Override
	public DataSourcePendingImport setupImport(Date date) {
		return new DataSourcePendingImportImpl(service, id, date);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSourceImpl) {
			return id.equals(((DataSourceImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
