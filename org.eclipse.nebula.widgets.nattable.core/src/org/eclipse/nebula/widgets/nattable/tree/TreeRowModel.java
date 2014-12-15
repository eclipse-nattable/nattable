/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453707
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link ITreeRowModel} that is used to perform
 * expand/collapse operations using the approach of hiding and showing rows.
 *
 * @param <T>
 *            The type of the elements in the tree
 */
public class TreeRowModel<T> extends AbstractTreeRowModel<T> {

    protected final Set<Integer> parentIndexes = new HashSet<Integer>();

    public TreeRowModel(ITreeData<T> treeData) {
        super(treeData);
    }

    @Override
    public boolean isCollapsed(int index) {
        return this.parentIndexes.contains(index);
    }

    /**
     * Clears the parent indexes that indicate a collapsed node.
     *
     * @deprecated Since this is not specified by the ITreeRowModel interface,
     *             this method shouldn't be used directly and therefore be
     *             removed.
     */
    @Deprecated
    public void clear() {
        this.parentIndexes.clear();
    }

    @Override
    public List<Integer> collapse(int index) {
        this.parentIndexes.add(index);
        notifyListeners();
        return getChildIndexes(index);
    }

    /**
     * Performs the collapse operation without notifying the listeners while
     * processing.
     *
     * @param index
     *            The index of the node in the collection that should be
     *            collapsed.
     *
     * @return The indexes of all children of the collapsed tree node that
     *         become invisible by performing the collapse operation.
     */
    protected Collection<Integer> internalCollapse(int index) {
        this.parentIndexes.add(index);
        return getChildIndexes(index);
    }

    @Override
    public List<Integer> collapseAll() {
        Set<Integer> collapsedChildren = new HashSet<Integer>();

        for (int i = (getTreeData().getElementCount() - 1); i >= 0; i--) {
            if (hasChildren(i) && !isCollapsed(i)) {
                collapsedChildren.addAll(internalCollapse(i));
            }
        }

        List<Integer> children = new ArrayList<Integer>(collapsedChildren);
        Collections.sort(children);
        notifyListeners();
        return children;
    }

    @Override
    public List<Integer> expand(int index) {
        List<Integer> children = new ArrayList<Integer>(internalExpand(index));
        Collections.sort(children);
        notifyListeners();
        return children;
    }

    /**
     * Performs the expand operations iteratively without notifying the
     * listeners while processing.
     *
     * @param index
     *            The index of the node in the collection that should be
     *            expanded.
     * @return The indexes of all children of the expanded tree node that become
     *         visible by performing the expand operation.
     */
    protected Collection<Integer> internalExpand(int index) {
        this.parentIndexes.remove(index);
        List<Integer> directChildren = getDirectChildIndexes(index);
        Set<Integer> expandedChildren = new HashSet<Integer>(directChildren);
        for (Integer child : directChildren) {
            if (hasChildren(child) && !isCollapsed(child)) {
                expandedChildren.addAll(internalExpand(child));
            }
        }
        return expandedChildren;
    }

    @Override
    public List<Integer> expandAll() {
        Set<Integer> expandedChildren = new HashSet<Integer>();
        for (int index : this.parentIndexes) {
            expandedChildren.addAll(getChildIndexes(index));
        }
        this.parentIndexes.clear();
        List<Integer> children = new ArrayList<Integer>(expandedChildren);
        Collections.sort(children);
        notifyListeners();
        return children;
    }

    @Override
    public List<Integer> expandToLevel(int level) {
        Set<Integer> expandedChildren = new HashSet<Integer>();
        List<Integer> parentCopy = new ArrayList<Integer>(this.parentIndexes);
        for (int index : parentCopy) {
            expandedChildren.addAll(internalExpandToLevel(index, level));
        }

        List<Integer> children = new ArrayList<Integer>(expandedChildren);
        Collections.sort(children);
        notifyListeners();
        return children;
    }

    @Override
    public List<Integer> expandToLevel(int parentIndex, int level) {
        List<Integer> children = new ArrayList<Integer>(internalExpandToLevel(parentIndex, level));
        Collections.sort(children);
        notifyListeners();
        return children;
    }

    /**
     * Performs the expand operations iteratively without notifying the
     * listeners while processing.
     *
     * @param index
     *            The index of the node in the collection that should be
     *            expanded.
     * @param level
     *            The level to which the tree nodes should be expanded.
     *
     * @return The indexes of all children of the expanded tree node that become
     *         visible by performing the expand operation.
     */
    protected Collection<Integer> internalExpandToLevel(int index, int level) {
        Set<Integer> expandedChildren = new HashSet<Integer>();
        if (depth(index) <= (level - 1)) {
            this.parentIndexes.remove(index);

            List<Integer> directChildren = getDirectChildIndexes(index);
            for (Integer child : directChildren) {
                expandedChildren.add(child);
                if (hasChildren(child) && depth(child) <= (level - 1)) {
                    expandedChildren.addAll(internalExpandToLevel(child, level));
                }
            }
        }
        return expandedChildren;
    }
}
