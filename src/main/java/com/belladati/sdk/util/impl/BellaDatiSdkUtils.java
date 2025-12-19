package com.belladati.sdk.util.impl;

import com.belladati.sdk.impl.BellaDatiServiceImpl;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utilities and helper methods used in BellaDati SDK.
 * 
 * 
 */
public class BellaDatiSdkUtils {

	public static Date parseJavaUtilDate(String sourceText) {
		return parseJavaUtilDate(sourceText, BellaDatiServiceImpl.DATE_TIME_FORMAT);
	}

	public static Date parseJavaUtilDate(String sourceText, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		try {
			return format.parse(sourceText);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String joinUriWithParams(String relativeUri, Map<String, String> uriParameters) {
		try {
			URIBuilder builder = new URIBuilder(relativeUri);
			for (Entry<String, String> entry : uriParameters.entrySet()) {
				builder.addParameter(entry.getKey(), entry.getValue());
			}
			return builder.build().toString();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String joinUriWithParams(String relativeUri, String... uriParameters) {
		try {
			URIBuilder builder = new URIBuilder(relativeUri);

			if (uriParameters != null) {
				int index = 0;
				while ((index + 2) <= uriParameters.length) {
					String key = uriParameters[index++];
					String value = uriParameters[index++];
					builder.addParameter(key, value);
				}
			}

			return builder.build().toString();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

}
