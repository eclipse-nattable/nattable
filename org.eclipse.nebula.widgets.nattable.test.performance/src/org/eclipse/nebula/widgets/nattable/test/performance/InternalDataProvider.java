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
package org.eclipse.nebula.widgets.nattable.test.performance;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class InternalDataProvider implements IDataProvider {
    private Object[][] data;
    private int numRows;
    private int numCols;

    // public InternalDataProvider(TableDataProvider tableData) {
    // this(tableData.getData(), tableData.getColumnCount(),
    // tableData.getRowCount());
    // }

    public InternalDataProvider(Object[][] data, int numCols, int numRows) {
        this.data = data;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    @Override
    public int getColumnCount() {
        return this.numCols;
    }

    @Override
    public int getRowCount() {
        return this.numRows;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.data[columnIndex][rowIndex];
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
