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
package org.eclipse.help.internal.search;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.internal.util.*;
import org.osgi.framework.*;

/**
 * Text search index.  Documents added to this index
 * can than be searched against a search query.
 */
public class SearchIndex {
	private IndexReader ir;
	private IndexWriter iw;
	private File indexDir;
	private String locale;
	private TocManager tocManager;
	private AnalyzerDescriptor analyzerDescriptor;
	private PluginVersionInfo docPlugins;
	// table of all document names, used during indexing batches
	private HelpProperties indexedDocs;
	private static final String INDEXED_CONTRIBUTION_INFO_FILE =
		"indexed_contributions";
	public static final String INDEXED_DOCS_FILE = "indexed_docs";
	private static final String DEPENDENCIES_VERSION_FILENAME =
		"indexed_dependencies";
	private static final String LUCENE_PLUGIN_ID = "org.apache.lucene";
	private File inconsistencyFile;
	private HTMLDocParser parser;
	private IndexSearcher searcher;
	private Object searcherCreateLock = new Object();
	private HelpProperties dependencies;
	private boolean closed = false;
	// Collection of searches occuring now
	private Collection searches = new ArrayList();
	/**
	 * Constructor.
	 * @param locale the locale this index uses
	 * @param analyzerDesc the analyzer used to index
	 */
	public SearchIndex(String locale, AnalyzerDescriptor analyzerDesc, TocManager tocManager) {
		this.locale = locale;
		this.analyzerDescriptor = analyzerDesc;
		this.tocManager = tocManager;

		indexDir = new File(HelpBasePlugin.getConfigurationDirectory(), "index/"+locale);
		
		inconsistencyFile =
			new File(indexDir.getParentFile(), locale + ".inconsistent");
		parser = new HTMLDocParser();
		if (!exists()) {
			unzipProductIndex();
		}
	}
	/**
	 * Indexes one document from a stream.
	 * Index has to be open and close outside of this method
	 * @param name the document identifier (could be a URL)
	 * @param url the URL of the document
	 * @return true if success
	 */
	public boolean addDocument(String name, URL url) {
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out.println(
				"SearchIndex.addDocument(" + name + ", " + url + ")");
		}
		try {
			Document doc = new Document();
			doc.add(Field.Keyword("name", name));

			try {
				try {
					parser.openDocument(url);
				} catch (IOException ioe) {
					HelpBasePlugin.logError(
						HelpBaseResources.getString("ES25", name),
						null);
					return false;
				}
				ParsedDocument parsed =
					new ParsedDocument(parser.getContentReader());

				doc.add(Field.Text("contents", parsed.newContentReader()));
				doc.add(
					Field.Text("exact_contents", parsed.newContentReader()));

				String title = parser.getTitle();
				doc.add(Field.UnStored("title", title));
				doc.add(Field.UnStored("exact_title", title));
				doc.add(Field.UnIndexed("raw_title", title));
				// doc.add(Field.UnIndexed("summary", parser.getSummary()));
				iw.addDocument(doc);
			} finally {
				parser.closeDocument();
			}
			indexedDocs.put(name, "0");
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(
				HelpBaseResources.getString(
					"ES16",
					name,
					indexDir.getAbsolutePath()),
				e);
			return false;
		}
	}

	/**
	 * Starts additions.
	 * To be called before adding documents.
	 */
	public synchronized boolean beginAddBatch() {
		try {
			if (iw != null) {
				iw.close();
			}
			boolean create = false;
			if (!exists()) {
				create = true;
				indexDir.mkdirs();
				if (!indexDir.exists())
					return false; // unable to setup index directory
			}
			indexedDocs =
				new HelpProperties(
					INDEXED_DOCS_FILE,
					indexDir);
			indexedDocs.restore();
			setInconsistent(true);
			iw =
				new IndexWriter(
					indexDir,
					analyzerDescriptor.getAnalyzer(),
					create);
			iw.mergeFactor = 20;
			iw.maxFieldLength = 1000000;
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES17"), e);
			return false;
		}
	}
	/**
	 * Starts deletions.
	 * To be called before deleting documents.
	 */
	public synchronized boolean beginDeleteBatch() {
		try {
			if (ir != null) {
				ir.close();
			}
			indexedDocs =
				new HelpProperties(
					INDEXED_DOCS_FILE,
					indexDir);
			indexedDocs.restore();
			setInconsistent(true);
			ir = IndexReader.open(indexDir);
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES18"), e);
			return false;
		}
	}
	/**
	 * Deletes a single document from the index.
	 * @param name - document name
	 * @return true if success
	 */
	public boolean removeDocument(String name) {
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out.println("SearchIndex.removeDocument(" + name + ")");
		}
		Term term = new Term("name", name);
		try {
			ir.delete(term);
			indexedDocs.remove(name);
		} catch (IOException e) {
			HelpBasePlugin.logError(
				HelpBaseResources.getString(
					"ES22",
					name,
					indexDir.getAbsolutePath()),
				e);
			return false;
		}
		return true;
	}
	/**
	 * Finish additions.
	 * To be called after adding documents.
	 */
	public synchronized boolean endAddBatch() {
		try {
			if (iw == null)
				return false;
			iw.optimize();
			iw.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			indexedDocs.save();
			indexedDocs = null;
			getDocPlugins().save();
			saveDependencies();
			setInconsistent(false);
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES19"), e);
			return false;
		}
	}
	/**
	 * Finish deletions.
	 * To be called after deleting documents.
	 */
	public synchronized boolean endDeleteBatch() {
		try {
			if (ir == null)
				return false;
			ir.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			indexedDocs.save();
			indexedDocs = null;
			getDocPlugins().save();
			saveDependencies();
			setInconsistent(false);
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES20"), e);
			return false;
		}
	}
	/**
	 * Checks if index exists and is usable.
	 * @return true if index exists
	 */
	public boolean exists() {
		return indexDir.exists() && !isInconsistent();
		// assume index exists if directory does
	}
	/** 
	 * Performs a query search on this index 
	 * @param fieldNames - Collection of field names of type String (e.g. "h1");
	 *  the search will be performed on the given fields
	 * @param fieldSearch - boolean indicating if field only search
	 *  should be performed; if set to false, default field "contents"
	 *  and all other fields will be searched
	 * @param searchResult SearchResult that will contain all the hits
	 * @return - an array of document ids. 
	 * Later, we can extend this to return more data (rank, # of occs, etc.)
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector)
		throws QueryTooComplexException {
		try {
			if(closed)
				return;
			registerSearch(Thread.currentThread());
			if(closed)
				return;

			QueryBuilder queryBuilder =
				new QueryBuilder(
					searchQuery.getSearchWord(),
					analyzerDescriptor);
			Query luceneQuery =
				queryBuilder.getLuceneQuery(
					searchQuery.getFieldNames(),
					searchQuery.isFieldSearch());
			String highlightTerms = queryBuilder.gethighlightTerms();
			if (luceneQuery != null) {
				if (searcher == null) {
					openSearcher();
				}
				Hits hits = searcher.search(luceneQuery);
				collector.addHits(hits, highlightTerms);
			}
		} catch (QueryTooComplexException qe) {
			throw qe;
		} catch (Exception e) {
			HelpBasePlugin.logError(
				HelpBaseResources.getString(
					"ES21",
					searchQuery.getSearchWord()),
				e);
		} finally{
			unregisterSearch(Thread.currentThread());
		}
	}
	public String getLocale() {
		return locale;
	}
	/**
	 * Returns the list of all the plugins in this session
	 * that have declared a help contribution.
	 */
	public PluginVersionInfo getDocPlugins() {
		if (docPlugins == null) {
			Collection docPluginsIds =
				tocManager.getContributingPlugins();
			docPlugins =
				new PluginVersionInfo(
					INDEXED_CONTRIBUTION_INFO_FILE,
					docPluginsIds,
					indexDir,
					!exists());
		}
		return docPlugins;
	}
	/**
	 * We use HelpProperties, but a list would suffice.
	 * We only need the key values.
	 * @return HelpProperties, keys are URLs of indexed documents
	 */
	public HelpProperties getIndexedDocs() {
		HelpProperties indexedDocs =
			new HelpProperties(INDEXED_DOCS_FILE, indexDir);
		if (exists())
			indexedDocs.restore();
		return indexedDocs;
	}
	/**
	 * Gets properties with versions of Lucene plugin and Analyzer
	 * used for indexing
	 */
	private HelpProperties getDependencies() {
		if (dependencies == null) {
			dependencies =
				new HelpProperties(
					DEPENDENCIES_VERSION_FILENAME,
					indexDir);
			dependencies.restore();
		}
		return dependencies;
	}
	/**
	 * Gets analyzer identifier from a file.
	 */
	private String readAnalyzerId() {
		String analyzerVersion = getDependencies().getProperty("analyzer");
		if (analyzerVersion == null) {
			return "";
		}
		return analyzerVersion;
	}
	/**
	 * Gets Lucene plugin version from a file.
	 */
	private boolean isLuceneCompatible() {
		String usedLuceneVersion = getDependencies().getProperty("lucene");
		String currentLuceneVersion = "";
		Bundle lucenePluginDescriptor = Platform.getBundle(LUCENE_PLUGIN_ID);
		if (lucenePluginDescriptor != null) {
			currentLuceneVersion += (String) lucenePluginDescriptor
					.getHeaders().get(Constants.BUNDLE_VERSION);
		}
		// Later might add code to return true for other known cases
		// of compatibility between post 1.2.1 versions.
		return currentLuceneVersion.equals(usedLuceneVersion);
	}
	/**
	 * Saves Lucene version and analyzer identifier to a file.
	 */
	private void saveDependencies() {
		getDependencies().put("analyzer", analyzerDescriptor.getId());
		Bundle luceneBundle = Platform.getBundle(LUCENE_PLUGIN_ID);
		if (luceneBundle != null) {
			String luceneBundleVersion = ""
					+ luceneBundle.getHeaders().get(Constants.BUNDLE_VERSION);
			getDependencies().put("lucene", luceneBundleVersion);
		} else {
			getDependencies().put("lucene", "");
		}
		getDependencies().save();
	}
	/**
	 * @return Returns true if index has been left in inconsistent state If
	 *         analyzer has changed to incompatible one, index is treated as
	 *         inconsistent as well.
	 */
	public boolean isInconsistent() {
		if (inconsistencyFile.exists()) {
			return true;
		}
		return !isLuceneCompatible()
			|| !analyzerDescriptor.isCompatible(readAnalyzerId());
	}
	/**
	 * Writes or deletes inconsistency flag file
	 */
	public void setInconsistent(boolean inconsistent) {
		if (inconsistent) {
			try {
				// parent directory already created by beginAddBatch on new index
				FileOutputStream fos = new FileOutputStream(inconsistencyFile);
				fos.close();
			} catch (IOException ioe) {
			}
		} else
			inconsistencyFile.delete();
	}

	public void openSearcher() throws IOException {
		synchronized (searcherCreateLock) {
			if (searcher == null) {
				searcher = new IndexSearcher(indexDir.getAbsolutePath());
			}
		}
	}
	/**
	 * Closes IndexReader used by Searcher. Should be called on platform
	 * shutdown, or when TOCs have changed when no more reading from this index
	 * is to be performed.
	 */
	public void close() {
		closed = true;
		// wait for all sarches to finish
		synchronized (searches) {
			while (searches.size() > 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
				}
			}
			//
			if (searcher != null) {
				try {
					searcher.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
	/**
	 * Finds and unzips prebuild index specified in preferences
	 */
	private void unzipProductIndex() {
		String indexPluginId =
			HelpBasePlugin.getDefault().getPluginPreferences().getString(
				"productIndex");
		if (indexPluginId == null || indexPluginId.length() <= 0) {
			return;
		}
		InputStream zipIn =
			ResourceLocator.openFromPlugin(
				indexPluginId,
				"doc_index.zip",
				getLocale());
		if (zipIn == null) {
			return;
		}
		byte[] buf = new byte[8192];
		File destDir = indexDir;
		ZipInputStream zis = new ZipInputStream(zipIn);
		FileOutputStream fos = null;
		try {
			ZipEntry zEntry;
			while ((zEntry = zis.getNextEntry()) != null) {
				// if it is empty directory, create it
				if (zEntry.isDirectory()) {
					new File(destDir, zEntry.getName()).mkdirs();
					continue;
				}
				// if it is a file, extract it
				String filePath = zEntry.getName();
				int lastSeparator = filePath.lastIndexOf("/");
				String fileDir = "";
				if (lastSeparator >= 0) {
					fileDir = filePath.substring(0, lastSeparator);
				}
				//create directory for a file
				new File(destDir, fileDir).mkdirs();
				//write file
				File outFile = new File(destDir, filePath);
				fos = new FileOutputStream(outFile);
				int n = 0;
				while ((n = zis.read(buf)) >= 0) {
					fos.write(buf, 0, n);
				}
				fos.close();
			}
			if (HelpBasePlugin.DEBUG_SEARCH) {
				System.out.println(
					"SearchIndex: Prebuilt index restored to " + destDir + ".");
			}

		} catch (IOException ioe) {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe2) {
				}
			}
		} finally {
			try {
				zipIn.close();
				if (zis != null)
					zis.close();
			} catch (IOException ioe) {
			}
		}
	}
	/**
	 * Returns true when the index
	 * must be updated.
	 */
	public synchronized boolean needsUpdating() {
		if (!exists()) {
			return true;
		}
		return getDocPlugins().detectChange();
	}

	/**
	 * @return Returns the tocManager.
	 */
	public TocManager getTocManager() {
		return tocManager;
	}
	private void registerSearch(Thread t) {
		synchronized (searches) {
			searches.add(t);
		}
	}
	private void unregisterSearch(Thread t) {
		synchronized (searches) {
			searches.remove(t);
		}
	}
	/**
	 * @return Returns the closed.
	 */
	public boolean isClosed() {
		return closed;
	}
}
