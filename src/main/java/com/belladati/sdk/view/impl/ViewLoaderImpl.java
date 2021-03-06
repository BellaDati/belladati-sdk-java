package com.belladati.sdk.view.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.utils.URIBuilder;

import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.belladati.sdk.view.impl.TableViewImpl.TableImpl;
import com.fasterxml.jackson.databind.JsonNode;

public class ViewLoaderImpl implements ViewLoader {

	private final BellaDatiServiceImpl service;
	private final String viewId;
	private final ViewType viewType;
	private final List<Filter<?>> filters = new ArrayList<Filter<?>>();

	private Interval<DateUnit> dateInterval;
	private Interval<TimeUnit> timeInterval;

	private Locale locale;

	public ViewLoaderImpl(BellaDatiServiceImpl service, String viewId, ViewType viewType) {
		this.service = service;
		this.viewId = viewId;
		this.viewType = viewType;
	}

	@Override
	public String getId() {
		return viewId;
	}

	@Override
	public ViewType getType() {
		return viewType;
	}

	@Override
	public ViewLoader setDateInterval(Interval<DateUnit> dateInterval) {
		this.dateInterval = dateInterval;
		return this;
	}

	@Override
	public ViewLoader setTimeInterval(Interval<TimeUnit> timeInterval) {
		this.timeInterval = timeInterval;
		return this;
	}

	@Override
	public ViewLoader addFilters(Filter<?>... filters) {
		return addFilters(Arrays.asList(filters));
	}

	@Override
	public ViewLoader addFilters(Collection<Filter<?>> filters) {
		this.filters.addAll(filters);
		return this;
	}

	@Override
	public ViewLoader setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	@Override
	public Object loadContent() {
		try {
			URIBuilder builder = new URIBuilder("api/reports/views/" + viewId + "/" + viewType.getUri());

			if (viewType == ViewType.IMAGE) {
				return new ImageViewImpl.ImageImpl(viewId, service.getAsImage(builder.build().toString()));
			}

			JsonNode json = service.getAsJson(service
				.appendLocale(service.appendDateTime(service.appendFilter(builder, filters), dateInterval, timeInterval), locale)
				.build().toString());
			if (viewType == ViewType.TABLE) {
				return new TableImpl(service, viewId, json, filters).setLocale(locale);
			}
			return json;
		} catch (URISyntaxException e) {
			throw new InternalConfigurationException(e);
		}
	}
}
