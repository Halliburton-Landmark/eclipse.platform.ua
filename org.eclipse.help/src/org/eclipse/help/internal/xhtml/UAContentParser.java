/***************************************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.HelpPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * 
 */
public class UAContentParser {

	private static String TAG_HTML = "html"; //$NON-NLS-1$
	protected static String XHTML1_TRANSITIONAL = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"; //$NON-NLS-1$
	protected static String XHTML1_STRICT = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"; //$NON-NLS-1$
	protected static String XHTML1_FRAMESET = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"; //$NON-NLS-1$


	protected static Hashtable dtdMap = new Hashtable();

	static {
		String dtdBaseLocation = "dtds/xhtml1-20020801/"; //$NON-NLS-1$

		String dtdLocation = dtdBaseLocation + "xhtml1-transitional.dtd"; //$NON-NLS-1$
		URL dtdURL_T = BundleUtil.getResourceAsURL(dtdLocation, "org.eclipse.ui.intro"); //$NON-NLS-1$
		dtdMap.put(XHTML1_TRANSITIONAL, dtdURL_T);

		dtdLocation = dtdBaseLocation + "xhtml1-strict.dtd"; //$NON-NLS-1$
		URL dtdURL_S = BundleUtil.getResourceAsURL(dtdLocation, "org.eclipse.ui.intro"); //$NON-NLS-1$
		dtdMap.put(XHTML1_STRICT, dtdURL_S);

		dtdLocation = dtdBaseLocation + "xhtml1-frameset.dtd"; //$NON-NLS-1$
		URL dtdURL_F = BundleUtil.getResourceAsURL(dtdLocation, "org.eclipse.ui.intro"); //$NON-NLS-1$
		dtdMap.put(XHTML1_FRAMESET, dtdURL_F);
	}



	private Document document;
	private boolean hasXHTMLContent;

	public UAContentParser(String content) {
		parseDocument(content);
	}

	public UAContentParser(InputStream content) {
		parseDocument(content);
	}

	/**
	 * Creates a config parser assuming that the passed content represents a URL to the content
	 * file.
	 */
	public void parseDocument(Object content) {
		document = doParse(content);
		if (document != null) {
			Element rootElement = document.getDocumentElement();
			// DocumentType docType = document.getDoctype();
			if (rootElement.getTagName().equals(TAG_HTML)) {
				hasXHTMLContent = true;
			}
		}
	}

	private DocumentBuilder createParser() {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setValidating(false);
			docFactory.setNamespaceAware(true);
			docFactory.setExpandEntityReferences(false);
			DocumentBuilder parser = docFactory.newDocumentBuilder();

			parser.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
						IOException {

					if (systemId.equals(XHTML1_TRANSITIONAL) || systemId.equals(XHTML1_STRICT)
							|| systemId.equals(XHTML1_FRAMESET)) {

						URL dtdURL = (URL) dtdMap.get(systemId);
						InputSource in = new InputSource(dtdURL.openStream());
						in.setSystemId(dtdURL.toExternalForm());
						return in;
					}
					return null;
				}
			});
			return parser;
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(pce.getMessage(), pce);
		}
		return null;
	}



	private Document doParse(Object fileObject) {
		try {
			DocumentBuilder parser = createParser();
			if (fileObject instanceof String)
				return parser.parse((String) fileObject);
			else if (fileObject instanceof InputStream) {
				BufferedInputStream in = new BufferedInputStream((InputStream)fileObject);
				String charset = getCharset(in);
				InputSource input = null;
				if (charset != null) {
					input = new InputSource(new InputStreamReader(in, charset));
				}
				else {
					input = new InputSource(in);
				}
				return parser.parse(input);
			}
			return null;
		}
		catch (Exception e) {
			// log it
			HelpPlugin.logError("An error occured while parsing: " + fileObject, e); //$NON-NLS-1$
			// wrap it in an unchecked wrapper so that it finds its way
			// to the error message
			throw new UndeclaredThrowableException(e);
		}
	}

	/**
	 * Returned the DOM representing the xml content file. May return null if parsing the file
	 * failed.
	 * 
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return document;
	}

	public boolean hasXHTMLContent() {
		return hasXHTMLContent;
	}

	/*
	 * Returns the charset specified in the meta tag, or null if can't find it.
	 */
	private static String getCharset(BufferedInputStream in) throws IOException {
		in.mark(XHTMLCharsetParser.MAX_OFFSET);
		byte[] buf = new byte[XHTMLCharsetParser.MAX_OFFSET];
		in.read(buf);
		in.reset();
		return XHTMLCharsetParser.getCharsetFromHTML(new ByteArrayInputStream(buf));
	}
}
