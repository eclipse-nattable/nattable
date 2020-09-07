/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     *            The column index of the cell whose value is requested.
     * @param rowIndex
     *            The row index of the cell whose value is requested.
     * @return The data value associated with the specified cell coordintates.
     */
    public Object getDataValue(int columnIndex, int rowIndex);

    /**
     * Sets the value at the given column and row index. Optional operation.
     * Should throw UnsupportedOperationException if this operation is not
     * supported.
     *
     * @param columnIndex
     *            The column index of the cell whose value should be changed.
     * @param rowIndex
     *            The row index of the cell whose value should be changed.
     * @param newValue
     *            The new value that should be set.
     */
    public void setDataValue(int columnIndex, int rowIndex, Object newValue);

    /**
     *
     * @return The number of columns this {@link IDataProvider} handles.
     */
    public int getColumnCount();

    /**
     *
     * @return The number of rows this {@link IDataProvider} handles.
     */
    public int getRowCount();

}
