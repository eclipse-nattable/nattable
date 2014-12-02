/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.runner;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;

public class NavContentProvider implements ITreeContentProvider {

    private Map<String, Collection<String>> pathToChildrenMap;

    private Collection<String> getChildren(final String parentPath) {
        Collection<String> children = this.pathToChildrenMap.get(parentPath);
        if (children == null) {
            children = new LinkedHashSet<String>();
            this.pathToChildrenMap.put(parentPath, children);
        }
        return children;
    }

    @Override
    public void dispose() {
        this.pathToChildrenMap = null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.pathToChildrenMap = new HashMap<String, Collection<String>>();
    }

    @Override
    public Object[] getChildren(Object parent) {
        return this.pathToChildrenMap.get(parent).toArray();
    }

    @Override
    public Object getParent(Object element) {
        String str = (String) element;
        int lastSlashIndex = str.lastIndexOf('/');
        if (lastSlashIndex < 0) {
            return null;
        } else {
            return str.substring(0, lastSlashIndex);
        }
    }

    @Override
    public boolean hasChildren(Object element) {
        return this.pathToChildrenMap.get(element) != null;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        Set<String> topLevelElements = new LinkedHashSet<String>();

        String[] examplePaths = (String[]) inputElement;
        for (final String examplePath : examplePaths) {
            String parentPath = "";
            String absolutePath = "";

            // remove the package name for the tree structure
            String path = examplePath;
            if (examplePath.startsWith(INatExample.TUTORIAL_EXAMPLES_PREFIX)) {
                path = examplePath.replace(INatExample.BASE_PATH, "");
            } else if (examplePath
                    .startsWith(INatExample.CLASSIC_EXAMPLES_PREFIX)) {
                path = examplePath.replace(INatExample.CLASSIC_BASE_PATH, "");
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

}
