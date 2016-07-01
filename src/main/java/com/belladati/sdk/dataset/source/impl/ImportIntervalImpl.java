package com.belladati.sdk.dataset.source.impl;

import com.belladati.sdk.dataset.source.ImportInterval;
import com.belladati.sdk.dataset.source.ImportIntervalUnit;

public class ImportIntervalImpl implements ImportInterval {

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
