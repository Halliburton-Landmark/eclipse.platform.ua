package org.eclipse.help.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.AppServer;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.help.ui.internal.browser.BrowserManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
  * This class is a UI plugin. This may need to change to regular 
  * plugin if the plugin class is moved into the base help.
  */
public class WorkbenchHelpPlugin extends AbstractUIPlugin {
	private static WorkbenchHelpPlugin plugin;
	private IBrowser browser;
	/**
	 * WorkbenchHelpPlugin constructor. It is called as part of plugin
	 * activation.
	 */
	public WorkbenchHelpPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	/**
	 * @return HelpViewerPlugin
	 */
	public static WorkbenchHelpPlugin getDefault() {
		return plugin;
	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		// stop the web app
		if (AppServer.isRunning()) {
			AppServer.remove("help", "org.eclipse.help.webapp");
		}
		BrowserManager.getInstance().closeAll();
		super.shutdown();
	}
	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
	}

	public IBrowser getHelpBrowser() {
		if (browser == null)
			browser = BrowserManager.getInstance().createBrowser();
		return browser;
	}
}