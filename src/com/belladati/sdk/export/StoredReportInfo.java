package com.belladati.sdk.export;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.impl.LocalizationImpl;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.util.PaginatedList;
import com.belladati.sdk.view.View;

class StoredReportInfo extends StoredLocalizable implements ReportInfo, Serializable {

	private static final long serialVersionUID = 493444312830584163L;

	private final String description;
	private final String ownerName;
	private final Date lastChange;
	private final transient BufferedImage thumbnail;

	private final transient PaginatedList<Comment> comments = new EmptyPaginatedList<Comment>();
	private final transient Report report = new StoredReport();
	private final List<View> views = new ArrayList<View>();

	StoredReportInfo(String id, String name, LocalizationImpl localization, String description, String ownerName,
		Date lastChange, BufferedImage thumbnail, List<View> views) {
		super(id, name, localization);
		this.description = description;
		this.ownerName = ownerName;
		this.lastChange = lastChange;
		this.thumbnail = thumbnail;
		this.views.addAll(views);
	}

	@Override
	public Report loadDetails() {
		return report;
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
	public Object loadThumbnail() throws IOException {
		return thumbnail;
	}

	@Override
	public PaginatedList<Comment> getComments() {
		return comments;
	}

	@Override
	public void postComment(String text) {
	}

	class StoredReport implements Report {

		@Override
		public String getId() {
			return StoredReportInfo.this.getId();
		}

		@Override
		public String getName() {
			return StoredReportInfo.this.getName();
		}

		@Override
		public String getName(Locale locale) {
			return StoredReportInfo.this.getName(locale);
		}

		@Override
		public boolean hasLocalization(Locale locale) {
			return StoredReportInfo.this.hasLocalization(locale);
		}

		@Override
		public String getDescription() {
			return StoredReportInfo.this.getDescription();
		}

		@Override
		public String getOwnerName() {
			return StoredReportInfo.this.getOwnerName();
		}

		@Override
		public Date getLastChange() {
			return StoredReportInfo.this.getLastChange();
		}

		@Override
		public Object loadThumbnail() throws IOException {
			return StoredReportInfo.this.loadThumbnail();
		}

		@Override
		public List<View> getViews() {
			return Collections.unmodifiableList(StoredReportInfo.this.views);
		}

		@Override
		public List<Attribute> getAttributes() {
			return Collections.emptyList();
		}

		@Override
		public PaginatedList<Comment> getComments() {
			return StoredReportInfo.this.getComments();
		}

		@Override
		public void postComment(String text) {
		}

		@Override
		public DataSetInfo getDataSet() {
			return null;
		}
	}

	/** Deserialization. Sets up the thumbnail and comments. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			// re-initialize the comments and report field
			// (there's no point storing them)
			Field comments = getClass().getDeclaredField("comments");
			comments.setAccessible(true);
			comments.set(this, new EmptyPaginatedList<Comment>());

			Field report = getClass().getDeclaredField("report");
			report.setAccessible(true);
			report.set(this, new StoredReport());

			if (in.readBoolean()) {
				// if we have a stored thumbnail image, load it
				Field thumbnail = getClass().getDeclaredField("thumbnail");
				thumbnail.setAccessible(true);
				thumbnail.set(this, ImageIO.read(in));
			}
		} catch (NoSuchFieldException e) {
			throw new InternalConfigurationException("Failed to set fields", e);
		} catch (IllegalAccessException e) {
			throw new InternalConfigurationException("Failed to set fields", e);
		} catch (SecurityException e) {
			throw new InternalConfigurationException("Failed to set fields", e);
		} catch (IllegalArgumentException e) {
			throw new InternalConfigurationException("Failed to set fields", e);
		}
	}

	/** Serialization. Writes the thumbnail. */
	private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
		out.defaultWriteObject();
		// set a flag indicating whether we have a thumbnail
		out.writeBoolean(thumbnail != null);
		if (thumbnail != null) {
			// then write the thumbnail itself, if one exists
			ImageIO.write(thumbnail, "png", out);
		}
	}
}
