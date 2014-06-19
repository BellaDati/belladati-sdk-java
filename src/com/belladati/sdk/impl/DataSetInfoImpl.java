package com.belladati.sdk.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.DataSetInfo;
import com.fasterxml.jackson.databind.JsonNode;

class DataSetInfoImpl implements DataSetInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String description;
	private final String ownerName;
	private final Date lastChange;
	private final LocalizationImpl localization;

	DataSetInfoImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = json.hasNonNull("description") ? json.get("description").asText() : "";
		this.ownerName = json.get("owner").asText();

		if (json.hasNonNull("lastChange")) {
			SimpleDateFormat format = new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT);
			Date lastChange;
			try {
				lastChange = format.parse(json.get("lastChange").asText());
			} catch (ParseException e) {
				lastChange = null;
			}
			this.lastChange = lastChange;
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
