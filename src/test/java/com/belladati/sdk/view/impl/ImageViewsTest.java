package com.belladati.sdk.view.impl;

import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.view.ImageView;
import com.belladati.sdk.view.ImageView.Image;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

@Test
public class ImageViewsTest extends SDKTest {

	private final String id = "id";
	private final String name = "name";

	private final String viewsImageUri = "/api/reports/views/" + id + "/image";

	@BeforeMethod(alwaysRun = true)
	protected void setupSources() throws Exception {
		server.register(viewsImageUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				holder.response.setEntity(new InputStreamEntity(getTestImageStream(), ContentType.DEFAULT_BINARY));
			}
		});
	}

	/** equals and hashcode work as expected */
	public void equality() {
		Image obj1 = new ImageViewImpl.ImageImpl(id, getTestBufferedImage());
		Image obj2 = new ImageViewImpl.ImageImpl(id, getTestBufferedImage());
		Image obj3 = new ImageViewImpl.ImageImpl("otherId", getTestBufferedImage());

		assertEquals(obj1, obj2);
		assertEquals(obj1.hashCode(), obj2.hashCode());

		assertFalse(obj1.equals(new Object()));
		assertNotEquals(obj1, obj3);
	}

	/** image is loaded correctly. */
	public void loadViewTable() throws UnknownViewTypeException {
		View view = new ImageViewImpl(getService(), builder.buildViewNode(id, name, "image"));

		ImageView.Image image = (Image) view.loadContent();

		assertEquals(image.getId(), id);
		assertEqualsBufferedImage(image.getImage(), getTestBufferedImage());
		assertTrue(image.toString().contains(id));
		assertEquals(view.toString(), name);
	}

	/** image is loaded correctly via service. */
	public void loadViewTableFromService() {
		ImageView.Image image = (Image) getService().loadViewContent(id, ViewType.IMAGE);

		assertEquals(image.getId(), id);
		assertEqualsBufferedImage(image.getImage(), getTestBufferedImage());
		assertTrue(image.toString().contains(id));
	}

	/** image is loaded correctly. */
	public void loadViewTableFromLoader() throws UnknownViewTypeException {
		View view = new ImageViewImpl(getService(), builder.buildViewNode(id, name, "image"));

		ImageView.Image image = (Image) view.createLoader().loadContent();

		assertEquals(image.getId(), id);
		assertEqualsBufferedImage(image.getImage(), getTestBufferedImage());
		assertTrue(image.toString().contains(id));
		assertEquals(view.toString(), name);
	}

	/** image is loaded correctly via service. */
	public void loadViewTableFromServiceLoader() {
		ImageView.Image image = (Image) getService().setupViewLoader(id, ViewType.IMAGE).loadContent();

		assertEquals(image.getId(), id);
		assertEqualsBufferedImage(image.getImage(), getTestBufferedImage());
		assertTrue(image.toString().contains(id));
	}

	public void updateImage_fromService() {
		final boolean[] executed = new boolean[1];
		server.register(viewsImageUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity("", ContentType.TEXT_PLAIN));
				executed[0] = true;
			}
		});

		getService().editImageView(id, getTestImageFile());
		assertTrue(executed[0]);
	}

	public void updateImage_fromImageView() throws UnknownViewTypeException {
		final boolean[] executed = new boolean[1];
		server.register(viewsImageUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity("", ContentType.TEXT_PLAIN));
				executed[0] = true;
			}
		});

		ImageView view = new ImageViewImpl(getService(), builder.buildViewNode(id, name, "image"));
		view.updateImage(getTestImageFile());
		assertTrue(executed[0]);
	}

}
