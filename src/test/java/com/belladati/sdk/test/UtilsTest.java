package com.belladati.sdk.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.entity.InputStreamEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.util.UriEncoder;

@Test
public class UtilsTest extends SDKTest {

	private final String utilsUrl = "/api/utils";
	private final String fileUrl = utilsUrl + "/file/%s";
	private final String mergePdfUrl = utilsUrl + "/mergePdfFiles/%s";

	private final String filepath1 = "file1.PDF";
	private final String filepath2 = "file 2 with spaces.pdf";
	private final String filepath3 = "C:\\path\\to\\my\\file3.PDF";
	private final String filepath4 = "/home/test/path/file4.pdf";

	@DataProvider(name = "loadFile_provider")
	private Object[][] loadFile_provider() {
		return new Object[][] { { filepath1 }, { filepath2 }, { filepath3 }, { filepath4 } };
	}

	@Test(dataProvider = "loadFile_provider")
	public void loadFile(String filepath) throws URISyntaxException, IOException {
		String encoded = UriEncoder.encode(filepath);
		server.register(String.format(fileUrl, encoded), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});

		ByteArrayInputStream stream = (ByteArrayInputStream) getService().loadFile(filepath);

		assertNotNull(stream);
		assertEquals(stream.available(), getTestImageStream().available());
	}

	@DataProvider(name = "mergePdfFiles_provider")
	private Object[][] mergePdfFiles_provider() {
		return new Object[][] { { Arrays.asList(filepath1) }, { Arrays.asList(filepath1, filepath2) },
			{ Arrays.asList(filepath1, filepath2, filepath3, filepath4) } };
	}

	@Test(dataProvider = "mergePdfFiles_provider")
	public void mergePdfFiles(List<String> paths) throws URISyntaxException, IOException {
		String joinedPaths = "";
		for (String path : paths) {
			if (!joinedPaths.isEmpty()) {
				joinedPaths += ";";
			}
			joinedPaths += UriEncoder.encode(path);
		}

		server.register(String.format(mergePdfUrl, joinedPaths), new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters().size(), 0);
				holder.response.setEntity(new InputStreamEntity(getTestImageStream()));
			}
		});

		ByteArrayInputStream stream = (ByteArrayInputStream) getService().mergePdfFiles(paths);

		assertNotNull(stream);
		assertEquals(stream.available(), getTestImageStream().available());
	}

}
