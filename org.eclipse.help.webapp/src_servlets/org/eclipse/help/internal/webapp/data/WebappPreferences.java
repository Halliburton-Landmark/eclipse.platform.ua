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

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;

/**
 * Preferences for availiable to webapp
 */
public class WebappPreferences {
	Preferences prefs;
	/**
	 * Constructor.
	 */
	public WebappPreferences() {
		prefs = HelpBasePlugin.getDefault().getPluginPreferences();
	}
	/**
	 * @return String - URL of banner page or null
	 */
	public String getBanner() {
		return prefs.getString("banner");
	}

	public String getBannerHeight() {
		return prefs.getString("banner_height");
	}

	public String getHelpHome() {
		return prefs.getString("help_home");
	}

	public boolean isBookmarksView() {
		return BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER
			&& "true".equals(prefs.getString("bookmarksView"));
	}

	public boolean isLinksView() {
		return BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER
			&& "true".equals(prefs.getString("linksView"));
	}

	public String getImagesDirectory() {
		String imagesDirectory = prefs.getString("imagesDirectory");
		if (imagesDirectory != null && imagesDirectory.startsWith("/"))
			imagesDirectory = UrlUtil.getHelpURL(imagesDirectory);
		return imagesDirectory;

	}

	public String getToolbarBackground() {
		return prefs.getString("advanced.toolbarBackground");
	}

	public String getBasicToolbarBackground() {
		return prefs.getString("basic.toolbarBackground");
	}

	public String getToolbarFont() {
		return prefs.getString("advanced.toolbarFont");
	}

	public String getViewBackground() {
		return prefs.getString("advanced.viewBackground");
	}

	public String getBasicViewBackground() {
		return prefs.getString("basic.viewBackground");
	}

	public String getViewFont() {
		return prefs.getString("advanced.viewFont");
	}

	public int getBookAtOnceLimit() {
		return prefs.getInt("loadBookAtOnceLimit");
	}

	public int getLoadDepth() {
		int value = prefs.getInt("dynamicLoadDepthsHint");
		if (value < 1) {
			return 1;
		}
		return value;
	}
	public boolean isWindowTitlePrefix() {
		return "true".equalsIgnoreCase(prefs.getString("windowTitlePrefix"));

	}

}
