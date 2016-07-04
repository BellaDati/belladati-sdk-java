package com.belladati.sdk.view.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.util.PageOrientation;
import com.belladati.sdk.util.PageSize;
import com.belladati.sdk.view.export.ViewExport;
import com.belladati.sdk.view.export.ViewExportType;
import com.belladati.sdk.view.export.ViewExporter;

/**
 * This class exports view to various export types {@link ViewExportType}
 * 
 * @author Pavol Kovac
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
			viewExport.setInputStream((InputStream) this.service.loadFile(builder.build().toString()));
		} catch (URISyntaxException e) {
			throw new InternalConfigurationException("Invalid URI", e);
		} catch (IOException e) {
			throw new ConnectionException(e);
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
			viewExport.setInputStream((InputStream) this.service.loadFile(builder.build().toString()));
		} catch (URISyntaxException e) {
			throw new InternalConfigurationException("Invalid URI", e);
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
		return viewExport;
	}

}
