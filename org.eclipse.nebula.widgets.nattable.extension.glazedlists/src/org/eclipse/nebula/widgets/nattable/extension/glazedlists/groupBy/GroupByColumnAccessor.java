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

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;

public class GroupByColumnAccessor<T> implements IColumnAccessor<Object> {

	protected final IColumnAccessor<T> columnAccessor;
	
	public GroupByColumnAccessor(IColumnAccessor<T> columnAccessor) {
		this.columnAccessor = columnAccessor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getDataValue(Object rowObject, int columnIndex) {
		if (rowObject instanceof GroupByObject) {
			GroupByObject groupByObject = (GroupByObject) rowObject;
			return groupByObject.getValue();
		} else {
			return columnAccessor.getDataValue((T) rowObject, columnIndex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setDataValue(Object rowObject, int columnIndex, Object newValue) {
		if (rowObject instanceof GroupByObject) {
			// do nothing
		} else {
			columnAccessor.setDataValue((T) rowObject, columnIndex, newValue);
		}
	}

	@Override
	public int getColumnCount() {
		return columnAccessor.getColumnCount();
	}
}
