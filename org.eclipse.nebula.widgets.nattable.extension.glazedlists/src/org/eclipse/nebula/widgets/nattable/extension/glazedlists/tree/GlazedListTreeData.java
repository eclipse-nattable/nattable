/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Deprecated expand/collapse methods since they should reside in ITreeRowModel
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455364
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeList.Node;

/**
 * Implementation of ITreeData that operates on a GlazedLists TreeList.
 *
 * @param <T>
 *            The type of objects that are contained in the TreeList.
 */
public class GlazedListTreeData<T> implements ITreeData<T> {

    private final TreeList<T> treeList;

    public GlazedListTreeData(TreeList<T> treeList) {
        this.treeList = treeList;
    }

    @Override
    public T getDataAtIndex(int index) {
        if (!isValidIndex(index)) {
            return null;
        }
        return this.treeList.get(index);
    }

    @Override
    public int getDepthOfData(T object) {
        return getDepthOfData(indexOf(object));
    }

    @Override
    public int getDepthOfData(int index) {
        if (!isValidIndex(index)) {
            return 0;
        }
        return this.treeList.depth(index);
    }

    @Override
    public int indexOf(T object) {
        return this.treeList.indexOf(object);
    }

    @Override
    public boolean hasChildren(T object) {
        return hasChildren(indexOf(object));
    }

    @Override
    public boolean hasChildren(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        return this.treeList.hasChildren(index);
    }

    @Override
    public List<T> getChildren(T object) {
        return getChildren(indexOf(object));
    }

    private List<T> getNodeChildren(Node<T> treeNode) {
        List<T> children = new ArrayList<T>();
        for (Node<T> child : treeNode.getChildren()) {
            children.add(child.getElement());
            children.addAll(getNodeChildren(child));
        }
        return children;
    }

    @Override
    public List<T> getChildren(T object, boolean fullDepth) {
        if (fullDepth == false) {
            return getChildren(object);
        }
        int index = indexOf(object);
        if (index >= 0) {
            Node<T> treeNode = this.treeList.getTreeNode(index);
            return getNodeChildren(treeNode);
        }
        return Collections.emptyList();
    }

    @Override
    public List<T> getChildren(int index) {
        if (!isValidIndex(index)) {
            return null;
        }

        List<T> children = null;
        if (index >= 0) {
            Node<T> treeNode = this.treeList.getTreeNode(index);
            if (treeNode != null) {
                List<Node<T>> childrenNodes = treeNode.getChildren();
                children = new ArrayList<T>(childrenNodes.size());
                for (Node<T> node : childrenNodes) {
                    children.add(node.getElement());
                }
            }
        }
        return children != null ? children : new ArrayList<T>();
    }

    @Override
    public int getElementCount() {
        return this.treeList.size();
    }

    @Override
    public boolean isValidIndex(int index) {
        return (!(index < 0) && index < this.treeList.size());
    }

    /**
     * @return The underlying {@link TreeList} this {@link GlazedListTreeData}
     *         is operating on.
     */
    public TreeList<T> getTreeList() {
        return this.treeList;
    }
}
