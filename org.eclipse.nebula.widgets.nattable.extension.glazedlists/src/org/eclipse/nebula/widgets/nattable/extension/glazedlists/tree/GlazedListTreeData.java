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
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeList.Node;

public class GlazedListTreeData<T> implements ITreeData<T> {

	private final TreeList<T> treeList;
	
	public GlazedListTreeData(TreeList<T> treeList) {
		this.treeList = treeList;
	}
	
	public String formatDataForDepth(int depth, int index){
		return formatDataForDepth(depth, this.treeList.get(index));
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
	
	public boolean hasChildren(int index) {
		return this.treeList.hasChildren(index);
	}

	@Override
	public List<T> getChildren(T object) {
		return getChildren(indexOf(object));
	}

	public List<T> getChildren(int index) {
		List <T> children = new ArrayList<T>();
		Node<T> treeNode = this.treeList.getTreeNode(index);
		if (treeNode != null) {
			List<Node<T>> childrenNodes = treeNode.getChildren();
			for(Node<T> node : childrenNodes){
				children.add(node.getElement());
			}
		}
		return children;
	}
	
	public void collapse(T object) {
		collapse(indexOf(object));
	};
	
	public void expand(T object) {
		expand(indexOf(object));
	};
	
	public void collapse(int index) {
		toggleExpanded(index, false);
	};
	
	public void expand(int index) {
		toggleExpanded(index, true);
	};
	
	private void toggleExpanded(int index, boolean expanded){
		this.treeList.setExpanded(index, expanded);
	}
	
	public boolean isExpanded(T object){
		return isExpanded(indexOf(object));
	}
	
	public boolean isExpanded(int index){
		return this.treeList.isExpanded(index);
	}
	
	public List<T> getRoots() {
		List<T> roots = new ArrayList<T>();
		List<Node<T>> rootNodes = this.treeList.getRoots();
		for (Node<T> root : rootNodes) {
			roots.add(root.getElement());
		}
		return roots;
	}
}
