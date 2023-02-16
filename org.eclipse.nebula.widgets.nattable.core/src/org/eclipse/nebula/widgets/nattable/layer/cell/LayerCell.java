/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class LayerCell extends AbstractLayerCell {

    private ILayer layer;

    private int columnPosition;
    private int rowPosition;

    private int originColumnPosition;
    private int originRowPosition;

    private int columnSpan;
    private int rowSpan;

    public LayerCell(ILayer layer, int columnPosition, int rowPosition, DataCell cell) {
        this(layer, cell.columnPosition, cell.rowPosition, columnPosition, rowPosition, cell.columnSpan, cell.rowSpan);
    }

    public LayerCell(ILayer layer, int columnPosition, int rowPosition) {
        this(layer, columnPosition, rowPosition, columnPosition, rowPosition, 1, 1);
    }

    public LayerCell(
            ILayer layer,
            int originColumnPosition,
            int originRowPosition,
            int columnPosition,
            int rowPosition,
            int columnSpan,
            int rowSpan) {

        this.layer = layer;

        this.originColumnPosition = originColumnPosition;
        this.originRowPosition = originRowPosition;

        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;

        this.columnSpan = columnSpan;
        this.rowSpan = rowSpan;
    }

    @Override
    public int getOriginColumnPosition() {
        return this.originColumnPosition;
    }

    @Override
    public int getOriginRowPosition() {
        return this.originRowPosition;
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    @Override
    public int getColumnPosition() {
        return this.columnPosition;
    }

    @Override
    public int getRowPosition() {
        return this.rowPosition;
    }

    @Override
    public int getColumnIndex() {
        if (getLayer() != null) {
            return getLayer().getColumnIndexByPosition(getColumnPosition());
        }
        return getColumnPosition();
    }

    @Override
    public int getRowIndex() {
        if (getLayer() != null) {
            return getLayer().getRowIndexByPosition(getRowPosition());
        }
        return getRowPosition();
    }

    @Override
    public int getColumnSpan() {
        return this.columnSpan;
    }

    @Override
    public int getRowSpan() {
        return this.rowSpan;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LayerCell other = (LayerCell) obj;
        if (this.columnSpan != other.columnSpan)
            return false;
        if (this.layer == null) {
            if (other.layer != null)
                return false;
        } else if (!this.layer.equals(other.layer))
            return false;
        if (this.originColumnPosition != other.originColumnPosition)
            return false;
        if (this.originRowPosition != other.originRowPosition)
            return false;
        if (this.rowSpan != other.rowSpan)
            return false;

        Object thisData = this.getDataValue();
        Object otherData = other.getDataValue();
        if ((thisData != null && otherData == null)
                || (thisData == null && otherData != null)
                || (thisData != null && !thisData.equals(otherData))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.columnSpan;
        result = prime * result + ((this.layer == null) ? 0 : this.layer.hashCode());
        result = prime * result + this.originColumnPosition;
        result = prime * result + this.originRowPosition;
        result = prime * result + this.rowSpan;
        result = prime * result + ((this.getDataValue() == null) ? 0 : this.getDataValue().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "LayerCell: [" //$NON-NLS-1$
                + "Data: " + getDataValue() //$NON-NLS-1$
                + ", layer: " + getLayer().getClass().getSimpleName() //$NON-NLS-1$
                + ", originColumnPosition: " + getOriginColumnPosition() //$NON-NLS-1$
                + ", originRowPosition: " + getOriginRowPosition() //$NON-NLS-1$
                + ", columnSpan: " + getColumnSpan() //$NON-NLS-1$
                + ", rowSpan: " + getRowSpan() //$NON-NLS-1$
                + "]"; //$NON-NLS-1$
    }
}
