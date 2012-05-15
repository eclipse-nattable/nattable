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



public interface ITreeData <T> {
	
	public String formatDataForDepth(int depth, T object);
	
	public T getDataAtIndex(int index);

	public int getDepthOfData(T object);

	public int indexOf(T child);
	
	public boolean hasChildren(T object);
	
	public List <T> getChildren(T object);
	
}
