package com.belladati.sdk.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.belladati.sdk.exception.BellaDatiRuntimeException;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.JsonNode;

class ReportInfoImpl implements ReportInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String description;
	private final String ownerName;
	private final Date lastChange;

	ReportInfoImpl(BellaDatiServiceImpl service, JsonNode json) {
		if (!json.hasNonNull("id") || !json.hasNonNull("name") || !json.hasNonNull("owner")) {
			throw new InvalidReportException(json);
		}
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
	public Report loadDetails() {
		return service.loadReport(id);
	}

	@Override
	public Object loadThumbnail() throws IOException {
		return service.loadReportThumbnail(id);
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
		if (obj instanceof ReportInfoImpl) {
			return id.equals(((ReportInfoImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	class InvalidReportException extends BellaDatiRuntimeException {
		/** The serialVersionUID */
		private static final long serialVersionUID = -4920843734203654180L;

		public InvalidReportException(JsonNode node) {
			super("Invalid report JSON: " + node.toString());
		}
	}
}
