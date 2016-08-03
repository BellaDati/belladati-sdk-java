package com.belladati.sdk.report.impl;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import com.belladati.sdk.exception.impl.InvalidReportException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.util.PaginatedList;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.belladati.sdk.util.impl.LocalizationImpl;
import com.fasterxml.jackson.databind.JsonNode;

public class ReportInfoImpl implements ReportInfo {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String description;
	private final String ownerName;
	private final Date lastChange;
	private final LocalizationImpl localization;

	public ReportInfoImpl(BellaDatiServiceImpl service, JsonNode json) {
		if (!json.hasNonNull("id") || !json.hasNonNull("name") || !json.hasNonNull("owner")) {
			throw new InvalidReportException(json);
		}
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
	public Report loadDetails() {
		return service.loadReport(id);
	}

	@Override
	public Object loadThumbnail() {
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
	public void deleteComment(String commentId) {
		service.deleteComment(commentId);
	}

	@Override
	public String createImageView(String viewName, File image, Integer width, Integer height) {
		return service.createImageView(id, viewName, image, width, height);
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

}
