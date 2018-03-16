/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

/**
 * This implementation of {@link ILayerCell} is used to support spanning updates
 * in case spanned rows/columns are hidden.
 *
 * @since 1.6
 */
public class SpanningLayerCell extends TransformedLayerCell {

    private int columnSpan;
    private int rowSpan;

    public SpanningLayerCell(ILayerCell cell, int columnSpan, int rowSpan) {
        super(cell);
        this.columnSpan = columnSpan;
        this.rowSpan = rowSpan;
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
    public boolean isSpannedCell() {
        return this.columnSpan > 1 || this.rowSpan > 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + this.columnSpan;
        result = prime * result + this.rowSpan;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpanningLayerCell other = (SpanningLayerCell) obj;
        if (this.columnSpan != other.columnSpan)
            return false;
        if (this.rowSpan != other.rowSpan)
            return false;
        return true;
    }

}
