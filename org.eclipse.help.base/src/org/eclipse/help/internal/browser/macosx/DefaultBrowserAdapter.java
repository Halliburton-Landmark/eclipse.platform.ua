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
package org.eclipse.help.internal.browser.macosx;

import java.io.IOException;

import org.eclipse.help.browser.IBrowser;

public class DefaultBrowserAdapter implements IBrowser {

	private static DefaultBrowserAdapter fgInstance;
	
	static DefaultBrowserAdapter getInstance() {
		if (fgInstance == null)
			fgInstance= new DefaultBrowserAdapter();
		return fgInstance;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#close()
	 */
	public void close() {
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#displayURL(String)
	 */
	public void displayURL(String url) {
		/*
		 * Code from Marc-Antoine Parent
		 */
		try {
			Runtime.getRuntime().exec(
				new String[] {
					"/usr/bin/osascript", 
					"-e", 
					"open location \"" + url +"\""
				}
			);
		} catch (IOException e) {
			// ignore silently
		}
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return false;
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
	}

	/**
	 * @see org.eclipse.help.ui.browser.IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
	}
}
