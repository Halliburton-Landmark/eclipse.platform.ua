/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.eclipse.core.boot.*;
import org.eclipse.help.internal.base.*;
/**
 * Lucene Analyzer.
 * LowerCaseTokenizer->WordTokenStream (uses word breaking in java.text)
 */
public class DefaultAnalyzer extends Analyzer {
	/**
	 * Constructor for Analyzer.
	 */
	private Locale locale;
	public DefaultAnalyzer(String localeString) {
		super();
		// Create a locale object for a given locale string
		Locale userLocale = getLocale(localeString);

		// Check if the locale is supported by BreakIterator
		// check here to do it only once.
		Locale[] availableLocales = BreakIterator.getAvailableLocales();
		for (int i = 0; i < availableLocales.length; i++) {
			if (userLocale.equals(availableLocales[i])) {
				locale = userLocale;
				break;
			}
		}
		if (locale == null && userLocale.getDisplayVariant().length() > 0) {
			// Check if the locale without variant is supported by BreakIterator
			Locale countryLocale =
				new Locale(userLocale.getLanguage(), userLocale.getCountry());
			for (int i = 0; i < availableLocales.length; i++) {
				if (countryLocale.equals(availableLocales[i])) {
					locale = countryLocale;
					break;
				}
			}
		}
		if (locale == null && userLocale.getCountry().length() > 0) {
			// Check if at least the language is supported by BreakIterator
			Locale language = new Locale(userLocale.getLanguage(), "");
			for (int i = 0; i < availableLocales.length; i++) {
				if (language.equals(availableLocales[i])) {
					locale = language;
					break;
				}
			}
		}

		if (locale == null) {
			// Locale is not supported, will use en_US
			HelpBasePlugin.logError(
				HelpBaseResources.getString("ES24", localeString),
				null);
			locale = new Locale("en", "US");
		}
	}
	/**
	 * Creates a TokenStream which tokenizes all the text
	 * in the provided Reader.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return new LowerCaseFilter(
			new WordTokenStream(fieldName, reader, locale));
	}

	/**
	 * Creates a Locale object out of a string representation
	 */
	private Locale getLocale(String clientLocale) {
		if (clientLocale == null)
			clientLocale = BootLoader.getNL();
		if (clientLocale == null)
			clientLocale = Locale.getDefault().toString();

		// break the string into tokens to get the Locale object
		StringTokenizer locales = new StringTokenizer(clientLocale, "_");
		if (locales.countTokens() == 1)
			return new Locale(locales.nextToken(), "");
		else if (locales.countTokens() == 2)
			return new Locale(locales.nextToken(), locales.nextToken());
		else if (locales.countTokens() == 3)
			return new Locale(
				locales.nextToken(),
				locales.nextToken(),
				locales.nextToken());
		else
			return Locale.getDefault();
	}
}
