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
package org.eclipse.help.internal.webapp.servlet;
import java.io.*;

import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.webapp.*;
/**
 * Helper class to generate xml files.
 */
public class XMLGenerator {
	private File outFile = null;
	private PrintWriter out = null;
	public int pad = 0;
	// XML escaped characters mapping
	private static final String invalidXML[] = { "&", ">", "<", "\"", "\'" };
	private static final String escapedXML[] =
		{ "&amp;", "&gt;", "&lt;", "&quot;", "&apos;" };
	/**
	 * Constructor.
	 */
	public XMLGenerator(Writer writer) {
		if (writer instanceof PrintWriter)
			this.out = (PrintWriter) writer;
		else
			this.out = new PrintWriter(writer);
	}
	/**
	 * Constructor.
	 */
	public XMLGenerator(File outFile) {
		super();
		this.outFile = outFile;
		try {
			out =
				new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(
							new FileOutputStream(outFile),
							"UTF8")),
					false /* no aotoFlush */
			);
			println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		} catch (IOException ioe) {
			HelpWebappPlugin.logError(
				HelpBaseResources.getString("E014", outFile.getAbsolutePath()),
				ioe);
		}
	}

	// returns a String that is a valid XML string
	// by XML escaping special characters
	public static String xmlEscape(String cdata) {
		for (int i = 0; i < invalidXML.length; i++)
			cdata = TString.change(cdata, invalidXML[i], escapedXML[i]);
		return cdata;
	}
	public void close() {
		out.flush();
		out.close();
		if (out.checkError())
			if (outFile != null)
				HelpWebappPlugin.logError(
					HelpBaseResources.getString("E015", outFile.getAbsolutePath()),
					null);
		out = null;
	}
	public void print(Object o) {
		if (out != null)
			out.print(o);
	}
	public void println(Object o) {
		print(o);
		print("\n");
	}
	public void printPad() {
		for (int i = 0; i < pad; i++)
			print(" ");
	}
}
