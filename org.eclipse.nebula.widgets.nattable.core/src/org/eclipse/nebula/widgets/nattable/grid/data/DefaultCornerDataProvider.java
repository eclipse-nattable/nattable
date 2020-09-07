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
package org.eclipse.nebula.widgets.nattable.grid.data;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DefaultCornerDataProvider implements IDataProvider {

    private final IDataProvider columnHeaderDataProvider;
    private final IDataProvider rowHeaderDataProvider;

    public DefaultCornerDataProvider(IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider) {
        this.columnHeaderDataProvider = columnHeaderDataProvider;
        this.rowHeaderDataProvider = rowHeaderDataProvider;
    }

    @Override
    public int getColumnCount() {
        return this.rowHeaderDataProvider.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return this.columnHeaderDataProvider.getRowCount();
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return null;
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
