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

public interface ITreeRowModel<T> {

	int depth(int index);

	boolean isLeaf(int index);

	String getObjectAtIndexAndDepth(int index, int depth);

	boolean hasChildren(int index);

	boolean isCollapsed(int index);

	List<Integer> collapse(int parentIndex);

	List<Integer> expand(int parentIndex);

}
