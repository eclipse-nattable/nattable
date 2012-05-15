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
package org.eclipse.nebula.widgets.nattable.test.fixture.data;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DataProviderFixture implements IDataProvider {

	private final int colCount;
	private final int rowCount;

	public DataProviderFixture(int colCount, int rowCount) {
		this.colCount = colCount;
		this.rowCount = rowCount;
	}

	public int getColumnCount() {
		return colCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		return "[" + columnIndex + "," + rowIndex + "]";
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}
	
}
