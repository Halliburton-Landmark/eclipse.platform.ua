/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search.federated;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A federated search job.
 */
public class FederatedSearchJob extends Job {
	public static final String FAMILY = "org.eclipse.help.base.searchEngine";
	private String expression;
	private FederatedSearchEntry entry;
	private ISearchEngineResultCollector collector;

	/**
	 * @param name
	 */
	public FederatedSearchJob(String expression, FederatedSearchEntry entry, ISearchEngineResultCollector collector) {
		super(entry.getEngineId());
		this.expression = expression;
		this.entry = entry;
		this.collector = collector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		try {
			entry.getEngine().run(expression, entry.getScope(), collector, monitor);
			return Status.OK_STATUS;
		}
		catch (CoreException e) {
			return e.getStatus();
		}
	}
	public boolean belongsTo(Object family) {
		return family == FAMILY;
	}
}