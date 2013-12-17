package com.belladati.sdk.impl;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	public void loadSizeNegative() {
		list.load(-1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadSizeZero() {
		list.load(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadBothSizeNegative() {
		list.load(0, -1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadBothSizeZero() {
		list.load(0, 0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void loadBothPageNegative() {
		list.load(-1, 1);
	}

	/** Verifies no parameters sent to server on default load. */
	public void load() {
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0).toString()));
			}
		});
		list.load();
	}

	/** Verifies parameters sent to server when size is set. */
	public void loadSize() {
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
		list.load(size);
	}

	/** Verifies parameters sent to server when page and size are set. */
	public void loadSizePage() {
		final int size = 3;
		final int page = 5;
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), buildParamMap(size, size * page));
				holder.response.setEntity(new StringEntity(buildResponse(size, 0).toString()));
			}
		});
		list.load(page, size);
	}

	/** Ensures old elements are discarded when the list is reloaded. */
	public void discardOnLoad() {
		final String id1 = "id1";
		final String id2 = "id2";
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0, id1).toString()));
			}
		});
		list.load();

		assertEquals(list.toList(), Arrays.asList(new Item(id1)));

		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0, id2).toString()));
			}
		});
		list.load();

		assertEquals(list.toList(), Arrays.asList(new Item(id2)));
		assertEquals(list.toString(), Arrays.asList(new Item(id2)).toString());
	}

	/** loadNext() calls regular load when not yet loaded. */
	public void loadNextCallsLoad() {
		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), Collections.emptyMap());
				holder.response.setEntity(new StringEntity(buildResponse(1, 0).toString()));
			}
		});
		list.loadNext();
	}

	/** loadNext() does nothing if the last page wasn't full. */
	public void loadNextEmpty() {
		list.load();
		list.loadNext();

		server.assertRequestUris(relativeUrl);
	}

	/** Next page is loaded correctly. */
	public void loadNext() {
		final String id1 = "id1";
		final String id2 = "id2";

		registerResponse(1, 0, id1);
		list.load();

		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), buildParamMap(1, 1));
				holder.response.setEntity(new StringEntity(buildResponse(1, 1, id2).toString()));
			}
		});
		list.loadNext();

		server.register(relativeUrl, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				assertEquals(holder.getUrlParameters(), buildParamMap(1, 2));
				holder.response.setEntity(new StringEntity(buildResponse(1, 2).toString()));
			}
		});
		list.loadNext();
		assertEquals(list.toList(), Arrays.asList(new Item(id1), new Item(id2)));
	}

	/** isLoaded() indicates whether the list has been loaded. */
	public void isLoaded() {
		assertFalse(list.isLoaded());

		list.load();

		assertTrue(list.isLoaded());
	}

	/** Initially we assume there's a next page. */
	public void hasNextInitial() {
		assertTrue(list.hasNextPage());
	}

	/** No next page if list is empty on load. */
	public void hasNextLoadEmpty() {
		list.load();

		assertFalse(list.hasNextPage());
	}

	/** No next page if first page wasn't full. */
	public void hasNextLoadPartial() {
		registerResponse(2, 0, "id");
		list.load();

		assertFalse(list.hasNextPage());
	}

	/** Has next page if first page was full. */
	public void hasNextLoadFull() {
		registerResponse(2, 0, "id1", "id2");
		list.load();

		assertTrue(list.hasNextPage());
	}

	/** No next page if first page was full, second page empty. */
	public void hasNextEmptyAfterFull() {
		registerResponse(2, 0, "id1", "id2");
		list.load();

		registerResponse(2, 2);
		list.loadNext();
		assertFalse(list.hasNextPage());
	}

	/** No next page if first page was full, second page partial. */
	public void hasNextPartialAfterFull() {
		registerResponse(2, 0, "id1", "id2");
		list.load();

		registerResponse(2, 2, "id3");
		list.loadNext();
		assertFalse(list.hasNextPage());
	}

	/** Has next page if first page was full, second page full. */
	public void hasNextFullAfterFull() {
		registerResponse(2, 0, "id1", "id2");
		list.load();

		registerResponse(2, 2, "id3", "id4");
		list.loadNext();
		assertTrue(list.hasNextPage());
	}

	/** First/last loaded page/index start out as -1. */
	public void initialFirstLastLoaded() {
		assertEquals(list.getFirstLoadedIndex(), -1);
		assertEquals(list.getLastLoadedIndex(), -1);
		assertEquals(list.getFirstLoadedPage(), -1);
		assertEquals(list.getLastLoadedPage(), -1);
	}

	/** First loaded page is updated on load. */
	public void firstLoadedPage() {
		list.load();
		assertEquals(list.getFirstLoadedPage(), 0);

		registerResponse(2, 10, "id", "id2");
		list.load(5, 2);
		assertEquals(list.getFirstLoadedPage(), 5);

		registerResponse(2, 12);
		list.loadNext();
		assertEquals(list.getFirstLoadedPage(), 5);
	}

	/** Last loaded page is updated on load and loadNext. */
	public void lastLoadedPage() {
		registerResponse(2, 10, "id", "id2");
		list.load(5, 2);
		assertEquals(list.getLastLoadedPage(), 5);

		registerResponse(2, 12);
		list.loadNext();
		assertEquals(list.getLastLoadedPage(), 6);

		registerResponse(1, 0, "id");
		list.load();
		assertEquals(list.getLastLoadedPage(), 0);
	}

	/** First loaded index is updated on load when items are found. */
	public void firstLoadedIndex() {
		list.load();
		assertEquals(list.getFirstLoadedIndex(), -1);

		registerResponse(2, 10, "id", "id2");
		list.load(5, 2);
		assertEquals(list.getFirstLoadedIndex(), 10);

		registerResponse(2, 12);
		list.loadNext();
		assertEquals(list.getFirstLoadedIndex(), 10);
	}

	/** Last loaded index is updated on load and loadNext when items are found. */
	public void lastLoadedIndex() {
		registerResponse(2, 10, "id", "id2");
		list.load(5, 2);
		assertEquals(list.getLastLoadedIndex(), 11);

		registerResponse(2, 12);
		list.loadNext();
		assertEquals(list.getLastLoadedIndex(), 11);

		registerResponse(1, 0, "id");
		list.load();
		assertEquals(list.getLastLoadedIndex(), 0);

		registerResponse(1, 0);
		list.load();
		assertEquals(list.getLastLoadedIndex(), -1);
	}

	/** Page size is updated on load. */
	public void pageSize() {
		assertEquals(list.getPageSize(), -1);

		registerResponse(2, 0);
		list.load();
		assertEquals(list.getPageSize(), 2);
	}

	/** Page size returned by the server overrides local page size. */
	public void sizeMismatch() {
		registerResponse(2, 0);
		list.load(1);
		assertEquals(list.getPageSize(), 2);
	}

	/** Offset returned by the server overrides local page. */
	public void pageMismatch() {
		registerResponse(2, 10);
		list.load(2, 3);
		assertEquals(list.getFirstLoadedPage(), 5);
		assertEquals(list.getLastLoadedPage(), 5);
	}

	/** contains method checks elements since last load. */
	public void contains() {
		String id = "id";
		assertFalse(list.contains(new Item(id)));

		registerResponse(3, 0, "a", id, "b");
		list.load();
		assertTrue(list.contains(new Item(id)));

		registerResponse(1, 0);
		list.load();
		assertFalse(list.contains(new Item(id)));
	}

	/** contains(id) method checks elements since last load. */
	public void containsId() {
		String id = "id";
		assertFalse(list.contains(id));

		registerResponse(3, 0, "a", id, "b");
		list.load();
		assertTrue(list.contains(id));

		registerResponse(1, 0);
		list.load();
		assertFalse(list.contains(id));
	}

	/** get() before load throws exception. */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void getIndexBeforeLoad() {
		list.get(0);
	}

	/** get() returns the element at the index. */
	public void getIndex() {
		String id = "id";
		registerResponse(20, 40, id);
		list.load(2, 20);
		assertEquals(list.get(40), new Item(id));
	}

	/** get() with an index before the first loaded element throws exception. */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void getIndexTooLow() {
		registerResponse(20, 40, "id");
		list.load(2, 20);
		list.get(39);
	}

	/** get() with an index after the last loaded element throws exception. */
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void getIndexTooHigh() {
		registerResponse(20, 40, "id", "id2");
		list.load(2, 20);
		list.get(42);
	}

	public void getIndexAfterLoadNext() {
		final String id1 = "id1";
		final String id2 = "id2";

		registerResponse(1, 40, id1);
		list.load();
		registerResponse(1, 41, id2);
		list.loadNext();

		assertEquals(list.get(41), new Item(id2));
	}

	/** indexOf method checks elements since last load. */
	public void indexOf() {
		String id = "id";
		assertEquals(list.indexOf(new Item(id)), -1);

		registerResponse(3, 0, "a", id, "b");
		list.load();
		assertEquals(list.indexOf(new Item(id)), 1);

		registerResponse(3, 9, "a", id, "b");
		list.load();
		assertEquals(list.indexOf(new Item(id)), 10);

		registerResponse(1, 0);
		list.load();
		assertEquals(list.indexOf(new Item(id)), -1);
	}

	/** indexOf(id) method checks elements since last load. */
	public void indexOfId() {
		String id = "id";
		assertEquals(list.indexOf(id), -1);

		registerResponse(3, 0, "a", id, "b");
		list.load();
		assertEquals(list.indexOf(id), 1);

		registerResponse(3, 9, "a", id, "b");
		list.load();
		assertEquals(list.indexOf(id), 10);

		registerResponse(1, 0);
		list.load();
		assertEquals(list.indexOf(id), -1);
	}

	/** List is empty if it hasn't been loaded or actually is empty. */
	public void isEmpty() {
		assertTrue(list.isEmpty());

		registerResponse(1, 0, "id");
		list.load();
		assertFalse(list.isEmpty());

		registerResponse(1, 0);
		list.load();
		assertTrue(list.isEmpty());
	}

	/** size() returns number of currently loaded elements. */
	public void size() {
		assertEquals(list.size(), 0);

		registerResponse(1, 0, "id");
		list.load();
		assertEquals(list.size(), 1);

		registerResponse(1, 1, "id2");
		list.loadNext();
		assertEquals(list.size(), 2);

		registerResponse(1, 0);
		list.load();
		assertEquals(list.size(), 0);
	}

	/** iterator() allows iterating over the list. */
	public void iterator() {
		final String id1 = "id1";
		final String id2 = "id2";

		assertFalse(list.iterator().hasNext());

		registerResponse(1, 0, id1);
		list.load();

		Iterator<Item> iterator = list.iterator();
		assertEquals(iterator.next(), new Item(id1));
		assertFalse(iterator.hasNext());

		registerResponse(1, 0, id2);
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

	@BeforeMethod
	protected void setupList() {
		BellaDatiClient client = new BellaDatiClient(server.getHttpURL(), false);
		BellaDatiServiceImpl service = new BellaDatiServiceImpl(client, new TokenHolder("key", "secret"));
		list = new PaginatedIdListImpl<Item>(service, relativeUrl, field) {
			@Override
			protected Item parse(BellaDatiServiceImpl service, JsonNode node) {
				return new Item(node.get("id").asText());
			}
		};
		registerResponse(1, 0);
	}
}
