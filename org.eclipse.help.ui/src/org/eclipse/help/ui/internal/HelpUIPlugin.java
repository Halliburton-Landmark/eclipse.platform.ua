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
package org.eclipse.help.ui.internal;
import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.plugin.*;

/**
 * This class is Help UI plugin.
 */
public class HelpUIPlugin extends AbstractUIPlugin {
	public final static String PLUGIN_ID = "org.eclipse.help.ui";
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_IE_ADAPTER = false;
	public static boolean DEBUG_IE_ADAPTER_IN_PROCESS = false;
	public static boolean DEBUG_INFOPOP = false;

	private static HelpUIPlugin plugin;
	/**
	 * Logs an Error message with an exception. Note that the message should
	 * already be localized to proper locale. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = "";
		Status errorStatus =
			new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		HelpPlugin.getDefault().getLog().log(errorStatus);
	}
	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper local. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpPlugin.DEBUG) {
			if (message == null)
				message = "";
			Status warningStatus =
				new Status(
					IStatus.WARNING,
					PLUGIN_ID,
					IStatus.OK,
					message,
					null);
			HelpPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * Plugin constructor. It is called as part of plugin activation.
	 */
	public HelpUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	/**
	 * Provides access to singleton
	 * 
	 * @return HelpUIPlugin
	 */
	public static HelpUIPlugin getDefault() {
		return plugin;
	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * 
	 * @exception CoreException
	 *                if this method fails to shut down this plug-in
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_IE_ADAPTER = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/ieadapter")); //$NON-NLS-1$
			DEBUG_IE_ADAPTER_IN_PROCESS = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/ieadapter/inprocess")); //$NON-NLS-1$
			DEBUG_INFOPOP = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/infopop")); //$NON-NLS-1$
		}

		BaseHelpSystem.setDefaultErrorUtil(new ErrorUtil());

		HelpPlugin.setRoleManager(
			new HelpRoleManager(
				((Workbench) PlatformUI.getWorkbench()).getActivityManager()));
	}

	public IBrowser getHelpBrowser() {
		return BaseHelpSystem.getHelpBrowser();
	}
}
