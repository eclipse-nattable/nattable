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

import java.util.HashSet;
import java.util.List;

public class TreeRowModel<T> extends AbstractTreeRowModel<T>{

	private final HashSet<Integer> parentIndexes = new HashSet<Integer>();

	public TreeRowModel(ITreeData<T> treeData) {
		super(treeData);
	}

	@Override
	public boolean isCollapsed(int index) {
		return this.parentIndexes.contains(index);
	}

	public void clear() {
		this.parentIndexes.clear();
	}

	@Override
	public List<Integer> collapse(int index) {
		this.parentIndexes.add(index);
		notifyListeners();
		return getChildIndexes(index);
	}

	@Override
	public List<Integer> expand(int index) {
		this.parentIndexes.remove(index);
		notifyListeners();
		List<Integer> children = getChildIndexes(index);
		this.parentIndexes.removeAll(children);
		return children;
	}

}
