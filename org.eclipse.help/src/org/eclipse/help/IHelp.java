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
package org.eclipse.help;
/**
 * Interface to the help system UI.
 * <p>
 * The Eclipse platform defines an extension point 
 * (<code>"org.eclipse.help.support"</code>) for plugging in a help system UI.
 * The help system UI is entirely optional.
 * Clients may provide a UI for presenting help to the user by implementing this
 * interface and including the name of their class in the 
 * <code>&lt;config&gt;</code> element in an extension to the platform's help 
 * support extension point (<code>"org.eclipse.help.support"</code>).
 * </p>
 * <p>
 * Note that an implementation of the help system UI is provided by the 
 * <code>"org.eclipse.help.ui"</code> plug-in (This plug-in is not 
 * mandatory, and can be removed). Since the platform can only make use of a 
 * single help system UI implementation, make sure that the platform is not 
 * configured with more than one plug-in trying to extend this extension point.
 * @deprecated use org.eclipse.help.HelpSystem.getTocs()
 *  and org.eclipse.helpHelpSystem.getContext(java.lang.String) to obtain help resources.
 * </p>
 */
public interface IHelp {
	
	/**
	 * Displays the entire help bookshelf.
	 * <p>
	 * This method is called by the platform to launch the help system UI
	 * </p> 
	 * @since 2.0
	 */
	public void displayHelp();
	
	/**
	 * Displays context-sensitive help for the given context.
	 * <p>
	 * (x,y) coordinates specify the location where the context sensitive 
	 * help UI will be presented. These coordinates are screen-relative 
	 * (ie: (0,0) is the top left-most screen corner).
	 * The platform is responsible for calling this method and supplying the 
	 * appropriate location.
	 * </p>
	 * 
	 * 
	 * @param context the context to display
	 * @param x horizontal position
	 * @param y verifical position
	 * @since 2.0
	 */
	public void displayContext(IContext context, int x, int y);
	
	/**
	 * Displays context-sensitive help for context with the given context id.
	 * <p>
	 * (x,y) coordinates specify the location where the context sensitive
	 * help UI will be presented. These coordinates are screen-relative 
	 * (ie: (0,0) is the top left-most screen corner).
	 * The platform is responsible for calling this method and supplying the 
	 * appropriate location.
	 * </p> 
	 * 
	 * @param contextId the help context identifier;
	 *  the parameter needs to have a form pluginID.pluginContextId,
	 *  where pluginID is ID of plug-in contributing a context,
	 *  and pluginContextID is ID of context contributed in a plug-in.
	 * @param x horizontal position
	 * @param y verifical position
	 * @see #getContext(String)
	 * @since 2.0
	 */
	public void displayContext(String contextId, int x, int y);
	
	/**
	 * Displays help content for the help resource with the given URL.
	 * <p>
	 * This method is called by the platform to launch the help system UI, displaying
	 * the documentation identified by the <code>href</code> parameter.
	 * </p> 
	 * <p>
	 * The help system makes no guarantee that all the help resources can be displayed or how they are displayed.
	 * </p>
	 * @param href the URL of the help resource.
	 * <p>Valid href are as described in 
	 * 	{@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
	 * </p>
	 * @since 2.0
	 */
	public void displayHelpResource(String href);
	
	/**
	 * Displays help content for the help resource.
	 * <p>
	 * This method is called by the platform to launch the help system UI, displaying
	 * the documentation identified by the <code>helpResource</code> parameter.
	 * <p>
	 * The help system makes no guarantee that all the help resources can be displayed or how they are displayed.
	 * </p>
	 * @see IHelp#displayHelpResource(String)
	 * @param helpResource the help resource to display.
	 * @since 2.0
	 */
	public void displayHelpResource(IHelpResource helpResource);
	
	
	/**
	 * Displays help content for the toc with the given URL.
	 * <p>
	 * This method is called by the platform to launch the help system UI, displaying
	 * the documentation identified by the <code>toc</code> parameter.
	 * </p> 
	 * <p>
	 * Valid toc are
	 * contributed through the <code>toc</code> element of the 
	 * <code>"org.eclipse.help.toc"</code> extension point.   
	 * </p> 
	 *
	 * @param toc the URL of the toc as specified in
	 * the <code>"org.eclipse.help.toc"</code> extenstion
	 * point
	 * @deprecated use displayHelpResource(toc) instead
	 */
	public void displayHelp(String toc);
	
	/**
	 * This method is an extension to the 
	 * <a href="#displayHelp(java.lang.String)">displayHelp(String toc)</a>
	 * method, providing the ability to open the specified help topic.
	 * <p>
	 * <code>selectedTopic</code> should be a valid help topic url contained in
	 * the specified <code>toc</code> and have the following format: 
	 * <em>/pluginID/path_to_document</em>
	 * <br>where
	 * <dl>
	 * <dt> <em>pluginID</em> is the unique identifier of the plugin containing the help topic, 
	 * </dt>
	 * <dt> <em>path_to_document</em> is the help topic path, relative to the plugin directory
	 * </dt>
	 * </dl>
	 * </p>
	 * @param toc the URL of the toc
	 * @param selectedTopic the help topic url.
	 * @see #displayHelp(java.lang.String)
	 * @deprecated use displayHelpResource(selectedTopic).
	 */
	public void displayHelp(String toc, String selectedTopic);

	
	/**
	 * Displays context-sensitive help for context with the given context id.
	 * <p>
	 * (x,y) coordinates specify the location where the context sensitive
	 * help UI will be presented. These coordinates are screen-relative 
	 * (ie: (0,0) is the top left-most screen corner).
	 * The platform is responsible for calling this method and supplying the 
	 * appropriate location.
	 * </p> 
	 * 
	 * @param contextId the help context identifier 
	 * @param x horizontal position
	 * @param y verifical position
	 * @see #getContext(String)
	 * @deprecated use displayContext(contextId, x, y)
	 */
	public void displayHelp(String contextId, int x, int y);
	
	/**
	 * Displays context-sensitive help for the given context.
	 * <p>
	 * (x,y) coordinates specify the location where the context sensitive 
	 * help UI will be presented. These coordinates are screen-relative 
	 * (ie: (0,0) is the top left-most screen corner).
	 * The platform is responsible for calling this method and supplying the 
	 * appropriate location.
	 * </p>
	 * 
	 * 
	 * @param context the context to display
	 * @param x horizontal position
	 * @param y verifical position
	 * @deprecated use displayContext(context, x, y)
	 */
	public void displayHelp(IContext context, int x, int y);
	
	/**
	 * Computes and returns context information for the given context id.
	 * 
	 * @param contextId the context id
	 * @return the context, or <code>null</code> if none
	 */
	public IContext getContext(String contextId);
	
	/**
	 * Returns the list of all integrated tables of contents available.
	 * @return an array of TOC's
	 * @since 2.0
	 */
	public IToc[] getTocs();
	/**
	 * Returns <code>true</code> if the context-sensitive help
	 * window is currently being displayed, <code>false</code> if not.
	 */
	public boolean isContextHelpDisplayed();
}
