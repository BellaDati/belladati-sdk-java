package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
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
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class IntervalTest extends SDKTest {

	private final String viewId = "viewId";
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

		JsonNode expectedJson = buildExpectedNode(TimeUnit.SECOND, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute time interval down to minutes */
	public void absoluteMinutesToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.MINUTE, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR))
			.put("minute", start.get(Calendar.MINUTE));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR)).put("minute", end.get(Calendar.MINUTE));

		JsonNode expectedJson = buildExpectedNode(TimeUnit.MINUTE, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute time interval down to hours */
	public void absoluteHoursToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, start, end);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		ObjectNode to = mapper.createObjectNode().put("hour", end.get(Calendar.HOUR));

		JsonNode expectedJson = buildExpectedNode(TimeUnit.HOUR, from, to, "absolute");

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

		JsonNode expectedJson = buildExpectedNode(DateUnit.DAY, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to weeks */
	public void absoluteWeeksToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.WEEK, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("week", start.get(Calendar.WEEK_OF_YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("week", end.get(Calendar.WEEK_OF_YEAR));

		JsonNode expectedJson = buildExpectedNode(DateUnit.WEEK, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to months */
	public void absoluteMonthsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.MONTH, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("month", start.get(Calendar.MONTH) + 1);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("month", end.get(Calendar.MONTH) + 1);

		JsonNode expectedJson = buildExpectedNode(DateUnit.MONTH, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to quarters */
	public void absoluteQuartersToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.QUARTER, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR))
			.put("quarter", start.get(Calendar.MONTH) / 3);
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR)).put("quarter", end.get(Calendar.MONTH) / 3);

		JsonNode expectedJson = buildExpectedNode(DateUnit.QUARTER, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** absolute date interval down to years */
	public void absoluteYearsToJson() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(DateUnit.YEAR, start, end);

		ObjectNode from = mapper.createObjectNode().put("year", start.get(Calendar.YEAR));
		ObjectNode to = mapper.createObjectNode().put("year", end.get(Calendar.YEAR));

		JsonNode expectedJson = buildExpectedNode(DateUnit.YEAR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** relative intervals */
	@Test(dataProvider = "intervalUnitProvider")
	public void relativeToJson(IntervalUnit unit) {
		Interval<?> interval = new RelativeInterval<IntervalUnit>(unit, -10, 10);

		JsonNode from = new IntNode(-10);
		JsonNode to = new IntNode(10);

		JsonNode expectedJson = buildExpectedNode(unit, from, to, "relative");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** start and end may be equal in absolute intervals */
	public void startEndEqualAbsolute() {
		Interval<?> interval = new AbsoluteInterval<IntervalUnit>(TimeUnit.HOUR, start, start);

		ObjectNode from = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));
		ObjectNode to = mapper.createObjectNode().put("hour", start.get(Calendar.HOUR));

		JsonNode expectedJson = buildExpectedNode(TimeUnit.HOUR, from, to, "absolute");

		assertEquals(interval.toJson(), expectedJson);
		assertEquals(interval.toString(), expectedJson.toString());
	}

	/** start and end may be equal in relative intervals */
	public void startEndEqualRelative() {
		Interval<?> interval = new RelativeInterval<IntervalUnit>(DateUnit.DAY, 10, 10);

		JsonNode from = new IntNode(10);
		JsonNode to = new IntNode(10);

		JsonNode expectedJson = buildExpectedNode(DateUnit.DAY, from, to, "relative");

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
				ObjectNode intervalNode = buildExpectedNode(TimeUnit.HOUR, from, to, "absolute");
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
				ObjectNode intervalNode = buildExpectedNode(DateUnit.YEAR, from, to, "absolute");
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
				ObjectNode intervalNode = buildExpectedNode(DateUnit.YEAR, from, to, "absolute");
				intervalNode.setAll(buildExpectedNode(TimeUnit.MINUTE, new IntNode(-10), new IntNode(10), "relative"));
				assertEquals(mapper.readTree(intervalString), intervalNode);
				holder.response.setEntity(new StringEntity("{}"));
			}
		});

		service.createViewLoader(viewId, ViewType.CHART)
			.setDateInterval(new AbsoluteInterval<DateUnit>(DateUnit.YEAR, start, end))
			.setTimeInterval(new RelativeInterval<TimeUnit>(TimeUnit.MINUTE, -10, 10)).loadContent();

		server.assertRequestUris(viewsUri);
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

	private ObjectNode buildExpectedNode(IntervalUnit intervalUnit, JsonNode from, JsonNode to, String type) {
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
