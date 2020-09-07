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
package org.eclipse.nebula.widgets.nattable.dataset.generator;

public class TableDataProvider {

    private Object[][] data;
    private int numRows;
    private int numCols;

    public TableDataProvider(Object[][] data, int numCols, int numRows) {
        this.data = data;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public int getColumnCount() {
        return this.numCols;
    }

    public int getRowCount() {
        return this.numRows;
    }

    public Object[][] getData() {
        return this.data;
    }
}
