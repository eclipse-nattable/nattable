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
	public String formatDataForDepth(int depth, int index) {
		return formatDataForDepth(depth, getDataAtIndex(index));
	}

	@Override
	public String formatDataForDepth(int depth, T object) {
		if (object != null) {
			return object.toString();
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	public T getDataAtIndex(int index) {
		return this.treeList.get(index);
	}

	@Override
	public int getDepthOfData(T object) {
		return getDepthOfData(indexOf(object));
	}

	@Override
	public int getDepthOfData(int index) {
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
		List<T> children = new ArrayList<T>();
		if (index >= 0) {
			Node<T> treeNode = this.treeList.getTreeNode(index);
			if (treeNode != null) {
				List<Node<T>> childrenNodes = treeNode.getChildren();
				for (Node<T> node : childrenNodes) {
					children.add(node.getElement());
				}
			}
		}
		return children;
	}

	@Override
	public int getElementCount() {
		return this.treeList.size();
	}
	
	/**
	 * Collapse the tree node that represent the given object.
	 * @param object The object that represents the tree node to collapse.
	 */
	public void collapse(T object) {
		collapse(indexOf(object));
	};

	/**
	 * Expand the tree node that represent the given object.
	 * @param object The object that represents the tree node to expand.
	 */
	public void expand(T object) {
		expand(indexOf(object));
	};

	/**
	 * Collapse the tree node at the given visible index.
	 * @param index The index of the tree node to collapse.
	 */
	public void collapse(int index) {
		this.treeList.setExpanded(index, false);
	};

	/**
	 * Expand the tree node at the given visible index.
	 * @param index The index of the tree node to expand.
	 */
	public void expand(int index) {
		this.treeList.setExpanded(index, true);
	};

	/**
	 * Will check the whole TreeList for its nodes and collapses them if they are expanded.
	 * After executing this method, all nodes in the TreeList will be collapsed.
	 */
	public void collapseAll() {
		this.treeList.getReadWriteLock().writeLock().lock();
		try {
			//iterating directly over the TreeList is a lot faster than checking the nodes
			//which is related that on collapsing we only need to iterate once from bottom to top
			for (int i = (this.treeList.size()-1); i >= 0; i--) {
				/*
				 * Checks if the node at the given visible index has children and is collapsible.
				 * If it is it will be collapsed otherwise skipped.
				 * This backwards searching and collapsing mechanism is necessary to ensure to
				 * really get every collapsible node in the whole tree structure.
				 */
				if (hasChildren(i) && isExpanded(i)) {
					this.treeList.setExpanded(i, false);
				}
			}
		}
		finally {
			this.treeList.getReadWriteLock().writeLock().unlock();
		}
	}
	
	/**
	 * Will check the whole TreeList for its nodes and expands them if they are collapsed.
	 * After executing this method, all nodes in the TreeList will be expanded.
	 */
	public void expandAll() {
		boolean expandPerformed = false;
		this.treeList.getReadWriteLock().writeLock().lock();
		try {
			//iterating directly over the TreeList is a lot faster than checking the nodes
			//which is related that on collapsing we only need to iterate once from bottom to top
			for (int i = (this.treeList.size()-1); i >= 0; i--) {
				/*
				 * Checks if the node at the given visible index has children and is expandable.
				 * If it is it will be expanded otherwise skipped.
				 * This backwards searching and expanding mechanism is necessary to ensure to
				 * really get every expandable node in the whole tree structure.
				 */
				if (hasChildren(i) && !isExpanded(i)) {
					this.treeList.setExpanded(i, true);
					expandPerformed = true;
				}
			}
		}
		finally {
			this.treeList.getReadWriteLock().writeLock().unlock();
		}
		
		//if at least one element was expanded we need to perform the step again
		//as we are only able to retrieve the visible nodes
		if (expandPerformed) {
			expandAll();
		}
	}
	
	/**
	 * Checks if the given element is expanded or not.
	 * <p>
	 * Note: The check only works for visible elements, therefore a check
	 * for an element that is below an collapsed element does not work.
	 * @param object The element that should be checked.
	 * @return <code>true</code> if the children of the given element
	 * 			are visible, <code>false</code> if not
	 */
	public boolean isExpanded(T object) {
		return isExpanded(indexOf(object));
	}

	/**
	 * Checks if the element at the given visual index is expanded or not.
	 * @param index The visual index of the element to check.
	 * @return <code>true</code> if the children of the element at given index
	 * 			are visible, <code>false</code> if not
	 */
	public boolean isExpanded(int index) {
		return this.treeList.isExpanded(index);
	}
}
