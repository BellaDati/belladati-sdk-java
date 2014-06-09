package com.belladati.sdk.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.dataset.data.DataRow;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourceImport;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.exception.dataset.data.UnknownServerColumnException;
import com.belladati.sdk.exception.server.NotFoundException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.AttributeValueImpl.InvalidAttributeValueException;
import com.belladati.sdk.impl.DataSourceImportImpl.InvalidDataSourceImportException;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.report.AttributeValue;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.user.User;
import com.belladati.sdk.util.CachedList;
import com.belladati.sdk.util.PaginatedIdList;
import com.belladati.sdk.util.PaginatedList;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class BellaDatiServiceImpl implements BellaDatiService {

	/** The serialVersionUID */
	private static final long serialVersionUID = 9054278401541000710L;

	public static final String DATE_TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

	final BellaDatiClient client;
	final TokenHolder tokenHolder;

	private final transient PaginatedIdList<DashboardInfo> dashboardList = new DashboardList();

	private final transient PaginatedIdList<ReportInfo> reportList = new ReportList();

	private final transient PaginatedIdList<DataSetInfo> dataSetList = new DataSetList();

	private final transient Map<String, PaginatedList<Comment>> commentLists = Collections
		.synchronizedMap(new HashMap<String, PaginatedList<Comment>>());

	private final transient Map<String, Map<String, CachedListImpl<AttributeValue>>> reportAttributeValues = new HashMap<String, Map<String, CachedListImpl<AttributeValue>>>();

	private final transient Map<String, CachedListImpl<DataSource>> dataSourceList = new HashMap<String, CachedListImpl<DataSource>>();

	private final transient Map<String, CachedListImpl<DataSourceImport>> dataSourceImportList = new HashMap<String, CachedListImpl<DataSourceImport>>();

	BellaDatiServiceImpl(BellaDatiClient client, TokenHolder tokenHolder) {
		this.client = client;
		this.tokenHolder = tokenHolder;
	}

	@Override
	public PaginatedIdList<DashboardInfo> getDashboardInfo() {
		return dashboardList;
	}

	@Override
	public Dashboard loadDashboard(String id) {
		return new DashboardImpl(this, loadJson("api/dashboards/" + id));
	}

	@Override
	public Object loadDashboardThumbnail(String id) throws IOException {
		return loadImage("api/dashboards/" + id + "/thumbnail");
	}

	@Override
	public PaginatedIdList<ReportInfo> getReportInfo() {
		return reportList;
	}

	@Override
	public Report loadReport(String id) {
		return new ReportImpl(this, loadJson("api/reports/" + id));
	}

	@Override
	public Object loadReportThumbnail(String reportId) throws IOException {
		return loadImage("api/reports/" + reportId + "/thumbnail");
	}

	/**
	 * Convenience method for other parts of the implementation to easily load
	 * data through the API.
	 * 
	 * @param uri URI to contact
	 * @return the resulting JSON response
	 */
	JsonNode loadJson(String uri) {
		return client.getJson(uri, tokenHolder);
	}

	/**
	 * Loads an image from the given URI.
	 * 
	 * @param relativeUrl the URI to load from
	 * @return the image from the server
	 * @throws IOException if the image cannot be loaded
	 */
	private Object loadImage(String relativeUrl) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(client.get(relativeUrl, tokenHolder));
		try {
			BufferedImage image = ImageIO.read(bais);
			if (image == null) {
				throw new IOException("Could not load image");
			}
			return image;
		} finally {
			bais.close();
		}
	}

	@Override
	public PaginatedList<Comment> getReportComments(String reportId) {
		PaginatedList<Comment> existing = commentLists.get(reportId);
		if (existing != null) {
			return existing;
		} else {
			synchronized (commentLists) {
				existing = commentLists.get(reportId);
				if (existing != null) {
					return existing;
				} else {
					PaginatedList<Comment> newList = new PaginatedListImpl<Comment>(this,
						"api/reports/" + reportId + "/comments", "comments") {
						@Override
						protected Comment parse(BellaDatiServiceImpl service, JsonNode node) {
							return new CommentImpl(service, node);
						}
					};
					commentLists.put(reportId, newList);
					return newList;
				}
			}
		}
	}

	@Override
	public void postComment(String reportId, String text) {
		client.post("api/reports/" + reportId + "/comments", tokenHolder,
			Collections.singletonList(new BasicNameValuePair("text", text)));
	}

	@Override
	public Object loadViewContent(String viewId, ViewType viewType, Filter<?>... filters) {
		return loadViewContent(viewId, viewType, Arrays.asList(filters));
	}

	@Override
	public Object loadViewContent(String viewId, ViewType viewType, Collection<Filter<?>> filters) {
		return createViewLoader(viewId, viewType).addFilters(filters).loadContent();
	}

	@Override
	public ViewLoader createViewLoader(String viewId, ViewType viewType) {
		return new ViewLoaderImpl(this, viewId, viewType);
	}

	@Override
	public CachedList<DataSource> getDataSources(String id) throws NotFoundException {
		CachedListImpl<DataSource> list = dataSourceList.get(id);
		if (list == null) {
			// we don't have this data set's sources in our cache yet
			list = new CachedListImpl<DataSource>(this, "api/dataSets/" + id + "/dataSources", "dataSources") {
				@Override
				protected DataSource parse(BellaDatiServiceImpl service, JsonNode node) throws ParseException {
					return new DataSourceImpl(service, node);
				}
			};
			dataSourceList.put(id, list);
		}
		return list;
	}

	@Override
	public CachedList<DataSourceImport> getDataSourceImports(String id) throws NotFoundException {
		CachedListImpl<DataSourceImport> list = dataSourceImportList.get(id);
		if (list == null) {
			// we don't have this data set's sources in our cache yet
			list = new CachedListImpl<DataSourceImport>(this, "api/dataSets/dataSources/" + id + "/executions", "executions") {
				@Override
				protected DataSourceImport parse(BellaDatiServiceImpl service, JsonNode node) throws ParseException {
					try {
						return new DataSourceImportImpl(node);
					} catch (InvalidDataSourceImportException e) {
						throw new ParseException(node, e);
					}
				}
			};
			dataSourceImportList.put(id, list);
		}
		return list;
	}

	@Override
	public DataSourcePendingImport setupDataSourceImport(String id, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Appends a filter parameter from the given filters to the URI builder.
	 * Won't do anything if the filter collection is empty.
	 * 
	 * @param builder the builder to append to
	 * @param filters filters to append
	 * @return the same builder, for chaining
	 */
	URIBuilder appendFilter(URIBuilder builder, Collection<Filter<?>> filters) {
		if (filters.size() > 0) {
			ObjectNode filterNode = new ObjectMapper().createObjectNode();
			for (Filter<?> filter : filters) {
				filterNode.setAll(filter.toJson());
			}
			ObjectNode drilldownNode = new ObjectMapper().createObjectNode();
			drilldownNode.put("drilldown", filterNode);
			builder.addParameter("filter", drilldownNode.toString());
		}
		return builder;
	}

	/**
	 * Appends a date/time definition parameter to the URI builder. Won't do
	 * anything if both intervals are <tt>null</tt>.
	 * 
	 * @param builder the builder to append to
	 * @param dateInterval date interval to append, or <tt>null</tt>
	 * @param timeInterval time interval to append, or <tt>null</tt>
	 * @return the same builder, for chaining
	 */
	URIBuilder appendDateTime(URIBuilder builder, Interval<DateUnit> dateInterval, Interval<TimeUnit> timeInterval) {
		if (dateInterval != null || timeInterval != null) {
			ObjectNode dateTimeNode = new ObjectMapper().createObjectNode();
			if (dateInterval != null) {
				dateTimeNode.setAll(dateInterval.toJson());
			}
			if (timeInterval != null) {
				dateTimeNode.setAll(timeInterval.toJson());
			}
			builder.addParameter("dateTimeDefinition", dateTimeNode.toString());
		}
		return builder;
	}

	@Override
	public synchronized CachedList<AttributeValue> getAttributeValues(String reportId, String attributeCode) {
		if (!reportAttributeValues.containsKey(reportId)) {
			// we don't have any values for this report yet, insert new map
			reportAttributeValues.put(reportId, new HashMap<String, CachedListImpl<AttributeValue>>());
		}

		Map<String, CachedListImpl<AttributeValue>> attributeValues = reportAttributeValues.get(reportId);

		CachedListImpl<AttributeValue> values = attributeValues.get(attributeCode);
		if (values == null) {
			// we don't have this attribute in our cache yet
			values = new CachedListImpl<AttributeValue>(this, "api/reports/" + reportId
				+ "/filter/drilldownAttributeValues?code=" + attributeCode, "values") {
				@Override
				protected AttributeValue parse(BellaDatiServiceImpl service, JsonNode node) throws ParseException {
					try {
						return new AttributeValueImpl(node);
					} catch (InvalidAttributeValueException e) {
						throw new ParseException(node, e);
					}
				}
			};
			attributeValues.put(attributeCode, values);
		}
		return values;
	}

	@Override
	public User loadUser(String userId) {
		return new UserImpl(loadJson("api/users/" + userId));
	}

	@Override
	public Object loadUserImage(String userId) throws IOException {
		return loadImage("api/users/" + userId + "/image");
	}

	@Override
	public String toString() {
		return "BellaDati Service(server: " + client.getBaseUrl() + ", key: " + tokenHolder.getConsumerKey() + ", token: "
			+ tokenHolder.getToken() + ")";
	}

	@Override
	public PaginatedIdList<DataSetInfo> getDataSetInfo() {
		return dataSetList;
	}

	@Override
	public DataSet loadDataSet(String id) throws NotFoundException {
		return new DataSetImpl(this, loadJson("api/dataSets/" + id));
	}

	@Override
	public void uploadData(String id, DataTable data) {
		List<DataRow> rows = data.getRows();
		if (rows.size() == 0) {
			// if we don't have data, do nothing
			return;
		}
		try {
			client.postUpload("api/import/" + id, tokenHolder, data.toJson().toString());
		} catch (UnexpectedResponseException e) {
			if (e.getResponseCode() == 400) {
				Pattern codePattern = Pattern.compile(".*?'(.*?)'.*");
				Matcher codeMatcher = codePattern.matcher(e.getResponseContent());
				if (codeMatcher.matches()) {
					throw new UnknownServerColumnException(id, codeMatcher.group(1));
				}
			}
			throw new UnexpectedResponseException(e.getResponseCode(), e.getResponseContent(), e);
		}
	}

	/** Deserialization. Sets up the element lists and maps as empty objects. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			Field dashboardList = getClass().getDeclaredField("dashboardList");
			dashboardList.setAccessible(true);
			dashboardList.set(this, new DashboardList());

			Field reportList = getClass().getDeclaredField("reportList");
			reportList.setAccessible(true);
			reportList.set(this, new ReportList());

			Field commentLists = getClass().getDeclaredField("commentLists");
			commentLists.setAccessible(true);
			commentLists.set(this, Collections.synchronizedMap(new HashMap<String, PaginatedList<Comment>>()));

			Field reportAttributeValues = getClass().getDeclaredField("reportAttributeValues");
			reportAttributeValues.setAccessible(true);
			reportAttributeValues.set(this, new HashMap<String, Map<String, CachedListImpl<AttributeValue>>>());
		} catch (NoSuchFieldException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		} catch (IllegalAccessException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		} catch (SecurityException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		} catch (IllegalArgumentException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		}
	}

	/** Paginated list class for dashboards. */
	private class DashboardList extends PaginatedIdListImpl<DashboardInfo> {
		public DashboardList() {
			super(BellaDatiServiceImpl.this, "api/dashboards", "dashboards");
		}

		@Override
		protected DashboardInfo parse(BellaDatiServiceImpl service, JsonNode node) {
			return new DashboardInfoImpl(service, node);
		}
	}

	/** Paginated list class for reports. */
	private class ReportList extends PaginatedIdListImpl<ReportInfo> {
		public ReportList() {
			super(BellaDatiServiceImpl.this, "api/reports", "reports");
		}

		@Override
		protected ReportInfo parse(BellaDatiServiceImpl service, JsonNode node) {
			return new ReportInfoImpl(service, node);
		}
	}

	/** Paginated list class for data sets. */
	private class DataSetList extends PaginatedIdListImpl<DataSetInfo> {
		public DataSetList() {
			super(BellaDatiServiceImpl.this, "api/dataSets", "dataSets");
		}

		@Override
		protected DataSetInfo parse(BellaDatiServiceImpl service, JsonNode node) {
			return new DataSetInfoImpl(service, node);
		}
	}
}
