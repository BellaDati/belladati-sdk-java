package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.BellaDati;
import com.belladati.sdk.BellaDatiConnection;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.auth.OAuthRequest;
import com.belladati.sdk.test.TestRequestHandler;

@Test
public class SerializationTest extends SDKTest {

	/** Connections can be saved and restored. */
	public void saveRestoreConnection() throws IOException, ClassNotFoundException {
		// set up connnection and server
		BellaDatiConnection oldConnection = BellaDati.connect(server.getHttpURL());
		server.register("/oauth/accessToken", "oauth_token=abc123&oauth_token_secret=123abc");

		// serialize connection
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(baos);
		output.writeObject(oldConnection);
		output.close();
		baos.close();
		byte[] bytes = baos.toByteArray();

		// deserialize connection
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream input = new ObjectInputStream(bais);
		BellaDatiConnection newConnection = (BellaDatiConnection) input.readObject();
		input.close();
		bais.close();

		// use deserialized connection
		newConnection.xAuth("key", "secret", "username", "password");
		server.assertRequestUris("/oauth/accessToken");
	}

	/** Service instances can be saved and restored, including their tokens. */
	public void saveRestoreService() throws IOException, ClassNotFoundException {
		String reportsUri = "/api/reports";
		final String id = "123";
		server.register(reportsUri + "/" + id, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.assertAuth(service.tokenHolder.getConsumerKey(), service.tokenHolder.getToken());
				holder.response.setEntity(new StringEntity(builder.buildReportNode(id, "", "", "", null).toString()));
			}
		});

		// serialize service
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(baos);
		output.writeObject(service);
		output.close();
		baos.close();
		byte[] bytes = baos.toByteArray();

		// deserialize service
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream input = new ObjectInputStream(bais);
		BellaDatiService newService = (BellaDatiService) input.readObject();
		input.close();
		bais.close();

		// use deserialized service
		newService.loadReport(id);
		server.assertRequestUris(reportsUri + "/" + id);

		assertNotNull(newService.getDashboardInfo());
		assertNotNull(newService.getReportInfo());
		assertNotNull(newService.getDataSetInfo());
		assertNotNull(newService.getReportComments(""));
		assertNotNull(newService.getAttributeValues("", ""));
		assertNotNull(newService.getDataSources(""));
		assertNotNull(newService.getDataSourceImports(""));
	}

	/**
	 * Pending OAuth requests can be saved and restored, including their tokens.
	 */
	public void saveRestoreOAuthRequest() throws IOException, ClassNotFoundException {
		// set up connnection and server
		BellaDatiConnection connection = BellaDati.connect(server.getHttpURL());
		final String key = "key";
		final String requestToken = "abc123";
		server.register("/oauth/requestToken", "oauth_token=" + requestToken + "&oauth_token_secret=123abc");
		OAuthRequest oldRequest = connection.oAuth(key, "secret");
		server.resetRequestUris();

		// serialize request
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(baos);
		output.writeObject(oldRequest);
		output.close();
		baos.close();
		byte[] bytes = baos.toByteArray();

		// deserialize request
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream input = new ObjectInputStream(bais);
		OAuthRequest newRequest = (OAuthRequest) input.readObject();
		input.close();
		bais.close();

		assertEquals(newRequest.getAuthorizationUrl().toString(), server.getHttpURL() + "/authorizeRequestToken/" + requestToken
			+ "/" + key);

		server.register("/oauth/accessToken", new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				holder.assertAuth(key, requestToken);
				holder.response.setEntity(new StringEntity("oauth_token=access&oauth_token_secret=accessSecret"));
			}
		});
		newRequest.requestAccess();
		server.assertRequestUris("/oauth/accessToken");
	}
}
