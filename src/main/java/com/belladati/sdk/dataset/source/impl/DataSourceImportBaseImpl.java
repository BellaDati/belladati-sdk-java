package com.belladati.sdk.dataset.source.impl;

import java.util.Date;

import com.belladati.sdk.dataset.source.DataSourceImportBase;
import com.belladati.sdk.dataset.source.ImportInterval;

public abstract class DataSourceImportBaseImpl implements DataSourceImportBase {

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

}
