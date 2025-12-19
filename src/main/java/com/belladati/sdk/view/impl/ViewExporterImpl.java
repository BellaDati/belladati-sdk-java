package com.belladati.sdk.view.impl;

import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.util.PageOrientation;
import com.belladati.sdk.util.PageSize;
import com.belladati.sdk.view.export.ViewExport;
import com.belladati.sdk.view.export.ViewExportType;
import com.belladati.sdk.view.export.ViewExporter;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;

/**
 * This class exports view to various export types {@link ViewExportType}
 * 
 * 
 */
public final class ViewExporterImpl implements ViewExporter {

	private final BellaDatiServiceImpl service;
	private final String viewId;

	public ViewExporterImpl(BellaDatiServiceImpl service, String viewId) {
		this.service = service;
		this.viewId = viewId;
	}

	@Override
	public String getId() {
		return viewId;
	}

	@Override
	public ViewExport exportPdf(PageSize pageSize, PageOrientation pageOrientation) {
		ViewExport viewExport = new ViewExport();
		try {
			URIBuilder builder = new URIBuilder("api/reports/views/" + viewId + "/export/pdf");
			if (pageSize != null) {
				builder.addParameter("pageSize", pageSize.name());
			}
			if (pageOrientation != null) {
				builder.addParameter("pageOrientation", pageOrientation.name());
			}
			viewExport.setViewId(viewId);
			viewExport.setExportType(ViewExportType.PDF);
			viewExport.setInputStream(this.service.getAsStream(builder.build().toString()));
		} catch (URISyntaxException e) {
			throw new InternalConfigurationException("Invalid URI", e);
		}
		return viewExport;
	}

	@Override
	public ViewExport exportPng(Integer width, Integer height) {
		ViewExport viewExport = new ViewExport();
		try {
			URIBuilder builder = new URIBuilder("api/reports/views/" + viewId + "/image");
			if (width != null) {
				builder.addParameter("width", width.toString());
			}
			if (height != null) {
				builder.addParameter("height", height.toString());
			}
			viewExport.setViewId(viewId);
			viewExport.setExportType(ViewExportType.PNG);
			viewExport.setInputStream(this.service.getAsStream(builder.build().toString()));
		} catch (URISyntaxException e) {
			throw new InternalConfigurationException("Invalid URI", e);
		}
		return viewExport;
	}

}
