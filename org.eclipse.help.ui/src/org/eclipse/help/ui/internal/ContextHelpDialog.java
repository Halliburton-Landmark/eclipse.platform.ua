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

package org.eclipse.help.ui.internal;

import org.eclipse.help.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;

/**
 * ContextHelpDialog
 */
public class ContextHelpDialog {
	private static ImageRegistry imgRegistry = null;
	private Color backgroundColour = null;
	private IContext context;
	private Color foregroundColour = null;
	private Color linkColour = null;
	private static HyperlinkHandler linkManager = new HyperlinkHandler();
	private Shell shell;
	private String infopopText;

	/**
	 * Listener for hyperlink selection.
	 */
	class LinkListener extends HyperlinkAdapter {
		IHelpResource topic;
		public LinkListener(IHelpResource topic) {
			this.topic = topic;
		}
		public void linkActivated(Control c) {
			launchLinks(topic);
		}

	}

	/**
	 * Constructor:
	 * @param context an array of String or an array of IContext
	 * @param x the x mouse location in the current display
	 * @param y the y mouse location in the current display
	 */
	ContextHelpDialog(IContext context, int x, int y) {
		this.context = context;
		Display display = Display.getCurrent();
		backgroundColour = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		foregroundColour = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		linkColour = display.getSystemColor(SWT.COLOR_BLUE);
		shell = new Shell(display.getActiveShell(), SWT.NONE);
		if (HelpUIPlugin.DEBUG_INFOPOP) {
			System.out.println(
				"ContextHelpDialog.ContextHelpDialog(): Shell is:"
					+ shell.toString());
		}
		WorkbenchHelp.setHelp(shell, IHelpUIConstants.F1_SHELL);

		shell.addListener(SWT.Deactivate, new Listener() {
			public void handleEvent(Event e) {
				if (HelpUIPlugin.DEBUG_INFOPOP) {
					System.out.println(
						"ContextHelpDialog shell deactivate listener: SWT.Deactivate called. ");
				}
				close();
			};
		});

		shell.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					if (HelpUIPlugin.DEBUG_INFOPOP) {
						System.out.println(
							"ContextHelpDialog: shell traverse listener: SWT.TRAVERSE_ESCAPE called. ");
					}
					e.doit = true;
				}
			}
		});

		shell.addControlListener(new ControlAdapter() {
			public void controlMoved(ControlEvent e) {
				if (HelpUIPlugin.DEBUG_INFOPOP) {
					System.out.println(
						"ContextHelpDialog: shell control adapter called.");
				}
				Rectangle clientArea = shell.getClientArea();
				shell.redraw(
					clientArea.x,
					clientArea.y,
					clientArea.width,
					clientArea.height,
					true);
				shell.update();
			}
		});
		if (HelpUIPlugin.DEBUG_INFOPOP) {
			System.out.println(
				"ContextHelpDialog.ContextHelpDialog(): Focus owner is: "
					+ Display.getCurrent().getFocusControl().toString());
		}
		linkManager.setHyperlinkUnderlineMode(
			HyperlinkHandler.UNDERLINE_ALWAYS);
		createContents(shell);
		shell.pack();
		// Correct x and y of the shell if it not contained within the screen
		int width = shell.getBounds().width;
		int height = shell.getBounds().height;
		// check lower boundaries
		x = x >= 0 ? x : 0;
		y = y >= 0 ? y : 0;
		// check upper boundaries
		Rectangle screen = display.getClientArea();
		x = x + width <= screen.width ? x : screen.width - width;
		y = y + height <= screen.height ? y : screen.height - height;
		shell.setLocation(x, y);

		initAccessible(shell);
	}
	public synchronized void close() {
		try {
			if (HelpUIPlugin.DEBUG_INFOPOP) {
				System.out.println("ContextHelpDialog.close()");
			}
			if (shell != null) {
				shell.close();
				if (!shell.isDisposed())
					shell.dispose();
				shell = null;
			}
		} catch (Throwable ex) {
		}
	}
	protected Control createContents(Composite contents) {
		initAccessible(contents);
		contents.setBackground(backgroundColour);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		contents.setLayout(layout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		// create the dialog area and button bar
		createInfoArea(contents);
		Control c=createLinksArea(contents);
		if(c!=null){
			// links exist, make them the only focusable controls
			contents.setTabList(new Control[] {c});
		}
		return contents;
	}
	private Control createInfoArea(Composite parent) {
		// Create the text field.    
		String styledText = context.getText();
		if (styledText == null) // no description found in context objects.
			styledText = HelpUIResources.getString("WW002");
		Description text = new Description(parent, SWT.MULTI | SWT.READ_ONLY);
		text.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					if (HelpUIPlugin.DEBUG_INFOPOP) {
						System.out.println(
							"ContextHelpDialog text TraverseListener.handleEvent(): SWT.TRAVERSE_ESCAPE.");
					}
					e.doit = true;
				}
			}
		});

		text.getCaret().setVisible(false);
		text.setBackground(backgroundColour);
		text.setForeground(foregroundColour);
		text.setFont(parent.getFont());
		StyledLineWrapper content = new StyledLineWrapper(styledText);
		text.setContent(content);
		text.setStyleRanges(content.getStyles());

		infopopText = text.getText();
		initAccessible(text);

		return text;
	}
	private Control createLink(Composite parent, IHelpResource topic) {
		Label image = new Label(parent, SWT.NONE);
		image.setImage(getImage());
		image.setBackground(backgroundColour);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		//data.horizontalIndent = 4;
		image.setLayoutData(data);
		HyperlinkLabel link = new HyperlinkLabel(parent, SWT.NONE);
		link.setText(topic.getLabel());
		link.setBackground(backgroundColour);
		link.setForeground(linkColour);
		link.setFont(parent.getFont());
		linkManager.registerHyperlink(link, new LinkListener(topic));
		return link;
	}
	private Control createLinksArea(Composite parent) {
		IHelpResource[] relatedTopics = context.getRelatedTopics();
		if (relatedTopics == null)
			return null;
		// Create control
		Composite composite = new Composite(parent, SWT.NONE);
		initAccessible(composite);

		composite.setBackground(backgroundColour);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 0;
		layout.verticalSpacing = 3;
		layout.horizontalSpacing = 2;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
		GridData data =
			new GridData(
				GridData.FILL_BOTH
					| GridData.HORIZONTAL_ALIGN_BEGINNING
					| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		// Create separator.    
		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBackground(backgroundColour);
		label.setForeground(foregroundColour);
		data =
			new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING
					| GridData.VERTICAL_ALIGN_BEGINNING
					| GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		// Create related links
		for (int i = 0; i < relatedTopics.length; i++) {
			createLink(composite, relatedTopics[i]);
		}
		return composite;
	}
	/**
	 * Called when related link has been chosen
	 * Opens help viewer with list of all related topics
	 */
	private void launchLinks(IHelpResource selectedTopic) {
		close();
		if (HelpUIPlugin.DEBUG_INFOPOP) {
			System.out.println("ContextHelpDialog.launchLinks(): closed shell");
		}
		BaseHelpSystem.getHelpDisplay().displayHelp(context, selectedTopic);
	}
	public synchronized void open() {
		try {
			shell.open();
			if (HelpUIPlugin.DEBUG_INFOPOP) {
				System.out.println(
					"ContextHelpDialog.open(): Focus owner after open is: "
						+ Display.getCurrent().getFocusControl().toString());
			}
		} catch (Throwable e) {
			HelpUIPlugin.logError(
				HelpUIResources.getString("ContextHelpDialog.open"),
				e);
		}
	}
	private Image getImage() {
		if (imgRegistry == null) {
			imgRegistry = HelpUIPlugin.getDefault().getImageRegistry();
			imgRegistry.put(
				IHelpUIConstants.IMAGE_KEY_F1TOPIC,
				ImageDescriptor.createFromURL(
					HelpUIResources.getImagePath(
						IHelpUIConstants.IMAGE_FILE_F1TOPIC)));
		}
		return imgRegistry.get(IHelpUIConstants.IMAGE_KEY_F1TOPIC);
	}
	public boolean isShowing() {
		return (shell != null && !shell.isDisposed() && shell.isVisible());
	}

	private void initAccessible(final Control control) {
		Accessible accessible = control.getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = infopopText;
			}

			public void getHelp(AccessibleEvent e) {
				e.result = control.getToolTipText();
			}
		});

		accessible
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = control.toControl(new Point(e.x, e.y));
				e.childID =
					(control.getBounds().contains(pt))
						? ACC.CHILDID_MULTIPLE
						: ACC.CHILDID_NONE;
			}

			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = control.getBounds();
				Point pt = control.toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 1;
			}

			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_LABEL;
			}

			public void getState(AccessibleControlEvent e) {
				e.detail = ACC.STATE_READONLY;
			}
		});
	}

	public class Description extends StyledText {
		/**
		 * @param parent
		 * @param style
		 */
		public Description(Composite parent, int style) {
			super(parent, style);
		}
		public boolean setFocus() {
			return false;
		}
		public boolean isFocusControl() {
			return false;
		}
	}

}
