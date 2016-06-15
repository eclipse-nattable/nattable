/*****************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class AbstractE4NatExamplePart extends AbstractNatExample {

    @Inject
    MPart part;

    protected void showSourceLinks(Composite parent, String examplePath) {
        Composite panel = new Composite(parent, SWT.NONE);
        RowLayout layout = new RowLayout();
        layout.spacing = 5;
        panel.setLayout(layout);

        GridDataFactory.defaultsFor(panel).applyTo(panel);

        Link link = new Link(panel, SWT.NONE);
        link.setText("<a href=\"" + examplePath + "\">View source</a>");

        final SelectionAdapter linkSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String path = event.text;
                path = path.replaceAll("\\.", "/");
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                String source = getResourceAsString("/src" + path + ".java");
                if (source != null) {
                    viewSource(part.getLabel(), source);
                }
            }
        };
        link.addSelectionListener(linkSelectionListener);

        Link cssLink = new Link(panel, SWT.NONE);
        cssLink.setText("<a href=\"/css/default.css\">View CSS</a>");

        final SelectionAdapter cssLinkSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String source = getResourceAsString(event.text);
                if (source != null) {
                    viewSource("default.css", source);
                }
            }
        };
        cssLink.addSelectionListener(cssLinkSelectionListener);
    }

    private String getResourceAsString(String resource) {
        InputStream inStream = getClass().getResourceAsStream(resource);

        if (inStream != null) {
            StringBuilder builder = new StringBuilder();
            try {
                int i = -1;
                while ((i = inStream.read()) != -1) {
                    builder.append((char) i);
                }

                return builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            MessageDialog.openError(null, "Error", "null stream for resource " + resource);
        }

        return null;
    }

    private void viewSource(String title, String source) {
        Shell shell = new Shell(Display.getDefault());
        shell.setText(title);
        shell.setLayout(new FillLayout());

        Browser text = new Browser(shell, SWT.MULTI);
        text.setBackground(GUIHelper.COLOR_WHITE);
        text.setText("<pre>" + source + "</pre>");

        shell.open();
    }

    @Override
    public Control createExampleControl(Composite parent) {
        return null;
    }

}
