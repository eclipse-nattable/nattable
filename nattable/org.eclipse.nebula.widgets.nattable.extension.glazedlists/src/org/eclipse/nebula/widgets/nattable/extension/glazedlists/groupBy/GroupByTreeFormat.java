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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;

import ca.odell.glazedlists.TreeList;

public class GroupByTreeFormat<T> implements TreeList.Format<Object> {

	private final GroupByModel model;
	private final IColumnAccessor<T> columnAccessor;

	public GroupByTreeFormat(GroupByModel model, IColumnAccessor<T> columnAccessor) {
		this.model = model;
		this.columnAccessor = columnAccessor;
	}

	public void getPath(List<Object> path, Object element) {
		for (int columnIndex : model.getGroupByColumnIndexes()) {
			Object columnValue = columnAccessor.getDataValue((T) element, columnIndex);
			path.add(new GroupByObject(columnValue));
		}

		path.add(element);
	}

	public boolean allowsChildren(Object element) {
		return true;
	}

	public Comparator<Object> getComparator(int depth) {
		return DefaultComparator.getInstance();
	}
	
}
