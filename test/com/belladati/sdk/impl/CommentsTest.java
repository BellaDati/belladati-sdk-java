package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.report.Comment;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class CommentsTest extends SDKTest {

	private final String commentsUri = "/api/reports/%s/comments";

	private final String reportId = "id";
	private final String authorId = "authorId";
	private final String author = "comment author";
	private final String text = "comment text";
	private final String when = "Fri, 16 Aug 2013 12:56:50 GMT";

	/** Regular comment data is loaded correctly. */
	public void loadReportComments() {
		PaginatedList<Comment> comments = service.getReportComments(reportId);

		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments",
			builder.buildCommentNode(authorId, author, text, when));

		comments.load();

		server.assertRequestUris(String.format(commentsUri, reportId));
		assertEquals(comments.size(), 1);

		Comment comment = comments.get(0);
		assertEquals(comment.getAuthorInfo().getId(), authorId);
		assertEquals(comment.getAuthorInfo().getName(), author);
		assertEquals(comment.getText(), text);
		Calendar expectedWhen = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedWhen.set(2013, 7, 16, 12, 56, 50);
		expectedWhen.set(Calendar.MILLISECOND, 0);
		assertEquals(comment.getDateTime(), expectedWhen.getTime());
	}

	/** Comment date may be null. */
	public void commentNullDate() {
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments",
			builder.buildCommentNode(authorId, author, text, null));

		assertNull(service.getReportComments(reportId).load().get(0).getDateTime());
	}

	/** Comment date may be missing. */
	public void commentWithoutDate() {
		ObjectNode node = builder.buildCommentNode(authorId, author, text, when);
		node.remove("when");
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments", node);

		assertNull(service.getReportComments(reportId).load().get(0).getDateTime());
	}

	/** Comment date may be invalid format. */
	public void commentInvalidDate() {
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments",
			builder.buildCommentNode(authorId, author, text, "something invalid"));

		assertNull(service.getReportComments(reportId).load().get(0).getDateTime());
	}

	/** Comment text may be missing. */
	public void commentWithoutText() {
		ObjectNode node = builder.buildCommentNode(authorId, author, text, when);
		node.remove("text");
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments", node);

		assertEquals(service.getReportComments(reportId).load().get(0).getText(), "");
	}

	/** Getting a report's comment list multiple times returns the same list. */
	public void commentListSame() {
		assertSame(service.getReportComments(reportId), service.getReportComments(reportId));
	}

	/** Getting comment lists for different reports returns different lists. */
	public void commentListDifferentReports() {
		assertNotSame(service.getReportComments(reportId), service.getReportComments(reportId + 1));
	}

	/** Comment list from report info is the same as from service. */
	public void commentListReportInfo() {
		assertSame(service.getReportComments(reportId),
			new ReportInfoImpl(service, builder.buildReportNode(reportId, "name", null, "owner", null)).getComments());
	}

	/** Comment list from report is the same as from service. */
	public void commentListReport() {
		assertSame(service.getReportComments(reportId),
			new ReportImpl(service, builder.buildReportNode(reportId, "name", null, "owner", null)).getComments());
	}

	/** Can post comments from service. */
	public void postCommentFromService() {
		registerPost();
		service.postComment(reportId, text);
		server.assertRequestUris(String.format(commentsUri, reportId));
	}

	/** Can post comments from report info. */
	public void postCommentFromReportInfo() {
		registerPost();
		new ReportInfoImpl(service, builder.buildReportNode(reportId, "name", null, "owner", null)).postComment(text);
		server.assertRequestUris(String.format(commentsUri, reportId));
	}

	/** Can post comments from report. */
	public void postCommentFromReport() {
		registerPost();
		new ReportImpl(service, builder.buildReportNode(reportId, "name", null, "owner", null)).postComment(text);
		server.assertRequestUris(String.format(commentsUri, reportId));
	}

	/** Registers a response to a comment POST, checking for the expected text. */
	private void registerPost() {
		server.register(String.format(commentsUri, reportId), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getFormParameters(), Collections.singletonMap("text", text));
				holder.response.setEntity(new StringEntity("OK"));
			}
		});
	}
}
