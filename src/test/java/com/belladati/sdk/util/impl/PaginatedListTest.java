package com.belladati.sdk.util.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.Test;

import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.impl.TokenHolder;
import com.belladati.sdk.impl.VolatileBellaDatiClient;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.util.PaginatedIdList;
import com.belladati.sdk.util.PaginatedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Tests behavior of the {@link PaginatedList}.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class PaginatedListTest extends SDKTest {

	private PaginatedIdList<Item> list;
	private final String relativeUrl = "/list";
	private final String field = "field";

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadSizeNegative() throws Exception {
		registerResponse(1, 0);
		server.start();
		setupList();
		list.load(-1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadSizeZero() throws Exception {
		registerResponse(1, 0);
		server.start();
		setupList();
		list.load(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadBothSizeNegative() throws Exception {
		registerResponse(1, 0);
		server.start();
		setupList();
		list.load(0, -1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadBothSizeZero() throws Exception {
		registerResponse(1, 0);
		server.start();
		setupList();
		list.load(0, 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadBothPageNegative() throws Exception {
		registerResponse(1, 0);
		server.start();
		setupList();
		list.load(-1, 1);
	}

	/** Verifies no parameters sent to server on default load. */
	public void load() throws Exception {
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0).toString()));
			}
		});
		server.start();
		setupList();
		list.load();
	}

	/** Verifies parameters sent to server when size is set. */
	public void loadSize() throws Exception {
		final int size = 3;
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				Map<String, String> expectedMap = new HashMap<String, String>();
				expectedMap.put("size", "" + size);
				expectedMap.put("offset", "" + 0);
				assertEquals(holder.getUrlParameters(), expectedMap);
				holder.response.setEntity(new StringEntity(buildResponse(size, 0).toString()));
			}
		});
		server.start();
		setupList();
		list.load(size);
	}

	/** Verifies parameters sent to server when page and size are set. */
	public void loadSizePage() throws Exception {
		final int size = 3;
		final int page = 5;
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), buildParamMap(size, size * page));
				holder.response.setEntity(new StringEntity(buildResponse(size, 0).toString()));
			}
		});
		server.start();
		setupList();
		list.load(page, size);
	}

	/** Ensures old elements are discarded when the list is reloaded. */
	public void discardOnLoad() throws Exception {
		final String id1 = "id1";
		final String id2 = "id2";
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0, id1).toString()));
			}
		});
		setupList();

		list.load();
		assertEquals(list.toList(), Arrays.asList(new Item(id1)));

		setupServer();
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0, id2).toString()));
			}
		});

		setupList(true);
		list.load();
		assertEquals(list.toList(), Arrays.asList(new Item(id2)));
		assertEquals(list.toString(), Arrays.asList(new Item(id2)).toString());
	}

	/** loadNext() calls regular load when not yet loaded. */
	public void loadNextCallsLoad() throws Exception {
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0).toString()));
			}
		});
		server.start();
		setupList();
		list.loadNext();
	}

	/** loadNext() does nothing if the last page wasn't full. */
	public void loadNextEmpty() throws Exception {
		registerResponse(1, 0);
		setupList();
		list.load();
		list.loadNext();

		server.assertRequestUris(relativeUrl);
	}

	/** Next page is loaded correctly. */
	public void loadNext() throws Exception {
		final String id1 = "id1";
		final String id2 = "id2";

		registerResponse(1, 0, id1);
		setupList();
		list.load();

		setupServer();
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), buildParamMap(1, 1));
				holder.response.setEntity(new StringEntity(buildResponse(1, 1, id2).toString()));
			}
		});
		setupList(true);
		list.loadNext();

		setupServer();
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), buildParamMap(1, 2));
				holder.response.setEntity(new StringEntity(buildResponse(1, 2).toString()));
			}
		});
		setupList(true);
		list.loadNext();
		assertEquals(list.toList(), Arrays.asList(new Item(id1), new Item(id2)));
	}

	/** isLoaded() indicates whether the list has been loaded. */
	public void isLoaded() throws Exception {
		registerResponse(1, 0);
		setupList();
		assertFalse(list.isLoaded());

		list.load();

		assertTrue(list.isLoaded());
	}

	/** Initially we assume there's a next page. */
	public void hasNextInitial() throws Exception {
		server.start();
		setupList();
		assertTrue(list.hasNextPage());
	}

	/** No next page if list is empty on load. */
	public void hasNextLoadEmpty() throws Exception {
		registerResponse(1, 0);
		setupList();
		list.load();

		assertFalse(list.hasNextPage());
	}

	/** No next page if first page wasn't full. */
	public void hasNextLoadPartial() throws Exception {
		registerResponse(2, 0, "id");
		setupList();
		list.load();

		assertFalse(list.hasNextPage());
	}

	/** Has next page if first page was full. */
	public void hasNextLoadFull() throws Exception {
		registerResponse(2, 0, "id1", "id2");
		setupList();
		list.load();

		assertTrue(list.hasNextPage());
	}

	/** No next page if first page was full, second page empty. */
	public void hasNextEmptyAfterFull() throws Exception {
		registerResponse(2, 0, "id1", "id2");
		registerResponse(2, 2);
		server.start();
		setupList();

		list.load();
		list.loadNext();
		assertFalse(list.hasNextPage());
	}

	/** No next page if first page was full, second page partial. */
	public void hasNextPartialAfterFull() throws Exception {
		registerResponse(2, 0, "id1", "id2");
		setupList();
		list.load();

		setupServer();
		registerResponse(2, 2, "id3");
		setupList(true);
		list.loadNext();
		assertFalse(list.hasNextPage());
	}

	/** Has next page if first page was full, second page full. */
	public void hasNextFullAfterFull() throws Exception {
		registerResponse(2, 0, "id1", "id2");
		setupList();
		list.load();

		setupServer();
		registerResponse(2, 2, "id3", "id4");
		setupList(true);
		list.loadNext();
		assertTrue(list.hasNextPage());
	}

	/** First/last loaded page/index start out as -1. */
	public void initialFirstLastLoaded() throws Exception {
		setupList();
		assertEquals(list.getFirstLoadedIndex(), -1);
		assertEquals(list.getLastLoadedIndex(), -1);
		assertEquals(list.getFirstLoadedPage(), -1);
		assertEquals(list.getLastLoadedPage(), -1);
	}

	/** First loaded page is updated on load. */
	public void firstLoadedPage() throws Exception {
		registerResponse(1, 0);
		setupList();
		list.load();
		assertEquals(list.getFirstLoadedPage(), 0);

		setupServer();
		registerResponse(2, 10, "id", "id2");
		setupList(true);
		list.load(5, 2);
		assertEquals(list.getFirstLoadedPage(), 5);

		setupServer();
		registerResponse(2, 12);
		setupList(true);
		list.loadNext();
		assertEquals(list.getFirstLoadedPage(), 5);
	}

	/** Last loaded page is updated on load and loadNext. */
	public void lastLoadedPage() throws Exception {
		registerResponse(2, 10, "id", "id2");
		setupList();
		list.load(5, 2);
		assertEquals(list.getLastLoadedPage(), 5);

		setupServer();
		registerResponse(2, 12);
		setupList(true);
		list.loadNext();
		assertEquals(list.getLastLoadedPage(), 6);

		setupServer();
		registerResponse(1, 0, "id");
		setupList(true);
		list.load();
		assertEquals(list.getLastLoadedPage(), 0);
	}

	/** First loaded index is updated on load when items are found. */
	public void firstLoadedIndex() throws Exception {
		registerResponse(1, 0);
		setupList();
		list.load();
		assertEquals(list.getFirstLoadedIndex(), -1);

		setupServer();
		registerResponse(2, 10, "id", "id2");
		setupList(true);
		list.load(5, 2);
		assertEquals(list.getFirstLoadedIndex(), 10);

		setupServer();
		registerResponse(2, 12);
		setupList(true);
		list.loadNext();
		assertEquals(list.getFirstLoadedIndex(), 10);
	}

	/** Last loaded index is updated on load and loadNext when items are found. */
	public void lastLoadedIndex() throws Exception {
		registerResponse(2, 10, "id", "id2");
		setupList();
		list.load(5, 2);
		assertEquals(list.getLastLoadedIndex(), 11);

		setupServer();
		registerResponse(2, 12);
		setupList(true);
		list.loadNext();
		assertEquals(list.getLastLoadedIndex(), 11);

		setupServer();
		registerResponse(1, 0, "id");
		setupList(true);
		list.load();
		assertEquals(list.getLastLoadedIndex(), 0);

		setupServer();
		registerResponse(1, 0);
		setupList(true);
		list.load();
		assertEquals(list.getLastLoadedIndex(), -1);
	}

	/** Page size is updated on load. */
	public void pageSize() throws Exception {
		registerResponse(1, 0);
		setupList();
		assertEquals(list.getPageSize(), -1);

		setupServer();
		registerResponse(2, 0);
		setupList(true);
		list.load();
		assertEquals(list.getPageSize(), 2);
	}

	/** Page size returned by the server overrides local page size. */
	public void sizeMismatch() throws Exception {
		registerResponse(2, 0);
		setupList();
		list.load(1);
		assertEquals(list.getPageSize(), 2);
	}

	/** Offset returned by the server overrides local page. */
	public void pageMismatch() throws Exception {
		registerResponse(2, 10);
		setupList();
		list.load(2, 3);
		assertEquals(list.getFirstLoadedPage(), 5);
		assertEquals(list.getLastLoadedPage(), 5);
	}

	/** contains method checks elements since last load. */
	public void contains() throws Exception {
		String id = "id";
		setupList();
		assertFalse(list.contains(new Item(id)));

		setupServer();
		registerResponse(3, 0, "a", id, "b");
		setupList();
		list.load();
		assertTrue(list.contains(new Item(id)));

		setupServer();
		registerResponse(1, 0);
		setupList();
		list.load();
		assertFalse(list.contains(new Item(id)));
	}

	/** contains(id) method checks elements since last load. */
	public void containsId() throws Exception {
		String id = "id";
		setupList();
		assertFalse(list.contains(id));

		setupServer();
		registerResponse(3, 0, "a", id, "b");
		setupList();
		list.load();
		assertTrue(list.contains(id));

		setupServer();
		registerResponse(1, 0);
		setupList();
		list.load();
		assertFalse(list.contains(id));
	}

	/** get() before load throws exception. */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void getIndexBeforeLoad() throws Exception {
		setupList();
		list.get(0);
	}

	/** get() returns the element at the index. */
	public void getIndex() throws Exception {
		String id = "id";
		registerResponse(20, 40, id);
		setupList();
		list.load(2, 20);
		assertEquals(list.get(40), new Item(id));
	}

	/** get() with an index before the first loaded element throws exception. */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void getIndexTooLow() throws Exception {
		registerResponse(20, 40, "id");
		setupList();
		list.load(2, 20);
		list.get(39);
	}

	/** get() with an index after the last loaded element throws exception. */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void getIndexTooHigh() throws Exception {
		registerResponse(20, 40, "id", "id2");
		setupList();
		list.load(2, 20);
		list.get(42);
	}

	public void getIndexAfterLoadNext() throws Exception {
		final String id1 = "id1";
		final String id2 = "id2";

		registerResponse(1, 40, id1);
		setupList();
		list.load();

		setupServer();
		registerResponse(1, 41, id2);
		setupList();
		list.loadNext();

		assertEquals(list.get(41), new Item(id2));
	}

	/** indexOf method checks elements since last load. */
	public void indexOf() throws Exception {
		String id = "id";
		setupList();
		assertEquals(list.indexOf(new Item(id)), -1);

		setupServer();
		registerResponse(3, 0, "a", id, "b");
		setupList();
		list.load();
		assertEquals(list.indexOf(new Item(id)), 1);

		setupServer();
		registerResponse(3, 9, "a", id, "b");
		setupList();
		list.load();
		assertEquals(list.indexOf(new Item(id)), 10);

		setupServer();
		registerResponse(1, 0);
		setupList();
		list.load();
		assertEquals(list.indexOf(new Item(id)), -1);
	}

	/** indexOf(id) method checks elements since last load. */
	public void indexOfId() throws Exception {
		String id = "id";
		setupList();
		assertEquals(list.indexOf(id), -1);

		setupServer();
		registerResponse(3, 0, "a", id, "b");
		setupList();
		list.load();
		assertEquals(list.indexOf(id), 1);

		setupServer();
		registerResponse(3, 9, "a", id, "b");
		setupList();
		list.load();
		assertEquals(list.indexOf(id), 10);

		setupServer();
		registerResponse(1, 0);
		setupList();
		list.load();
		assertEquals(list.indexOf(id), -1);
	}

	/** List is empty if it hasn't been loaded or actually is empty. */
	public void isEmpty() throws Exception {
		setupList();
		assertTrue(list.isEmpty());

		setupServer();
		registerResponse(1, 0, "id");
		setupList();
		list.load();
		assertFalse(list.isEmpty());

		setupServer();
		registerResponse(1, 0);
		setupList();
		list.load();
		assertTrue(list.isEmpty());
	}

	/** size() returns number of currently loaded elements. */
	public void size() throws Exception {
		setupList();
		assertEquals(list.size(), 0);

		setupServer();
		registerResponse(1, 0, "id");
		setupList(true);
		list.load();
		assertEquals(list.size(), 1);

		setupServer();
		registerResponse(1, 1, "id2");
		setupList(true);
		list.loadNext();
		assertEquals(list.size(), 2);

		setupServer();
		registerResponse(1, 0);
		setupList(true);
		list.load();
		assertEquals(list.size(), 0);
	}

	/** iterator() allows iterating over the list. */
	public void iterator() throws Exception {
		final String id1 = "id1";
		final String id2 = "id2";

		setupList();
		assertFalse(list.iterator().hasNext());

		setupServer();
		registerResponse(1, 0, id1);
		setupList();
		list.load();

		Iterator<Item> iterator = list.iterator();
		assertEquals(iterator.next(), new Item(id1));
		assertFalse(iterator.hasNext());

		setupServer();
		registerResponse(1, 0, id2);
		setupList(true);
		list.loadNext();
		iterator = list.iterator();
		assertEquals(iterator.next(), new Item(id1));
		assertEquals(iterator.next(), new Item(id2));
		assertFalse(iterator.hasNext());
	}

	private Map<String, String> buildParamMap(int size, int offset) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("size", "" + size);
		paramMap.put("offset", "" + offset);
		return paramMap;
	}

	/**
	 * Builds an API response with the given size and offset parameters,
	 * containing elements with the given IDs.
	 * 
	 * @param size size field in the JSON object
	 * @param offset offset field in the JSON object
	 * @param ids list of IDs to return (optional)
	 * @return a JSON API response
	 */
	private JsonNode buildResponse(int size, int offset, String... ids) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ArrayNode items = mapper.createArrayNode();
		for (String id : ids) {
			items.add(mapper.createObjectNode().put("id", id));
		}
		node.put("size", "" + size).put("offset", "" + offset).put(field, items);
		return node;
	}

	/**
	 * Tells the server to respond to this test's URL with the given size,
	 * offset and element IDs.
	 * 
	 * @param size size field in the JSON response
	 * @param offset offset field in the JSON response
	 * @param ids list of IDs to return (optional)
	 */
	private void registerResponse(final int size, final int offset, final String... ids) {
		server.register(relativeUrl, buildResponse(size, offset, ids).toString());
	}

	protected void setupList() throws Exception {
		setupList(false);
	}

	private VolatileBellaDatiClient client;

	protected void setupList(boolean retainList) throws Exception {
		server.start();
		if (!retainList) {
			client = new VolatileBellaDatiClient(server.getHttpURL(), false);
			BellaDatiServiceImpl service = new BellaDatiServiceImpl(client, new TokenHolder("key", "secret"));
			list = new PaginatedIdListImpl<Item>(service, relativeUrl, field) {
				@Override
				protected Item parse(BellaDatiServiceImpl service, JsonNode node) {
					return new Item(node.get("id").asText());
				}
			};
		} else {
			client.setBaseUrl(server.getHttpURL());
		}
	}
}
