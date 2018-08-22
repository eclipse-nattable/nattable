/*******************************************************************************
 * Copyright (c) 2017, 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import java.lang.reflect.Method;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;

/**
 * Implementation of {@link CellKeyHandler} that uses {@link IdIndexIdentifier}
 * as key object. Operates via {@link IRowDataProvider} and
 * {@link IRowIdAccessor} to find the row index even in sorted or filtered
 * collections again.
 *
 * @param <T>
 *            The type of the objects handled inside the NatTable.
 *
 * @since 1.6
 */
public class IdIndexKeyHandler<T> implements CellKeyHandler<IdIndexIdentifier<T>> {

    private final IRowDataProvider<T> rowDataProvider;
    private final IRowIdAccessor<T> rowIdAccessor;

    private Class<?> clazz;

    public IdIndexKeyHandler(IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor) {
        this.rowDataProvider = rowDataProvider;
        this.rowIdAccessor = rowIdAccessor;

        // check the concrete parameter type of the IRowIdAccessor
        for (Method method : this.rowIdAccessor.getClass().getDeclaredMethods()) {
            for (Class<?> class1 : method.getParameterTypes()) {
                if (class1 != Object.class) {
                    this.clazz = class1;
                }
            }
        }
    }

    @Override
    public IdIndexIdentifier<T> getKey(int columnIndex, int rowIndex) {
        T rowObject = this.rowDataProvider.getRowObject(rowIndex);
        // only generate the key via row ID if the row object is of the type of
        // the IRowIdAccessor
        // Needed for example in case of GroupBy where GroupByObjects are added
        // to the underlying list
        if (rowObject.getClass().equals(this.clazz)) {
            Object rowId = this.rowIdAccessor.getRowId(rowObject);
            return new IdIndexIdentifier<T>(columnIndex, rowId, rowObject);
        }
        return null;
    }

    @Override
    public IdIndexIdentifier<T> getKeyWithColumnUpdate(IdIndexIdentifier<T> oldKey, int columnIndex) {
        return new IdIndexIdentifier<T>(columnIndex, oldKey.rowId, oldKey.rowObject);
    }

    @Override
    public IdIndexIdentifier<T> getKeyWithRowUpdate(IdIndexIdentifier<T> oldKey, int rowIndex) {
        return getKey(oldKey.columnIndex, rowIndex);
    }

    @Override
    public int getColumnIndex(IdIndexIdentifier<T> key) {
        return key.columnIndex;
    }

    @Override
    public int getRowIndex(IdIndexIdentifier<T> key) {
        return this.rowDataProvider.indexOfRowObject(key.rowObject);
    }

    @Override
    public boolean updateOnHorizontalStructuralChange() {
        // return true because the column is identified by index
        return true;
    }

    @Override
    public boolean updateOnVerticalStructuralChange() {
        // return false because the row is identified via row id
        return false;
    }

}
