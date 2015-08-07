package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Locale;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.belladati.sdk.impl.ViewImpl.UnknownViewTypeException;
import com.belladati.sdk.util.Localizable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test(dataProvider = "l10nElements")
public class LocalizableTest extends SDKTest {

	private final String id = "id";
	private final String name = "default name";

	private final Locale locale1 = Locale.ENGLISH;
	private final Locale locale2 = new Locale("tr");

	/** no localization defaults to name */
	public void noLocalization(ObjectNode json, ElementCreator creator) {
		Localizable element = creator.create(json);

		assertFalse(element.hasLocalization(locale1));
		assertFalse(element.hasLocalization(locale2));

		assertEquals(element.getName(locale1), name);
		assertEquals(element.getName(locale2), name);
	}

	/** localization is returned */
	public void hasLocalization(ObjectNode json, ElementCreator creator) {
		ObjectNode l10n = new ObjectMapper().createObjectNode();
		String l1name = "english name";
		String l2name = "turkish name";
		l10n.put("en", l1name).put("tr", l2name);
		json.put("localization", l10n);

		Localizable element = creator.create(json);

		assertTrue(element.hasLocalization(locale1));
		assertTrue(element.hasLocalization(locale2));

		assertEquals(element.getName(locale1), l1name);
		assertEquals(element.getName(locale2), l2name);
	}

	/** distinguish between localized and non-localized languages */
	public void hasPartialLocalization(ObjectNode json, ElementCreator creator) {
		ObjectNode l10n = new ObjectMapper().createObjectNode();
		String l1name = "english name";
		l10n.put("en", l1name);
		json.put("localization", l10n);

		Localizable element = creator.create(json);

		assertTrue(element.hasLocalization(locale1));
		assertFalse(element.hasLocalization(locale2));

		assertEquals(element.getName(locale1), l1name);
		assertEquals(element.getName(locale2), name);
	}

	/** case doesn't matter to find locale */
	public void caseMismatch(ObjectNode json, ElementCreator creator) {
		ObjectNode l10n = new ObjectMapper().createObjectNode();
		String lname = "english name";
		l10n.put("eN", lname);
		json.put("localization", l10n);

		Localizable element = creator.create(json);

		Locale locale = new Locale("En");
		assertTrue(element.hasLocalization(locale));

		assertEquals(element.getName(locale), lname);
	}

	@DataProvider(name = "l10nElements")
	protected Object[][] provideElements() {
		return new Object[][] { { builder.buildDataSetNode(id, name, null, null, null), new ElementCreator() {
			@Override
			public Localizable create(JsonNode json) {
				return new DataSetImpl(service, json);
			}
		} }, { builder.buildDataSetNode(id, name, null, null, null), new ElementCreator() {
			@Override
			public Localizable create(JsonNode json) {
				return new DataSetInfoImpl(service, json);
			}
		} }, { builder.buildReportNode(id, name, null, null, null), new ElementCreator() {
			@Override
			public Localizable create(JsonNode json) {
				return new ReportImpl(service, json);
			}
		} }, { builder.buildReportNode(id, name, null, "", null), new ElementCreator() {
			@Override
			public Localizable create(JsonNode json) {
				return new ReportInfoImpl(service, json);
			}
		} }, { builder.buildViewNode(id, name, "chart"), new ElementCreator() {
			@Override
			public Localizable create(JsonNode json) {
				try {
					return ViewImpl.buildView(service, json);
				} catch (UnknownViewTypeException e) {
					fail("Exception building the view");
					return null;
				}
			}
		} } };
	}

	private interface ElementCreator {
		Localizable create(JsonNode json);
	}
}
