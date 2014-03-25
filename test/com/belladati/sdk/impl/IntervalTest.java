package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.testng.annotations.Test;

import com.belladati.sdk.intervals.AbsoluteInterval;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.IntervalUnit;
import com.belladati.sdk.intervals.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class IntervalTest extends SDKTest {

	private final Calendar start = new GregorianCalendar(2012, 1, 2, 3, 4, 5);
	private final Calendar end = new GregorianCalendar(2013, 7, 8, 9, 10, 11);

	private final ObjectMapper mapper = new ObjectMapper();

	public void absoluteSecondsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.SECOND, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE)).put("second", start.get(Calendar.SECOND));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE))
			.put("second", end.get(Calendar.SECOND));

		JsonNode expectedJson = buildExpectedNode(TimeUnit.SECOND, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteMinutesToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.MINUTE, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE));

		JsonNode expectedJson = buildExpectedNode(TimeUnit.MINUTE, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteHoursToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR));

		JsonNode expectedJson = buildExpectedNode(TimeUnit.HOUR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteDaysToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1).put("day", start.get(Calendar.DAY_OF_MONTH));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1)
			.put("day", end.get(Calendar.DAY_OF_MONTH));

		JsonNode expectedJson = buildExpectedNode(DateUnit.DAY, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteWeeksToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.WEEK, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("week", start.get(Calendar.WEEK_OF_YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("week", end.get(Calendar.WEEK_OF_YEAR));

		JsonNode expectedJson = buildExpectedNode(DateUnit.WEEK, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteMonthsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.MONTH, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1);

		JsonNode expectedJson = buildExpectedNode(DateUnit.MONTH, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteQuartersToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.QUARTER, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("quarter", start.get(Calendar.MONTH) / 3);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("quarter", end.get(Calendar.MONTH) / 3);

		JsonNode expectedJson = buildExpectedNode(DateUnit.QUARTER, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	public void absoluteYearsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.YEAR, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		JsonNode expectedJson = buildExpectedNode(DateUnit.YEAR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	private ObjectNode buildExpectedNode(IntervalUnit intervalUnit, JsonNode from, JsonNode to, String type) {
		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode timeInterval = mapper.createObjectNode();
		ObjectNode intervalNode = mapper.createObjectNode();
		expectedJson.put("timeInterval", timeInterval);
		timeInterval.put("interval", intervalNode);
		timeInterval.put("aggregationType", intervalUnit.name());

		intervalNode.put("from", from);
		intervalNode.put("to", to);
		intervalNode.put("type", type);

		return expectedJson;
	}
}
