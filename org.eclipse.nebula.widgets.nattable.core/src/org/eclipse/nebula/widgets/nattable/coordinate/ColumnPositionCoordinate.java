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
package org.eclipse.nebula.widgets.nattable.coordinate;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public final class ColumnPositionCoordinate {

    private ILayer layer;

    public int columnPosition;

    public ColumnPositionCoordinate(ILayer layer, int columnPosition) {
        this.layer = layer;
        this.columnPosition = columnPosition;
    }

    public ILayer getLayer() {
        return this.layer;
    }

    public int getColumnPosition() {
        return this.columnPosition;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[" + this.layer + ":" + this.columnPosition + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColumnPositionCoordinate other = (ColumnPositionCoordinate) obj;
        if (this.columnPosition != other.columnPosition)
            return false;
        if (this.layer == null) {
            if (other.layer != null)
                return false;
        } else if (!this.layer.equals(other.layer))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.columnPosition;
        result = prime * result + ((this.layer == null) ? 0 : this.layer.hashCode());
        return result;
    }

}
