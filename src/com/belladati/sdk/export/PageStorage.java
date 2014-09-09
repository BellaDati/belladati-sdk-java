package com.belladati.sdk.export;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.dashboard.Dashlet.Type;
import com.belladati.sdk.impl.LocalizationImpl;
import com.belladati.sdk.impl.ReportImpl;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.util.Resource;
import com.belladati.sdk.view.View;

/**
 * Stores reports and dashboards as self-contained objects.
 * <p />
 * All requests to the server are made while storing and the page's content is
 * kept inside the resulting object. Calling load*() methods on the returned
 * object will return the stored data and doesn't communicate with the server.
 * <p />
 * All returned objects are {@link Serializable}.
 * 
 * @author Chris Hennigfeld
 */
public class PageStorage {

	protected final ViewStorage viewStorage;

	/**
	 * Creates a new default instance.
	 */
	public PageStorage() {
		this(new ViewStorage());
	}

	/**
	 * Creates a new instance using the given storage to store child views.
	 * 
	 * @param viewStorage used to store child views
	 */
	public PageStorage(ViewStorage viewStorage) {
		this.viewStorage = viewStorage;
	}

	/**
	 * Stores a ReportInfo. Makes server requests as needed to fetch the
	 * report's content.
	 * 
	 * @param reportInfo the report info to store
	 * @return a {@link Serializable} object holding all the report's data
	 */
	public ReportInfo storeReport(ReportInfo reportInfo) {
		return storeReport(reportInfo.loadDetails());
	}

	/**
	 * Stores a Report. Makes server requests as needed to fetch the report's
	 * content.
	 * 
	 * @param report the report to store
	 * @return a {@link Serializable} object holding all the report's data
	 */
	public ReportInfo storeReport(Report report) {
		return new StoredReportInfo(readId(report), readName(report), readLocalization(report), readDescription(report),
			readOwnerName(report), readLastChange(report), readThumbnail(report), storeViews(report));
	}

	/**
	 * Stores a DashboardInfo. Makes server requests as needed to fetch the
	 * dashboard's content.
	 * 
	 * @param dashboardInfo the dashboard info to store
	 * @return a {@link Serializable} object holding all the dashboard's data
	 */
	public DashboardInfo storeDashboard(DashboardInfo dashboardInfo) {
		return storeDashboard(dashboardInfo.loadDetails());
	}

	/**
	 * Stores a Dashboard. Makes server requests as needed to fetch the
	 * dashboard's content.
	 * 
	 * @param dashboard the dashboard to store
	 * @return a {@link Serializable} object holding all the dashboard's data
	 */
	public DashboardInfo storeDashboard(Dashboard dashboard) {
		return new StoredDashboardInfo(readId(dashboard), readName(dashboard), readLastChange(dashboard),
			readThumbnail(dashboard), storeDashlets(dashboard));
	}

	/**
	 * Reads a resource's ID. Override to customize.
	 * 
	 * @param res resources to read from
	 * @return the resources's ID
	 */
	protected String readId(Resource res) {
		return res.getId();
	}

	/**
	 * Reads a resources's name. Override to customize.
	 * 
	 * @param res resources to read from
	 * @return the resources's name
	 */
	protected String readName(Resource res) {
		return res.getName();
	}

	/**
	 * Reads a report's description. Override to customize.
	 * 
	 * @param report report to read from
	 * @return the report's description
	 */
	protected String readDescription(Report report) {
		return report.getDescription();
	}

	/**
	 * Reads a report's owner name. Override to customize.
	 * 
	 * @param report report to read from
	 * @return the report's owner name
	 */
	protected String readOwnerName(Report report) {
		return report.getOwnerName();
	}

	/**
	 * Reads a report's last change date. Override to customize.
	 * 
	 * @param report report to read from
	 * @return the report's last change date
	 */
	protected Date readLastChange(Report report) {
		return report.getLastChange();
	}

	/**
	 * Reads a report's localization. Override to customize.
	 * 
	 * @param report report to read from
	 * @return the report's localization
	 */
	protected LocalizationImpl readLocalization(Report report) {
		return (report instanceof ReportImpl) ? ((ReportImpl) report).getLocalization() : null;
	}

	/**
	 * Reads a report's thumbnail image. Override to customize.
	 * 
	 * @param report report to read from
	 * @return the report's thumbnail image
	 */
	protected BufferedImage readThumbnail(Report report) {
		try {
			return (BufferedImage) report.loadThumbnail();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Stores a report's views. Override to customize.
	 * 
	 * @param report report to store
	 * @return the report's stored views
	 */
	protected List<View> storeViews(Report report) {
		List<View> stored = new ArrayList<View>();
		for (View view : report.getViews()) {
			stored.add(viewStorage.storeView(view));
		}
		return stored;
	}

	/**
	 * Reads a dashboard's last change date. Override to customize.
	 * 
	 * @param dashboard dashboard to read from
	 * @return the dashboard's last change date
	 */
	protected Date readLastChange(Dashboard dashboard) {
		return dashboard.getLastChange();
	}

	/**
	 * Reads a dashboard's thumbnail image. Override to customize.
	 * 
	 * @param dashboard dashboard to read from
	 * @return the dashboard's thumbnail image
	 */
	protected BufferedImage readThumbnail(Dashboard dashboard) {
		try {
			return (BufferedImage) dashboard.loadThumbnail();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Stores a dashboard's dashlets. Override to customize.
	 * 
	 * @param dashboard dashboard to read from
	 * @return the dashboard's stored dashlets
	 */
	protected List<Dashlet> storeDashlets(Dashboard dashboard) {
		List<Dashlet> stored = new ArrayList<Dashlet>();
		for (Dashlet dashlet : dashboard.getDashlets()) {
			stored.add(storeDashlet(dashlet));
		}
		return stored;
	}

	/**
	 * Stores an individual dashlet. Override to customize.
	 * 
	 * @param dashlet dashlet to store
	 * @return the stored dashlet
	 */
	protected Dashlet storeDashlet(Dashlet dashlet) {
		Type type = readDashletType(dashlet);
		if (type == Type.TEXT) {
			return new StoredDashlet(type, readDashletContent(dashlet));
		} else {
			return new StoredDashlet(type, viewStorage.storeView((View) readDashletContent(dashlet)));
		}
	}

	/**
	 * Reads a dashlet's type. Override to customize.
	 * 
	 * @param dashlet dashlet to read from
	 * @return the dashlet's type
	 */
	protected Type readDashletType(Dashlet dashlet) {
		return dashlet.getType();
	}

	/**
	 * Reads a dashlet's content. Override to customize.
	 * 
	 * @param dashlet dashlet to read from
	 * @return the dashlet's content
	 */
	protected Object readDashletContent(Dashlet dashlet) {
		return dashlet.getContent();
	}
}
