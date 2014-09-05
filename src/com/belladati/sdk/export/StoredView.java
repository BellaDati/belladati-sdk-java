package com.belladati.sdk.export;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.LocalizationImpl;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.view.View;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;

abstract class StoredView extends StoredLocalizable implements View, Serializable {

	private static final long serialVersionUID = 554576890904608571L;
	private final ViewType type;

	private final ViewLoader loader = new StoredViewLoader();

	StoredView(String id, String name, ViewType type, LocalizationImpl localization) {
		super(id, name, localization);
		this.type = type;
	}

	@Override
	public ViewType getType() {
		return type;
	}

	@Override
	public boolean isDateIntervalSupported() {
		return false;
	}

	@Override
	public boolean isTimeIntervalSupported() {
		return false;
	}

	@Override
	public boolean hasPredefinedDateInterval() {
		return false;
	}

	@Override
	public boolean hasPredefinedTimeInterval() {
		return false;
	}

	@Override
	public Interval<DateUnit> getPredefinedDateInterval() {
		return null;
	}

	@Override
	public Interval<TimeUnit> getPredefinedTimeInterval() {
		return null;
	}

	@Override
	public ViewLoader createLoader() {
		return loader;
	}

	private class StoredViewLoader implements ViewLoader, Serializable {
		private static final long serialVersionUID = 7019763555105302040L;

		@Override
		public String getId() {
			return id;
		}

		@Override
		public ViewType getType() {
			return type;
		}

		@Override
		public Object loadContent() {
			return StoredView.this.loadContent();
		}

		@Override
		public ViewLoader setDateInterval(Interval<DateUnit> dateInterval) {
			return this;
		}

		@Override
		public ViewLoader setTimeInterval(Interval<TimeUnit> timeInterval) {
			return this;
		}

		@Override
		public ViewLoader addFilters(Filter<?>... filters) {
			return this;
		}

		@Override
		public ViewLoader addFilters(Collection<Filter<?>> filters) {
			return this;
		}

		@Override
		public ViewLoader setLocale(Locale locale) {
			return this;
		}

	}
}
