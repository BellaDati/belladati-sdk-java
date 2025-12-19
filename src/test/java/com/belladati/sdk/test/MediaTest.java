package com.belladati.sdk.test;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class MediaTest extends SDKTest {

	private final String mediaUrl = "/api/import/media";
	private final String imageUrl = mediaUrl + "/image";

	private final String name = "test name";

	public void uploadImage_withoutName() {
		final boolean[] executed = new boolean[1];
		server.register(imageUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity(""));
				executed[0] = true;
			}
		});

		getService().uploadImage(getTestImageFile(), null);
		assertTrue(executed[0]);
	}

	public void uploadImage_withName() {
		final boolean[] executed = new boolean[1];
		server.register(imageUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException, ParseException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity(""));
				executed[0] = true;
			}
		});

		getService().uploadImage(getTestImageFile(), name);
		assertTrue(executed[0]);
	}

}
