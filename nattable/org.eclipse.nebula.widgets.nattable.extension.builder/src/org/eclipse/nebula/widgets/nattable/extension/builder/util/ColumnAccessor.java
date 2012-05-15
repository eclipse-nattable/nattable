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
package org.eclipse.nebula.widgets.nattable.extension.builder.util;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableColumn;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;


public class ColumnAccessor<T extends TableRow> implements IColumnPropertyAccessor<T> {

	private final TableColumn[] colProps;

	public ColumnAccessor(TableColumn[] colProps) {
		this.colProps = colProps;
	}

	public Object getDataValue(TableRow rowObj, int col) {
		return rowObj.getValue(colProps[col].index);
	}

	public int getColumnCount() {
		return colProps.length;
	}

	public int getColumnIndex(String propertyName) {
		for (int columnIndex = 0; columnIndex < colProps.length; columnIndex++) {
			if (colProps[columnIndex].rowObjectPropertyName.equals(propertyName)) {
				return columnIndex;
			}
		}
		return -1;
	}

	public String getColumnProperty(int columnIndex) {
		return colProps[columnIndex].rowObjectPropertyName;
	}

	public void setDataValue(TableRow rowObject, int columnIndex, Object newValue) {
		rowObject.setValue(columnIndex, newValue);
	}
}
