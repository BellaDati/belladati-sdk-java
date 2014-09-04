package com.belladati.sdk.impl;

import java.util.Collection;
import java.util.Locale;

import com.belladati.sdk.exception.interval.InvalidIntervalException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.intervals.AbsoluteInterval;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.RelativeInterval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class ViewImpl implements View {

	/**
	 * Builds an instance based on the given node. Will select an appropriate
	 * class to instantiate based on the view's type.
	 */
	static ViewImpl buildView(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		switch (parseType(node)) {
		case TABLE:
			return new TableViewImpl(service, node);
		default:
			return new JsonViewImpl(service, node);
		}
	}

	/**
	 * Parses the view type from the given JSON node.
	 * 
	 * @param node the node to examine
	 * @return the view type from the node
	 * @throws UnknownViewTypeException if no view type was found or it couldn't
	 *             be parsed
	 */
	private static ViewType parseType(JsonNode node) throws UnknownViewTypeException {
		if (node.hasNonNull("type")) {
			try {
				return ViewType.valueOf(node.get("type").asText().toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException e) {
				throw new UnknownViewTypeException(node.get("type").asText());
			}
		} else {
			throw new UnknownViewTypeException("missing type");
		}
	}

	/**
	 * Parses the date interval from the given date/time definition node.
	 * 
	 * @param node the node to examine
	 * @return the node's date interval, or <tt>null</tt> if none is defined or
	 *         it's invalid
	 */
	private static Interval<DateUnit> parseDateInterval(JsonNode node) {
		try {
			if (node.hasNonNull("dateInterval") && node.get("dateInterval").hasNonNull("aggregationType")) {
				JsonNode dateInterval = node.get("dateInterval");
				DateUnit unit = DateUnit.valueOf(dateInterval.get("aggregationType").asText().toUpperCase(Locale.ENGLISH));
				JsonNode interval = dateInterval.get("interval");
				String type = interval.get("type").asText().toLowerCase(Locale.ENGLISH);
				if ("relative".equals(type)) {
					// the server may send numbers inside strings
					// or numbers as decimals, e.g. 3.0
					// regardless, we treat them all as int
					String from = interval.get("from").asText();
					String to = interval.get("to").asText();
					return new RelativeInterval<DateUnit>(unit, (int) Float.parseFloat(from), (int) Float.parseFloat(to));
				} else if ("absolute".equals(type)) {
					// an absolute interval
					return new AbsoluteInterval<DateUnit>(unit, unit.parseAbsolute(interval.get("from")),
						unit.parseAbsolute(interval.get("to")));
				}
			}
		} catch (InvalidIntervalException e) {
			// ignore the interval
		} catch (NumberFormatException e) {
			// ignore the interval
		} catch (IllegalArgumentException e) {
			// ignore the interval
		}
		return null;
	}

	/**
	 * Parses the time interval from the given date/time definition node.
	 * 
	 * @param node the node to examine
	 * @return the node's time interval, or <tt>null</tt> if none is defined or
	 *         it's invalid
	 */
	private static Interval<TimeUnit> parseTimeInterval(JsonNode node) {
		try {
			if (node.hasNonNull("timeInterval") && node.get("timeInterval").hasNonNull("aggregationType")) {
				JsonNode timeInterval = node.get("timeInterval");
				TimeUnit unit = TimeUnit.valueOf(timeInterval.get("aggregationType").asText().toUpperCase(Locale.ENGLISH));
				JsonNode interval = timeInterval.get("interval");
				String type = interval.get("type").asText().toLowerCase(Locale.ENGLISH);
				if ("relative".equals(type)) {
					// the server may send numbers inside strings
					// or numbers as decimals, e.g. 3.0
					// regardless, we treat them all as int
					String from = interval.get("from").asText();
					String to = interval.get("to").asText();
					return new RelativeInterval<TimeUnit>(unit, (int) Float.parseFloat(from), (int) Float.parseFloat(to));
				} else if ("absolute".equals(type)) {
					// an absolute interval
					return new AbsoluteInterval<TimeUnit>(unit, unit.parseAbsolute(interval.get("from")),
						unit.parseAbsolute(interval.get("to")));
				}
			}
		} catch (InvalidIntervalException e) {
			// ignore the interval
		} catch (NumberFormatException e) {
			// ignore the interval
		} catch (IllegalArgumentException e) {
			// ignore the interval
		}
		return null;
	}

	protected final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final ViewType type;
	private final boolean dateIntervalSupported;
	private final boolean timeIntervalSupported;

	private final Interval<DateUnit> dateInterval;
	private final Interval<TimeUnit> timeInterval;

	private final LocalizationImpl localization;

	ViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		this.service = service;

		this.id = node.get("id").asText();
		this.name = node.get("name").asText();
		this.type = parseType(node);

		if (node.hasNonNull("dateTimeDefinition") && this.type != ViewType.TABLE) {
			// we have a date/time definition and are not dealing with a table
			JsonNode definition = node.get("dateTimeDefinition");
			if (definition.hasNonNull("dateSupported")) {
				dateIntervalSupported = definition.get("dateSupported").asBoolean();
			} else {
				dateIntervalSupported = false;
			}
			if (definition.hasNonNull("timeSupported")) {
				timeIntervalSupported = definition.get("timeSupported").asBoolean();
			} else {
				timeIntervalSupported = false;
			}
			dateInterval = parseDateInterval(definition);
			timeInterval = parseTimeInterval(definition);
		} else {
			dateIntervalSupported = false;
			timeIntervalSupported = false;
			dateInterval = null;
			timeInterval = null;
		}

		this.localization = new LocalizationImpl(node);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getName(Locale locale) {
		return localization.getName(locale);
	}

	@Override
	public boolean hasLocalization(Locale locale) {
		return localization.hasLocalization(locale);
	}

	@Override
	public ViewType getType() {
		return type;
	}

	@Override
	public Object loadContent(Filter<?>... filters) {
		return service.loadViewContent(id, type, filters);
	}

	@Override
	public Object loadContent(Collection<Filter<?>> filters) {
		return service.loadViewContent(id, type, filters);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViewImpl) {
			return id.equals(((ViewImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean isDateIntervalSupported() {
		return dateIntervalSupported;
	}

	@Override
	public boolean isTimeIntervalSupported() {
		return timeIntervalSupported;
	}

	@Override
	public boolean hasPredefinedDateInterval() {
		return dateInterval != null;
	}

	@Override
	public boolean hasPredefinedTimeInterval() {
		return timeInterval != null;
	}

	@Override
	public Interval<DateUnit> getPredefinedDateInterval() {
		return dateInterval;
	}

	@Override
	public Interval<TimeUnit> getPredefinedTimeInterval() {
		return timeInterval;
	}

	public LocalizationImpl getLocalization() {
		return localization;
	}

	@Override
	public ViewLoader createLoader() {
		return new ViewLoaderImpl(service, id, type);
	}

	static class UnknownViewTypeException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -9179478821813868612L;

		public UnknownViewTypeException(String type) {
			super("Unknown view type: " + type);
		}
	}

}
