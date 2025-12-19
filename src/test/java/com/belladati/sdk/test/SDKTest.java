package com.belladati.sdk.test;

import com.belladati.sdk.dataset.source.ImportIntervalUnit;
import com.belladati.sdk.impl.BellaDatiClient;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.impl.TokenHolder;
import com.belladati.sdk.view.ViewType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Superclass for all SDK test classes using a local test server.
 * 
 * 
 */
public class SDKTest {

	/** the server; a new instance is created for every test method */
	protected RequestTrackingServer server;

	/** builds JSON objects */
	protected final JsonBuilder builder = new JsonBuilder();

	/** system default locale */
	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	@BeforeMethod(alwaysRun = true)
	protected void setupServer() throws Exception {
		server = new RequestTrackingServer();
		service = null;
	}

	private BellaDatiServiceImpl service;

	protected BellaDatiServiceImpl getService() {
		if (service == null) {
			try {
				server.start();
			} catch (Exception e) {
				throw new RuntimeException("Cannot start test server", e);
			}
			BellaDatiClient client = new BellaDatiClient(server.getHttpURL(), false);
			service = new BellaDatiServiceImpl(client, new TokenHolder("key", "secret"));
		}
		return service;
	}

	@AfterMethod(alwaysRun = true)
	protected void tearDownServer() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	/** resets the locale back to what it was */
	@AfterMethod(groups = "locale")
	protected void resetLocale() {
		Locale.setDefault(DEFAULT_LOCALE);
	}

	@DataProvider(name = "viewTypes")
	protected Object[][] viewTypeProvider() {
		return new Object[][] { { "chart", ViewType.CHART }, { "kpi", ViewType.KPI }, { "text", ViewType.TEXT },
			{ "table", ViewType.TABLE }, { "map", ViewType.MAP } };
	}

	@DataProvider(name = "jsonViewTypes")
	protected Object[][] jsonViewTypeProvider() {
		return new Object[][] { { "chart", ViewType.CHART }, { "kpi", ViewType.KPI }, { "text", ViewType.TEXT },
			{ "map", ViewType.MAP } };
	}

	@DataProvider(name = "unsupportedViewTypes")
	protected Object[][] unsupportedViewTypeProvider() {
		// we don't support tables yet
		return new Object[][] { { "unknown type" } };
	}

	@DataProvider(name = "intervalUnits")
	protected Object[][] intervalUnitsProvider() {
		return new Object[][] { new Object[] { "HOUR", ImportIntervalUnit.HOUR, 1, 60 },
			new Object[] { "HOUR2", ImportIntervalUnit.HOUR, 2, 120 }, new Object[] { "HOUR4", ImportIntervalUnit.HOUR, 4, 240 },
			new Object[] { "HOUR8", ImportIntervalUnit.HOUR, 8, 480 }, new Object[] { "DAY", ImportIntervalUnit.DAY, 1, 1440 },
			new Object[] { "DAY2", ImportIntervalUnit.DAY, 2, 2880 }, new Object[] { "WEEK", ImportIntervalUnit.WEEK, 1, 10080 },
			new Object[] { "WEEK2", ImportIntervalUnit.WEEK, 2, 20160 },
			new Object[] { "MONTH", ImportIntervalUnit.MONTH, 1, 44640 },
			new Object[] { "QUARTER", ImportIntervalUnit.QUARTER, 1, 133920 },
			new Object[] { "YEAR", ImportIntervalUnit.YEAR, 1, 525600 } };
	}

	protected final BufferedImage getTestBufferedImage() {
		InputStream stream = getTestImageStream();
		if (stream == null) {
			return null;
		} else {
			try {
				BufferedImage image = ImageIO.read(stream);
				if (image == null) {
					System.err.println("Cannot load test image - InputStream is null");
				}
				return image;
			} catch (Throwable e) {
				System.err.println("Cannot load test image - Error: " + e);
				return null;
			}
		}
	}

	protected final File getTestImageFile() {
		return getResourceAsFile("belladati.png");
	}

	protected final InputStream getTestImageStream() {
		return getResourceAsStream("belladati.png");
	}

	protected final InputStream getResourceAsStream(String name) {
		try {
			InputStream stream = SDKTest.class.getResourceAsStream(name);
			if (stream == null) {
				System.err.println("Cannot load resource '" + name + "' - InputStream is null");
			}
			return stream;
		} catch (Throwable e) {
			System.err.println("Cannot load resource '" + name + "' - Error: " + e);
			return null;
		}
	}

	protected final File getResourceAsFile(String name) {
		try {
			return new File(SDKTest.class.getResource(name).toURI());
		} catch (Throwable e) {
			System.err.println("Cannot load resource '" + name + "' - Error: " + e);
			return null;
		}
	}

	protected final void assertEqualsBufferedImage(BufferedImage actual, BufferedImage expected) {
		if (expected == null) {
			assertNull(actual);
		} else {
			assertNotNull(actual);
			assertEquals(actual.getWidth(), expected.getWidth());
			assertEquals(actual.getHeight(), expected.getHeight());
		}
	}

}
