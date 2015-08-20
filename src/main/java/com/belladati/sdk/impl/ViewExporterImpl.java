package com.belladati.sdk.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.view.export.ViewExport;
import com.belladati.sdk.view.export.ViewExportType;
import com.belladati.sdk.view.export.ViewExporter;

/**
 * This class exports view to various export types {@link ViewExportType}
 * 
 * @author pavol.kovac
 */
public final class ViewExporterImpl implements ViewExporter {

	private final BellaDatiServiceImpl service;

	public ViewExporterImpl(BellaDatiServiceImpl service) {
		this.service = service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewExport exportToPdf(final String viewId) {
		ViewExport viewExport = new ViewExport();
		URIBuilder builder = null;
		try {
			builder = new URIBuilder("api/reports/views/" + viewId + "/export/pdf");
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

}
