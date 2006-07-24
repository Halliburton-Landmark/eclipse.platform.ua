/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.index;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests help keyword index functionality.
 */
public class AllIndexTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllIndexTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllIndexTests() {
		addTest(IgnoredTocsTest.suite());
	}
}
