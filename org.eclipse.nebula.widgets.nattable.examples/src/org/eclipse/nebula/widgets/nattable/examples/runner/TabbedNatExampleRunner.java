/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - extended width of tree
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.runner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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

public class TabbedNatExampleRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TabbedNatExampleRunner.class);

    private static CTabFolder tabFolder;
    private static Map<INatExample, Control> exampleControlMap = new HashMap<>();
    private static Map<String, INatExample> examplePathMap = new HashMap<>();
    private static Link link;;

    public static void run(String... examplePaths)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        run(1000, 600, examplePaths);
    }

    public static void run(int shellWidth, int shellHeight, final String... examplePaths)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Setup
        final Display display = Display.getDefault();
        final Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setLayout(new GridLayout(2, false));
        shell.setSize(shellWidth, shellHeight);
        shell.setText("NatTable Examples Application");

        Image nebula16 = new Image(display, TabbedNatExampleRunner.class.getResourceAsStream("nebula_logo_16.png"));
        Image nebula32 = new Image(display, TabbedNatExampleRunner.class.getResourceAsStream("nebula_logo_32.png"));
        Image nebula64 = new Image(display, TabbedNatExampleRunner.class.getResourceAsStream("nebula_logo_64.png"));

        shell.setImages(new Image[] { nebula16, nebula32, nebula64 });

        // Nav tree
        final TreeViewer navTreeViewer = new TreeViewer(shell);
        GridData gridData = new GridData(GridData.FILL_VERTICAL);
        gridData.widthHint = 300;
        navTreeViewer.getControl().setLayoutData(gridData);
        final NavContentProvider contentProvider = new NavContentProvider();
        navTreeViewer.setContentProvider(contentProvider);
        navTreeViewer.setLabelProvider(new NavLabelProvider(contentProvider));
        navTreeViewer.setInput(examplePaths);
        navTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                TreeSelection selection = (TreeSelection) event.getSelection();
                for (TreePath path : selection.getPaths()) {
                    // check for item - if node expand/collapse, if child open
                    if (contentProvider.hasChildren(path.getLastSegment()
                            .toString())) {
                        boolean expanded = navTreeViewer.getExpandedState(path);
                        navTreeViewer.setExpandedState(path, !expanded);
                    } else {
                        openExampleInTab(path.getLastSegment().toString());
                    }
                }
            }

        });

        tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        for (INatExample example : exampleControlMap.keySet()) {
            // Stop
            example.onStop();

            Control exampleControl = exampleControlMap.get(example);
            exampleControl.dispose();
        }

        tabFolder.dispose();

        shell.dispose();
        display.dispose();
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends INatExample> getExampleClass(String examplePath) {
        String className = examplePath.replace('/', '.');
        try {
            Class<?> clazz = Class.forName(className);
            if (INatExample.class.isAssignableFrom(clazz)
                    && !Modifier.isAbstract(clazz.getModifiers())) {
                return (Class<? extends INatExample>) clazz;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static INatExample getExample(String examplePath) {
        INatExample example = examplePathMap.get(examplePath);
        if (example == null) {
            String path = examplePath;
            if (examplePath.startsWith("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                path = examplePath.replace("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX, INatExample.BASE_PATH + "/");
            } else if (examplePath.startsWith("/" + INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                path = examplePath.replace("/" + INatExample.CLASSIC_EXAMPLES_PREFIX, INatExample.CLASSIC_BASE_PATH + "/");
            }

            if (path.startsWith("/"))
                path = path.substring(1);

            Class<? extends INatExample> exampleClass = getExampleClass(path);
            if (exampleClass != null) {
                try {
                    example = exampleClass.getDeclaredConstructor().newInstance();
                    examplePathMap.put(examplePath, example);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return example;
    }

    private static void openExampleInTab(final String examplePath) {
        final INatExample example = getExample(examplePath);
        if (example == null) {
            return;
        }

        final String exampleName = example.getName();
        final CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
        tabItem.setText(exampleName);
        tabItem.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                // Stop
                example.onStop();

                Control exampleControl = exampleControlMap.get(example);
                if (exampleControl != null && !exampleControl.isDisposed()) {
                    exampleControl.dispose();
                }

                exampleControlMap.remove(example);
                examplePathMap.remove(examplePath);
                link.dispose();
            }
        });

        final Composite tabComposite = new Composite(tabFolder, SWT.NONE);
        tabComposite.setLayout(new GridLayout(1, false));

        // Create example control
        final Control exampleControl = example.createExampleControl(tabComposite);
        exampleControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        exampleControlMap.put(example, exampleControl);

        // Description
        final String description = example.getDescription();
        if (description != null && description.length() > 0) {
            final Group descriptionGroup = new Group(tabComposite, SWT.NONE);
            descriptionGroup.setText("Description");
            descriptionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            descriptionGroup.setLayout(new FillLayout());

            final Label descriptionLabel = new Label(descriptionGroup, SWT.WRAP);
            descriptionLabel.setText(description);
        }

        link = new Link(tabComposite, SWT.NONE);
        link.setText("<a href=\"" + examplePath + "\">View source</a>");

        final SelectionAdapter linkSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String path = event.text;
                if (path.startsWith("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                    path = path.replace("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX, INatExample.BASE_PATH + "/");
                } else if (path.startsWith("/" + INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                    path = path.replace("/" + INatExample.CLASSIC_EXAMPLES_PREFIX, INatExample.CLASSIC_BASE_PATH + "/");
                }
                String source = getResourceAsString(path + ".java");
                if (source != null) {
                    viewSource(exampleName, source);
                }
            }
        };
        link.addSelectionListener(linkSelectionListener);

        tabItem.setControl(tabComposite);

        // Start
        example.onStart();

        tabFolder.setSelection(tabItem);
    }

    private static String getResourceAsString(String resource) {
        try (InputStream inStream = TabbedNatExampleRunner.class.getResourceAsStream(resource)) {
            if (inStream != null) {
                StringBuilder strBuf = new StringBuilder();
                int i = -1;
                while ((i = inStream.read()) != -1) {
                    strBuf.append((char) i);
                }

                return strBuf.toString();
            } else {
                System.out.println("null stream for resource " + resource);
            }
        } catch (IOException e) {
            LOG.error("Failed to read resource {}", resource, e);
        }

        return null;
    }

    private static void viewSource(String title, String source) {
        Shell shell = new Shell(Display.getDefault());
        shell.setText(title);
        shell.setLayout(new FillLayout());

        Browser text = new Browser(shell, SWT.MULTI);
        text.setBackground(GUIHelper.COLOR_WHITE);
        text.setText("<pre>" + source + "</pre>");

        shell.open();
    }

}
