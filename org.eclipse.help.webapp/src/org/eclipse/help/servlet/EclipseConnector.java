/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.eclipse.help.internal.HelpSystem;

/**
 * Performs transfer of data from eclipse to a jsp/servlet
 */
public class EclipseConnector {
	private ServletContext context;
	private static IFilter[] noFilters = new IFilter[0];
	private static final String errorPageBegin =
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"
			+ "<html><head>\n"
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
			+ "</head>\n"
			+ "<body><p>\n";
	private static final String errorPageEnd = "</p></body></html>";

	/**
	 * Constructor.
	 */
	public EclipseConnector(ServletContext context) {
		this.context = context;
	}

	public InputStream openStream(String url, HttpServletRequest request) {
		try {
			URLConnection con = openConnection(url, request);
			return con.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}

	public void transfer(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {

		try {

			String url = getURL(req);
			if (url == null)
				return;
			if (url.toLowerCase().startsWith("file:/")) {
				int i = url.indexOf('?');
				if (i != -1)
					url = url.substring(0, i);
				if (!UrlUtil.validate(url, req, context))
					return;
			}

			URLConnection con = openConnection(url, req);
			resp.setContentType(con.getContentType());
			resp.setHeader(
				"Cache-Control",
				"max-age="
					+ (con.getExpiration() - System.currentTimeMillis()));
			InputStream is = con.getInputStream();
			if (is == null
				&& (url.toLowerCase().endsWith("htm")
					|| url.toLowerCase().endsWith("html"))) {
				String error =
					errorPageBegin
						+ WebappResources.getString("noTopic", req)
						+ errorPageEnd;
				is = new ByteArrayInputStream(error.getBytes("UTF8"));
			}
			OutputStream os = resp.getOutputStream();

			IFilter[] filters = getFilters(req);
			if (filters.length == 0)
				transferContent(is, os);
			else {
				ByteArrayOutputStream tempOut = new ByteArrayOutputStream(4096);
				transferContent(is, tempOut);
				byte[] tempBuffer = tempOut.toByteArray();
				for (int i = 0; i < filters.length; i++) {
					tempBuffer = filters[i].filter(tempBuffer);
				}
				ByteArrayInputStream tempIn =
					new ByteArrayInputStream(tempBuffer);
				transferContent(tempIn, os);
			}
			os.flush();
			is.close();

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * Write the body to the response
	 */
	private void transferContent(InputStream inputStream, OutputStream out)
		throws IOException {
		try {
			// Prepare the input stream for reading
			BufferedInputStream dataStream =
				new BufferedInputStream(inputStream);

			// Create a fixed sized buffer for reading.
			// We could create one with the size of availabe data...
			byte[] buffer = new byte[4096];
			int len = 0;
			while (true) {
				len = dataStream.read(buffer); // Read file into the byte array
				if (len == -1)
					break;
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Gets content from the named url (this could be and eclipse defined url)
	 */
	private URLConnection openConnection(
		String url,
		HttpServletRequest request)
		throws Exception {
		//System.out.println("help content for: " + url);

		URLConnection con = null;
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER) {
			// it is an infocentre, add client locale to url
			String locale =
				request == null
					? Locale.getDefault().toString()
					: request.getLocale().toString();
			if (url.indexOf('?') >= 0) {
				url = url + "&lang=" + locale;
			} else {
				url = url + "?lang=" + locale;
			}
		}
		URL helpURL = new URL(url);
		String protocol = helpURL.getProtocol();
		if (!("help".equals(protocol)
			|| "search".equals(protocol)
			|| "links".equals(protocol)
			|| "file".equals(protocol)
			|| "livehelp".equals(protocol))) {
			throw new IOException();
		}
		con = helpURL.openConnection();

		con.setAllowUserInteraction(false);
		con.setDoInput(true);
		con.connect();
		return con;
	}

	/**
	 * Extracts the url from a request
	 */
	private String getURL(HttpServletRequest req) {
		String query = "";
		boolean firstParam = true;
		for (Enumeration params = req.getParameterNames();
			params.hasMoreElements();
			) {
			String param = (String) params.nextElement();
			String[] values = req.getParameterValues(param);
			if (values == null)
				continue;
			for (int i = 0; i < values.length; i++) {
				if (firstParam) {
					query += "?" + param + "=" + values[i];
					firstParam = false;
				} else
					query += "&" + param + "=" + values[i];
			}
		}

		// the request contains the eclipse url help: or search:
		String url = req.getPathInfo() + query;
		if (url.startsWith("/"))
			url = url.substring(1);
		return url;
	}

	/**
	 * Returns the filters for this url, if any
	 * @return array of IFilter
	 */
	private IFilter[] getFilters(HttpServletRequest req) {
		String uri = req.getRequestURI();
		//String agent = req.getHeader("User-Agent").toLowerCase(Locale.US);
		//boolean ie = (agent.indexOf("msie") != -1);
		if (uri != null && (uri.endsWith("html") || uri.endsWith("htm"))) {
			if (req.getParameter("resultof") != null)
				return new IFilter[] {
					new FramesetFilter(req),
					new HighlightFilter(req)};
			else
				return new IFilter[] { new FramesetFilter(req)};
		} else
			return noFilters;
	}
}