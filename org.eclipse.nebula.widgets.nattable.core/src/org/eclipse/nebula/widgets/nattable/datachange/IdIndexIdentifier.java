/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

/**
 * Identifier implementation based on column index and row id. The row object
 * itself is also kept inside this class to calculate the current row index
 * again.
 *
 * @param <T>
 *            The type of the row object.
 *
 * @since 1.6
 */
public class IdIndexIdentifier<T> {

    public final int columnIndex;
    public final Object rowId;
    public final T rowObject;

    public IdIndexIdentifier(int columnIndex, Object rowId, T rowObject) {
        this.columnIndex = columnIndex;
        this.rowId = rowId;
        this.rowObject = rowObject;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.columnIndex;
        result = prime * result + ((this.rowId == null) ? 0 : this.rowId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IdIndexIdentifier<?> other = (IdIndexIdentifier<?>) obj;
        if (this.columnIndex != other.columnIndex)
            return false;
        if (this.rowId == null) {
            if (other.rowId != null)
                return false;
        } else if (!this.rowId.equals(other.rowId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IdIndexIdentifier [columnIndex=" + this.columnIndex + ", rowId=" + this.rowId + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}