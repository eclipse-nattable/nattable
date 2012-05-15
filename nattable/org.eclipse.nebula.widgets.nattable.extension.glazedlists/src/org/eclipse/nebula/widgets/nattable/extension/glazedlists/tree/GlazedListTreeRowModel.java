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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModelListener;


public class GlazedListTreeRowModel<T> implements ITreeRowModel<T>{

	private final Collection<ITreeRowModelListener> listeners = new HashSet<ITreeRowModelListener>();

	private final GlazedListTreeData<T> treeData;

	public GlazedListTreeRowModel(GlazedListTreeData<T> treeData) {
		this.treeData = treeData;
	}

	public void registerRowGroupModelListener(ITreeRowModelListener listener) {
		this.listeners.add(listener);
	}

	public void notifyListeners() {
		for (ITreeRowModelListener listener : this.listeners) {
			listener.treeRowModelChanged();
		}
	}

	public int depth(int index) {
		return this.treeData
				.getDepthOfData(this.treeData.getDataAtIndex(index));
	}

	public boolean isLeaf(int index) {
		return !hasChildren(index);
	}

	public String getObjectAtIndexAndDepth(int index, int depth) {
		return this.treeData.formatDataForDepth(depth, index);
	}

	public boolean hasChildren(int index) {
		return this.treeData.hasChildren(index);
	}

	public boolean isCollapsed(int index) {
		return !this.treeData.isExpanded(index);
	}

	public void clear() {
		
	}

	/**
	 * @return TRUE if the row group this index is collapseable
	 */
	public boolean isCollapseable(int index) {
		return hasChildren(index);
	}

	public List<Integer> collapse(int index) {
			this.treeData.collapse(index);
			notifyListeners();
			return new ArrayList<Integer>();
	}

	public List<Integer> expand(int index) {
		
			this.treeData.expand(index);
			notifyListeners();
			return new ArrayList<Integer>();
	}

	public List<Integer> getChildIndexes(int parentIndex) {
		List<Integer> result = new ArrayList<Integer>();
		List<T> children = this.treeData.getChildren(parentIndex);
		for (T child : children) {
			int index = this.treeData.indexOf(child);
			result.add(index);
			result.addAll(getChildIndexes(index));
		}
		return result;
	}

}
