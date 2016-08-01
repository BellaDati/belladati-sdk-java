package com.belladati.sdk.view.impl;

import static org.testng.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.PageOrientation;
import com.belladati.sdk.util.PageSize;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.export.ViewExport;
import com.belladati.sdk.view.export.ViewExportType;
import com.belladati.sdk.view.export.ViewExporter;

@Test
public class ViewExporterTest extends SDKTest {

	private final String viewsUri = "/api/reports/views/";
	private final String id = "id";
	private final String name = "My View Name";

	private final String URI_PNG = viewsUri + id + "/image";
	private final String URI_PDF = viewsUri + id + "/export/pdf";

	@BeforeMethod(alwaysRun = true)
	protected void setupSources() throws Exception {
		server.register(URI_PNG, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});
		server.register(URI_PDF, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});
	}

	public void exportPng_defaults() throws UnknownViewTypeException, IOException {
		View view = new TableViewImpl(service, builder.buildViewNode(id, name, "table"));

		ViewExporter exporter = view.createExporter();
		assertEquals(exporter.getId(), id);

		ViewExport result = exporter.exportPng(null, null);
		server.assertRequestUris(URI_PNG);

		assertEquals(result.getViewId(), id);
		assertEquals(result.getExportType(), ViewExportType.PNG);

		BufferedImage image = ImageIO.read(result.getInputStream());
		assertEquals(image.getWidth(), 56);
		assertEquals(image.getHeight(), 46);
	}

	public void exportPng_params() throws UnknownViewTypeException, IOException {
		View view = new TableViewImpl(service, builder.buildViewNode(id, name, "chart"));

		ViewExporter exporter = view.createExporter();
		assertEquals(exporter.getId(), id);

		ViewExport result = exporter.exportPng(123, 456);
		server.assertRequestUris(URI_PNG);

		assertEquals(result.getViewId(), id);
		assertEquals(result.getExportType(), ViewExportType.PNG);

		BufferedImage image = ImageIO.read(result.getInputStream());
		assertEquals(image.getWidth(), 56);
		assertEquals(image.getHeight(), 46);
	}

	public void exportPdf_defaults() throws UnknownViewTypeException, IOException {
		View view = new TableViewImpl(service, builder.buildViewNode(id, name, "table"));

		ViewExporter exporter = view.createExporter();
		assertEquals(exporter.getId(), id);

		ViewExport result = exporter.exportPdf(null, null);
		server.assertRequestUris(URI_PDF);

		assertEquals(result.getViewId(), id);
		assertEquals(result.getExportType(), ViewExportType.PDF);

		InputStream stream = result.getInputStream();
		assertEquals(stream.available(), 4236);
	}

	public void exportPdf_params() throws UnknownViewTypeException, IOException {
		View view = new TableViewImpl(service, builder.buildViewNode(id, name, "table"));

		ViewExporter exporter = view.createExporter();
		assertEquals(exporter.getId(), id);

		ViewExport result = exporter.exportPdf(PageSize.A3, PageOrientation.LANDSCAPE);
		server.assertRequestUris(URI_PDF);

		assertEquals(result.getViewId(), id);
		assertEquals(result.getExportType(), ViewExportType.PDF);

		InputStream stream = result.getInputStream();
		assertEquals(stream.available(), 4236);
	}

}
