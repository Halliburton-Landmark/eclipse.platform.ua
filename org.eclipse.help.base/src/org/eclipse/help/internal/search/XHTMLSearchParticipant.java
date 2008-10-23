/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;
import org.eclipse.help.search.XMLSearchParticipant;
import org.xml.sax.Attributes;

/**
 * The search participant responsible for indexing XHTML documents.
 */
public class XHTMLSearchParticipant extends XMLSearchParticipant {
	
	private String title;
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.search.XMLSearchParticipant#handleEndElement(java.lang.String, org.eclipse.help.search.XMLSearchParticipant.IParsedXMLContent)
	 */
	protected void handleEndElement(String name, IParsedXMLContent data) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.search.XMLSearchParticipant#handleStartElement(java.lang.String, org.xml.sax.Attributes, org.eclipse.help.search.XMLSearchParticipant.IParsedXMLContent)
	 */
	protected void handleStartElement(String name, Attributes attributes, IParsedXMLContent data) {
	    title = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.search.XMLSearchParticipant#handleText(java.lang.String, org.eclipse.help.search.XMLSearchParticipant.IParsedXMLContent)
	 */
	protected void handleText(String text, IParsedXMLContent data) {
		String stackPath = getElementStackPath();
		IPath path = new Path(stackPath);
		if (path.segment(1).equalsIgnoreCase("body")) { //$NON-NLS-1$
			data.addText(text);
			data.addToSummary(text);
		} else if (path.segment(1).equalsIgnoreCase("head")) { //$NON-NLS-1$
			if (path.segment(path.segmentCount() -1).equalsIgnoreCase("title")) { //$NON-NLS-1$
				if (title == null) { 
					title = text;
				} else {
					title = title + text;
				}
				data.setTitle(title);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.search.XMLSearchParticipant#preprocess(java.io.InputStream, java.lang.String, java.lang.String)
	 */
	protected InputStream preprocess(InputStream in, String name, String locale) {
		try {
			return DynamicXHTMLProcessor.process(name, in, locale, false);
		}
		catch (Throwable t) {
			String msg = "An error occured while pre-processing help XHTML document \"" + name + "\" for search indexing"; //$NON-NLS-1$ //$NON-NLS-2$
			HelpBasePlugin.logError(msg, t);
			return in;
		}
	}
}