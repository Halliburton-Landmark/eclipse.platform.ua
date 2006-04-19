package org.eclipse.help.internal.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.xhtml.XHTMLContentDescriber;
import org.eclipse.help.search.ISearchIndex;
import org.eclipse.help.search.LuceneSearchParticipant;


public class HTMLSearchParticipant extends LuceneSearchParticipant {

	private HTMLDocParser parser;
	private String indexPath;
	private IContentDescriber xhtmlDescriber;

	public HTMLSearchParticipant(String indexPath) {
		parser = new HTMLDocParser();
		this.indexPath = indexPath;
	}

	public IStatus addDocument(ISearchIndex index, String pluginId, String name, URL url, String id,
			Document doc) {
		// if it's XHTML, forward it on to the proper search participant
		if (isXHTML(pluginId, url)) {
			SearchManager manager = BaseHelpSystem.getSearchManager();
			LuceneSearchParticipant xhtmlParticipant = manager.getParticipant("org.eclipse.help.base.xhtml"); //$NON-NLS-1$
			return xhtmlParticipant.addDocument(index, pluginId, name, url, id, doc);
		}
		// otherwise, treat it as HTML
		else {		
			try {
				try {
					try {
						parser.openDocument(url);
					} catch (IOException ioe) {
						return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.ERROR,
								"Help document " //$NON-NLS-1$
										+ name + " cannot be opened.", //$NON-NLS-1$
								null);
					}
					ParsedDocument parsed = new ParsedDocument(parser.getContentReader());
					doc.add(Field.Text("contents", parsed.newContentReader())); //$NON-NLS-1$
					doc.add(Field.Text("exact_contents", parsed //$NON-NLS-1$
							.newContentReader()));
					String title = parser.getTitle();
					doc.add(Field.UnStored("title", title)); //$NON-NLS-1$
					doc.add(Field.UnStored("exact_title", title)); //$NON-NLS-1$
					doc.add(Field.UnIndexed("raw_title", title)); //$NON-NLS-1$
					doc.add(Field.UnIndexed("summary", parser.getSummary(title))); //$NON-NLS-1$
				} finally {
					parser.closeDocument();
				}
			} catch (IOException e) {
				return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.ERROR,
						"IO exception occurred while adding document " + name //$NON-NLS-1$
								+ " to index " + indexPath + ".", //$NON-NLS-1$ //$NON-NLS-2$
						e);
			}
			return Status.OK_STATUS;
		}
	}
	
	/**
	 * Returns whether or not the given content should be treated as XHTML.
	 * 
	 * @param pluginId the plugin id containing the content
	 * @param url the URL to the content
	 * @return whether the content should be treated as XHTML
	 */
	private boolean isXHTML(String pluginId, URL url) {
		// if the search participant isn't bound to the plugin we shouldn't use it
		SearchManager manager = BaseHelpSystem.getSearchManager();
		if (manager.isParticipantBound(pluginId, "org.eclipse.help.base.xhtml")) { //$NON-NLS-1$
			if (xhtmlDescriber == null) {
				xhtmlDescriber = new XHTMLContentDescriber();
			}
			InputStream in = null;
			try {
				in = url.openStream();
				return (xhtmlDescriber.describe(in, null) == IContentDescriber.VALID);
			}
			catch (Exception e) {
				// if anything goes wrong, treat it as not xhtml
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException e) {
						// nothing we can do
					}
				}
			}
		}
		return false;
	}
}
