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

import org.apache.lucene.analysis.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;

/**
 * Text Analyzer Descriptor.  Encapsulates Lucene Analyzer
 */
public class AnalyzerDescriptor {
	private Analyzer luceneAnalyzer;
	private String id;
	private String lang;

	/**
	 * Constructor
	 */
	public AnalyzerDescriptor(String locale) {

		// try creating the analyzer for the specified locale (usually lang_country)
		this.luceneAnalyzer = createAnalyzer(locale);

		// 	try creating configured analyzer for the language only
		if (this.luceneAnalyzer == null) {
			String language = null;
			if (locale.length() > 2) {
				language = locale.substring(0, 2);
				this.luceneAnalyzer = createAnalyzer(language);
			}
		}

		// if all fails, create default analyzer
		if (this.luceneAnalyzer == null) {
			this.id =
				HelpBasePlugin.getDefault().getDescriptor().getUniqueIdentifier()
					+ "#"
					+ HelpBasePlugin
						.getDefault()
						.getDescriptor()
						.getVersionIdentifier()
						.toString();
			this.luceneAnalyzer = new DefaultAnalyzer(locale);
			this.lang = locale;
		}

	}
	/**
	 * Gets the analyzer.
	 * @return Returns a Analyzer
	 */
	public Analyzer getAnalyzer() {
		return new SmartAnalyzer(lang, luceneAnalyzer);
	}

	/**
	 * Gets the id.
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the language for the analyzer
	 * @return Returns a String
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Creates analyzer for a locale, 
	 * if it is configured in the org.eclipse.help.luceneAnalyzer
	 * extension point. The identifier of the analyzer  and locale and lang are also set.
	 * @return Analyzer or null if no analyzer is configured
	 * for given locale.
	 */
	private Analyzer createAnalyzer(String locale) {
		// find extension point
		IConfigurationElement configElements[] =
			Platform.getPluginRegistry().getConfigurationElementsFor(
				HelpBasePlugin.PLUGIN_ID,
				"luceneAnalyzer");
		for (int i = 0; i < configElements.length; i++) {
			if (!configElements[i].getName().equals("analyzer"))
				continue;
			String analyzerLocale = configElements[i].getAttribute("locale");
			if (analyzerLocale == null || !analyzerLocale.equals(locale))
				continue;
			try {
				Object analyzer =
					configElements[i].createExecutableExtension("class");
				if (!(analyzer instanceof Analyzer))
					continue;
				else {
					String pluginId =
						configElements[i]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getUniqueIdentifier();
					String pluginVersion =
						configElements[i]
							.getDeclaringExtension()
							.getDeclaringPluginDescriptor()
							.getVersionIdentifier()
							.toString();
					this.luceneAnalyzer = (Analyzer) analyzer;
					this.id = pluginId + "#" + pluginVersion;
					this.lang = locale;
					if (HelpBasePlugin.PLUGIN_ID.equals(pluginId)) {
						// The analyzer is contributed by help plugin.
						// Continue in case there is another analyzer for the same locale
						// let another analyzer take precendence over one from help
					} else {
						// the analyzer does not come from help
						return this.luceneAnalyzer;
					}
				}
			} catch (CoreException ce) {
				HelpBasePlugin.logError(
					HelpBaseResources.getString(
						"ES23",
						configElements[i].getAttribute("class"),
						locale),
					ce);
			}
		}

		return this.luceneAnalyzer;
	}
	/**
	 * Checks whether analyzer is compatible with a given analyzer
	 * @param analyzerId id of analyzer used in the past by the index;
	 *  id has a form: pluginID#pluginVersion
	 * @return true when it is known that given analyzer is compatible with
	 *  this  analyzer
	 */
	public boolean isCompatible(String analyzerId) {
		if (id.equals(analyzerId)) {
			return true;
		}
		// analyzers between versions 2.0.1 and 3.0.0 of org.eclipse.help plugin
		// are compatible (logic unchanged), index can be preserved between them
		if (analyzerId.compareTo(HelpBasePlugin.PLUGIN_ID + "#2.0.1") >= 0
			&& analyzerId.compareTo(HelpBasePlugin.PLUGIN_ID + "#3.0.0") <= 0
			&& id.compareTo(HelpBasePlugin.PLUGIN_ID + "#2.0.1") >= 0
			&& id.compareTo(HelpBasePlugin.PLUGIN_ID + "#3.0.0") <= 0) {
			return true;
		}
		return false;
	}

}
