/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.swt;


import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

/**
 * Factory to create all UI forms widgets for the Forms intro presentation.
 */
public class PageWidgetFactory {

    protected HyperlinkAdapter hyperlinkAdapter = new HyperlinkAdapter() {

        public void linkActivated(HyperlinkEvent e) {
            String url = (String) e.getHref();
            IntroURLParser parser = new IntroURLParser(url);
            if (parser.hasIntroUrl()) {
                // execute the action embedded in the IntroURL
                parser.getIntroURL().execute();
                return;
            } else if (parser.hasProtocol()) {
                Util.openBrowser(url);
                return;
            }
            DialogUtil.displayInfoMessage(((Control) e.getSource()).getShell(),
                    IntroPlugin.getString("HyperlinkAdapter.urlIs") //$NON-NLS-1$
                            + " " + url); //$NON-NLS-1$
        }

        public void linkEntered(HyperlinkEvent e) {
        }

        public void linkExited(HyperlinkEvent e) {
        }
    };


    protected FormToolkit toolkit;
    protected PageStyleManager styleManager;


    /*
     * protect bad creation.
     */
    protected PageWidgetFactory(FormToolkit toolkit,
            PageStyleManager styleManager) {
        this.toolkit = toolkit;
        this.styleManager = styleManager;
    }


    public void createIntroElement(Composite parent,
            AbstractIntroElement element) {
        // check if this element is filtered, and if yes, do not create it.
        boolean isFiltered = getFilterState(element);
        if (isFiltered)
            return;

        Control c = null;
        switch (element.getType()) {
        case AbstractIntroElement.GROUP:
            IntroGroup group = (IntroGroup) element;
            c = createGroup(parent, group);
            updateLayoutData(c, element);
            // c must be a composite.
            Composite newParent = (Composite) c;
            if (c instanceof Section)
                // client is a composite also.
                newParent = (Composite) ((Section) newParent).getClient();
            AbstractIntroElement[] children = group.getChildren();
            for (int i = 0; i < children.length; i++)
                createIntroElement(newParent, children[i]);
            break;
        case AbstractIntroElement.LINK:
            IntroLink link = (IntroLink) element;
            c = createImageHyperlink(parent, link);
            updateLayoutData(c, element);
            break;
        case AbstractIntroElement.TEXT:
            IntroText text = (IntroText) element;
            c = createText(parent, text);
            updateLayoutData(c, element);
            break;
        case AbstractIntroElement.IMAGE:
            IntroImage image = (IntroImage) element;
            c = createImage(parent, image);
            updateLayoutData(c, element);
            break;
        case AbstractIntroElement.HTML:
            IntroHTML html = (IntroHTML) element;
            if (html.isInlined()) {
                IntroText htmlText = html.getIntroText();
                if (htmlText != null)
                    c = createText(parent, htmlText);
                else {
                    IntroImage htmlImage = html.getIntroImage();
                    if (htmlImage != null)
                        c = createImage(parent, htmlImage);
                }
            } else {
                // embedded HTML, so we can show it from a link.
                String embddedLink = html.getSrc();
                if (embddedLink == null)
                    break;
                String linkText = StringUtil
                        .concat(
                                "<p><a href=\"http://org.eclipse.ui.intro/openBrowser?url=",
                                embddedLink, "\">",
                                IntroPlugin.getString("HTML.embeddedLink"),
                                "</a></p>").toString();
                linkText = generateFormText(linkText);
                c = createFormText(parent, linkText, null);
            }
            if (c != null)
                updateLayoutData(c, element);
            break;
        default:
            break;
        }
    }


    private void updateLayoutData(Control c, AbstractIntroElement element) {
        TableWrapData currentTd = (TableWrapData) c.getLayoutData();
        if (currentTd == null) {
            currentTd = new TableWrapData(TableWrapData.FILL,
                    TableWrapData.FILL);
            currentTd.grabHorizontal = true;
            c.setLayoutData(currentTd);
        }

        currentTd.colspan = styleManager
                .getColSpan((AbstractBaseIntroElement) element);
        currentTd.rowspan = styleManager
                .getRowSpan((AbstractBaseIntroElement) element);

    }

    private Composite createGroup(Composite parent, IntroGroup group) {
        String label = group.getLabel();
        String description = styleManager.getDescription(group);
        int numColumns = styleManager.getNumberOfColumns(group);
        numColumns = numColumns < 1 ? 1 : numColumns;
        int vspacing = styleManager.getVerticalLinkSpacing();
        Composite client = null;
        Composite control = null;
        if (description != null || label != null) {
            int style = description != null ? Section.DESCRIPTION : SWT.NULL;
            Section section = toolkit.createSection(parent, style);
            if (label != null)
                section.setText(label);
            if (description != null)
                section.setDescription(description);
            colorControl(section, group);
            client = toolkit.createComposite(section, SWT.WRAP);
            section.setClient(client);
            control = section;
        } else {
            client = toolkit.createComposite(parent, SWT.WRAP);
            control = client;
        }

        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = numColumns;
        layout.verticalSpacing = vspacing;
        client.setLayout(layout);
        //Util.highlight(client, SWT.COLOR_YELLOW);
        return control;
    }

    /**
     * Creates an Image Hyperlink from an IntroLink. Model object is NOT cached.
     * 
     * @param body
     * @param link
     */
    private Control createImageHyperlink(Composite parent, IntroLink link) {
        Control control;
        Hyperlink linkControl;
        boolean showLinkDescription = styleManager.getShowLinkDescription();
        Image linkImage = styleManager.getImage(link, "link-icon", //$NON-NLS-1$
                ImageUtil.DEFAULT_LINK);
        if (showLinkDescription && link.getText() != null) {
            Composite container = toolkit.createComposite(parent);
            TableWrapLayout layout = new TableWrapLayout();
            layout.leftMargin = layout.rightMargin = 0;
            layout.topMargin = layout.bottomMargin = 0;
            layout.verticalSpacing = 0;
            layout.numColumns = 2;
            container.setLayout(layout);

            Label ilabel = toolkit.createLabel(container, null);
            ilabel.setImage(linkImage);
            TableWrapData td = new TableWrapData();
            td.valign = TableWrapData.TOP;
            td.rowspan = 2;
            ilabel.setLayoutData(td);

            linkControl = toolkit.createHyperlink(container, null, SWT.WRAP);
            td = new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM);
            td.grabVertical = true;
            linkControl.setLayoutData(td);
            //Util.highlight(linkControl, SWT.COLOR_RED);
            //Util.highlight(container, SWT.COLOR_DARK_YELLOW);

            Control desc = createText(container, link.getIntroText());
            td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
            td.grabHorizontal = true;
            td.grabVertical = true;
            desc.setLayoutData(td);
            control = container;
        } else {
            ImageHyperlink imageLink = toolkit.createImageHyperlink(parent,
                    SWT.WRAP);
            imageLink.setImage(linkImage);
            TableWrapData td = new TableWrapData();
            td.grabHorizontal = true;
            imageLink.setLayoutData(td);
            linkControl = imageLink;
            control = linkControl;
        }
        linkControl.setText(link.getLabel());
        linkControl.setFont(PageStyleManager.getDefaultFont());
        colorControl(linkControl, link);
        linkControl.setHref(link.getUrl());
        linkControl.addHyperlinkListener(hyperlinkAdapter);
        //Util.highlight(linkControl, SWT.COLOR_DARK_YELLOW);
        return control;
    }

    /**
     * Creates a forms Text or FormattedText.
     * 
     * @param body
     * @param link
     */
    protected Control createText(Composite parent, IntroText text) {
        Color fg = styleManager.getColor(toolkit, text);
        boolean isBold = styleManager.isBold(text);
        // formatted case. If text is alredy formatted, the bold property is
        // ignored.
        if (text.isFormatted())
            return createFormText(parent, generateFormText(text.getText()), fg);

        // non formatted case.
        if (isBold)
            return createFormText(parent, generateBoldFormText(text.getText()),
                    fg);
        else
            return createText(parent, text.getText(), fg);

    }

    private Control createFormText(Composite parent, String text, Color fg) {
        FormText formText = toolkit.createFormText(parent, true);
        formText.addHyperlinkListener(hyperlinkAdapter);
        formText.setText(text, true, true);
        if (fg != null)
            formText.setForeground(fg);
        return formText;
    }


    private Control createText(Composite parent, String text, Color fg) {
        Label label = toolkit.createLabel(parent, text, SWT.WRAP);
        if (fg != null)
            label.setForeground(fg);
        return label;
    }



    protected Control createImage(Composite parent, IntroImage image) {
        Label ilabel = null;
        Image imageFile = styleManager.getImage(image);
        if (imageFile != null) {
            ilabel = toolkit.createLabel(parent, null, SWT.LEFT);
            ilabel.setImage(imageFile);
            if (image.getAlt() != null)
                ilabel.setToolTipText(image.getAlt());
        }
        // for images, do not use default layout. Grab horizontal is not what we
        // want.
        TableWrapData td = new TableWrapData();
        ilabel.setLayoutData(td);
        return ilabel;
    }


    private void colorControl(Control elementControl,
            AbstractBaseIntroElement element) {
        Color fg = styleManager.getColor(toolkit, element);
        if (fg != null)
            elementControl.setForeground(fg);
    }


    private String generateFormText(String text) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<form>"); //$NON-NLS-1$
        sbuf.append(text);
        sbuf.append("</form>"); //$NON-NLS-1$
        return sbuf.toString();
    }

    private String generateBoldFormText(String text) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<form>"); //$NON-NLS-1$
        sbuf.append("<p>");
        sbuf.append("<b>");
        sbuf.append(text);
        sbuf.append("</b>");
        sbuf.append("</p>");
        sbuf.append("</form>"); //$NON-NLS-1$
        return sbuf.toString();
    }

    /**
     * Check the filter state of the element. Only base elements have the filter
     * attribute.
     * 
     * @param element
     * @return
     */
    private boolean getFilterState(AbstractIntroElement element) {
        if (element.isOfType(AbstractIntroElement.BASE_ELEMENT))
            return ((AbstractBaseIntroElement) element).isFiltered();
        else
            return false;
    }


}



