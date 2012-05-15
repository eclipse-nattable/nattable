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
package org.eclipse.nebula.widgets.nattable.data;

import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

/**
 * Provide data to the table.
 *
 * @see DataLayer
 * @see ListDataProvider
 */
public interface IDataProvider {

	/**
	 * Gets the value at the given column and row index.
	 *
	 * @param columnIndex
	 * @param rowIndex
	 * @return the data value associated with the specified cell
	 */
	public Object getDataValue(int columnIndex, int rowIndex);

	/**
	 * Sets the value at the given column and row index. Optional operation. Should throw UnsupportedOperationException
	 * if this operation is not supported.
	 *
	 * @param columnIndex
	 * @param rowIndex
	 * @param newValue
	 */
	public void setDataValue(int columnIndex, int rowIndex, Object newValue);

	public int getColumnCount();

	public int getRowCount();

}
