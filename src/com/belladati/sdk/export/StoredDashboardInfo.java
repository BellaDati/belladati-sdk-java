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

import javax.imageio.ImageIO;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.exception.InternalConfigurationException;

class StoredDashboardInfo implements DashboardInfo, Serializable {

	private static final long serialVersionUID = 493444312830584163L;

	private final String id;
	private final String name;
	private final Date lastChange;
	private final transient BufferedImage thumbnail;

	private final transient Dashboard dashboard = new StoredDashboard();
	private final List<Dashlet> dashlets = new ArrayList<Dashlet>();

	StoredDashboardInfo(String id, String name, Date lastChange, BufferedImage thumbnail, List<Dashlet> dashlets) {
		this.id = id;
		this.name = name;
		this.lastChange = lastChange;
		this.thumbnail = thumbnail;
		this.dashlets.addAll(dashlets);
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
	public Dashboard loadDetails() {
		return dashboard;
	}

	@Override
	public Date getLastChange() {
		return lastChange != null ? (Date) lastChange.clone() : null;
	}

	@Override
	public Object loadThumbnail() throws IOException {
		return thumbnail;
	}

	class StoredDashboard implements Dashboard {

		@Override
		public String getId() {
			return StoredDashboardInfo.this.getId();
		}

		@Override
		public String getName() {
			return StoredDashboardInfo.this.getName();
		}

		@Override
		public Date getLastChange() {
			return StoredDashboardInfo.this.getLastChange();
		}

		@Override
		public Object loadThumbnail() throws IOException {
			return StoredDashboardInfo.this.loadThumbnail();
		}

		@Override
		public List<Dashlet> getDashlets() {
			return Collections.unmodifiableList(StoredDashboardInfo.this.dashlets);
		}
	}

	/** Deserialization. Sets up the thumbnail and comments. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			// re-initialize the dashboard field
			// (there's no point storing it)
			Field dashboard = getClass().getDeclaredField("dashboard");
			dashboard.setAccessible(true);
			dashboard.set(this, new StoredDashboard());

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
