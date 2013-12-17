package com.belladati.sdk.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.fasterxml.jackson.databind.JsonNode;

class DashboardInfoImpl implements DashboardInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final Date lastChange;

	DashboardInfoImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();

		if (json.hasNonNull("lastChange")) {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
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
	public Date getLastChange() {
		return lastChange != null ? (Date) lastChange.clone() : null;
	}

	@Override
	public Dashboard loadDetails() {
		return service.loadDashboard(id);
	}

	@Override
	public Object loadThumbnail() throws IOException {
		return service.loadDashboardThumbnail(id);
	}

	@Override
	public String toString() {
		return name;
	}
}
