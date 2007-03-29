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
package org.eclipse.ua.tests.intro.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.CustomizableIntroPart;

public class OpenIntroTest extends PerformanceTestCase {

	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(OpenIntroTest.class);
	}

	public void testOpenIntro() throws Exception {
		tagAsSummary("Open welcome", Dimension.ELAPSED_PROCESS);

		// warm-up
		for (int i=0;i<3;++i) {
			closeIntro();
			openIntro();
		}
		
		// run the tests
		for (int i=0;i<50;++i) {
			closeIntro();
			startMeasuring();
			openIntro();
			stopMeasuring();
		}
		
		commitMeasurements();
		assertPerformance();
	}
	
	public static void closeIntro() throws Exception {
		IIntroManager manager = PlatformUI.getWorkbench().getIntroManager();
		IIntroPart part = manager.getIntro();
		if (part != null) {
			manager.closeIntro(part);
		}
		ExtensionPointManager.getInst().clear();
		flush();
	}

	private static void openIntro() throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IIntroManager manager = workbench.getIntroManager();
		CustomizableIntroPart introPart = (CustomizableIntroPart)manager.showIntro(workbench.getActiveWorkbenchWindow(), false);

		/*
		 * We don't have the internal test hook .internal_isFinishedLoading()
		 * in 3.2 (only in 3.2.2), so I comment this out. This test isn't
		 * being included in the test suite.
		 */
		/*
		Display display = Display.getDefault();
		while (!introPart.internal_isFinishedLoading()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		*/
		flush();
	}
	
	private static void flush() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}
	}
}
