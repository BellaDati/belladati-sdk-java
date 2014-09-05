package com.belladati.sdk.export;

import java.io.Serializable;
import java.util.Locale;

import com.belladati.sdk.impl.LocalizationImpl;

abstract class StoredLocalizable implements Serializable {

	private static final long serialVersionUID = 554576890904608571L;

	protected final String id;
	protected final String name;

	protected final LocalizationImpl localization;

	StoredLocalizable(String id, String name, LocalizationImpl localization) {
		this.id = id;
		this.name = name;
		this.localization = localization;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getName(Locale locale) {
		return localization != null ? localization.getName(locale) : name;
	}

	public boolean hasLocalization(Locale locale) {
		return localization != null && localization.hasLocalization(locale);
	}

}
