/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.osgi.framework.Bundle;

public class CoreItem extends ViewItem {
	protected boolean buttonsHandled = false;

	protected Image completeImage;
	protected Image restartImage;
	protected Image skipImage;
	protected Image startImage;

	private ArrayList listOfSubItemCompositeHolders;

	/**
	 * Constructor for CoreItem.
	 * @param parent
	 * @param contentItem
	 */
	public CoreItem(FormToolkit toolkit, ScrolledForm form, Item item, Color itemColor, CheatSheetViewer viewer) {
		super(toolkit, form, item, itemColor, viewer);
	}

	private void createButtonComposite() {
		buttonComposite = toolkit.createComposite(bodyWrapperComposite);
		GridLayout buttonlayout = new GridLayout(4, false);
		buttonlayout.marginHeight = 2;
		buttonlayout.marginWidth = 2;
		buttonlayout.verticalSpacing = 2;

		TableWrapData buttonData = new TableWrapData(TableWrapData.FILL);

		buttonComposite.setLayout(buttonlayout);
		buttonComposite.setLayoutData(buttonData);
		buttonComposite.setBackground(itemColor);

		Label spacer = toolkit.createLabel(buttonComposite, null);
		spacer.setBackground(itemColor);
		GridData spacerData = new GridData();
		spacerData.widthHint = 16;
		spacer.setLayoutData(spacerData);
	}

	private void createButtons(Action action) {
		if (action != null ) {
			final ImageHyperlink startButton = createButton(buttonComposite, startImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
			toolkit.adapt(startButton, true, true);
			startButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					viewer.runPerformAction(startButton);
				}
			});
		}
		if (item.isSkip()) {
			final ImageHyperlink skipButton = createButton(buttonComposite, skipImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.SKIP_TASK_TOOLTIP));
			toolkit.adapt(skipButton, true, true);
			skipButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					viewer.advanceItem(skipButton, false);
				}
			});
		}
		if (action == null || action.isConfirm()) {
			final ImageHyperlink completeButton = createButton(buttonComposite, completeImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.COMPLETE_TASK_TOOLTIP));
			toolkit.adapt(completeButton, true, true);
			completeButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					viewer.advanceItem(completeButton, true);
				}
			});
		}
	}

	private void createSubItemButtonComposite() {
		buttonComposite = toolkit.createComposite(bodyWrapperComposite);
		GridLayout xbuttonlayout = new GridLayout(6, false);
		xbuttonlayout.marginHeight = 2;
		xbuttonlayout.marginWidth = 2;
		xbuttonlayout.verticalSpacing = 2;

		TableWrapData xbuttonData = new TableWrapData(TableWrapData.FILL);

		buttonComposite.setLayout(xbuttonlayout);
		buttonComposite.setLayoutData(xbuttonData);
		buttonComposite.setBackground(itemColor);
	}

	private void createSubItemButtons(SubItem sub, String thisValue, int index) {
		int added = 0;
		
		//Spacer label added.
		Label checkDoneLabel = toolkit.createLabel(buttonComposite, null);
		checkDoneLabel.setBackground(itemColor);
		GridData checkDoneData = new GridData();
		checkDoneData.widthHint = 16;
		checkDoneLabel.setLayoutData(checkDoneData);
		added++;

		//Now add the label.
		String labelText = null;
		if( thisValue != null ) {
			labelText = performLineSubstitution(sub.getLabel(), "${this}", thisValue); //$NON-NLS-1$
		} else {
			labelText = sub.getLabel();
		}
		Label label = toolkit.createLabel(buttonComposite, labelText);
		label.setBackground(itemColor);
		added++;

		Action subAction = null;
		if(sub.getPerformWhen() != null) {
			sub.getPerformWhen().setSelectedAction(viewer.getManager());
			subAction = sub.getPerformWhen().getSelectedAction();
		} else {
			subAction = sub.getAction();
		};
		
		final int fi = index;
		ImageHyperlink startButton = null;
		if (subAction != null) {
			added++;
			startButton = createButton(buttonComposite, startImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
			final ImageHyperlink finalStartButton = startButton;
			toolkit.adapt(startButton, true, true);
			startButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					viewer.runSubItemPerformAction(finalStartButton, fi);
				}
			});
		}
		if (sub.isSkip()) {
			added++;
			final ImageHyperlink skipButton = createButton(buttonComposite, skipImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.SKIP_TASK_TOOLTIP));
			toolkit.adapt(skipButton, true, true);
			skipButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					viewer.advanceSubItem(skipButton, false, fi);
				}
			});
		}
		if (subAction == null || subAction.isConfirm()) {
			added++;
			final ImageHyperlink completeButton = createButton(buttonComposite, completeImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.COMPLETE_TASK_TOOLTIP));
			toolkit.adapt(completeButton, true, true);
			completeButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					viewer.advanceSubItem(completeButton, true, fi);
				}
			});
		}

		while (added < 6) {
			// Add filler labels as needed to complete the row
			Label filler = toolkit.createLabel(buttonComposite, null);
			filler.setBackground(itemColor);
			added++;
		}
		listOfSubItemCompositeHolders.add(new SubItemCompositeHolder(checkDoneLabel, startButton, thisValue, sub));
	}

	/**
	 * @see org.eclipse.ui.internal.cheatsheets.views.ViewItem#disposeOfStuff()
	 */
	public void dispose() {
		super.dispose();

		if (startImage != null)
			startImage.dispose();
		if (skipImage != null)
			skipImage.dispose();
		if (completeImage != null)
			completeImage.dispose();
		if (restartImage != null)
			restartImage.dispose();
	}

	public ArrayList getListOfSubItemCompositeHolders() {
		return listOfSubItemCompositeHolders;
	}

	private ImageHyperlink getStartButton() {
		if(buttonComposite != null) {
			Control[] controls = buttonComposite.getChildren();
			for (int i = 0; i < controls.length; i++) {
				Control control = controls[i];
				if(control instanceof ImageHyperlink) {
					String toolTipText = control.getToolTipText();
					if( toolTipText != null &&
						(toolTipText.equals(CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP)) ||
						 toolTipText.equals(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTART_TASK_TOOLTIP)))) {
						return (ImageHyperlink)control;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.ui.internal.cheatsheets.ViewItem#handleButtons()
	 */
	/*package*/ void handleButtons() {
		if(item.isDynamic()) {
			handleDynamicButtons();
			return;
		} else if( item.getSubItems() != null && item.getSubItems().size() > 0) {
			try{
				handleSubButtons();
			}catch(Exception e){
				//Need to log exception here. 
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.LESS_THAN_2_SUBITEMS), e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
				org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.LESS_THAN_2_SUBITEMS), null, status);
			}
		}

		if (buttonsHandled)
			return;

		createButtonComposite();
		createButtons(item.getAction());
		buttonsHandled = true;
	}

	private void handleDynamicButtons() {
		if( item.getSubItems() != null && item.getSubItems().size() > 0 ) {
			handleDynamicSubItemButtons();
		} else if( item.getPerformWhen() != null ) {
			handlePerformWhenButtons();
		}
	}

	private void handleDynamicSubItemButtons() {
		boolean refreshRequired = false;
		if(buttonComposite != null) {
			Control[] children = buttonComposite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				control.dispose();
			}
			
			refreshRequired = true;
		} else {
			createSubItemButtonComposite();
		}

		//Instantiate the list to store the sub item composites.
		listOfSubItemCompositeHolders = new ArrayList(20);

		//loop throught the number of sub items, make a new composite for each sub item.
		//Add the spacer, the label, then the buttons that are applicable for each sub item.
		int i=0;
		for (Iterator iter = item.getSubItems().iterator(); iter.hasNext(); i++) {
			AbstractSubItem subItem = (AbstractSubItem)iter.next();
			if( subItem instanceof RepeatedSubItem ) {

				//Get the sub item to add.
				RepeatedSubItem repeatedSubItem = (RepeatedSubItem)subItem;
				String values = repeatedSubItem.getValues();
				values = viewer.getManager().getVariableData(values);
				if(values == null || values.length() <= 0) {
					//TODO: throw exception, show warning to user
				}

				SubItem sub = (SubItem)repeatedSubItem.getSubItems().get(0);
				
				StringTokenizer tokenizer = new StringTokenizer(values, ","); //$NON-NLS-1$
				while(tokenizer.hasMoreTokens()) {
					String value = tokenizer.nextToken();
					createSubItemButtons(sub, value, i++);
				}
				
				// Decrement the counter by because the outer loop increments it prior to the next iteration
				i--;
			} else if( subItem instanceof ConditionalSubItem ) {
				//Get the sub item to add.
				ConditionalSubItem sub = (ConditionalSubItem)subItem;

				sub.setSelectedSubItem(viewer.getManager());
				SubItem selectedSubItem = sub.getSelectedSubItem();

				createSubItemButtons(selectedSubItem, null, i);
			} else if( subItem instanceof SubItem ) {
				createSubItemButtons((SubItem)subItem, null, i);
			}
		}

		if(refreshRequired) {
			buttonComposite.layout();
		}
	}

	private void handlePerformWhenButtons() {
		boolean refreshRequired = false;

		if(buttonComposite != null) {
			Control[] controls = buttonComposite.getChildren();
			for (int i = 0; i < controls.length; i++) {
				Control control = controls[i];
				if(control instanceof ImageHyperlink) {
					control.dispose();
				}
			}
			
			refreshRequired = true;
		} else {
			createButtonComposite();
		}

		item.getPerformWhen().setSelectedAction(viewer.getManager());
		Action performAction = item.getPerformWhen().getSelectedAction();

		createButtons(performAction);
		
		if(refreshRequired) {
			buttonComposite.layout();
		}
	}

	private void handleSubButtons() throws Exception {
		if (buttonsHandled)
			return;
		//Instantiate the list to store the sub item composites.
		listOfSubItemCompositeHolders = new ArrayList(20);

		ArrayList sublist = item.getSubItems();
		
		if(sublist == null || sublist.size()<=1)
			throw new Exception(ICheatSheetResource.LESS_THAN_2_SUBITEMS);
		
		createSubItemButtonComposite();

		//loop throught the number of sub items, make a new composite for each sub item.
		//Add the spacer, the label, then the buttons that are applicable for each sub item.
		for (int i = 0; i < sublist.size(); i++) {
			createSubItemButtons((SubItem)sublist.get(i), null, i);
		}
		buttonsHandled = true;
	}

	protected void init() {
		super.init();

		String imageFileName = "icons/full/clcl16/start_task.gif"; //$NON-NLS-1$
		URL imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		startImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/skip_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		skipImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/complete_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		completeImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/restart_task.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		restartImage = imageDescriptor.createImage();
	}

	public String performLineSubstitution(String line, String variable, String value) {
		StringBuffer buffer = new StringBuffer(line.length());

		StringDelimitedTokenizer tokenizer = new StringDelimitedTokenizer(line, variable);
		boolean addValue = false;

		while (tokenizer.hasMoreTokens()) {
			if (addValue) {
				buffer.append(value);
			}
			buffer.append(tokenizer.nextToken());
			addValue = true;
		}
		if (tokenizer.endsWithDelimiter()) {
			buffer.append(value);
		}

		return buffer.toString();
	}
	/*package*/
	byte runAction(CheatSheetManager csm) {
		Action action = item.getAction();
		if(action == null) {
			if(item.getPerformWhen() != null){
				action = item.getPerformWhen().getSelectedAction();
			}
		}

		if(action != null) {
			return runAction(action.getPluginID(), action.getActionClass(), action.getParams(), csm);
		}

		return VIEWITEM_ADVANCE;
	}

	/**
	 * Run an action
	 */
	/*package*/
	byte runAction(String pluginId, String className, String[] params, CheatSheetManager csm) {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_FINDING_PLUGIN_FOR_ACTION), null);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_FINDING_PLUGIN_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}
		Class actionClass;
		IAction action;
		try {
			actionClass = bundle.loadClass(className);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}
		try {
			action = (IAction) actionClass.newInstance();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}

		final boolean[] listenerFired = { false };
		final boolean[] listenerResult = { false };
		IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if(event.getNewValue() instanceof Boolean) {
					listenerFired[0] = true;
					listenerResult[0] = ((Boolean)event.getNewValue()).booleanValue();
				}
			}
		};

		// Add PropertyChangeListener to the action, so we can detemine if a action was succesfull
		action.addPropertyChangeListener(propertyChangeListener);

		// Run the action for this ViewItem
		if (action instanceof ICheatSheetAction) {
			// Prepare parameters
			String[] clonedParams = null;
			if(params != null && params.length > 0) {
				clonedParams = new String[params.length];
				System.arraycopy(params, 0, clonedParams, 0, params.length);
				for (int i = 0; i < clonedParams.length; i++) {
					String param = clonedParams[i];
					if(param != null && param.startsWith("${") && param.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
						param = param.substring(2,param.length()-1);
						String value = csm.getData(param);
						clonedParams[i] = value == null ? "" : value; //$NON-NLS-1$
					}
				}
			}			
			((ICheatSheetAction) action).run(clonedParams, csm);
		} else
			action.run();

		// Remove the PropertyChangeListener
		action.removePropertyChangeListener(propertyChangeListener);

		if (listenerFired[0]) {
			if (listenerResult[0]) {
				return VIEWITEM_ADVANCE;
			} else {
				return VIEWITEM_DONOT_ADVANCE;
			}
		}

		return VIEWITEM_ADVANCE;
	}

	/*package*/
	byte runSubItemAction(CheatSheetManager csm, int index) {
		if (item.getSubItems() != null && item.getSubItems().size()>0 && listOfSubItemCompositeHolders != null) {
			SubItemCompositeHolder s = (SubItemCompositeHolder) listOfSubItemCompositeHolders.get(index);
			if(s != null) {
				SubItem subItem = s.getSubItem();
				Action action = subItem.getAction();
				if(action == null) {
					if(subItem.getPerformWhen() != null){
						action = subItem.getPerformWhen().getSelectedAction();
					}
				}

				if(action != null) {
					try {
						if(s.getThisValue() != null) {
							csm.setData("this", s.getThisValue()); //$NON-NLS-1$
						}
						String[] params = action.getParams();
						return runAction(action.getPluginID(), action.getActionClass(), params, csm);
					} finally {
						if(s.getThisValue() != null) {
							csm.setData("this", null); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return VIEWITEM_ADVANCE;
	}

	/*package*/void setButtonsHandled(boolean handled){
		buttonsHandled = handled;
	}
	
	/*package*/ void setIncomplete() {
		super.setIncomplete();
			
		//check for sub items and reset their icons.
		ArrayList l = getListOfSubItemCompositeHolders();
		if(l != null){
			for(int j=0; j<l.size(); j++){
				SubItemCompositeHolder s = (SubItemCompositeHolder)l.get(j);
				if(s.isCompleted() || s.isSkipped())
					s.getIconLabel().setImage(null);
				if(s.startButton != null)
					s.getStartButton().setImage(startImage);	
					
			}					
		}	
	}

	/*package*/ void setRestartImage() {
		ImageHyperlink startButton = getStartButton();
		if (startButton != null) {
			startButton.setImage(restartImage);
			startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTART_TASK_TOOLTIP));
		}
	}

	/*package*/ void setStartImage() {
		ImageHyperlink startButton = getStartButton();
		if (startButton != null) {
			startButton.setImage(startImage);
			startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
		}
	}
}
