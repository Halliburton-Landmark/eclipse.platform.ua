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
package org.eclipse.help.internal.webapp.data;


import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.*;
import org.eclipse.help.internal.webapp.servlet.*;
import org.eclipse.help.internal.workingset.*;

/**
 * This class manages help working sets
 */
public class WorkingSetManagerData extends RequestData {
	private final static int NONE = 0;
	private final static int ADD = 1;
	private final static int REMOVE = 2;
	private final static int EDIT = 3;

	private String name;
	private WebappWorkingSetManager wsmgr;

	public WorkingSetManagerData(
		ServletContext context,
		HttpServletRequest request,
		HttpServletResponse response) {
		super(context, request);
		wsmgr = new WebappWorkingSetManager(request, response, getLocale());
		name = request.getParameter("workingSet");

		switch (getOperation()) {
			case ADD :
				addWorkingSet();
				break;
			case REMOVE :
				removeWorkingSet();
				break;
			case EDIT :
				editWorkingSet();
				break;
			default :
				break;
		}
	}

	public void addWorkingSet() {
		if (name != null && name.length() > 0) {

			String[] hrefs = request.getParameterValues("hrefs");
			if (hrefs == null)
				hrefs = new String[0];

			ArrayList selectedElements = new ArrayList(hrefs.length);
			for (int i = 0; i < hrefs.length; i++) {
				AdaptableHelpResource res = getAdaptableHelpResource(hrefs[i]);
				if (res != null)
					selectedElements.add(res);
			}

			AdaptableHelpResource[] elements =
				new AdaptableHelpResource[selectedElements.size()];
			selectedElements.toArray(elements);
			WorkingSet ws = wsmgr.createWorkingSet(name, elements);
			wsmgr.addWorkingSet(ws);
		}
	}

	public void removeWorkingSet() {
		if (name != null && name.length() > 0) {

			WorkingSet ws = wsmgr.getWorkingSet(name);
			if (ws != null)
				wsmgr.removeWorkingSet(ws);
		}
	}

	public void editWorkingSet() {
		if (name != null && name.length() > 0) {

			String oldName = request.getParameter("oldName");
			if (oldName == null || oldName.length() == 0)
				oldName = name;
			WorkingSet ws = wsmgr.getWorkingSet(oldName);
			if (ws != null) {
				String[] hrefs = request.getParameterValues("hrefs");
				if (hrefs == null)
					hrefs = new String[0];

				ArrayList selectedElements = new ArrayList(hrefs.length);
				for (int i = 0; i < hrefs.length; i++) {
					AdaptableHelpResource res =
						getAdaptableHelpResource(hrefs[i]);
					if (res != null)
						selectedElements.add(res);
				}

				AdaptableHelpResource[] elements =
					new AdaptableHelpResource[selectedElements.size()];
				selectedElements.toArray(elements);

				ws.setElements(elements);
				ws.setName(name);
				// should also change the name....

				// We send this notification, so that the manager fires to its listeners
				wsmgr.workingSetChanged(ws);
			}
		}
	}

	public String[] getWorkingSets() {
		WorkingSet[] workingSets = wsmgr.getWorkingSets();
		String[] sets = new String[workingSets.length];
		for (int i = 0; i < workingSets.length; i++)
			sets[i] = workingSets[i].getName();

		return sets;
	}

	public String getWorkingSetName() {
		if (name == null || name.length() == 0) {
			// See if anything is set in the preferences
			name =wsmgr.getCurrentWorkingSet();
			if (name == null
				|| name.length() == 0
				|| wsmgr.getWorkingSet(name) == null)
				name = ServletResources.getString("All", request);
		}
		return name;
	}

	public WorkingSet getWorkingSet() {
		if (name != null && name.length() > 0)
			return wsmgr.getWorkingSet(name);
		else
			return null;
	}

	public boolean isCurrentWorkingSet(int i) {
		WorkingSet[] workingSets = wsmgr.getWorkingSets();
		return workingSets[i].getName().equals(name);
	}

	private int getOperation() {
		String op = request.getParameter("operation");
		if ("add".equals(op))
			return ADD;
		else if ("remove".equals(op))
			return REMOVE;
		else if ("edit".equals(op))
			return EDIT;
		else
			return NONE;
	}

	private AdaptableHelpResource getAdaptableHelpResource(String internalId) {
		AdaptableHelpResource res = wsmgr.getAdaptableToc(internalId);
		if (res == null)
			res = wsmgr.getAdaptableTopic(internalId);
		return res;
	}
}
