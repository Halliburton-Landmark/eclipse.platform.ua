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
package org.eclipse.help.internal.appserver;

import org.eclipse.core.runtime.*;
/**
 */
public class AppserverPlugin extends Plugin {
	public final static String HOST_KEY = "host";
	public final static String PORT_KEY = "port";

	private final static String APP_SERVER_EXTENSION_ID =
		"org.eclipse.help.appserver.server";
	private static final String APP_SERVER_CLASS_ATTRIBUTE = "class";
	private static final String APP_SERVER_DEFAULT_ATTRIBUTE = "default";

	// singleton object
	private static AppserverPlugin plugin;
	
	private  IWebappServer appServer;
	private  IPluginDescriptor contributingServerPlugin;
	private  boolean initialized = false;

	private String hostAddress;
	private int port;

	/**
	 */
	public AppserverPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;

	}
	/**
	 */
	public static AppserverPlugin getDefault() {
		return plugin;
	}
	/**
	 * Returns the instance of WebappServer.
	 */
	public synchronized IWebappServer getAppServer() throws CoreException {
		if (appServer == null) {
			createWebappServer();
			startWebappServer();
		}
		return appServer;
	}
	/**
	 */
	public void shutdown() throws CoreException {
		if (appServer != null) {
			appServer.stop();
		}
	}
	/**
	 */
	public void startup() throws CoreException {
	}

	/**
	 * Initializes the default preferences settings for this plug-in.
	 */
	protected void initializeDefaultPluginPreferences() {
		getPluginPreferences().setDefault(PORT_KEY, 0);
	}
	/**
	 * Obtains Tomcat plug-in ID
	 */
	public static String getID() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	/**
	 * Returns the plugin that contributes the server implementation
	 * @return IPluginDescriptor
	 */
	public IPluginDescriptor getContributingServerPlugin() {
		return contributingServerPlugin;
	}
	
	private void createWebappServer() throws CoreException {
		initialized = true;
		// Initializes the app server by getting an instance via
		// app-server the extension point

		// get the app server extension from the system plugin registry
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IExtensionPoint point =
			pluginRegistry.getExtensionPoint(APP_SERVER_EXTENSION_ID);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0) {
				// We need to pick up the non-default configuration
				IConfigurationElement[] elements =
					extensions[0].getConfigurationElements();
				if (elements.length == 0)
					return;
				IConfigurationElement serverElement = null;
				for (int i = 0; i < elements.length; i++) {
					String defaultValue =
						elements[i].getAttribute(APP_SERVER_DEFAULT_ATTRIBUTE);
					if (defaultValue == null || defaultValue.equals("false")) {
						serverElement = elements[i];
						break;
					}
				}
				// if all the servers are default, then pick the first one
				if (serverElement == null)
					serverElement = elements[0];

				// Instantiate the app server
				try {
					appServer =
						(
							IWebappServer) serverElement
								.createExecutableExtension(
							APP_SERVER_CLASS_ATTRIBUTE);
					contributingServerPlugin = serverElement.getDeclaringExtension().getDeclaringPluginDescriptor();
				} catch (CoreException e) {
					getLog().log(e.getStatus());
					throw e;
				}
			}
		}
	}

	private void startWebappServer() throws CoreException {
		// Initialize host and port from preferences
		hostAddress = getPluginPreferences().getString(HOST_KEY);
		if ("".equals(hostAddress)) {
			hostAddress = null;
		}
		port = getPluginPreferences().getInt(PORT_KEY);

		// apply host and port overrides passed as command line arguments
		try {
			String hostCommandLineOverride = System.getProperty("server_host");
			if (hostCommandLineOverride != null
				&& hostCommandLineOverride.trim().length() > 0) {
				hostAddress = hostCommandLineOverride;
			}
		} catch (Exception e) {
		}
		try {
			String portCommandLineOverride = System.getProperty("server_port");
			if (portCommandLineOverride != null
				&& portCommandLineOverride.trim().length() > 0) {
				port = Integer.parseInt(portCommandLineOverride);
			}
		} catch (Exception e) {
		}

		if (appServer == null)
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					getID(),
					IStatus.OK,
					AppserverResources.getString("Appserver.start"),
					null));
					
		appServer.start(port, hostAddress);
	}
}
