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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;
import org.eclipse.nebula.widgets.nattable.examples.NatTableExamples;
import org.eclipse.nebula.widgets.nattable.examples.runner.NavContentProvider;
import org.eclipse.nebula.widgets.nattable.examples.runner.NavLabelProvider;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

public class NavigationPart {

    public static final String E4_EXAMPLES_PREFIX = "E4 Examples/";
    public static final String E4_BASE_PATH = "/org/eclipse/nebula/widgets/nattable/examples/e4/part";

    private EPartService partService;
    private ClassLoader exampleClassLoader;

    @Inject
    EModelService modelService;

    @Inject
    MApplication app;

    @Inject
    public NavigationPart(EPartService partService) {
        this.partService = partService;

        exampleClassLoader = FrameworkUtil.getBundle(NatTableExamples.class).adapt(BundleWiring.class).getClassLoader();
    }

    @PostConstruct
    public void postConstruct(Composite parent) {
        final TreeViewer navTreeViewer = new TreeViewer(parent);

        final NavContentProvider contentProvider = new NavContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                Set<String> topLevelElements = new LinkedHashSet<>();

                String[] examplePaths = (String[]) inputElement;
                for (final String examplePath : examplePaths) {
                    String parentPath = "";
                    String absolutePath = "";

                    // remove the package name for the tree structure
                    String path = examplePath;
                    if (examplePath.startsWith(INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                        path = examplePath.replace(INatExample.BASE_PATH, "");
                    } else if (examplePath.startsWith(INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                        path = examplePath.replace(INatExample.CLASSIC_BASE_PATH, "");
                    } else if (examplePath.startsWith(E4_EXAMPLES_PREFIX)) {
                        path = examplePath.replace(E4_BASE_PATH, "");
                    }
                    final StringTokenizer tok = new StringTokenizer(path, "/");
                    while (tok.hasMoreTokens()) {
                        final String pathElement = tok.nextToken();
                        if (parentPath.length() == 0) {
                            topLevelElements.add("/" + pathElement);
                        }
                        absolutePath += "/" + pathElement;

                        final Collection<String> children = getChildren(parentPath);
                        children.add(absolutePath);

                        parentPath = absolutePath;
                    }
                }

                return topLevelElements.toArray();
            }

        };
        navTreeViewer.setContentProvider(contentProvider);
        navTreeViewer.setLabelProvider(new NavLabelProvider(contentProvider) {

            @Override
            public String getText(Object element) {
                String str = (String) element;
                if (!contentProvider.hasChildren(element)) {
                    INatExample example = getExample(str);
                    return example.getName();
                }

                int lastSlashIndex = str.lastIndexOf('/');
                if (lastSlashIndex < 0) {
                    return format(str);
                } else {
                    return format(str.substring(lastSlashIndex + 1));
                }
            }

        });
        navTreeViewer.setInput(getExamplePaths());
        navTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                TreeSelection selection = (TreeSelection) event.getSelection();
                for (TreePath path : selection.getPaths()) {
                    // check for item - if node expand/collapse, if child open
                    if (contentProvider.hasChildren(path.getLastSegment().toString())) {
                        boolean expanded = navTreeViewer.getExpandedState(path);
                        navTreeViewer.setExpandedState(path, !expanded);
                    } else {
                        openExampleInTab(path.getLastSegment().toString());
                    }
                }
            }

        });

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(navTreeViewer.getControl());
    }

    private String[] getExamplePaths() {
        List<String> examples = null;

        try {
            InputStream inputStream = NatTableExamples.class.getResourceAsStream("/examples.index");
            if (inputStream != null) {
                examples = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                while (line != null) {
                    examples.add(line);
                    line = reader.readLine();
                }
                reader.close();
            } else {
                System.out.println("examples.index not found, reconstructing");
                examples = NatTableExamples.createExamplesIndex(null);
            }

            // add e4 examples
            examples.add(E4_EXAMPLES_PREFIX + "/org/eclipse/nebula/widgets/nattable/examples/e4/part/CSSExample");
            examples.add(E4_EXAMPLES_PREFIX + "/org/eclipse/nebula/widgets/nattable/examples/e4/part/DarkExample");
            examples.add(E4_EXAMPLES_PREFIX + "/org/eclipse/nebula/widgets/nattable/examples/e4/part/PercentageExample");
            examples.add(E4_EXAMPLES_PREFIX + "/org/eclipse/nebula/widgets/nattable/examples/e4/part/TreeExample");
            examples.add(E4_EXAMPLES_PREFIX + "/org/eclipse/nebula/widgets/nattable/examples/e4/part/SelectionListenerExample");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return examples != null ? examples.toArray(new String[0]) : null;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends INatExample> getExampleClass(String examplePath, ClassLoader loader) {
        String className = examplePath.replace('/', '.');
        try {
            Class<?> clazz = Class.forName(className, true, loader);
            if (INatExample.class.isAssignableFrom(clazz)
                    && !Modifier.isAbstract(clazz.getModifiers())) {
                return (Class<? extends INatExample>) clazz;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public INatExample getExample(String examplePath) {
        INatExample example = null;
        String path = examplePath;
        ClassLoader loader = exampleClassLoader;
        if (examplePath.startsWith("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
            path = examplePath.replace("/" + INatExample.TUTORIAL_EXAMPLES_PREFIX, INatExample.BASE_PATH + "/");
        } else if (examplePath.startsWith("/" + INatExample.CLASSIC_EXAMPLES_PREFIX)) {
            path = examplePath.replace("/" + INatExample.CLASSIC_EXAMPLES_PREFIX, INatExample.CLASSIC_BASE_PATH + "/");
        } else if (examplePath.startsWith("/" + E4_EXAMPLES_PREFIX)) {
            path = examplePath.replace("/" + E4_EXAMPLES_PREFIX, E4_BASE_PATH + "/");
            loader = this.getClass().getClassLoader();
        }

        if (path.startsWith("/"))
            path = path.substring(1);

        Class<? extends INatExample> exampleClass = getExampleClass(path, loader);
        if (exampleClass != null) {
            try {
                example = exampleClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return example;
    }

    private void openExampleInTab(final String examplePath) {
        final INatExample example = getExample(examplePath);
        if (example == null) {
            return;
        }

        MPartStack stack = (MPartStack) modelService.find("org.eclipse.nebula.widgets.nattable.examples.e4.partstack.0", app);

        MPart part = null;
        if (examplePath.startsWith("/" + E4_EXAMPLES_PREFIX)) {
            part = (MPart) modelService.find(example.getClass().getName(), app);
            if (part == null) {
                part = partService.createPart(example.getClass().getName());
                part.getTags().add(LifecycleManager.CLOSE_ON_SHUTDOWN_TAG);
                part.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
                stack.getChildren().add(part);
            }
        } else {
            part = partService.createPart("org.eclipse.nebula.widgets.nattable.examples.e4.partdescriptor.natexample");
            part.getTags().add(LifecycleManager.CLOSE_ON_SHUTDOWN_TAG);
            part.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
            part.getTransientData().put("example", example);
            part.getTransientData().put("examplePath", examplePath);

            stack.getChildren().add(part);
        }
        part.setLabel(example.getName());

        partService.showPart(part, PartState.ACTIVATE);
    }
}