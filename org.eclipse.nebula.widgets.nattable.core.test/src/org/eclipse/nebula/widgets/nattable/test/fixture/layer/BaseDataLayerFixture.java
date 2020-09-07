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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * A DataLayer for use in unit tests with a pre-canned
 */
public class BaseDataLayerFixture extends DataLayer {

    public BaseDataLayerFixture() {
        this(5, 7);
    }

    public BaseDataLayerFixture(int colCount, int rowCount) {
        setDataProvider(initDataProvider(colCount, rowCount));
        initCellLabelAccumulator();
    }

    private IDataProvider initDataProvider(final int colCount, final int rowCount) {
        return new IDataProvider() {
            Map<String, Object> dataStore = new HashMap<String, Object>();

            @Override
            public int getColumnCount() {
                return colCount;
            }

            @Override
            public int getRowCount() {
                return rowCount;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                String key = "[" + columnIndex + ", " + rowIndex + "]";
                if (this.dataStore.get(key) == null) {
                    return key;
                } else {
                    return this.dataStore.get(key);
                }
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex,
                    Object newValue) {
                this.dataStore.put("[" + columnIndex + ", " + rowIndex + "]",
                        newValue);
            }

        };
    }

    private void initCellLabelAccumulator() {
        setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels,
                    int columnPosition, int rowPosition) {
                configLabels.addLabel("DEFAULT");
            }

        });
    }

}
