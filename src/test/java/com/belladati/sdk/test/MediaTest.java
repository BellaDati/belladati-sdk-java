package com.belladati.sdk.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;

@Test
public class MediaTest extends SDKTest {

	private final String mediaUrl = "/api/import/media";
	private final String imageUrl = mediaUrl + "/image";

	private final String name = "test name";

	public void uploadImage_withoutName() {
		server.register(imageUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity(""));
			}
		});

		service.uploadImage(getTestImageFile(), null);
	}

	public void uploadImage_withName() {
		server.register(imageUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new StringEntity(""));
			}
		});

		service.uploadImage(getTestImageFile(), name);
	}

}
