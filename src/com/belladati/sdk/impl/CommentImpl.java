package com.belladati.sdk.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.belladati.sdk.report.Comment;
import com.belladati.sdk.user.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;

class CommentImpl implements Comment {

	private final String text;
	private final Date date;
	private final UserInfo authorInfo;

	CommentImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.text = json.hasNonNull("text") ? json.get("text").asText() : "";

		this.authorInfo = new UserInfoImpl(service, json.get("authorId").asText(), json.get("author").asText());

		if (json.hasNonNull("when")) {
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			Date date;
			try {
				date = format.parse(json.get("when").asText());
			} catch (ParseException e) {
				date = null;
			}
			this.date = date;
		} else {
			this.date = null;
		}
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public Date getDateTime() {
		return date != null ? (Date) date.clone() : null;
	}

	@Override
	public UserInfo getAuthorInfo() {
		return authorInfo;
	}

	@Override
	public String toString() {
		return text;
	}
}
