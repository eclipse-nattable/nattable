/*****************************************************************************
 * Copyright (c) 2016, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;
import org.eclipse.nebula.widgets.nattable.examples.NatTableExamples;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NatExamplePart {

    private static final Logger LOG = LoggerFactory.getLogger(NatExamplePart.class);

    INatExample example;
    Control exampleControl;

    @PostConstruct
    public void postConstruct(Composite parent, MPart part) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);

        example = (INatExample) part.getTransientData().get("example");
        exampleControl = example.createExampleControl(parent);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(exampleControl);

        // Description
        final String description = example.getDescription();
        if (description != null && description.length() > 0) {
            final Group descriptionGroup = new Group(parent, SWT.NONE);
            descriptionGroup.setText("Description");
            descriptionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            descriptionGroup.setLayout(new FillLayout());

            final Label descriptionLabel = new Label(descriptionGroup, SWT.WRAP);
            descriptionLabel.setText(description);
        }

        // Source Link
        Link link = new Link(parent, SWT.NONE);
        link.setText("<a href=\"" + part.getTransientData().get("examplePath") + "\">View source</a>");

        final SelectionAdapter linkSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String path = event.text;
                if (path.startsWith("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                    path = path.replace("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX, INatExample.BASE_PATH + "/");
                } else if (path.startsWith("/" + INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                    path = path.replace("/" + INatExample.CLASSIC_EXAMPLES_PREFIX, INatExample.CLASSIC_BASE_PATH + "/");
                }
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

        example.onStart();
    }

    private String getResourceAsString(String resource) {
        try (InputStream inStream = NatTableExamples.class.getResourceAsStream(resource)) {
            if (inStream != null) {
                StringBuilder builder = new StringBuilder();
                int i = -1;
                while ((i = inStream.read()) != -1) {
                    builder.append((char) i);
                }

                return builder.toString();
            } else {
                MessageDialog.openError(null, "Error", "null stream for resource " + resource);
            }
        } catch (IOException e) {
            LOG.error("Error on reading resource {}", resource, e);
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

    @PreDestroy
    public void dispose() {
        example.onStop();

        if (exampleControl != null && !exampleControl.isDisposed()) {
            exampleControl.dispose();
        }
    }
}