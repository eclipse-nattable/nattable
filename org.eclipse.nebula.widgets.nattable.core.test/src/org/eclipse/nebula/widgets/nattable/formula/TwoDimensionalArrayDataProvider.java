/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class TwoDimensionalArrayDataProvider implements IDataProvider {

    private Object[][] data;

    public TwoDimensionalArrayDataProvider(Object[][] data) {
        this.data = data;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.data[columnIndex][rowIndex];
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        this.data[columnIndex][rowIndex] = newValue;
    }

    @Override
    public int getColumnCount() {
        return this.data.length;
    }

    @Override
    public int getRowCount() {
        return this.data[0] != null ? this.data[0].length : 0;
    }

}