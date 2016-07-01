package com.belladati.sdk.dashboard.impl;

import java.io.IOException;
import java.util.Date;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.fasterxml.jackson.databind.JsonNode;

public class DashboardInfoImpl implements DashboardInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final Date lastChange;

	public DashboardInfoImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();

		if (json.hasNonNull("lastChange")) {
			this.lastChange = BellaDatiSdkUtils.parseJavaUtilDate(json.get("lastChange").asText());
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DashboardInfoImpl) {
			return id.equals(((DashboardInfoImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
