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
package org.eclipse.nebula.widgets.nattable.test.performance;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class InternalDataProvider implements IDataProvider {
	private Object[][] data;
	private int numRows;
	private int numCols;

//	public InternalDataProvider(TableDataProvider tableData) {
//		this(tableData.getData(), tableData.getColumnCount(), tableData.getRowCount());
//	}
	
	public InternalDataProvider(Object[][] data, int numCols, int numRows) {
		this.data = data;
		this.numRows = numRows;
		this.numCols = numCols;
	}

	public int getColumnCount() {
		return numCols;
	}

	public int getRowCount() {
		return numRows;
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		return data[columnIndex][rowIndex];
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}
	
}
