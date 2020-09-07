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

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 * The default {@link IDataProvider} for the column header. Returns the data
 * value from the provided static data.
 */
public class DefaultColumnHeaderDataProvider implements IDataProvider {

    private final String[] propertyNames;

    private Map<String, String> propertyToLabelMap;

    /**
     * @param columnLabels
     *            The labels that should be shown in the column header.
     */
    public DefaultColumnHeaderDataProvider(final String[] columnLabels) {
        this.propertyNames = columnLabels;
    }

    /**
     *
     * @param propertyNames
     *            The property names/keys that are also used to access the row
     *            objects via reflection.
     * @param propertyToLabelMap
     *            The mapping between property name/key to the value that should
     *            be shown in the column header.
     */
    public DefaultColumnHeaderDataProvider(final String[] propertyNames, Map<String, String> propertyToLabelMap) {
        this.propertyNames = propertyNames;
        this.propertyToLabelMap = propertyToLabelMap;
    }

    /**
     *
     * @param columnIndex
     *            The column index for which the column header label is
     *            requested.
     * @return The column header label for the given column index.
     */
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

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        if (columnIndex < 0 || columnIndex >= this.propertyNames.length) {
            return null;
        }
        return getColumnHeaderLabel(columnIndex);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
