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
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.AbstractTreeRowModel;


public class GlazedListTreeRowModel<T> extends AbstractTreeRowModel<T>{

	public GlazedListTreeRowModel(GlazedListTreeData<T> treeData) {
		super(treeData);
	}

	@Override
	public boolean isCollapsed(int index) {
		return !this.getTreeData().isExpanded(index);
	}

	@Override
	public List<Integer> collapse(int index) {
		this.getTreeData().collapse(index);
		notifyListeners();
		return new ArrayList<Integer>();
	}
	
	@Override
	public List<Integer> collapseAll() {
		this.getTreeData().collapseAll();
		notifyListeners();
		return new ArrayList<Integer>();
	}

	@Override
	public List<Integer> expand(int index) {
		this.getTreeData().expand(index);
		notifyListeners();
		return new ArrayList<Integer>();
	}

	@Override
	public List<Integer> expandAll() {
		this.getTreeData().expandAll();
		notifyListeners();
		return new ArrayList<Integer>();
	}
	
	@Override
	protected GlazedListTreeData<T> getTreeData() {
		return (GlazedListTreeData<T>) super.getTreeData();
	}
}
