package com.belladati.sdk.dataset.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.Indicator;
import com.belladati.sdk.dataset.data.DataRow;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.exception.impl.InvalidAttributeException;
import com.belladati.sdk.exception.impl.InvalidIndicatorException;
import com.belladati.sdk.exception.impl.InvalidReportException;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.report.impl.ReportInfoImpl;
import com.belladati.sdk.util.CachedList;
import com.belladati.sdk.util.PaginatedIdList;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.belladati.sdk.util.impl.LocalizationImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class DataSetImpl implements DataSet {

	private final BellaDatiServiceImpl service;

	private final String id;
	private final String name;
	private final String description;
	private final String ownerName;
	private final Date lastChange;
	private final List<Attribute> attributes;
	private final List<Indicator> indicators;
	private final List<ReportInfo> reports;
	private final LocalizationImpl localization;

	public DataSetImpl(BellaDatiServiceImpl service, JsonNode json) {
		this.service = service;

		this.id = json.get("id").asText();
		this.name = json.get("name").asText();
		this.description = json.hasNonNull("description") ? json.get("description").asText() : "";
		this.ownerName = json.get("owner").asText();

		if (json.hasNonNull("lastChange")) {
			this.lastChange = BellaDatiSdkUtils.parseJavaUtilDate(json.get("lastChange").asText());
		} else {
			this.lastChange = null;
		}

		List<Attribute> attributes = new ArrayList<Attribute>();
		if (json.hasNonNull("attributes") && json.get("attributes") instanceof ArrayNode) {
			for (JsonNode attribute : (ArrayNode) json.get("attributes")) {
				try {
					attributes.add(new AttributeImpl(service, id, attribute));
				} catch (InvalidAttributeException e) {
					// nothing to do, just ignore the attribute
				}
			}
		}
		this.attributes = Collections.unmodifiableList(attributes);

		List<Indicator> indicators = new ArrayList<Indicator>();
		if (json.hasNonNull("indicators") && json.get("indicators") instanceof ArrayNode) {
			for (JsonNode indicator : (ArrayNode) json.get("indicators")) {
				try {
					indicators.add(new IndicatorImpl(indicator));
				} catch (InvalidIndicatorException e) {
					// nothing to do, just ignore the indicator
				}
			}
		}
		this.indicators = Collections.unmodifiableList(indicators);

		List<ReportInfo> reports = new ArrayList<ReportInfo>();
		if (json.hasNonNull("reports") && json.get("reports") instanceof ArrayNode) {
			for (JsonNode report : (ArrayNode) json.get("reports")) {
				try {
					reports.add(new ReportInfoImpl(service, report));
				} catch (InvalidReportException e) {
					// nothing to do, just ignore the report
				}
			}
		}
		this.reports = Collections.unmodifiableList(reports);

		this.localization = new LocalizationImpl(json);
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
	public String getDescription() {
		return description;
	}

	@Override
	public String getOwnerName() {
		return ownerName;
	}

	@Override
	public Date getLastChange() {
		return lastChange != null ? (Date) lastChange.clone() : null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSetImpl) {
			return id.equals(((DataSetImpl) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public List<Indicator> getIndicators() {
		return indicators;
	}

	@Override
	public List<ReportInfo> getReports() {
		return reports;
	}

	@Override
	public CachedList<DataSource> getDataSources() {
		return service.getDataSources(id);
	}

	@Override
	public DataTable createDataTable() {
		List<String> columns = new ArrayList<String>();
		for (Attribute attribute : attributes) {
			columns.add(attribute.getCode());
		}
		for (Indicator indicator : indicators) {
			columns.add(indicator.getCode());
		}
		return DataTable.createBasicInstance(columns);
	}

	@Override
	public DataSet uploadData(DataTable data) {
		service.uploadData(id, data);
		return this;
	}

	@Override
	public PaginatedIdList<DataRow> getData() {
		return service.getDataSetData(id);
	}

	@Override
	public void postData(DataRow row) {
		service.postDataSetData(id, row);
	}

}
