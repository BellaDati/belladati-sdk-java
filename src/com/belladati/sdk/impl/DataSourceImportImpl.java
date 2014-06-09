package com.belladati.sdk.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.belladati.sdk.dataset.source.DataSourceImport;
import com.belladati.sdk.dataset.source.ImportInterval;
import com.fasterxml.jackson.databind.JsonNode;

class DataSourceImportImpl implements DataSourceImport {

	private final String id;
	private final Date lastImport;
	private final String callerName;
	private final boolean overwriting;
	private final ImportInterval interval;

	DataSourceImportImpl(JsonNode json) throws InvalidDataSourceImportException {
		this.id = json.get("id").asText();
		this.callerName = json.get("createdBy").asText();
		this.overwriting = json.hasNonNull("overwritingPolicy") && !json.get("overwritingPolicy").asText().isEmpty();

		if (!json.hasNonNull("when")) {
			throw new InvalidDataSourceImportException(json);
		}
		SimpleDateFormat format = new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT);
		try {
			this.lastImport = format.parse(json.get("when").asText());
		} catch (ParseException e) {
			throw new InvalidDataSourceImportException(json);
		}

		if (json.hasNonNull("repeateInterval")) {
			interval = new ImportIntervalImpl(parseMinutes(json));
		} else {
			interval = null;
		}
	}

	private int parseMinutes(JsonNode json) throws InvalidDataSourceImportException {
		try {
			ImportPeriod period = ImportPeriod.valueOf(json.get("repeateInterval").asText());
			if (period == ImportPeriod.CUSTOM) {
				return parseCustomMinutes(json);
			}
			return period.minutes;
		} catch (IllegalArgumentException e) {
			throw new InvalidDataSourceImportException(json);
		}
	}

	private int parseCustomMinutes(JsonNode json) throws InvalidDataSourceImportException {
		if (json.hasNonNull("repeateIntervalCustom")) {
			try {
				int minutes = Integer.parseInt(json.get("repeateIntervalCustom").asText());
				if (minutes > 0) {
					return minutes;
				}
			} catch (NumberFormatException e) {
				// nothing to do - exception is thrown below
			}
		}
		throw new InvalidDataSourceImportException(json);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Date getLastExecutionDate() {
		return lastImport;
	}

	@Override
	public String getCallerName() {
		return callerName;
	}

	@Override
	public ImportInterval getImportInterval() {
		return interval;
	}

	@Override
	public boolean isOverwriting() {
		return overwriting;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSourceImportImpl) {
			return id.equals(((DataSourceImportImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	class InvalidDataSourceImportException extends Exception {
		/** The serialVersionUID */
		private static final long serialVersionUID = -4920843734203654180L;

		public InvalidDataSourceImportException(JsonNode node) {
			super("Invalid data source import JSON: " + node.toString());
		}
	}

	private class ImportIntervalImpl implements ImportInterval {

		private final int minutes;

		private ImportIntervalImpl(int minutes) {
			this.minutes = minutes;
		}

		@Override
		public int getMinutes() {
			return minutes;
		}

		@Override
		public Date getNextExecution() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(lastImport);
			cal.add(Calendar.MINUTE, minutes);
			return cal.getTime();
		}

	}
	private enum ImportPeriod {
		CUSTOM(100), HOUR(60), HOUR2(HOUR.minutes * 2), HOUR4(HOUR.minutes * 4), HOUR8(HOUR.minutes * 8), DAY(24 * 60), DAY2(
			DAY.minutes * 2), WEEK(DAY.minutes * 7), WEEK2(WEEK.minutes * 2), MONTH(DAY.minutes * 31), QUARTER(MONTH.minutes * 3), YEAR(
			DAY.minutes * 365);

		private int minutes;

		private ImportPeriod(int minutes) {
			this.minutes = minutes;
		}
	}
}
