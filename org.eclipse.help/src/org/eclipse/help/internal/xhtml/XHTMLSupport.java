/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import org.eclipse.help.HelpSystem;
import org.w3c.dom.Document;


/**
 * Central class for XHTML support in help.
 */
public class XHTMLSupport {

	// singleton for performance.
	private static UAContentFilterProcessor filterProcessor = new UAContentFilterProcessor();




	private Document document = null;

	private UAContentMergeProcessor mergeProcessor = null;
	private UACharsetProcessor charsetProcessor = null;


	public XHTMLSupport(String pluginID, String file, Document document, String locale) {
		this.document = document;
		mergeProcessor = new UAContentMergeProcessor(pluginID, file, document, locale);
		charsetProcessor = new UACharsetProcessor();
	}

	/**
	 * Processes the DOM, with filtering turned on.
	 * 
	 * @return the resulting DOM
	 */
	public Document processDOM() {
		return processDOM(true);
	}

	/**
	 * Processes the DOM. Filtering will only be done if requested. Filtering
	 * may be skipped, for example, for indexing.
	 * 
	 * @param filter whether or not to filter
	 * @return the resulting DOM
	 */
	public Document processDOM(boolean filter) {

		// filters do not apply to shared help systems
		if (filter && !HelpSystem.isShared()) {
			// resolve filters.
			filterProcessor.applyFilters(document);
		}

		// resolve includes.
		mergeProcessor.resolveIncludes();

		// resolve anchors.
		mergeProcessor.resolveContentExtensions();

		// set charset to UTF-8, since that's how we're going to output it
		charsetProcessor.processCharset(document);
		
		return document;
	}

	public static UAContentFilterProcessor getFilterProcessor() {
		return filterProcessor;
	}

	/**
	 * Used by the UI plugin to override base functionality and add more filtering capabilities.
	 */
	public static void setFilterProcessor(UAContentFilterProcessor filterProcessor) {
		XHTMLSupport.filterProcessor = filterProcessor;
	}

}
