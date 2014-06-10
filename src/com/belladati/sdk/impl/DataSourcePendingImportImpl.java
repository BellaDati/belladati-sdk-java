package com.belladati.sdk.impl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.dataset.data.OverwritePolicy;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
import com.belladati.sdk.dataset.source.ImportInterval;
import com.belladati.sdk.dataset.source.ImportIntervalUnit;
import com.belladati.sdk.exception.server.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class DataSourcePendingImportImpl extends DataSourceImportBaseImpl implements DataSourcePendingImport {

	private final BellaDatiServiceImpl service;
	private final String sourceId;
	private OverwritePolicy overwritePolicy = OverwritePolicy.deleteNone();
	private boolean posted = false;

	DataSourcePendingImportImpl(BellaDatiServiceImpl service, String sourceId, Date date) {
		super("", date);
		this.service = service;
		this.sourceId = sourceId;
	}

	@Override
	public boolean isOverwriting() {
		return !OverwritePolicy.deleteNone().equals(overwritePolicy);
	}

	@Override
	public DataSourcePendingImport setOverwritePolicy(OverwritePolicy policy) throws IllegalStateException {
		if (posted) {
			throw new IllegalStateException("Import already submitted to server.");
		}
		this.overwritePolicy = policy == null ? OverwritePolicy.deleteNone() : policy;
		return this;
	}

	@Override
	public OverwritePolicy getOverwritePolicy() {
		return overwritePolicy;
	}

	@Override
	public DataSourcePendingImport setRepeatInterval(ImportIntervalUnit unit, int factor) throws IllegalStateException {
		if (posted) {
			throw new IllegalStateException("Import already submitted to server.");
		}
		if (unit == null || factor <= 0) {
			this.interval = null;
		} else {
			this.interval = new ImportIntervalImpl(unit, factor);
		}
		return this;
	}

	@Override
	public JsonNode toJson() {
		ObjectNode node = new ObjectMapper().createObjectNode().put("when",
			new SimpleDateFormat(BellaDatiServiceImpl.DATE_TIME_FORMAT).format(nextImport));
		if (isOverwriting()) {
			node.put("overwrite", overwritePolicy.toJson());
		}
		if (interval != null) {
			ImportPeriod period = findPeriod(interval);
			node.put("repeateInterval", period.name());
			if (period == ImportPeriod.CUSTOM) {
				node.put("customRepeateInterval", interval.getMinutes());
			}
		}
		return node;
	}

	@Override
	public void post() throws NotFoundException, IllegalStateException {
		if (posted) {
			throw new IllegalStateException("Import already submitted to server.");
		}
		service.client.post("api/dataSets/dataSources/" + sourceId + "/schedule", service.tokenHolder,
			Collections.singletonList(new BasicNameValuePair("params", toJson().toString())));
		posted = true;
	}

	ImportPeriod findPeriod(ImportInterval interval) {
		for (ImportPeriod period : ImportPeriod.values()) {
			if (period.minutes == interval.getMinutes()) {
				return period;
			}
		}
		return ImportPeriod.CUSTOM;
	}
}
