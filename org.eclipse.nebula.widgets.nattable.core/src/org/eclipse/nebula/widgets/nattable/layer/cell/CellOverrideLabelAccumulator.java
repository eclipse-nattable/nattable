/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.io.Serializable;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/*
 * Allows application of config labels to cell(s) containing a specified data value.
 * Internally the class generated a 'key' using a combination of the cell value and its column position.
 * The registered labels are tracked using this key.
 *
 * Note: First Map's key is displayMode, inner Map's key is fieldName, the inner Map's value is cellValue
 */
public class CellOverrideLabelAccumulator<T> extends AbstractOverrider {
    private IRowDataProvider<T> dataProvider;

    public CellOverrideLabelAccumulator(IRowDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels,
            int columnPosition, int rowPosition) {
        List<String> cellLabels = getConfigLabels(
                this.dataProvider.getDataValue(columnPosition, rowPosition),
                columnPosition);
        if (cellLabels == null) {
            return;
        }
        for (String configLabel : cellLabels) {
            configLabels.addLabel(configLabel);
        }
    }

    protected List<String> getConfigLabels(Object value, int col) {
        CellValueOverrideKey key = new CellValueOverrideKey(value, col);
        return getOverrides(key);
    }

    /**
     * Register a config label on the cell
     *
     * @param cellValue
     *            data value of the cell. This is the backing data value, not
     *            the display value.
     * @param col
     *            column index of the cell
     * @param configLabel
     *            to apply. Styles for the cell have to be registered against
     *            this label.
     */
    public void registerOverride(Object cellValue, int col, String configLabel) {
        registerOverrides(new CellValueOverrideKey(cellValue, col), configLabel);
    }
}

/**
 * Class used as a key for storing cell labels in an internal map.
 */
class CellValueOverrideKey implements Serializable {
    private static final long serialVersionUID = 1L;
    Object cellValue;
    int col;

    CellValueOverrideKey(Object cellValue, int col) {
        this.cellValue = cellValue;
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CellValueOverrideKey other = (CellValueOverrideKey) obj;
        if (this.cellValue == null) {
            if (other.cellValue != null)
                return false;
        } else if (!this.cellValue.equals(other.cellValue))
            return false;
        if (this.col != other.col)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.cellValue == null) ? 0 : this.cellValue.hashCode());
        result = prime * result + this.col;
        return result;
    }

    public String getComposite() {
        return this.cellValue + String.valueOf(this.col);
    }
}
