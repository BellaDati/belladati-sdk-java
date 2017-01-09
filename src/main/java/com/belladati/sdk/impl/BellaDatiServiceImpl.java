package com.belladati.sdk.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.dashboard.impl.DashboardImpl;
import com.belladati.sdk.dashboard.impl.DashboardInfoImpl;
import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.dataset.data.DataColumn;
import com.belladati.sdk.dataset.data.DataRow;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.dataset.impl.AttributeValueImpl;
import com.belladati.sdk.dataset.impl.DataSetImpl;
import com.belladati.sdk.dataset.impl.DataSetInfoImpl;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourceImport;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
import com.belladati.sdk.dataset.source.impl.DataSourceImpl;
import com.belladati.sdk.dataset.source.impl.DataSourceImportImpl;
import com.belladati.sdk.dataset.source.impl.DataSourcePendingImportImpl;
import com.belladati.sdk.domain.Domain;
import com.belladati.sdk.domain.DomainCreateBuilder;
import com.belladati.sdk.domain.DomainEditBuilder;
import com.belladati.sdk.domain.DomainInfo;
import com.belladati.sdk.domain.impl.DomainCreateBuilderImpl;
import com.belladati.sdk.domain.impl.DomainEditBuilderImpl;
import com.belladati.sdk.domain.impl.DomainImpl;
import com.belladati.sdk.domain.impl.DomainInfoImpl;
import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.exception.dataset.data.UnknownServerColumnException;
import com.belladati.sdk.exception.impl.InvalidAttributeValueException;
import com.belladati.sdk.exception.impl.InvalidDataSourceImportException;
import com.belladati.sdk.exception.server.InvalidJsonException;
import com.belladati.sdk.exception.server.InvalidStreamException;
import com.belladati.sdk.exception.server.NotFoundException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.form.Form;
import com.belladati.sdk.form.FormDataPostBuilder;
import com.belladati.sdk.form.impl.FormDataPostBuilderImpl;
import com.belladati.sdk.form.impl.FormImpl;
import com.belladati.sdk.intervals.DateUnit;
import com.belladati.sdk.intervals.Interval;
import com.belladati.sdk.intervals.TimeUnit;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.report.impl.CommentImpl;
import com.belladati.sdk.report.impl.ReportImpl;
import com.belladati.sdk.report.impl.ReportInfoImpl;
import com.belladati.sdk.user.User;
import com.belladati.sdk.user.UserCreateBuilder;
import com.belladati.sdk.user.UserEditBuilder;
import com.belladati.sdk.user.UserGroup;
import com.belladati.sdk.user.UserGroupCreateBuilder;
import com.belladati.sdk.user.UserRequestType;
import com.belladati.sdk.user.impl.UserCreateBuilderImpl;
import com.belladati.sdk.user.impl.UserEditBuilderImpl;
import com.belladati.sdk.user.impl.UserGroupCreateBuilderImpl;
import com.belladati.sdk.user.impl.UserGroupImpl;
import com.belladati.sdk.user.impl.UserImpl;
import com.belladati.sdk.util.CachedList;
import com.belladati.sdk.util.MultipartPiece;
import com.belladati.sdk.util.PaginatedIdList;
import com.belladati.sdk.util.PaginatedList;
import com.belladati.sdk.util.impl.BellaDatiSdkUtils;
import com.belladati.sdk.util.impl.CachedListImpl;
import com.belladati.sdk.util.impl.MultipartFileImpl;
import com.belladati.sdk.util.impl.MultipartTextImpl;
import com.belladati.sdk.util.impl.PaginatedIdListImpl;
import com.belladati.sdk.util.impl.PaginatedListImpl;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.belladati.sdk.view.export.ViewExporter;
import com.belladati.sdk.view.impl.ViewExporterImpl;
import com.belladati.sdk.view.impl.ViewLoaderImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BellaDatiServiceImpl implements BellaDatiService {

	/** The serialVersionUID */
	private static final long serialVersionUID = 9054278401541000710L;

	public static final String DATE_TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

	private final BellaDatiClient client;
	private final TokenHolder tokenHolder;

	private final transient CachedList<DomainInfo> domainList = new DomainList();

	private final transient Map<String, CachedList<User>> users = Collections
		.synchronizedMap(new HashMap<String, CachedList<User>>());

	private final transient Map<String, CachedList<UserGroup>> userGroups = Collections
		.synchronizedMap(new HashMap<String, CachedList<UserGroup>>());

	private final transient PaginatedIdList<DashboardInfo> dashboardList = new DashboardList();

	private final transient PaginatedIdList<ReportInfo> reportList = new ReportList();

	private final transient PaginatedIdList<DataSetInfo> dataSetList = new DataSetList();

	private final transient Map<String, PaginatedList<Comment>> commentLists = Collections
		.synchronizedMap(new HashMap<String, PaginatedList<Comment>>());

	private final transient Map<String, Map<String, CachedList<AttributeValue>>> dataSetAttributeValues = new HashMap<String, Map<String, CachedList<AttributeValue>>>();

	private final transient Map<String, CachedList<DataSource>> dataSourceList = new HashMap<String, CachedList<DataSource>>();

	private final transient Map<String, CachedList<DataSourceImport>> dataSourceImportList = new HashMap<String, CachedList<DataSourceImport>>();

	private final transient Map<String, PaginatedIdList<DataRow>> dataSetData = Collections
		.synchronizedMap(new HashMap<String, PaginatedIdList<DataRow>>());

	private final transient CachedList<Form> importFormList = new ImportFormList();

	public BellaDatiServiceImpl(BellaDatiClient client, TokenHolder tokenHolder) {
		this.client = client;
		this.tokenHolder = tokenHolder;
	}

	public BellaDatiClient getClient() {
		return client;
	}

	public TokenHolder getTokenHolder() {
		return tokenHolder;
	}

	@Override
	public CachedList<DomainInfo> getDomainInfo() {
		return domainList;
	}

	@Override
	public Domain loadDomain(String id) throws NotFoundException {
		return new DomainImpl(this, getAsJson("api/domains/" + id));
	}

	@Override
	public CachedList<User> getDomainUsers(String domainId, String userGroupId) {
		final String cacheKey = domainId + "-" + userGroupId;
		CachedList<User> existing = users.get(cacheKey);
		if (existing != null) {
			return existing;
		} else {
			synchronized (users) {
				existing = users.get(cacheKey);
				if (existing != null) {
					return existing;
				} else {
					String params = userGroupId != null && !userGroupId.isEmpty() ? "?userGroup_id=" + userGroupId : "";
					String endpoint = "api/domains/" + domainId + "/users" + params;
					CachedList<User> newList = new CachedListImpl<User>(this, endpoint, "users") {
						@Override
						protected User parse(BellaDatiServiceImpl service, JsonNode node) {
							return new UserImpl(service, node);
						}
					};
					users.put(cacheKey, newList);
					return newList;
				}
			}
		}
	}

	@Override
	public CachedList<UserGroup> getDomainUserGroups(String domainId) {
		CachedList<UserGroup> existing = userGroups.get(domainId);
		if (existing != null) {
			return existing;
		} else {
			synchronized (userGroups) {
				existing = userGroups.get(domainId);
				if (existing != null) {
					return existing;
				} else {
					CachedList<UserGroup> newList = new CachedListImpl<UserGroup>(this, "api/domains/" + domainId + "/userGroups",
						"userGroups") {
						@Override
						protected UserGroup parse(BellaDatiServiceImpl service, JsonNode node) {
							return new UserGroupImpl(node);
						}
					};
					userGroups.put(domainId, newList);
					return newList;
				}
			}
		}
	}

	@Override
	public DomainCreateBuilder setupDomainCreateBuilder() {
		return new DomainCreateBuilderImpl(this);
	}

	@Override
	public DomainEditBuilder setupDomainEditBuilder(String domainId) {
		return new DomainEditBuilderImpl(this, domainId);
	}

	@Override
	public UserGroupCreateBuilder setupUserGroupCreateBuilder(String domainId) {
		return new UserGroupCreateBuilderImpl(this, domainId);
	}

	@Override
	public UserCreateBuilder setupUserCreateBuilder(String domainId) {
		return new UserCreateBuilderImpl(this, domainId);
	}

	@Override
	public UserEditBuilder setupUserEditBuilder(String userId) {
		return new UserEditBuilderImpl(this, userId);
	}

	@Override
	public PaginatedIdList<DashboardInfo> getDashboardInfo() {
		return dashboardList;
	}

	@Override
	public Dashboard loadDashboard(String id) {
		return new DashboardImpl(this, getAsJson("api/dashboards/" + id));
	}

	@Override
	public Object loadDashboardThumbnail(String id) {
		return getAsImage("api/dashboards/" + id + "/thumbnail");
	}

	@Override
	public PaginatedIdList<ReportInfo> getReportInfo() {
		return reportList;
	}

	@Override
	public Report loadReport(String id) {
		return new ReportImpl(this, getAsJson("api/reports/" + id));
	}

	@Override
	public Object loadReportThumbnail(String reportId) {
		return getAsImage("api/reports/" + reportId + "/thumbnail");
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
					PaginatedList<Comment> newList = new PaginatedListImpl<Comment>(this, "api/reports/" + reportId + "/comments",
						"comments") {

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
	public void deleteComment(String commentId) throws NotFoundException {
		client.delete("api/reports/comments/" + commentId, tokenHolder);
	}

	@Override
	public Object loadViewContent(String viewId, ViewType viewType, Filter<?>... filters) {
		return loadViewContent(viewId, viewType, Arrays.asList(filters));
	}

	@Override
	public Object loadViewContent(String viewId, ViewType viewType, Collection<Filter<?>> filters) {
		return setupViewLoader(viewId, viewType).addFilters(filters).loadContent();
	}

	@Override
	public ViewLoader setupViewLoader(String viewId, ViewType viewType) {
		return new ViewLoaderImpl(this, viewId, viewType);
	}

	@Override
	public ViewExporter setupViewExporter(String viewId) {
		return new ViewExporterImpl(this, viewId);
	}

	@Override
	public CachedList<DataSource> getDataSources(String id) throws NotFoundException {
		CachedList<DataSource> list = dataSourceList.get(id);
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
		CachedList<DataSourceImport> list = dataSourceImportList.get(id);
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
		return new DataSourcePendingImportImpl(this, id, date);
	}

	/**
	 * Appends a filter parameter from the given filters to the URI builder.
	 * Won't do anything if the filter collection is empty.
	 * 
	 * @param builder the builder to append to
	 * @param filters filters to append
	 * @return the same builder, for chaining
	 */
	public URIBuilder appendFilter(URIBuilder builder, Collection<Filter<?>> filters) {
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
	public URIBuilder appendDateTime(URIBuilder builder, Interval<DateUnit> dateInterval, Interval<TimeUnit> timeInterval) {
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

	/**
	 * Appends a locale language parameter to the URI builder. Won't do anything
	 * if the locale is <tt>null</tt>.
	 * 
	 * @param builder the builder to append to
	 * @param locale the locale to append
	 * @return the same builder, for chaining
	 */
	public URIBuilder appendLocale(URIBuilder builder, Locale locale) {
		if (locale != null) {
			builder.addParameter("lang", locale.getLanguage());
		}
		return builder;
	}

	@Override
	public synchronized CachedList<AttributeValue> getAttributeValues(String dataSetId, String attributeCode) {
		if (!dataSetAttributeValues.containsKey(dataSetId)) {
			// we don't have any values for this report yet, insert new map
			dataSetAttributeValues.put(dataSetId, new HashMap<String, CachedList<AttributeValue>>());
		}

		Map<String, CachedList<AttributeValue>> attributeValues = dataSetAttributeValues.get(dataSetId);

		CachedList<AttributeValue> values = attributeValues.get(attributeCode);
		if (values == null) {
			// we don't have this attribute in our cache yet
			values = new CachedListImpl<AttributeValue>(this,
				"api/dataSets/" + dataSetId + "/attributes/" + attributeCode + "/values", "values") {
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
	public void postAttributeValueImage(String dataSetId, String attributeCode, String attributeValue, File image)
		throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setPath("api/dataSets/" + dataSetId + "/attributes/" + attributeCode + "/" + attributeValue + "/image");
		String relativeUri = builder.build().toString();

		List<MultipartPiece<?>> multipart = new ArrayList<>();
		multipart.add(new MultipartFileImpl("file", image));

		postMultipart(relativeUri, multipart);
	}

	@Override
	public User loadUser(String userId) {
		return new UserImpl(this, getAsJson("api/users/" + userId));
	}

	@Override
	public User loadUserByUsername(String username) {
		return new UserImpl(this, getAsJson("api/users/username/" + username));
	}

	@Override
	public Object loadUserImage(String userId) {
		return getAsImage("api/users/" + userId + "/image");
	}

	@Override
	public String loadUserStatus(String userId) throws NotFoundException {
		return new String(client.get("api/users/" + userId + "/status", tokenHolder));
	}

	@Override
	public void postUserStatus(String userId, String status) throws NotFoundException {
		client.post("api/users/" + userId + "/status", tokenHolder,
			Collections.singletonList(new BasicNameValuePair("status", status)));
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
		return new DataSetImpl(this, getAsJson("api/dataSets/" + id));
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

	@Override
	public void uploadImage(File image, String name) {
		List<MultipartPiece<?>> multipart = new ArrayList<>();
		multipart.add(new MultipartFileImpl("file", image));
		if (name != null) {
			multipart.add(new MultipartTextImpl("name", name));
		}

		postMultipart("api/import/media/image", multipart);
	}

	/** Deserialization. Sets up the element lists and maps as empty objects. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			Field domainList = getClass().getDeclaredField("domainList");
			domainList.setAccessible(true);
			domainList.set(this, new DomainList());

			Field dashboardList = getClass().getDeclaredField("dashboardList");
			dashboardList.setAccessible(true);
			dashboardList.set(this, new DashboardList());

			Field reportList = getClass().getDeclaredField("reportList");
			reportList.setAccessible(true);
			reportList.set(this, new ReportList());

			Field dataSetList = getClass().getDeclaredField("dataSetList");
			dataSetList.setAccessible(true);
			dataSetList.set(this, new DataSetList());

			Field commentLists = getClass().getDeclaredField("commentLists");
			commentLists.setAccessible(true);
			commentLists.set(this, Collections.synchronizedMap(new HashMap<String, PaginatedList<Comment>>()));

			Field reportAttributeValues = getClass().getDeclaredField("dataSetAttributeValues");
			reportAttributeValues.setAccessible(true);
			reportAttributeValues.set(this, new HashMap<String, Map<String, CachedListImpl<AttributeValue>>>());

			Field dataSourceList = getClass().getDeclaredField("dataSourceList");
			dataSourceList.setAccessible(true);
			dataSourceList.set(this, new HashMap<String, CachedListImpl<DataSource>>());

			Field importFormList = getClass().getDeclaredField("importFormList");
			importFormList.setAccessible(true);
			importFormList.set(this, new ImportFormList());

			Field dataSourceImportList = getClass().getDeclaredField("dataSourceImportList");
			dataSourceImportList.setAccessible(true);
			dataSourceImportList.set(this, new HashMap<String, CachedListImpl<DataSourceImport>>());
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

	/** Cached list class for domains. */
	private class DomainList extends CachedListImpl<DomainInfo> {
		public DomainList() {
			super(BellaDatiServiceImpl.this, "api/domains", "domains");
		}

		@Override
		protected DomainInfo parse(BellaDatiServiceImpl service, JsonNode node) {
			return new DomainInfoImpl(service, node);
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

	/** Cached list class for import forms. */
	private class ImportFormList extends CachedListImpl<Form> {
		public ImportFormList() {
			super(BellaDatiServiceImpl.this, "api/import/forms", "importForms");
		}

		@Override
		protected Form parse(BellaDatiServiceImpl service, JsonNode node) {
			return new FormImpl(node);
		}
	}

	@Override
	public byte[] post(String uri) throws URISyntaxException {
		return post(uri, Collections.<String, String> emptyMap());
	}

	@Override
	public byte[] post(String uri, Map<String, String> uriParameters) throws URISyntaxException {
		return postForm(uri, uriParameters, Collections.<String, String> emptyMap());
	}

	@Override
	public byte[] post(String uri, byte[] content) throws URISyntaxException {
		return post(uri, Collections.<String, String> emptyMap(), content);
	}

	@Override
	public byte[] post(String uri, Map<String, String> uriParameters, byte[] content) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(uri);
		for (Entry<String, String> entry : uriParameters.entrySet()) {
			builder.addParameter(entry.getKey(), entry.getValue());
		}
		return client.postData(builder.build().toString(), tokenHolder, content);
	}

	@Override
	public byte[] postMultipart(String relativeUrl, List<? extends MultipartPiece<?>> multipart) {
		return client.postMultipart(relativeUrl, tokenHolder, multipart);
	}

	@Override
	public byte[] postForm(String uri, Map<String, String> formParameters) throws URISyntaxException {
		return postForm(uri, Collections.<String, String> emptyMap(), formParameters);
	}

	@Override
	public byte[] postForm(String uri, Map<String, String> uriParameters, Map<String, String> formParameters)
		throws URISyntaxException {
		URIBuilder builder = new URIBuilder(uri);
		for (Entry<String, String> entry : uriParameters.entrySet()) {
			builder.addParameter(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry : formParameters.entrySet()) {
			formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return client.post(builder.build().toString(), tokenHolder, formParams);
	}

	@Override
	public byte[] get(String uri) throws URISyntaxException {
		return get(uri, Collections.<String, String> emptyMap());
	}

	@Override
	public byte[] get(String uri, Map<String, String> uriParameters) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(uri);
		for (Entry<String, String> entry : uriParameters.entrySet()) {
			builder.addParameter(entry.getKey(), entry.getValue());
		}
		return client.get(builder.build().toString(), tokenHolder);
	}

	/**
	 * Helper method to invoke GET operation on the specified relative URI and to read result as {@link JsonNode}.
	 * 
	 * @param relativeUri the relative URI to load JSON from
	 * @return the JsonNode parsed from the response
	 * @throws InvalidJsonException if response cannot be parsed into JSON
	 */
	public JsonNode getAsJson(String relativeUri) throws InvalidJsonException {
		return client.getAsJson(relativeUri, tokenHolder);
	}

	/**
	 * Helper method to invoke GET operation on the specified relative URI and to read result as {@link BufferedImage}.
	 * 
	 * @param relativeUri the relative URI to load image from
	 * @return the BufferedImage parsed from the response
	 * @throws InvalidStreamException if response cannot be parsed into image
	 */
	public BufferedImage getAsImage(String relativeUri) throws InvalidStreamException {
		return client.getAsImage(relativeUri, tokenHolder);
	}

	/**
	 * Helper method to invoke GET operation on the specified relative URI and to read result as {@link ByteArrayInputStream}.
	 * 
	 * @param relativeUri the relative URI to load stream from
	 * @return the ByteArrayInputStream parsed from the response
	 */
	public ByteArrayInputStream getAsStream(String relativeUri) {
		return client.getAsStream(relativeUri, tokenHolder);
	}

	@Override
	public CachedList<Form> getImportForms() {
		return importFormList;
	}

	@Override
	public Form loadImportForm(String id) throws NotFoundException {
		return new FormImpl(getAsJson("api/import/forms/" + id));
	}

	@Override
	public FormDataPostBuilder setupFormDataPostBuilder(String formId) {
		return new FormDataPostBuilderImpl(this, formId);
	}

	@Override
	public String createImageView(String reportId, String viewName, File image, Integer width, Integer height) {
		Map<String, String> uriParams = new HashMap<>();
		if (width != null) {
			uriParams.put("width", width.toString());
		}
		if (height != null) {
			uriParams.put("height", height.toString());
		}
		String relativeUri = BellaDatiSdkUtils.joinUriWithParams("api/reports/" + reportId + "/images", uriParams);

		List<MultipartPiece<?>> multipart = new ArrayList<>();
		multipart.add(new MultipartTextImpl("name", viewName));
		multipart.add(new MultipartFileImpl("file", image));

		byte[] response = postMultipart(relativeUri, multipart);
		return new String(response);
	}

	@Override
	public void editImageView(String viewId, File image) {
		List<MultipartPiece<?>> multipart = new ArrayList<>();
		multipart.add(new MultipartFileImpl("file", image));

		String relativeUri = "api/reports/views/" + viewId + "/image";
		postMultipart(relativeUri, multipart);
	}

	@Override
	public Object loadFile(String absolutePath) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setPath("api/utils/file/" + absolutePath);
		return getAsStream(builder.build().toString());
	}

	@Override
	public Object mergePdfFiles(List<String> paths) throws URISyntaxException {
		String joinedPaths = StringUtils.join(paths, ";");
		URIBuilder builder = new URIBuilder();
		builder.setPath("api/utils/mergePdfFiles/" + joinedPaths);
		return getAsStream(builder.build().toString());
	}

	@Override
	public String createUserRequest(String username, UserRequestType requestType) {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("request_type", requestType.name()));

		byte[] response = client.post("api/users/" + username + "/requests", tokenHolder, formParams);
		return new String(response);
	}

	@Override
	public String createAccessToken(String username, Integer validity, String domainId) {
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		if (validity != null) {
			formParams.add(new BasicNameValuePair("validity", validity.toString()));
		}
		if (domainId != null) {
			formParams.add(new BasicNameValuePair("domain_id", domainId));
		}

		byte[] response = client.post("api/users/" + username + "/accessToken", tokenHolder, formParams);
		return new String(response);
	}

	@Override
	public PaginatedIdList<DataRow> getDataSetData(String dataSetId) {
		PaginatedIdList<DataRow> existing = dataSetData.get(dataSetId);
		if (existing != null) {
			return existing;
		} else {
			synchronized (dataSetData) {
				existing = dataSetData.get(dataSetId);
				if (existing != null) {
					return existing;
				} else {
					DataRowList newList = new DataRowList(dataSetId);
					dataSetData.put(dataSetId, newList);
					return newList;
				}
			}
		}
	}

	/** Paginated list class for data rows. */
	private class DataRowList extends PaginatedIdListImpl<DataRow> {

		public DataRowList(String dataSetId) {
			super(BellaDatiServiceImpl.this, "api/dataSets/" + dataSetId + "/data", "data");
		}

		@Override
		protected DataRow parse(BellaDatiServiceImpl service, JsonNode node) {
			String rowId = node.hasNonNull("UID") ? node.get("UID").asText() : null;

			List<DataColumn> columns = new ArrayList<>();
			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				if (!fieldName.equalsIgnoreCase("UID")) {
					columns.add(new DataColumn(fieldName));
				}
			}

			DataRow row = new DataRow(rowId, columns);
			List<String> values = new ArrayList<>();
			for (DataColumn column : columns) {
				values.add(node.get(column.getCode()).asText());
			}
			row.setAll(values.toArray(new String[values.size()]));

			return row;
		}
	}

}
