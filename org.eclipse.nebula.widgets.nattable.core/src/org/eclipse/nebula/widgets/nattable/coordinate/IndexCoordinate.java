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

public final class IndexCoordinate {

    public final int columnIndex;

    public final int rowIndex;

    public IndexCoordinate(int columnIndex, int rowIndex) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    public int getRowIndex() {
        return this.rowIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IndexCoordinate other = (IndexCoordinate) obj;
        if (this.columnIndex != other.columnIndex)
            return false;
        if (this.rowIndex != other.rowIndex)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.columnIndex;
        result = prime * result + this.rowIndex;
        return result;
    }

    @Override
    public String toString() {
        return "IndexCoordinate [columnIndex=" + this.columnIndex + ", rowIndex=" + this.rowIndex + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
