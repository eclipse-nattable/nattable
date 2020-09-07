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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * A DataLayer for use in unit tests with a pre-canned
 *
 * Default column layout: DO NOT FORMAT !
 *
 * Position: 0 1 2 3 4 Width : 150 100 35 100 80 -------|------|----|------|----
 *
 * Default Row layout: Position | Height 0 | 40 1 | 70 2 | 25 3 | 40 4 | 50 5 |
 * 40 6 | 100
 */
public class DataLayerFixture extends BaseDataLayerFixture {

    public DataLayerFixture(int colCount, int rowCount, int colWidth,
            int rowHeight) {
        super(colCount, rowCount);
        setDefaultColumnWidth(colWidth);
        setDefaultRowHeight(rowHeight);
    }

    public DataLayerFixture(int preferedColumnWidth, int preferedRowHeight) {
        setDefaultColumnWidth(preferedColumnWidth);
        setDefaultRowHeight(preferedRowHeight);
    }

    public DataLayerFixture() {
        setDefaultColumnWidth(100);
        setColumnWidthByPosition(0, 150);
        setColumnWidthByPosition(2, 35);
        setColumnWidthByPosition(4, 80);

        setDefaultRowHeight(40);
        setRowHeightByPosition(1, 70);
        setRowHeightByPosition(2, 25);
        setRowHeightByPosition(4, 50);
        setRowHeightByPosition(6, 100);
    }

    @Override
    public IDataProvider getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public String toString() {
        return "DataLayerFixture";
    }
}
