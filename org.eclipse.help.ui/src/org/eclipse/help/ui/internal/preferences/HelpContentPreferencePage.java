/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.base.remote.DefaultPreferenceFileHandler;
import org.eclipse.help.internal.base.remote.PreferenceFileHandler;
import org.eclipse.help.internal.base.remote.RemoteHelp;
import org.eclipse.help.internal.base.remote.RemoteIC;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Preference page to set remote infocenters
 */
public class HelpContentPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private InfocenterDisplay remoteICPage;

	private Button checkbox;
	private Label descLabel;

	/**
	 * Creates the preference page
	 */
	public HelpContentPreferencePage() {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IHelpUIConstants.PREF_PAGE_HELP_CONTENT);

		initializeDialogUnits(parent);

		createCheckbox(parent);
		
		descLabel = new Label(parent, SWT.NONE);
		descLabel.setText(Messages.HelpContentPage_title);
		Dialog.applyDialogFont(descLabel);
		
		remoteICPage = new InfocenterDisplay(this);
		remoteICPage.createContents(parent);
		

		initializeTableEnablement(checkbox.getSelection());
		
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();

		// Restore Defaults functionality here		
		HelpContentBlock currentBlock=remoteICPage.getHelpContentBlock();
		currentBlock.getRemoteICviewer().getRemoteICList().removeAllRemoteICs(currentBlock.getRemoteICList());
		currentBlock.getRemoteICviewer().getRemoteICList().loadDefaultPreferences();
		currentBlock.restoreDefaultButtons();
		checkbox.setSelection(new DefaultPreferenceFileHandler().isRemoteHelpOn());
		changeListener.handleEvent(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		HelpContentBlock currentBlock;
		RemoteIC[] currentRemoteICArray;
		
		InstanceScope instanceScope = new InstanceScope();
		IEclipsePreferences prefs = instanceScope.getNode(HelpBasePlugin.PLUGIN_ID);
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.PreferencePage#performOk()
		 */
		prefs.putBoolean(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, checkbox.getSelection());
	
		currentBlock=remoteICPage.getHelpContentBlock();
		currentRemoteICArray=currentBlock.getRemoteICList();
     	PreferenceFileHandler.commitRemoteICs(currentRemoteICArray);
		
    	RemoteHelp.notifyPreferenceChange();
		return super.performOk();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setButtonLayoutData(org.eclipse.swt.widgets.Button)
	 */
	protected GridData setButtonLayoutData(Button button) {
		return super.setButtonLayoutData(button);
	}

	private void createCheckbox(Composite parent) {
		checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(Messages.HelpContentPreferencePage_remote);
		checkbox.addListener(SWT.Selection, changeListener);

		boolean isOn = Platform.getPreferencesService().getBoolean
		    (HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, false, null);
		checkbox.setSelection(isOn);
		Dialog.applyDialogFont(checkbox);	
	}
	
	/*
	 * Initialize the table enablement with the current checkbox selection 
	 */
	
	private void initializeTableEnablement(boolean isRemoteHelpEnabled)
	{
		
		HelpContentBlock currentBlock=remoteICPage.getHelpContentBlock();
		
		if(isRemoteHelpEnabled)
			currentBlock.restoreDefaultButtons();
		else
			currentBlock.disableAllButtons();
			
		// Disable/Enable table
		currentBlock.getRemoteICviewer().getTable().setEnabled(isRemoteHelpEnabled);
	}

	/*
	 * Listens for any change in the UI and checks for valid input and correct
	 * enablement.
	 */
	private Listener changeListener = new Listener() {
		public void handleEvent(Event event) {

			HelpContentBlock currentBlock=remoteICPage.getHelpContentBlock();			
			
			boolean isRemoteHelpEnabled=checkbox.getSelection();
			//  Disable/Enable buttons
			if(isRemoteHelpEnabled)
				currentBlock.restoreDefaultButtons();
			else
				currentBlock.disableAllButtons();
				
			// Disable/Enable table
			currentBlock.getRemoteICviewer().getTable().setEnabled(isRemoteHelpEnabled);
			
			
			
		}
	};

}
