package com.belladati.sdk.dataset.source.impl;

import com.belladati.sdk.dataset.source.ImportIntervalUnit;

public enum ImportPeriod {

	CUSTOM(ImportIntervalUnit.MINUTE, 100),

	HOUR(ImportIntervalUnit.HOUR, 60),

	HOUR2(ImportIntervalUnit.HOUR, HOUR.minutes * 2),

	HOUR4(ImportIntervalUnit.HOUR, HOUR.minutes * 4),

	HOUR8(ImportIntervalUnit.HOUR, HOUR.minutes * 8),

	DAY(ImportIntervalUnit.DAY, 24 * 60),

	DAY2(ImportIntervalUnit.DAY, DAY.minutes * 2),

	WEEK(ImportIntervalUnit.WEEK, DAY.minutes * 7),

	WEEK2(ImportIntervalUnit.WEEK, WEEK.minutes * 2),

	MONTH(ImportIntervalUnit.MONTH, DAY.minutes * 31),

	QUARTER(ImportIntervalUnit.QUARTER, MONTH.minutes * 3),

	YEAR(ImportIntervalUnit.YEAR, DAY.minutes * 365);

	final ImportIntervalUnit unit;
	final int minutes;

	private ImportPeriod(ImportIntervalUnit unit, int minutes) {
		this.unit = unit;
		this.minutes = minutes;
	}

}
