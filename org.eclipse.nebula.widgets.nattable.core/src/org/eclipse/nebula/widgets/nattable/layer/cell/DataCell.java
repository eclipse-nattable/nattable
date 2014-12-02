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
package org.eclipse.nebula.widgets.nattable.layer.cell;

public class DataCell {

    protected int columnPosition;

    protected int rowPosition;

    protected int columnSpan;

    protected int rowSpan;

    public DataCell(int columnPosition, int rowPosition) {
        this(columnPosition, rowPosition, 1, 1);
    }

    public DataCell(int columnPosition, int rowPosition, int columnSpan,
            int rowSpan) {
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
        this.columnSpan = columnSpan;
        this.rowSpan = rowSpan;
    }

    public int getColumnPosition() {
        return this.columnPosition;
    }

    public int getRowPosition() {
        return this.rowPosition;
    }

    public int getColumnSpan() {
        return this.columnSpan;
    }

    public int getRowSpan() {
        return this.rowSpan;
    }

    public boolean isSpannedCell() {
        return this.columnSpan > 1 || this.rowSpan > 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataCell other = (DataCell) obj;
        if (this.columnPosition != other.columnPosition)
            return false;
        if (this.columnSpan != other.columnSpan)
            return false;
        if (this.rowPosition != other.rowPosition)
            return false;
        if (this.rowSpan != other.rowSpan)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.columnPosition;
        result = prime * result + this.columnSpan;
        result = prime * result + this.rowPosition;
        result = prime * result + this.rowSpan;
        return result;
    }

}
