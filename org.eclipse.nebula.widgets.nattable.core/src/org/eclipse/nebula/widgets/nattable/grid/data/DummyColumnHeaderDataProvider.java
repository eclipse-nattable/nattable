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
package org.eclipse.nebula.widgets.nattable.grid.data;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DummyColumnHeaderDataProvider implements IDataProvider {

	private final IDataProvider bodyDataProvider;

	public DummyColumnHeaderDataProvider(IDataProvider bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}
	
	public int getColumnCount() {
		return bodyDataProvider.getColumnCount();
	}

	public int getRowCount() {
		return 1;
	}
	
	public Object getDataValue(int columnIndex, int rowIndex) {
		return "Column " + (columnIndex + 1); //$NON-NLS-1$
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

}
