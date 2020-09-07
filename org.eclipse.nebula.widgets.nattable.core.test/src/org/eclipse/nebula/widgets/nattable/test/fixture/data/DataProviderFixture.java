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
package org.eclipse.nebula.widgets.nattable.test.fixture.data;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DataProviderFixture implements IDataProvider {

    private final int colCount;
    private final int rowCount;

    public DataProviderFixture(int colCount, int rowCount) {
        this.colCount = colCount;
        this.rowCount = rowCount;
    }

    @Override
    public int getColumnCount() {
        return this.colCount;
    }

    @Override
    public int getRowCount() {
        return this.rowCount;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return "[" + columnIndex + "," + rowIndex + "]";
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
