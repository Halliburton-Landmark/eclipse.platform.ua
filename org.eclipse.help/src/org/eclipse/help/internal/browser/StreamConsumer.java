package org.eclipse.help.internal.browser;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

/**
 * Used to receive output from processes
 */
public class StreamConsumer extends Thread {
	BufferedReader bReader;
	public StreamConsumer(InputStream inputStream) {
		super();
		setDaemon(true);
		bReader = new BufferedReader(new InputStreamReader(inputStream));
	}
	public void run() {
		try {
			String line;
			while (null != (line = bReader.readLine())) {
				BrowserLog.log(line);
			}
			bReader.close();
		} catch (IOException ioe) {
			HelpPlugin.logError(Resources.getString("WE001"), ioe);
		}
	}
}