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
package org.eclipse.help.internal.webapp.data;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.HelpSystem;

/**
 * Helper class for contents.jsp initialization
 */
public class RequestData {
	public final static int MODE_WORKBENCH = HelpSystem.MODE_WORKBENCH;
	public final static int MODE_INFOCENTER = HelpSystem.MODE_INFOCENTER;
	public final static int MODE_STANDALONE = HelpSystem.MODE_STANDALONE;

	protected ServletContext context;
	protected HttpServletRequest request;
	protected String locale;
	protected WebappPreferences preferences;
	/**
	 * Constructs the data for a request.
	 * @param context
	 * @param request
	 */
	public RequestData(ServletContext context, HttpServletRequest request) {
		this.context = context;
		this.request = request;
		preferences = new WebappPreferences();

		locale = UrlUtil.getLocale(request);
	}

	/**
	 * Returns the preferences object
	 */
	public WebappPreferences getPrefs() {
		return preferences;
	}

	public boolean isGecko() {
		return UrlUtil.isGecko(request);
	}

	public boolean isIE() {
		return UrlUtil.isIE(request);
	}

	public boolean isKonqueror() {
		return UrlUtil.isKonqueror(request);
	}

	public boolean isMozilla() {
		return UrlUtil.isMozilla(request);
	}

	public String getMozillaVersion() {
		return UrlUtil.getMozillaVersion(request);
	}

	public boolean isOpera() {
		return UrlUtil.isOpera(request);
	}

	public String getLocale() {
		return locale;
	}

	public int getMode() {
		return HelpSystem.getMode();
	}

	public String getDBCSParameter(String name) {
		if (UrlUtil.isIE(request)
			&& request.getParameter("encoding") != null) {
			// parameter is escaped using JavaScript
			return UrlUtil.unescape(
				UrlUtil.getRawRequestParameter(request, name));
		} else {
			return request.getParameter(name);
		}
	}

	public String[] getDBCSParameters(String name) {
		if (UrlUtil.isIE(request)
			&& request.getParameter("encoding") != null) {
			// parameter is escaped using JavaScript
			String[] rawValues = UrlUtil.getRawRequestParameters(request, name);
			if (rawValues == null || rawValues.length == 0) {
				return null;
			} else {
				String[] values = new String[rawValues.length];
				for (int i = 0; i < rawValues.length; i++)
					values[i] = UrlUtil.unescape(rawValues[i]);
				return values;
			}
		} else {
			return request.getParameterValues(name);
		}
	}
}
