package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import org.testng.annotations.Test;

import com.belladati.sdk.dataset.data.OverwritePolicy;
import com.belladati.sdk.exception.dataset.data.NoColumnsException;
import com.belladati.sdk.exception.interval.InvalidAbsoluteIntervalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class OverwritePolicyTest extends SDKTest {

	private final String attribute = "attribute";

	/** never overwrite */
	public void noOverwrite() {
		assertEquals(OverwritePolicy.deleteNone().toJson(), new ObjectMapper().createObjectNode());
	}

	/** delete all */
	public void deleteAll() {
		JsonNode json = OverwritePolicy.deleteAll().toJson();
		assertEquals(json, new ObjectMapper().createObjectNode().put("policy", "DELETE_ALL"));
	}

	/** overwrite when all attributes match */
	public void allAttributes() {
		JsonNode json = OverwritePolicy.byAllAttributes().toJson();
		assertEquals(json.get("policy").asText(), "DELETE_BY_MEMBERS");
		assertTrue(Boolean.parseBoolean(json.get("attributesAll").asText()));
	}

	/** no attributes set */
	@Test(expectedExceptions = NoColumnsException.class)
	public void attributesNone() {
		OverwritePolicy.byAttributes(Collections.<String> emptyList());
	}

	/** attributes set as strings */
	public void attributesStrings() {
		String a2 = "a2";

		JsonNode json = OverwritePolicy.byAttributes(attribute, a2).toJson();
		assertEquals(json.get("policy").asText(), "DELETE_BY_MEMBERS");
		assertFalse(json.hasNonNull("attributesAll"));
		assertEquals(json.get("attributes"), new ObjectMapper().createArrayNode().add(attribute).add(a2));
	}

	/** attributes set as list */
	public void attributesList() {
		String a2 = "a2";

		assertEquals(OverwritePolicy.byAttributes(Arrays.asList(attribute, a2)).toJson(),
			OverwritePolicy.byAttributes(attribute, a2).toJson());
	}

	/** date range */
	public void dateFromTo() {
		Calendar start = new GregorianCalendar(2012, 3, 18);
		Calendar end = new GregorianCalendar(2013, 11, 12);
		JsonNode json = OverwritePolicy.byDateFromTo(attribute, start, end).toJson();

		assertEquals(json.get("policy").asText(), "DELETE_ALL");
		assertEquals(json.get("dateAttribute").asText(), attribute);
		assertEquals(json.get("dateFrom").asText(), "2012-04-18");
		assertEquals(json.get("dateTo").asText(), "2013-12-12");
	}

	/** date range with end before start */
	@Test(expectedExceptions = InvalidAbsoluteIntervalException.class)
	public void dateFromBeforeTo() {
		Calendar start = new GregorianCalendar(2012, 3, 18);
		Calendar end = new GregorianCalendar(2012, 3, 17);
		OverwritePolicy.byDateFromTo(attribute, start, end);
	}

	/** changing the calendar objects doesn't change the interval */
	public void dateImmutable() {
		Calendar start = new GregorianCalendar(2012, 3, 18);
		Calendar end = new GregorianCalendar(2013, 11, 12);
		OverwritePolicy policy = OverwritePolicy.byDateFromTo(attribute, start, end);

		start.set(Calendar.MONTH, 0);
		end.set(Calendar.MONTH, 0);

		JsonNode json = policy.toJson();

		assertEquals(json.get("dateFrom").asText(), "2012-04-18");
		assertEquals(json.get("dateTo").asText(), "2013-12-12");
	}

	/** all entries from a date */
	public void dateFrom() {
		Calendar start = new GregorianCalendar(2012, 3, 18);
		JsonNode json = OverwritePolicy.byDateFrom(attribute, start).toJson();

		assertEquals(json.get("policy").asText(), "DELETE_ALL");
		assertEquals(json.get("dateAttribute").asText(), attribute);
		assertEquals(json.get("dateFrom").asText(), "2012-04-18");
		assertFalse(json.hasNonNull("dateTo"));
	}

	/** all entries before a date */
	public void dateTo() {
		Calendar end = new GregorianCalendar(2012, 3, 18);
		JsonNode json = OverwritePolicy.byDateTo(attribute, end).toJson();

		assertEquals(json.get("policy").asText(), "DELETE_ALL");
		assertEquals(json.get("dateAttribute").asText(), attribute);
		assertEquals(json.get("dateTo").asText(), "2012-04-18");
		assertFalse(json.hasNonNull("dateFrom"));
	}
}
