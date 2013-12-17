package com.belladati.sdk.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.impl.DashletImpl.DashletException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

class DashboardImpl implements Dashboard {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final Date lastChange;
	private final List<Dashlet> dashlets;

	DashboardImpl(BellaDatiServiceImpl service, JsonNode json) {
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
}
