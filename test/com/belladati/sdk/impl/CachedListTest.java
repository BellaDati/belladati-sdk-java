package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class CachedListTest extends SDKTest {

	private CachedList<Item> list;

	private final String uri = "/list";
	private final String field = "field";

	/** Loading a cached list calls the right URL. */
	public void load() {
		assertEquals(list.get(), Collections.emptyList());

		server.register(uri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse().toString()));
			}
		});
		list.load();

		server.assertRequestUris(uri);

		assertEquals(list.get(), Collections.emptyList());
		assertEquals(list.get(), list.toList());
	}

	/** first time load is called just once */
	public void loadFirstTime() {
		server.register(uri, buildResponse().toString());

		list.loadFirstTime();
		assertTrue(list.isLoaded());

		list.loadFirstTime();

		server.assertRequestUris(uri);
	}

	/** Items are parsed correctly. */
	public void loadItems() {

		final String id1 = "id1";
		final String id2 = "id2";

		server.register(uri, buildResponse(id1, id2).toString());
		list.load();

		Item item1 = new Item(id1);
		Item item2 = new Item(id2);
		assertEquals(list.get(), Arrays.asList(item1, item2));
		assertEquals(list.get(), list.toList());

		assertEquals(list.toString(), list.get().toString());
	}

	/** Items are parsed correctly during loadFirstTime. */
	public void loadFirstTimeItems() {

		final String id1 = "id1";
		final String id2 = "id2";

		server.register(uri, buildResponse(id1, id2).toString());
		list.loadFirstTime();

		Item item1 = new Item(id1);
		Item item2 = new Item(id2);
		assertEquals(list.get(), Arrays.asList(item1, item2));
		assertEquals(list.get(), list.toList());

		assertEquals(list.toString(), list.get().toString());
	}

	/** Previously loaded items are discarded when load is called again. */
	public void discardOnLoad() {
		final String id1 = "id1";
		final String id2 = "id2";

		server.register(uri, buildResponse(id1).toString());
		list.load();

		server.register(uri, buildResponse(id2).toString());
		list.load();

		assertEquals(list.get(), Arrays.asList(new Item(id2)));
	}

	/** Previously loaded items are not discarded when loadFirstTime is called. */
	public void noDiscardOnLoadFirstTime() {
		final String id1 = "id1";
		final String id2 = "id2";

		server.register(uri, buildResponse(id1).toString());
		list.load();

		server.register(uri, buildResponse(id2).toString());
		list.loadFirstTime();

		server.assertRequestUris(uri);
		assertEquals(list.get(), Arrays.asList(new Item(id1)));
	}

	/** isLoaded is set correctly */
	public void isLoaded() {
		assertFalse(list.isLoaded());

		server.register(uri, buildResponse().toString());
		list.load();

		assertTrue(list.isLoaded());
	}

	private JsonNode buildResponse(String... ids) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ArrayNode items = mapper.createArrayNode();
		for (String id : ids) {
			items.add(mapper.createObjectNode().put("id", id));
		}
		node.put(field, items);
		return node;
	}

	@BeforeMethod
	protected void setupList() {
		BellaDatiClient client = new BellaDatiClient(server.getHttpURL(), false);
		BellaDatiServiceImpl service = new BellaDatiServiceImpl(client, new TokenHolder("key", "secret"));
		list = new CachedListImpl<Item>(service, uri, field) {
			@Override
			protected Item parse(BellaDatiServiceImpl service, JsonNode node) {
				return new Item(node.get("id").asText());
			}
		};
	}
}
