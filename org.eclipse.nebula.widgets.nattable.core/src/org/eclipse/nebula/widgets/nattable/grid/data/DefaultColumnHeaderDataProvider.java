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

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class DefaultColumnHeaderDataProvider implements IDataProvider {

    private final String[] propertyNames;

    private Map<String, String> propertyToLabelMap;

    public DefaultColumnHeaderDataProvider(final String[] columnLabels) {
        this.propertyNames = columnLabels;
    }

    public DefaultColumnHeaderDataProvider(final String[] propertyNames,
            Map<String, String> propertyToLabelMap) {
        this.propertyNames = propertyNames;
        this.propertyToLabelMap = propertyToLabelMap;
    }

    public String getColumnHeaderLabel(int columnIndex) {
        String propertyName = this.propertyNames[columnIndex];
        if (this.propertyToLabelMap != null) {
            String label = this.propertyToLabelMap.get(propertyName);
            if (label != null) {
                return label;
            }
        }
        return propertyName;
    }

    @Override
    public int getColumnCount() {
        return this.propertyNames.length;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    /**
     * This class does not support multiple rows in the column header layer.
     */
    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return getColumnHeaderLabel(columnIndex);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
