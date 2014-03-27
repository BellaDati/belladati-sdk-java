package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.exception.interval.InvalidAbsoluteIntervalException;
import com.belladati.sdk.exception.interval.InvalidRelativeIntervalException;
import com.belladati.sdk.exception.interval.NullIntervalException;
import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.intervals.AbsoluteInterval;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.IntervalUnit;
import com.belladati.sdk.intervals.RelativeInterval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.test.TestRequestHandler;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

@Test
public class IntervalTest extends SDKTest {

	private final String viewId = "viewId";
	private final String viewName = "name";
	private final String viewsUri = String.format("/api/reports/views/%s/chart", viewId);

	private final Calendar start = new GregorianCalendar(2012, 1, 2, 3, 4, 5);
	private final Calendar end = new GregorianCalendar(2013, 7, 8, 9, 10, 11);

	private final ObjectMapper mapper = new ObjectMapper();

	/** absolute time interval down to seconds */
	public void absoluteSecondsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.SECOND, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE)).put("second", start.get(Calendar.SECOND));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE))
			.put("second", end.get(Calendar.SECOND));

		JsonNode expectedJson = buildIntervalNode(TimeUnit.SECOND, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute time interval down to minutes */
	public void absoluteMinutesToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.MINUTE, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE));

		JsonNode expectedJson = buildIntervalNode(TimeUnit.MINUTE, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute time interval down to hours */
	public void absoluteHoursToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR));

		JsonNode expectedJson = buildIntervalNode(TimeUnit.HOUR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to days */
	public void absoluteDaysToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1).put("day", start.get(Calendar.DAY_OF_MONTH));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1)
			.put("day", end.get(Calendar.DAY_OF_MONTH));

		JsonNode expectedJson = buildIntervalNode(DateUnit.DAY, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to weeks */
	public void absoluteWeeksToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.WEEK, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("week", start.get(Calendar.WEEK_OF_YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("week", end.get(Calendar.WEEK_OF_YEAR));

		JsonNode expectedJson = buildIntervalNode(DateUnit.WEEK, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to months */
	public void absoluteMonthsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.MONTH, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1);

		JsonNode expectedJson = buildIntervalNode(DateUnit.MONTH, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to quarters */
	public void absoluteQuartersToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.QUARTER, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("quarter", start.get(Calendar.MONTH) / 3);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("quarter", end.get(Calendar.MONTH) / 3);

		JsonNode expectedJson = buildIntervalNode(DateUnit.QUARTER, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to years */
	public void absoluteYearsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.YEAR, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		JsonNode expectedJson = buildIntervalNode(DateUnit.YEAR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** relative intervals */
	@Test(dataProvider = "intervalUnitProvider")
	public void relativeToJson(IntervalUnit unit) {
		Interval<?> interval = new RelativeInterval<IntervalUnit>(unit, -10, 10);

		JsonNode from = new IntNode(-10);
		JsonNode to = new IntNode(10);

		JsonNode expectedJson = buildIntervalNode(unit, from, to, "relative");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** start and end may be equal in absolute intervals */
	public void startEndEqualAbsolute() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, start, start);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		ObjectNode to = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));

		JsonNode expectedJson = buildIntervalNode(TimeUnit.HOUR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** start and end may be equal in relative intervals */
	public void startEndEqualRelative() {
		Interval<?> interval = new RelativeInterval<IntervalUnit>(DateUnit.DAY, 10, 10);

		JsonNode from = new IntNode(10);
		JsonNode to = new IntNode(10);

		JsonNode expectedJson = buildIntervalNode(DateUnit.DAY, from, to, "relative");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** start may not be later than end in absolute intervals */
	public void startAfterEndAbsolute() {
		try {
			new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, end, start);
			fail("did not throw exception");
		} catch (InvalidAbsoluteIntervalException e) {
			assertEquals(e.getIntervalUnit(), TimeUnit.HOUR);
			assertEquals(e.getStart(), end);
			assertEquals(e.getEnd(), start);
		}
	}

	/** start may not be greater than end in relative intervals */
	public void startAfterEndRelative() {
		try {
			new RelativeInterval<IntervalUnit>(DateUnit.DAY, 10, -10);
			fail("did not throw exception");
		} catch (InvalidRelativeIntervalException e) {
			assertEquals(e.getIntervalUnit(), DateUnit.DAY);
			assertEquals((int) e.getStart(), 10);
			assertEquals((int) e.getEnd(), -10);
		}
	}

	/** interval unit may not be null in absolute intervals */
	@Test(expectedExceptions = NullIntervalException.class)
	public void unitNullAbsolute() {
		new AbsoluteInterval<IntervalUnit>(null, start, end);
	}

	/** interval unit may not be null in relative intervals */
	@Test(expectedExceptions = NullIntervalException.class)
	public void unitNullRelative() {
		new RelativeInterval<IntervalUnit>(null, -1, 1);
	}

	/** interval start may not be null */
	@Test(expectedExceptions = NullIntervalException.class)
	public void startNullAbsolute() {
		new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, null, end);
	}

	/** interval end may not be null */
	@Test(expectedExceptions = NullIntervalException.class)
	public void endNullAbsolute() {
		new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, start, null);
	}

	/** Query parameters with time only are correct. */
	public void queryStringTimeOnly() throws UnknownViewTypeException {
		final ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		final ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR));

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String intervalString = holder.getUrlParameters().get("dateTimeDefinition");
				assertNotNull(intervalString);
				ObjectNode intervalNode = buildIntervalNode(TimeUnit.HOUR, from, to, "absolute");
				assertEquals(mapper.readTree(intervalString), intervalNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		service.createViewLoader(viewId, ViewType.CHART)
			.setTimeInterval(new AbsoluteInterval<TimeUnit>(TimeUnit.HOUR, start, end)).loadContent();

		server.assertRequestUris(viewsUri);
	}

	/** Query parameters with date only are correct. */
	public void queryStringDateOnly() throws UnknownViewTypeException {
		final ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR));
		final ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String intervalString = holder.getUrlParameters().get("dateTimeDefinition");
				assertNotNull(intervalString);
				ObjectNode intervalNode = buildIntervalNode(DateUnit.YEAR, from, to, "absolute");
				assertEquals(mapper.readTree(intervalString), intervalNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		service.createViewLoader(viewId, ViewType.CHART)
			.setDateInterval(new AbsoluteInterval<DateUnit>(DateUnit.YEAR, start, end)).loadContent();

		server.assertRequestUris(viewsUri);
	}

	/** Query parameters with both intervals are correct. */
	public void queryStringBoth() throws UnknownViewTypeException {
		final ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR));
		final ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		server.register(viewsUri, new TestRequestHandler() {
			@Override
			protected void handle(HttpHolder holder) throws IOException {
				String intervalString = holder.getUrlParameters().get("dateTimeDefinition");
				assertNotNull(intervalString);
				ObjectNode intervalNode = buildIntervalNode(DateUnit.YEAR, from, to, "absolute");
				intervalNode.setAll(buildIntervalNode(TimeUnit.MINUTE, new IntNode(-10), new IntNode(10), "relative"));
				assertEquals(mapper.readTree(intervalString), intervalNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		service.createViewLoader(viewId, ViewType.CHART)
			.setDateInterval(new AbsoluteInterval<DateUnit>(DateUnit.YEAR, start, end))
			.setTimeInterval(new RelativeInterval<TimeUnit>(TimeUnit.MINUTE, -10, 10)).loadContent();

		server.assertRequestUris(viewsUri);
	}

	/** absolute time interval in seconds */
	public void predefinedSecondInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE)).put("second", start.get(Calendar.SECOND));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE))
			.put("second", end.get(Calendar.SECOND));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(TimeUnit.SECOND, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(0, 0, 0, start.get(Calendar.HOUR), start.get(Calendar.MINUTE),
			start.get(Calendar.SECOND));
		Calendar expectedEnd = new GregorianCalendar(0, 0, 0, end.get(Calendar.HOUR), end.get(Calendar.MINUTE),
			end.get(Calendar.SECOND));

		assertFalse(view.hasPredefinedDateInterval());
		assertTrue(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		AbsoluteInterval<TimeUnit> interval = (AbsoluteInterval<TimeUnit>) view.getPredefinedTimeInterval();
		assertEquals(interval.getIntervalUnit(), TimeUnit.SECOND);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
	}

	/** absolute time interval in minutes */
	public void predefinedMinuteInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(TimeUnit.MINUTE, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(0, 0, 0, start.get(Calendar.HOUR), start.get(Calendar.MINUTE), 0);
		Calendar expectedEnd = new GregorianCalendar(0, 0, 0, end.get(Calendar.HOUR), end.get(Calendar.MINUTE), 0);

		assertFalse(view.hasPredefinedDateInterval());
		assertTrue(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		AbsoluteInterval<TimeUnit> interval = (AbsoluteInterval<TimeUnit>) view.getPredefinedTimeInterval();
		assertEquals(interval.getIntervalUnit(), TimeUnit.MINUTE);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
	}

	/** absolute time interval in hours */
	public void predefinedHourInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(TimeUnit.HOUR, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(0, 0, 0, start.get(Calendar.HOUR), 0, 0);
		Calendar expectedEnd = new GregorianCalendar(0, 0, 0, end.get(Calendar.HOUR), 0, 0);

		assertFalse(view.hasPredefinedDateInterval());
		assertTrue(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		AbsoluteInterval<TimeUnit> interval = (AbsoluteInterval<TimeUnit>) view.getPredefinedTimeInterval();
		assertEquals(interval.getIntervalUnit(), TimeUnit.HOUR);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
	}

	/** absolute date interval in days */
	public void predefinedDayInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1).put("day", start.get(Calendar.DAY_OF_MONTH));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1)
			.put("day", end.get(Calendar.DAY_OF_MONTH));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.DAY, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(start.get(Calendar.YEAR), start.get(Calendar.MONTH),
			start.get(Calendar.DAY_OF_MONTH));
		Calendar expectedEnd = new GregorianCalendar(end.get(Calendar.YEAR), end.get(Calendar.MONTH),
			end.get(Calendar.DAY_OF_MONTH));

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.DAY);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
		assertNull(view.getPredefinedTimeInterval());
	}

	/** absolute week interval in weeks */
	public void predefinedWeekInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("week", start.get(Calendar.WEEK_OF_YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("week", end.get(Calendar.WEEK_OF_YEAR));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.WEEK, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(start.get(Calendar.YEAR), 0, 0);
		expectedStart.set(Calendar.WEEK_OF_YEAR, start.get(Calendar.WEEK_OF_YEAR));
		Calendar expectedEnd = new GregorianCalendar(end.get(Calendar.YEAR), 0, 0);
		expectedEnd.set(Calendar.WEEK_OF_YEAR, end.get(Calendar.WEEK_OF_YEAR));

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.WEEK);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
		assertNull(view.getPredefinedTimeInterval());
	}

	/** absolute date interval in months */
	public void predefinedMonthInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1);

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.MONTH, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(start.get(Calendar.YEAR), start.get(Calendar.MONTH), 0);
		Calendar expectedEnd = new GregorianCalendar(end.get(Calendar.YEAR), end.get(Calendar.MONTH), 0);

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.MONTH);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
		assertNull(view.getPredefinedTimeInterval());
	}

	/** absolute date interval in quarters */
	public void predefinedQuarterInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("quarter", start.get(Calendar.MONTH) / 3);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("quarter", end.get(Calendar.MONTH) / 3);

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.QUARTER, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(start.get(Calendar.YEAR), (start.get(Calendar.MONTH) / 3) * 3, 0);
		Calendar expectedEnd = new GregorianCalendar(end.get(Calendar.YEAR), (end.get(Calendar.MONTH) / 3) * 3, 0);

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.QUARTER);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
		assertNull(view.getPredefinedTimeInterval());
	}

	/** absolute date interval in years */
	public void predefinedYearInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.YEAR, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		Calendar expectedStart = new GregorianCalendar(start.get(Calendar.YEAR), 0, 0);
		Calendar expectedEnd = new GregorianCalendar(end.get(Calendar.YEAR), 0, 0);

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.YEAR);
		assertEquals(interval.getStart(), expectedStart);
		assertEquals(interval.getEnd(), expectedEnd);
		assertNull(view.getPredefinedTimeInterval());
	}

	/** predefined relative intervals */
	@Test(dataProvider = "intervalUnitProvider")
	public void predefinedRelativeInterval(IntervalUnit unit) throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(unit, new IntNode(-3), new IntNode(3), "rElAtIvE"));
		View view = ViewImpl.buildView(service, viewNode);

		if (unit instanceof DateUnit) {
			assertTrue(view.hasPredefinedDateInterval());
			assertFalse(view.hasPredefinedTimeInterval());
			RelativeInterval<DateUnit> interval = (RelativeInterval<DateUnit>) view.getPredefinedDateInterval();
			assertEquals(interval.getIntervalUnit(), unit);
			assertEquals(interval.getStart(), -3);
			assertEquals(interval.getEnd(), 3);
			assertNull(view.getPredefinedTimeInterval());
		} else {
			assertFalse(view.hasPredefinedDateInterval());
			assertTrue(view.hasPredefinedTimeInterval());
			assertNull(view.getPredefinedDateInterval());
			RelativeInterval<TimeUnit> interval = (RelativeInterval<TimeUnit>) view.getPredefinedTimeInterval();
			assertEquals(interval.getIntervalUnit(), unit);
			assertEquals(interval.getStart(), -3);
			assertEquals(interval.getEnd(), 3);
		}
	}

	/** an interval that's not absolute or relative */
	@Test(dataProvider = "intervalUnitProvider")
	public void predefinedNotAbsoluteOrRelativeInterval(IntervalUnit unit) throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(unit, new IntNode(-3), new IntNode(3), "something else"));
		View view = ViewImpl.buildView(service, viewNode);

		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** invalid relative intervals are ignored */
	@Test(dataProvider = "intervalUnitProvider")
	public void invalidRelativeInterval(IntervalUnit unit) throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(unit, new TextNode("abc"), new IntNode(3), "relative"));
		View view = ViewImpl.buildView(service, viewNode);

		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** invalid absolute intervals are ignored */
	public void invalidAbsoluteInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", "abc");
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.YEAR, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** intervals with numeric strings instead of numbers are allowed */
	@Test(dataProvider = "intervalUnitProvider")
	public void stringRelativeInterval(IntervalUnit unit) throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(unit, new TextNode("-3"), new IntNode(3), "relative"));
		View view = ViewImpl.buildView(service, viewNode);

		if (unit instanceof DateUnit) {
			assertTrue(view.hasPredefinedDateInterval());
			assertFalse(view.hasPredefinedTimeInterval());
			RelativeInterval<DateUnit> interval = (RelativeInterval<DateUnit>) view.getPredefinedDateInterval();
			assertEquals(interval.getIntervalUnit(), unit);
			assertEquals(interval.getStart(), -3);
			assertEquals(interval.getEnd(), 3);
			assertNull(view.getPredefinedTimeInterval());
		} else {
			assertFalse(view.hasPredefinedDateInterval());
			assertTrue(view.hasPredefinedTimeInterval());
			assertNull(view.getPredefinedDateInterval());
			RelativeInterval<TimeUnit> interval = (RelativeInterval<TimeUnit>) view.getPredefinedTimeInterval();
			assertEquals(interval.getIntervalUnit(), unit);
			assertEquals(interval.getStart(), -3);
			assertEquals(interval.getEnd(), 3);
		}
	}

	/** intervals with numeric strings instead of numbers are allowed */
	public void stringAbsoluteInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", "" + start.get(Calendar.YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.YEAR, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.YEAR);
		assertEquals(interval.getStart(), new GregorianCalendar(start.get(Calendar.YEAR), 0, 0));
		assertEquals(interval.getEnd(), new GregorianCalendar(end.get(Calendar.YEAR), 0, 0));
		assertNull(view.getPredefinedTimeInterval());
	}

	/** intervals with numeric fraction strings instead of numbers are allowed */
	@Test(dataProvider = "intervalUnitProvider")
	public void fractionRelativeInterval(IntervalUnit unit) throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(unit, new TextNode("-3.0"), new IntNode(3), "relative"));
		View view = ViewImpl.buildView(service, viewNode);

		if (unit instanceof DateUnit) {
			assertTrue(view.hasPredefinedDateInterval());
			assertFalse(view.hasPredefinedTimeInterval());
			RelativeInterval<DateUnit> interval = (RelativeInterval<DateUnit>) view.getPredefinedDateInterval();
			assertEquals(interval.getIntervalUnit(), unit);
			assertEquals(interval.getStart(), -3);
			assertEquals(interval.getEnd(), 3);
			assertNull(view.getPredefinedTimeInterval());
		} else {
			assertFalse(view.hasPredefinedDateInterval());
			assertTrue(view.hasPredefinedTimeInterval());
			assertNull(view.getPredefinedDateInterval());
			RelativeInterval<TimeUnit> interval = (RelativeInterval<TimeUnit>) view.getPredefinedTimeInterval();
			assertEquals(interval.getIntervalUnit(), unit);
			assertEquals(interval.getStart(), -3);
			assertEquals(interval.getEnd(), 3);
		}
	}

	/** intervals with numeric fraction strings instead of numbers are allowed */
	public void fractionAbsoluteInterval() throws UnknownViewTypeException {
		ObjectNode from = mapper.createObjectNode().put("year", "" + start.get(Calendar.YEAR) + ".0");
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		viewNode.put("dateTimeDefinition", buildIntervalNode(DateUnit.YEAR, from, to, "absolute"));
		View view = ViewImpl.buildView(service, viewNode);

		assertTrue(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		AbsoluteInterval<DateUnit> interval = (AbsoluteInterval<DateUnit>) view.getPredefinedDateInterval();
		assertEquals(interval.getIntervalUnit(), DateUnit.YEAR);
		assertEquals(interval.getStart(), new GregorianCalendar(start.get(Calendar.YEAR), 0, 0));
		assertEquals(interval.getEnd(), new GregorianCalendar(end.get(Calendar.YEAR), 0, 0));
		assertNull(view.getPredefinedTimeInterval());
	}

	/** intervals with invalid units are ignored */
	public void invalidUnits() throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		ObjectNode definitionNode = buildIntervalNode(DateUnit.DAY, new TextNode("-3"), new IntNode(3), "relative");
		definitionNode.setAll(buildIntervalNode(TimeUnit.HOUR, new TextNode("-3"), new IntNode(3), "relative"));
		((ObjectNode) definitionNode.get("dateInterval")).put("aggregationType", "not a date unit");
		((ObjectNode) definitionNode.get("timeInterval")).put("aggregationType", "not a time unit");
		viewNode.put("dateTimeDefinition", definitionNode);
		View view = ViewImpl.buildView(service, viewNode);

		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** intervals with null units are ignored */
	public void noUnits() throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		ObjectNode definitionNode = buildIntervalNode(DateUnit.DAY, new TextNode("-3"), new IntNode(3), "relative");
		definitionNode.setAll(buildIntervalNode(TimeUnit.HOUR, new TextNode("-3"), new IntNode(3), "relative"));
		((ObjectNode) definitionNode.get("dateInterval")).remove("aggregationType");
		((ObjectNode) definitionNode.get("timeInterval")).remove("aggregationType");
		viewNode.put("dateTimeDefinition", definitionNode);
		View view = ViewImpl.buildView(service, viewNode);

		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** intervals with start after end are ignored */
	public void startAfterEnd() throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		ObjectNode definitionNode = buildIntervalNode(DateUnit.DAY, new IntNode(3), new IntNode(-3), "relative");
		definitionNode.setAll(buildIntervalNode(TimeUnit.HOUR, new IntNode(3), new IntNode(-3), "relative"));
		viewNode.put("dateTimeDefinition", definitionNode);
		View view = ViewImpl.buildView(service, viewNode);

		assertFalse(view.hasPredefinedDateInterval());
		assertFalse(view.hasPredefinedTimeInterval());
		assertNull(view.getPredefinedDateInterval());
		assertNull(view.getPredefinedTimeInterval());
	}

	/** interval units are not case-sensitive */
	public void unitCaseInsensitive() throws UnknownViewTypeException {
		ObjectNode viewNode = builder.buildViewNode(viewId, viewName, "chart");
		ObjectNode definitionNode = buildIntervalNode(DateUnit.DAY, new TextNode("-3"), new IntNode(3), "relative");
		definitionNode.setAll(buildIntervalNode(TimeUnit.HOUR, new TextNode("-3"), new IntNode(3), "relative"));
		((ObjectNode) definitionNode.get("dateInterval")).put("aggregationType", "dAy");
		((ObjectNode) definitionNode.get("timeInterval")).put("aggregationType", "hOuR");
		viewNode.put("dateTimeDefinition", definitionNode);
		View view = ViewImpl.buildView(service, viewNode);

		assertTrue(view.hasPredefinedDateInterval());
		assertTrue(view.hasPredefinedTimeInterval());
	}

	/** two intervals with same parameters are equal */
	public void intervalEquals() {
		assertEquals(new RelativeInterval<IntervalUnit>(DateUnit.DAY, -1, 1), new RelativeInterval<IntervalUnit>(DateUnit.DAY,
			-1, 1));
		assertEquals(new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, start, end), new AbsoluteInterval<IntervalUnit>(
			DateUnit.DAY, start, end));
		assertEquals(new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, Calendar.getInstance(), Calendar.getInstance()),
			new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, Calendar.getInstance(), Calendar.getInstance()));

		assertNotEquals(new RelativeInterval<IntervalUnit>(DateUnit.DAY, -1, 1), new RelativeInterval<IntervalUnit>(DateUnit.DAY,
			-1, -1));
		assertNotEquals(new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, start, end), new AbsoluteInterval<IntervalUnit>(
			DateUnit.DAY, start, start));

		assertNotEquals(new RelativeInterval<IntervalUnit>(DateUnit.DAY, -1, 1), new RelativeInterval<IntervalUnit>(
			DateUnit.MONTH, -1, 1));
		assertNotEquals(new AbsoluteInterval<IntervalUnit>(DateUnit.DAY, start, end), new AbsoluteInterval<IntervalUnit>(
			DateUnit.MONTH, start, end));
	}

	@DataProvider(name = "intervalUnitProvider")
	protected Object[][] provideIntervalUnits() {
		List<IntervalUnit[]> unitList = new ArrayList<IntervalUnit[]>();
		for (IntervalUnit unit : DateUnit.values()) {
			unitList.add(new IntervalUnit[] { unit });
		}

		for (IntervalUnit unit : TimeUnit.values()) {
			unitList.add(new IntervalUnit[] { unit });
		}

		return unitList.toArray(new IntervalUnit[0][0]);
	}

	private ObjectNode buildIntervalNode(IntervalUnit intervalUnit, JsonNode from, JsonNode to, String type) {
		ObjectNode expectedJson = mapper.createObjectNode();
		ObjectNode timeInterval = mapper.createObjectNode();
		ObjectNode intervalNode = mapper.createObjectNode();
		String unitType = intervalUnit instanceof DateUnit ? "dateInterval" : "timeInterval";
		expectedJson.put(unitType, timeInterval);
		timeInterval.put("interval", intervalNode);
		timeInterval.put("aggregationType", intervalUnit.name());

		intervalNode.put("from", from);
		intervalNode.put("to", to);
		intervalNode.put("type", type);

		return expectedJson;
	}
}
