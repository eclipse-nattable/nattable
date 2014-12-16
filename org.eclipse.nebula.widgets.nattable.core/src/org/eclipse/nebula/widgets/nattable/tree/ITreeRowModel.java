/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453707, 455364
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.export.IExportFormatter;

/**
 * The {@link ITreeRowModel} is used by the {@link TreeLayer} to build up and
 * interact with the tree model. It deals with the nodes and the corresponding
 * expand/collapse states.
 *
 * @param <T>
 *            The type of the elements in the tree
 */
public interface ITreeRowModel<T> {

    /**
     * @param index
     *            The index of the tree element to check.
     * @return The number of ancestors of the node at the specified index. Root
     *         nodes have depth 0, other nodes depth is one greater than the
     *         depth of their parent node.
     */
    int depth(int index);

    /**
     * @param index
     *            The index of the tree element to check.
     * @return <code>true</code> if the tree element at the given index has no
     *         children and therefore is a leaf, <code>false</code> if the tree
     *         element has children and therefore is a node.
     */
    boolean isLeaf(int index);

    /**
     * Returns a formatted String representation for the tree node at the given
     * index with the given depth.
     * <p>
     * Currently only used for exporting a tree to excel. Possibly not necessary
     * in the future when implementing an exporter that formats converts and
     * formats tree node values in another way.
     * </p>
     *
     * @param index
     *            The index of the requested tree node.
     * @param depth
     *            The depth of the tree node at the requested index.
     * @return The String representation of the tree node at the given index
     *         with the given depth.
     *
     * @deprecated formatting should be done in the {@link IExportFormatter}
     */
    @Deprecated
    String getObjectAtIndexAndDepth(int index, int depth);

    /**
     * @param index
     *            The index of the tree element to check.
     * @return <code>true</code> if the tree element at the given index has
     *         children, <code>false</code> if not.
     */
    boolean hasChildren(int index);

    /**
     * @param index
     *            The index of the tree element to check.
     * @return <code>true</code> if the children of the tree node at the given
     *         index are visible, <code>false</code> if not.
     */
    boolean isCollapsed(int index);

    /**
     * @param object
     *            The element that should be checked.
     * @return <code>true</code> if the children of the given element are
     *         visible, <code>false</code> if not.
     */
    boolean isCollapsed(T object);

    /**
     * Checks if the tree node at the given index is collapsible or not.
     *
     * @param index
     *            The index of the tree node to check.
     * @return <code>true</code> if the tree node at the given index is
     *         collapsible, <code>false</code> if not.
     */
    boolean isCollapsible(int index);

    /**
     * Collapses the tree node at the given index.
     *
     * @param parentIndex
     *            The index of the node in the collection that should be
     *            collapsed.
     * @return The indexes of all children of the collapsed tree node that
     *         become invisible by performing the collapse operation.
     */
    List<Integer> collapse(int parentIndex);

    /**
     * Collapse the tree node that represent the given object.
     *
     * @param object
     *            The object that represents the tree node to collapse.
     *
     * @return The indexes of all children of the collapsed tree node that
     *         become invisible by performing the collapse operation.
     */
    List<Integer> collapse(T object);

    /**
     * Collapses all tree nodes.
     *
     * @return The indexes of all children that are hidden after the collapse
     *         operation is performed.
     */
    List<Integer> collapseAll();

    /**
     * Expands the tree node at the given index.
     *
     * @param parentIndex
     *            The index of the node in the collection that should be
     *            expanded.
     * @return The indexes of all children of the expanded tree node that become
     *         visible by performing the expand operation.
     */
    List<Integer> expand(int parentIndex);

    /**
     * Expands the tree node at the given index to a certain level.
     *
     * @param parentIndex
     *            The index of the node in the collection that should be
     *            expanded.
     * @param level
     *            The level to which the tree node should be expanded.
     * @return The indexes of all children that are showed after the expand
     *         operation is performed.
     */
    List<Integer> expandToLevel(int parentIndex, int level);

    /**
     * Expands the tree node that represents the given object.
     *
     * @param object
     *            The object that represents the tree node to expand.
     *
     * @return The indexes of all children of the expanded tree node that become
     *         visible by performing the expand operation.
     */
    List<Integer> expand(T object);

    /**
     * Expands the tree node that represents the given object to a certain
     * level.
     *
     * @param object
     *            The object that represents the tree node to expand.
     * @param level
     *            The level to which the tree node should be expanded.
     * @return The indexes of all children that are showed after the expand
     *         operation is performed.
     */
    List<Integer> expandToLevel(T object, int level);

    /**
     * Expands all tree nodes.
     *
     * @return The indexes of all children that are showed after the expand
     *         operation is performed.
     */
    List<Integer> expandAll();

    /**
     * Expands all tree nodes to a certain level.
     *
     * @param level
     *            The level to which the tree nodes should be expanded.
     * @return The indexes of all children that are showed after the expand
     *         operation is performed.
     */
    List<Integer> expandToLevel(int level);

    /**
     * This method returns <b>all visible</b> child indexes below the node at
     * the given index. It search all the way down the tree structure to find
     * every child, even the sub children, sub sub children and so on.
     * <p>
     * If you only need to get the direct child indexes of the node at the given
     * index you need to use {@link ITreeRowModel#getDirectChildIndexes(int)}
     * instead.
     *
     * @param parentIndex
     *            The index for which the child indexes are requested.
     * @return The list of all child indexes for the node at the given index.
     */
    List<Integer> getChildIndexes(int parentIndex);

    /**
     * This method returns only the direct <b>visible</b> child indexes of the
     * node at the given index. It does not search all the way down for further
     * sub children.
     * <p>
     * If you need to get all child indexes of the node at the given index you
     * need to use {@link ITreeRowModel#getChildIndexes(int)} instead.
     *
     * @param parentIndex
     *            The index for which the direct child indexes are requested.
     * @return The list of the direct child indexes for the node at the given
     *         index.
     */
    List<Integer> getDirectChildIndexes(int parentIndex);

    /**
     * This method returns <b>all</b> children below the node at the given
     * index. It search all the way down the tree structure to find every child,
     * even the sub children, sub sub children and so on.
     * <p>
     * If you only need to get the direct children of the node at the given
     * index you need to use {@link ITreeRowModel#getDirectChildren(int)}
     * instead.
     *
     * @param parentIndex
     *            The index for which the children are requested.
     * @return The list of all children for the node at the given index.
     */
    public List<T> getChildren(int parentIndex);

    /**
     * This method returns only the direct children of the node at the given
     * index. It does not search all the way down for further sub children.
     * <p>
     * If you need to get all children of the node at the given index you need
     * to use {@link ITreeRowModel#getChildren(int)} instead.
     *
     * @param parentIndex
     *            The index for which the direct children are requested.
     * @return The list of the direct children for the node at the given index.
     */
    public List<T> getDirectChildren(int parentIndex);
}
