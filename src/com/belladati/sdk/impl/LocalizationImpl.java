package com.belladati.sdk.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.belladati.sdk.util.Localizable;
import com.fasterxml.jackson.databind.JsonNode;

class LocalizationImpl implements Localizable {

	private final String name;
	private final Map<String, String> l10n;

	LocalizationImpl(JsonNode json) {
		this.name = json.get("name").asText();

		Map<String, String> l10n = new HashMap<String, String>();
		if (json.hasNonNull("localization")) {
			JsonNode locNode = json.get("localization");
			for (Iterator<Entry<String, JsonNode>> iterator = locNode.fields(); iterator.hasNext();) {
				Entry<String, JsonNode> entry = iterator.next();
				if (entry.getValue().isTextual()) {
					l10n.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue().asText());
				}
			}
		}

		this.l10n = Collections.unmodifiableMap(l10n);
	}

	@Override
	public String getName(Locale locale) {
		String loc = l10n.get(locale.getLanguage().toLowerCase(Locale.ENGLISH));
		return loc != null ? loc : name;
	}

	@Override
	public boolean hasLocalization(Locale locale) {
		return l10n.containsKey(locale.getLanguage().toLowerCase(Locale.ENGLISH));
	}

}
