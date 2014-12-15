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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.AbstractTreeRowModel;

import ca.odell.glazedlists.TreeList;

public class GlazedListTreeRowModel<T> extends AbstractTreeRowModel<T> {

    public GlazedListTreeRowModel(GlazedListTreeData<T> treeData) {
        super(treeData);
    }

    /**
     * Note: The check only works for visible elements, therefore a check for an
     * element that is below an collapsed element does not work.
     */
    @Override
    public boolean isCollapsed(int index) {
        if (!this.getTreeData().isValidIndex(index)) {
            return false;
        }

        return !this.getTreeData().getTreeList().isExpanded(index);
    }

    /**
     * Collapse the tree node at the given visible index.
     *
     * @param index
     *            The index of the node in the collection that should be
     *            collapsed.
     *
     * @return Returns an empty list because the list transformation for
     *         expand/collapse is handled by GlazedLists {@link TreeList}.
     */
    @Override
    public List<Integer> collapse(int index) {
        if (this.getTreeData().isValidIndex(index)) {
            this.getTreeData().getTreeList().setExpanded(index, false);
            notifyListeners();
        }

        return new ArrayList<Integer>();
    }

    /**
     * Will check the whole TreeList for its nodes and collapses them if they
     * are expanded. After executing this method, all nodes in the TreeList will
     * be collapsed.
     *
     * @return Returns an empty list because the list transformation for
     *         expand/collapse is handled by GlazedLists {@link TreeList}.
     */
    @Override
    public List<Integer> collapseAll() {
        TreeList<T> treeList = this.getTreeData().getTreeList();
        treeList.getReadWriteLock().writeLock().lock();
        try {
            // iterating directly over the TreeList is a lot faster than
            // checking the nodes
            // which is related that on collapsing we only need to iterate once
            // from bottom to top
            for (int i = (treeList.size() - 1); i >= 0; i--) {
                /*
                 * Checks if the node at the given visible index has children
                 * and is collapsible. If it is it will be collapsed otherwise
                 * skipped. This backwards searching and collapsing mechanism is
                 * necessary to ensure to really get every collapsible node in
                 * the whole tree structure.
                 */
                if (hasChildren(i) && !isCollapsed(i)) {
                    treeList.setExpanded(i, false);
                }
            }
        } finally {
            treeList.getReadWriteLock().writeLock().unlock();
        }

        notifyListeners();
        return new ArrayList<Integer>();
    }

    /**
     * Expand the tree node at the given visible index.
     *
     * @param index
     *            The index of the node in the collection that should be
     *            expanded.
     *
     * @return Returns an empty list because the list transformation for
     *         expand/collapse is handled by GlazedLists {@link TreeList}.
     */
    @Override
    public List<Integer> expand(int index) {
        if (this.getTreeData().isValidIndex(index)) {
            this.getTreeData().getTreeList().setExpanded(index, true);
            notifyListeners();
        }

        return new ArrayList<Integer>();
    }

    @Override
    public List<Integer> expandToLevel(int parentIndex, int level) {
        if (this.getTreeData().isValidIndex(parentIndex)) {
            internalExpandToLevel(parentIndex, level);
            notifyListeners();
        }

        return new ArrayList<Integer>();
    }

    /**
     * Performs the expand operations iteratively without notifying the
     * listeners while processing.
     *
     * @param parentIndex
     *            The index of the node in the collection that should be
     *            expanded.
     * @param level
     *            The level to which the tree nodes should be expanded.
     */
    protected void internalExpandToLevel(int parentIndex, int level) {
        if (this.getTreeData().isValidIndex(parentIndex)
                && depth(parentIndex) <= (level - 1)) {

            this.getTreeData().getTreeList().setExpanded(parentIndex, true);

            List<Integer> directChildren = getDirectChildIndexes(parentIndex);
            // iterate backwards because the indexes are changing on expand
            Collections.sort(directChildren, Collections.reverseOrder());
            for (Integer child : directChildren) {
                if (hasChildren(child) && depth(child) <= (level - 1)) {
                    internalExpandToLevel(child, level);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return Returns an empty list because the list transformation for
     *         expand/collapse is handled by GlazedLists {@link TreeList}.
     */
    @Override
    public List<Integer> expandAll() {
        internalExpandAll();
        notifyListeners();
        return new ArrayList<Integer>();
    }

    /**
     * Performs the expand operations iteratively without notifying the
     * listeners while processing.
     */
    protected void internalExpandAll() {
        TreeList<T> treeList = this.getTreeData().getTreeList();

        boolean expandPerformed = false;
        treeList.getReadWriteLock().writeLock().lock();
        try {
            // iterating directly over the TreeList is a lot faster than
            // checking the nodes
            for (int i = (treeList.size() - 1); i >= 0; i--) {
                /*
                 * Checks if the node at the given visible index has children
                 * and is expandable. If it is it will be expanded otherwise
                 * skipped. This backwards searching and expanding mechanism is
                 * necessary to ensure to really get every expandable node in
                 * the whole tree structure.
                 */
                if (hasChildren(i) && isCollapsed(i)) {
                    treeList.setExpanded(i, true);
                    expandPerformed = true;
                }
            }
        } finally {
            treeList.getReadWriteLock().writeLock().unlock();
        }

        // if at least one element was expanded we need to perform the step
        // again as we are only able to retrieve the visible nodes
        if (expandPerformed) {
            expandAll();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return Returns an empty list because the list transformation for
     *         expand/collapse is handled by GlazedLists {@link TreeList}.
     */
    @Override
    public List<Integer> expandToLevel(int level) {
        internalExpandToLevel(level);
        notifyListeners();
        return new ArrayList<Integer>();
    }

    /**
     * Performs the expand operations iteratively without notifying the
     * listeners while processing.
     *
     * @param level
     *            The level to which the tree nodes should be expanded.
     */
    protected void internalExpandToLevel(int level) {
        TreeList<T> treeList = this.getTreeData().getTreeList();

        boolean expandPerformed = false;
        treeList.getReadWriteLock().writeLock().lock();
        try {
            // iterating directly over the TreeList is a lot faster than
            // checking the nodes
            for (int i = (treeList.size() - 1); i >= 0; i--) {
                /*
                 * Checks if the node at the given visible index has children,
                 * is expandable and is on a level below the given level. If it
                 * is it will be expanded otherwise skipped. This backwards
                 * searching and expanding mechanism is necessary to ensure to
                 * really get every expandable node in the whole tree structure.
                 */
                if (hasChildren(i) && isCollapsed(i) && treeList.getTreeNode(i).path().size() <= level) {
                    treeList.setExpanded(i, true);
                    expandPerformed = true;
                }
            }
        } finally {
            treeList.getReadWriteLock().writeLock().unlock();
        }

        // if at least one element was expanded we need to perform the step
        // again as we are only able to retrieve the visible nodes
        if (expandPerformed) {
            expandToLevel(level);
        }
    }

    @Override
    protected GlazedListTreeData<T> getTreeData() {
        return (GlazedListTreeData<T>) super.getTreeData();
    }
}
