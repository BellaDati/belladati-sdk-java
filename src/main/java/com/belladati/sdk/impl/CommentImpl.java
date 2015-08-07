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
			SimpleDateFormat format = new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT);
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
	public boolean equals(Object obj) {
		if (obj instanceof CommentImpl) {
			if (authorInfo.equals(((CommentImpl) obj).authorInfo) && text.equals(((CommentImpl) obj).text)) {
				if (date == null) {
					return ((CommentImpl) obj).date == null;
				} else {
					return date.equals(((CommentImpl) obj).date);
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return authorInfo.hashCode() ^ text.hashCode();
	}

	@Override
	public String toString() {
		return text;
	}
}
