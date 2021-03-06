/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.execution;

import org.eclipse.jface.action.Action;

/**
 * Class used to test calling an action which returns a failure status
 */

public class FailingAction extends Action {

	@Override
	public void run() {
		// Return a failure status
		notifyResult(false);
	}

}
