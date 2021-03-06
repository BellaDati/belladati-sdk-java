package com.belladati.sdk.dashboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.exception.impl.DashletException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class DashboardImpl implements Dashboard {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final Date lastChange;
	private final List<Dashlet> dashlets;

	public DashboardImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();

		if (json.hasNonNull("lastChange")) {
			this.lastChange = BellaDatiSdkUtils.parseJavaUtilDate(json.get("lastChange").asText());
		} else {
			this.lastChange = null;
		}

		List<Dashlet> dashlets = new ArrayList<Dashlet>();
		if (json.hasNonNull("dashlets") && json.get("dashlets") instanceof ArrayNode) {
			for (JsonNode view : (ArrayNode) json.get("dashlets")) {
				try {
					DashletImpl viewInfo = new DashletImpl(this.service, view);
					dashlets.add(viewInfo);
				} catch (DashletException e) {
					// nothing to do, just ignore the view
				}
			}
		}
		this.dashlets = Collections.unmodifiableList(dashlets);
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
	public List<Dashlet> getDashlets() {
		return dashlets;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DashboardImpl) {
			return id.equals(((DashboardImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
