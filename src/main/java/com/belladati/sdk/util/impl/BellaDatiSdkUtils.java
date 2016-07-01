package com.belladati.sdk.util.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.belladati.sdk.impl.BellaDatiServiceImpl;

/**
 * Utilities and helper methods used in BellaDati SDK.
 * 
 * @author Lubomir Elko
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

}
