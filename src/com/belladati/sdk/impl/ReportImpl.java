package com.belladati.sdk.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.impl.AttributeImpl.InvalidAttributeException;
import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.report.Attribute;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.util.PaginatedList;
import com.belladati.sdk.view.View;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

class ReportImpl implements Report {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String description;
	private final String ownerName;
	private final Date lastChange;
	private final List<View> viewInfos;
	private final List<Attribute> attributes;
	private final DataSetInfo dataSet;

	ReportImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = json.hasNonNull("description") ? json.get("description").asText() : "";
		this.ownerName = json.get("owner").asText();

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

		List<View> viewInfos = new ArrayList<View>();
		if (json.hasNonNull("views") && json.get("views") instanceof ArrayNode) {
			for (JsonNode view : (ArrayNode) json.get("views")) {
				try {
					viewInfos.add(ViewImpl.buildView(service, view));
				} catch (UnknownViewTypeException e) {
					// nothing to do, just ignore the view
				}
			}
		}
		this.viewInfos = Collections.unmodifiableList(viewInfos);

		List<Attribute> attributes = new ArrayList<Attribute>();
		if (json.hasNonNull("dataSet") && json.get("dataSet").hasNonNull("drilldownAttributes")
			&& json.get("dataSet").get("drilldownAttributes") instanceof ArrayNode) {
			for (JsonNode attribute : (ArrayNode) json.get("dataSet").get("drilldownAttributes")) {
				try {
					attributes.add(new AttributeImpl(service, id, attribute));
				} catch (InvalidAttributeException e) {
					// nothing to do, just ignore the attribute
				}
			}
		}
		this.attributes = Collections.unmodifiableList(attributes);

		if (json.hasNonNull("dataSet") && json.get("dataSet").hasNonNull("id")) {
			this.dataSet = new DataSetInfoImpl(service, json.get("dataSet"));
		} else {
			this.dataSet = null;
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
	public List<View> getViews() {
		return viewInfos;
	}

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public PaginatedList<Comment> getComments() {
		return service.getReportComments(id);
	}

	@Override
	public void postComment(String text) {
		service.postComment(id, text);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReportImpl) {
			return id.equals(((ReportImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public DataSetInfo getDataSet() {
		return dataSet;
	}
}
