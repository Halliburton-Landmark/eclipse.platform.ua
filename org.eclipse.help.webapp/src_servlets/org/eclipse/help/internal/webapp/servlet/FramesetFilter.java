/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.net.URLEncoder;

import javax.servlet.http.*;

import org.eclipse.help.internal.webapp.data.*;

/**
 * This class inserts a script for showing the page inside the appropriate
 * frameset when bookmarked.
 */
public class FramesetFilter implements IFilter {
	private static final String scriptPart1 = "<script>if( self == top ){ window.location.replace( \""; //$NON-NLS-1$
	private static final String scriptPart3 = "\");}</script>"; //$NON-NLS-1$

	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		String uri = req.getRequestURI();
		String url = req.getPathInfo();

		if (uri == null || !uri.endsWith("html") && !uri.endsWith("htm")) { //$NON-NLS-1$ //$NON-NLS-2$
			return out;
		}

		if ("/nftopic".equals(req.getServletPath()) ||  //$NON-NLS-1$
			"/ntopic".equals(req.getServletPath()) ||  //$NON-NLS-1$
			UrlUtil.isBot(req)) { 
			return out;
		}

		String noframes = req.getParameter("noframes"); //$NON-NLS-1$
		if ("true".equals(noframes)) { //$NON-NLS-1$
			return out;
		}

		String path = req.getPathInfo();
		if (path == null) {
			return out;
		}
		StringBuffer script = new StringBuffer(scriptPart1);
		for (int i; 0 <= (i = path.indexOf('/')); path = path.substring(i + 1)) {
			script.append("../"); //$NON-NLS-1$
		}
		script.append("?topic="); //$NON-NLS-1$
		

		// Sanitize the url
		try{
			url = URLEncoder.encode(url, "UTF-8"); //$NON-NLS-1$
			script.append(url);
		} catch (UnsupportedEncodingException uee){
			return out;
		}

		script.append(scriptPart3);
		try {
			return new FilterHTMLHeadOutputStream(out, script.toString()
					.getBytes("ASCII")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}
}
