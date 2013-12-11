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
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.List;



public interface ITreeData<T> {
	
	String formatDataForDepth(int depth, T object);
	
	String formatDataForDepth(int depth, int index);

	int getDepthOfData(T object);

	int getDepthOfData(int index);

	/**
	 * @param index The index for which the corresponding object in the tree
	 * 			structure is requested.
	 * @return The object at the given index in the tree structure.
	 */
	T getDataAtIndex(int index);

	/**
	 * @param child The child whose index is requested.
	 * @return The index of the given child object in the tree structure.
	 */
	int indexOf(T child);
	
	/**
	 * @param object The object which should be checked for children.
	 * @return <code>true</code> if the object has children in the tree structure,
	 * 			<code>false</code> if it is a leaf.
	 */
	boolean hasChildren(T object);
	
	/**
	 * @param index The index of the object in the tree structure which should be checked for children.
	 * @return <code>true</code> if the object has children in the tree structure,
	 * 			<code>false</code> if it is a leaf.
	 */
	boolean hasChildren(int index);
	
	/**
	 * Return the child objects below the given object if the object
	 * is a tree node. It will only return the direct children and will
	 * not search for sub children.
	 * @param object The object whose children are requested.
	 * @return The children of the given object.
	 */
	List<T> getChildren(T object);
	
	/**
	 * Return the child objects below the given object if the object
	 * is a tree node.
	 * @param object The object whose children are requested.
	 * @param fullDepth to return only direct children or search for sub children 
	 * @return The children of the given object.
	 */
	List<T> getChildren(T object, boolean fullDepth);
	
	/**
	 * Return the child objects below the object at the given index if the object
	 * is a tree node. It will only return the direct children and will
	 * not search for sub children.
	 * @param index The index of the object whose children are requested.
	 * @return The children of the object at the given index.
	 */
	List<T> getChildren(int index);
	
	/**
	 * @return The number of elements handled by this ITreeData.
	 */
	int getElementCount();
}
