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
import java.net.*;
import java.text.*;
import java.util.*;
/**
 * Uses a resource bundle to load images and strings from
 * a property file.
 * This class needs to properly use the desired locale.
 */
public class HelpUIResources {
	//*** NOTE: change this to properly load a resource bundle help.properties
	//***       for a desired locale....
	private static ResourceBundle resBundle;
	private static URL imageURL;
	static {
		resBundle = ResourceBundle.getBundle(HelpUIResources.class.getName());
		try {
			imageURL =
				new URL(
					HelpUIPlugin.getDefault().getDescriptor().getInstallURL(),
					"icons/");
		} catch (MalformedURLException e) {
		}
	}
	/**
	 * WorkbenchResources constructor comment.
	 */
	public HelpUIResources() {
		super();
	}
	/**
	 * Returns a string from a property file
	 */
	public static URL getImagePath(String name) {
		URL imagePathURL = null;
		try {
			imagePathURL = new URL(imageURL, name);
			return imagePathURL;
		} catch (MalformedURLException e) {
		}
		//return image;
		return null;
	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name) {
		try {
			return resBundle.getString(name);
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(stringFromPropertiesFile, new Object[] { replace0 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(String name, String replace0, String replace1) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2,
		String replace3) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2, replace3 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2,
		String replace3,
		String replace4) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2, replace3, replace4 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
	/**
	 * Returns a string from a property file
	 */
	public static String getString(
		String name,
		String replace0,
		String replace1,
		String replace2,
		String replace3,
		String replace4,
		String replace5) {
		try {
			String stringFromPropertiesFile = resBundle.getString(name);
			stringFromPropertiesFile =
				MessageFormat.format(
					stringFromPropertiesFile,
					new Object[] { replace0, replace1, replace2, replace3, replace4, replace5 });
			return stringFromPropertiesFile;
		} catch (Exception e) {
			return name;
		}

	}
}
