/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.List;

public interface ITreeRowModel<T> {

	int depth(int index);

	boolean isLeaf(int index);

	String getObjectAtIndexAndDepth(int index, int depth);

	boolean hasChildren(int index);

	boolean isCollapsed(int index);

	boolean isCollapseable(int index);
	
	List<Integer> collapse(int parentIndex);

	List<Integer> collapseAll();

	List<Integer> expand(int parentIndex);

	List<Integer> expandAll();
	
	/**
	 * This method returns <b>all visible</b> child indexes below the node at the given index.
	 * It search all the way down the tree structure to find every child, even the
	 * sub children, sub sub children and so on.
	 * <p>
	 * If you only need to get the direct child indexes of the node at the given index
	 * you need to use {@link ITreeRowModel#getDirectChildIndexes(int)} instead.
	 * @param parentIndex The index for which the child indexes are requested.
	 * @return The list of all child indexes for the node at the given index.
	 */
	List<Integer> getChildIndexes(int parentIndex);
	
	/**
	 * This method returns only the direct <b>visible</b> child indexes of the node at the given index.
	 * It does not search all the way down for further sub children.
	 * <p>
	 * If you need to get all child indexes of the node at the given index
	 * you need to use {@link ITreeRowModel#getChildIndexes(int)} instead.
	 * @param parentIndex The index for which the direct child indexes are requested.
	 * @return The list of the direct child indexes for the node at the given index.
	 */
	List<Integer> getDirectChildIndexes(int parentIndex);
	
	/**
	 * This method returns <b>all</b> children below the node at the given index.
	 * It search all the way down the tree structure to find every child, even the
	 * sub children, sub sub children and so on.
	 * <p>
	 * If you only need to get the direct children of the node at the given index
	 * you need to use {@link ITreeRowModel#getDirectChildren(int)} instead.
	 * @param parentIndex The index for which the children are requested.
	 * @return The list of all children for the node at the given index.
	 */
	public List<T> getChildren(int parentIndex);
	
	/**
	 * This method returns only the direct children of the node at the given index.
	 * It does not search all the way down for further sub children.
	 * <p>
	 * If you need to get all children of the node at the given index
	 * you need to use {@link ITreeRowModel#getChildren(int)} instead.
	 * @param parentIndex The index for which the direct children are requested.
	 * @return The list of the direct children for the node at the given index.
	 */
	public List<T> getDirectChildren(int parentIndex);
}
