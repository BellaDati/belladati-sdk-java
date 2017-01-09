package com.belladati.sdk.dataset.impl;

import java.util.Date;
import java.util.Locale;

import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.dataset.data.DataRow;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.util.PaginatedIdList;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.belladati.sdk.util.impl.LocalizationImpl;
import com.fasterxml.jackson.databind.JsonNode;

public class DataSetInfoImpl implements DataSetInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String description;
	private final String ownerName;
	private final Date lastChange;
	private final LocalizationImpl localization;

	public DataSetInfoImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = json.hasNonNull("description") ? json.get("description").asText() : "";
		this.ownerName = json.get("owner").asText();

		if (json.hasNonNull("lastChange")) {
			this.lastChange = BellaDatiSdkUtils.parseJavaUtilDate(json.get("lastChange").asText());
		} else {
			this.lastChange = null;
		}

		this.localization = new LocalizationImpl(json);
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
	public String getName(Locale locale) {
		return localization.getName(locale);
	}

	@Override
	public boolean hasLocalization(Locale locale) {
		return localization.hasLocalization(locale);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getOwnerName() {
		return ownerName;
	}

	@Override
	public Date getLastChange() {
		return lastChange != null ? (Date) lastChange.clone() : null;
	}

	@Override
	public DataSet loadDetails() {
		return service.loadDataSet(id);
	}

	@Override
	public PaginatedIdList<DataRow> getData() {
		return service.getDataSetData(id);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSetInfoImpl) {
			return id.equals(((DataSetInfoImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
