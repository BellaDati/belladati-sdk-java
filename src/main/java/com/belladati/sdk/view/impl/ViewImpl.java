package com.belladati.sdk.view.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.exception.interval.InvalidIntervalException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.Filter.MultiValueFilter;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.belladati.sdk.intervals.AbsoluteInterval;
import com.belladati.sdk.intervals.CustomInterval;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.RelativeInterval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.util.impl.LocalizationImpl;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.belladati.sdk.view.export.ViewExporter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class ViewImpl implements View {

	/**
	 * 
	 * Builds an instance based on the given node. Will select an appropriate
	 * class to instantiate based on the view's type.
	 * 
	 * @param service BelladatiService
	 * @param node json node to create view from
	 * @return assembled ViewImpl
	 * @throws UnknownViewTypeException Thrown if view is unknow or not supported
	 */
	public static ViewImpl buildView(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		switch (parseType(node)) {
		case TABLE:
			return new TableViewImpl(service, node);
		case IMAGE:
			return new ImageViewImpl(service, node);
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
				} else if ("custom".equals(type)) {
					// a custom interval
					return new CustomInterval<DateUnit>(unit, interval.get("from").asText(), interval.get("to").asText());
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
				} else if ("custom".equals(type)) {
					// a custom interval
					return new CustomInterval<TimeUnit>(unit, interval.get("from").asText(), interval.get("to").asText());
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

	private static Set<Filter<?>> parseFilter(JsonNode node) {
		if (!node.hasNonNull("filter") || !node.get("filter").isObject() || !node.get("filter").hasNonNull("drilldown")
			|| !node.get("filter").get("drilldown").isObject()) {
			return Collections.emptySet();
		}

		Set<Filter<?>> filters = new HashSet<Filter<?>>();

		for (Iterator<Entry<String, JsonNode>> entries = node.get("filter").get("drilldown").fields(); entries.hasNext();) {
			Entry<String, JsonNode> entry = entries.next();
			String code = entry.getKey();
			String op = entry.getValue().get("op").asText();
			FilterOperation<?> operation = findOperation(op);
			if (operation != null) {
				Filter<?> filter = operation.createFilter(null, null, code);
				if (filter instanceof MultiValueFilter && entry.getValue().hasNonNull("values")) {
					for (JsonNode value : (ArrayNode) entry.getValue().get("values")) {
						((MultiValueFilter) filter).addAll(new FilterValue(value.asText()));
					}
				}
				filters.add(filter);
			}

		}
		return filters;
	}

	private static FilterOperation<?> findOperation(String op) {
		for (FilterOperation<?> operation : FilterOperation.values()) {
			if (operation.toString().equalsIgnoreCase(op)) {
				return operation;
			}
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

	private final Set<Filter<?>> filters;

	private final LocalizationImpl localization;

	protected ViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		this.service = service;

		this.id = node.get("id").asText();
		this.name = node.get("name").asText();
		this.type = parseType(node);
		this.filters = parseFilter(node);

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
	public boolean hasPredefinedFilters() {
		return !filters.isEmpty();
	}

	@Override
	public Set<Filter<?>> getPredefinedFilters() {
		return filters;
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

	@Override
	public ViewLoader createLoader() {
		return service.setupViewLoader(id, type);
	}

	@Override
	public ViewExporter createExporter() {
		return service.setupViewExporter(id);
	}

}
