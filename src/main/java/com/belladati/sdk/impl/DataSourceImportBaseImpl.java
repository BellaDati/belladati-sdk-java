package com.belladati.sdk.impl;

import java.util.Date;

import com.belladati.sdk.dataset.source.DataSourceImportBase;
import com.belladati.sdk.dataset.source.ImportInterval;
import com.belladati.sdk.dataset.source.ImportIntervalUnit;

abstract class DataSourceImportBaseImpl implements DataSourceImportBase {

	protected final String id;
	protected ImportInterval interval;
	protected final Date nextImport;

	protected DataSourceImportBaseImpl(String id, Date nextImport) {
		this.id = id;
		this.nextImport = nextImport;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ImportInterval getRepeatInterval() {
		return interval;
	}

	public void setRepeatInterval(ImportInterval interval) {
		this.interval = interval;
	}

	public Date getNextExecutionDate() {
		return nextImport;
	}

	protected static class ImportIntervalImpl implements ImportInterval {

		private final ImportIntervalUnit unit;
		private final int factor;

		protected ImportIntervalImpl(ImportIntervalUnit unit, int factor) {
			this.unit = unit;
			this.factor = factor;
		}

		@Override
		public int getMinutes() {
			return unit.getMinutes() * factor;
		}

		@Override
		public ImportIntervalUnit getUnit() {
			return unit;
		}

		@Override
		public int getFactor() {
			return factor;
		}
	}

	enum ImportPeriod {
		CUSTOM(ImportIntervalUnit.MINUTE, 100), HOUR(ImportIntervalUnit.HOUR, 60), HOUR2(ImportIntervalUnit.HOUR,
			HOUR.minutes * 2), HOUR4(ImportIntervalUnit.HOUR, HOUR.minutes * 4), HOUR8(ImportIntervalUnit.HOUR, HOUR.minutes * 8), DAY(
			ImportIntervalUnit.DAY, 24 * 60), DAY2(ImportIntervalUnit.DAY, DAY.minutes * 2), WEEK(ImportIntervalUnit.WEEK,
			DAY.minutes * 7), WEEK2(ImportIntervalUnit.WEEK, WEEK.minutes * 2), MONTH(ImportIntervalUnit.MONTH, DAY.minutes * 31), QUARTER(
			ImportIntervalUnit.QUARTER, MONTH.minutes * 3), YEAR(ImportIntervalUnit.YEAR, DAY.minutes * 365);

		final ImportIntervalUnit unit;
		final int minutes;

		private ImportPeriod(ImportIntervalUnit unit, int minutes) {
			this.unit = unit;
			this.minutes = minutes;
		}
	}
}
