/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class TransformedLayerCell extends AbstractLayerCell {

    private ILayerCell cell;

    public TransformedLayerCell(ILayerCell cell) {
        this.cell = cell;
    }

    @Override
    public int getOriginColumnPosition() {
        return this.cell.getOriginColumnPosition();
    }

    @Override
    public int getOriginRowPosition() {
        return this.cell.getOriginRowPosition();
    }

    @Override
    public ILayer getLayer() {
        return this.cell.getLayer();
    }

    @Override
    public int getColumnPosition() {
        return this.cell.getColumnPosition();
    }

    @Override
    public int getRowPosition() {
        return this.cell.getRowPosition();
    }

    @Override
    public int getColumnIndex() {
        return this.cell.getColumnIndex();
    }

    @Override
    public int getRowIndex() {
        return this.cell.getRowIndex();
    }

    @Override
    public int getColumnSpan() {
        return this.cell.getColumnSpan();
    }

    @Override
    public int getRowSpan() {
        return this.cell.getRowSpan();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TransformedLayerCell other = (TransformedLayerCell) obj;
        if (this.cell == null) {
            if (other.cell != null)
                return false;
        } else if (!this.cell.equals(other.cell))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.cell == null) ? 0 : this.cell.hashCode());
        return result;
    }

}
