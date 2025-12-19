package com.belladati.sdk.report.impl;

import com.belladati.sdk.report.Comment;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test
public class CommentsTest extends SDKTest {

	private final String commentsUri = "/api/reports/%s/comments";
	private final String deleteUri = "/api/reports/comments/%s";

	private final String reportId = "id";
	private final String commentId = "comId";
	private final String authorId = "authorId";
	private final String author = "comment author";
	private final String text = "comment text";
	private final String when = "Fri, 16 Aug 2013 12:56:50 GMT";

	/** Regular comment data is loaded correctly. */
	public void loadReportComments() {
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments",
			builder.buildCommentNode(commentId, authorId, author, text, when));

		PaginatedList<Comment> comments = getService().getReportComments(reportId);
		comments.load();

		server.assertRequestUris(String.format(commentsUri, reportId));
		assertEquals(comments.size(), 1);

		Comment comment = comments.get(0);
		assertEquals(comment.getId(), commentId);
		assertEquals(comment.getAuthorInfo().getId(), authorId);
		assertEquals(comment.getAuthorInfo().getName(), author);
		assertEquals(comment.getText(), text);
		Calendar expectedWhen = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		expectedWhen.set(2013, 7, 16, 12, 56, 50);
		expectedWhen.set(Calendar.MILLISECOND, 0);
		assertEquals(comment.getDateTime(), expectedWhen.getTime());

		assertEquals(comment.toString(), text);
	}

	/** Comment date may be null. */
	public void commentNullDate() {
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments",
			builder.buildCommentNode(commentId, authorId, author, text, null));

		assertNull(getService().getReportComments(reportId).load().get(0).getDateTime());
	}

	/** Comment date may be missing. */
	public void commentWithoutDate() {
		ObjectNode node = builder.buildCommentNode(commentId, authorId, author, text, when);
		node.remove("when");
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments", node);

		assertNull(getService().getReportComments(reportId).load().get(0).getDateTime());
	}

	/** Comment date may be invalid format. */
	public void commentInvalidDate() {
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments",
			builder.buildCommentNode(commentId, authorId, author, text, "something invalid"));

		assertNull(getService().getReportComments(reportId).load().get(0).getDateTime());
	}

	/** Comment text may be missing. */
	public void commentWithoutText() {
		ObjectNode node = builder.buildCommentNode(commentId, authorId, author, text, when);
		node.remove("text");
		server.registerPaginatedItem(String.format(commentsUri, reportId), "comments", node);

		assertEquals(getService().getReportComments(reportId).load().get(0).getText(), "");
	}

	/** Getting a report's comment list multiple times returns the same list. */
	public void commentListSame() {
		assertSame(getService().getReportComments(reportId), getService().getReportComments(reportId));
	}

	/** Getting comment lists for different reports returns different lists. */
	public void commentListDifferentReports() {
		assertNotSame(getService().getReportComments(reportId), getService().getReportComments(reportId + 1));
	}

	/** Comment list from report info is the same as from getService(). */
	public void commentListReportInfo() {
		assertSame(getService().getReportComments(reportId),
			new ReportInfoImpl(getService(), builder.buildReportNode(reportId, "name", null, "owner", null)).getComments());
	}

	/** Comment list from report is the same as from getService(). */
	public void commentListReport() {
		assertSame(getService().getReportComments(reportId),
			new ReportImpl(getService(), builder.buildReportNode(reportId, "name", null, "owner", null)).getComments());
	}

	/** Can post comments from getService(). */
	public void postCommentFromService() {
		registerPost();
		getService().postComment(reportId, text);
		server.assertRequestUris(String.format(commentsUri, reportId));
	}

	/** Can post comments from report info. */
	public void postCommentFromReportInfo() {
		registerPost();
		new ReportInfoImpl(getService(), builder.buildReportNode(reportId, "name", null, "owner", null)).postComment(text);
		server.assertRequestUris(String.format(commentsUri, reportId));
	}

	/** Can post comments from report. */
	public void postCommentFromReport() {
		registerPost();
		new ReportImpl(getService(), builder.buildReportNode(reportId, "name", null, "owner", null)).postComment(text);
		server.assertRequestUris(String.format(commentsUri, reportId));
	}

	/** Can delete comments from getService(). */
	public void deleteCommentFromService() {
		registerDelete();
		getService().deleteComment(commentId);
		server.assertRequestUris(String.format(deleteUri, commentId));
	}

	/** Can delete comments from report info. */
	public void deleteCommentFromReportInfo() {
		registerDelete();
		new ReportInfoImpl(getService(), builder.buildReportNode(reportId, "name", null, "owner", null)).deleteComment(commentId);
		server.assertRequestUris(String.format(deleteUri, commentId));
	}

	/** Can delete comments from report. */
	public void deleteCommentFromReport() {
		registerDelete();
		new ReportImpl(getService(), builder.buildReportNode(reportId, "name", null, "owner", null)).deleteComment(commentId);
		server.assertRequestUris(String.format(deleteUri, commentId));
	}

	/** equals and hashcode work as expected */
	public void equality() {
		Comment com1 = new CommentImpl(getService(), builder.buildCommentNode(commentId, authorId, author, text, when));
		Comment com2 = new CommentImpl(getService(), builder.buildCommentNode(commentId, "", "", "", null));
		Comment com3 = new CommentImpl(getService(), builder.buildCommentNode("otherId", authorId, "", text, when));

		assertEquals(com1, com2);
		assertEquals(com1.hashCode(), com2.hashCode());

		assertFalse(com1.equals(new Object()));
		assertNotEquals(com1, com3);
	}

	/**
	 * Registers a response to a comment POST, checking for the expected text.
	 */
	private void registerPost() {
		server.register(String.format(commentsUri, reportId), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getFormParameters(), Collections.singletonMap("text", text));
				holder.response.setEntity(new StringEntity("OK"));
			}
		});
	}

	/**
	 * Registers a response to a comment DELETE.
	 */
	private void registerDelete() {
		server.register(String.format(deleteUri, commentId), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				holder.response.setEntity(new StringEntity("OK"));
			}
		});
	}

}
