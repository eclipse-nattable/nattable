/*******************************************************************************
 * Copyright (c) 2017, 2022 Dirk Fauth and others.
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

import java.lang.reflect.Method;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(IdIndexKeyHandler.class);

    private final IRowDataProvider<T> rowDataProvider;
    private final IRowIdAccessor<T> rowIdAccessor;

    private Class<?> clazz;

    /**
     * Creates an {@link IdIndexKeyHandler} that identifies the type to handle
     * via reflection. If the reflection fails or produces strange results, try
     * to pass the type via
     * {@link IdIndexKeyHandler#IdIndexKeyHandler(IRowDataProvider, IRowIdAccessor, Class)}
     *
     * @param rowDataProvider
     *            The {@link IRowDataProvider} needed to retrieve the modified
     *            row object.
     * @param rowIdAccessor
     *            The {@link IRowIdAccessor} needed to retrieve the row object
     *            id.
     */
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

        if (this.clazz == null && rowDataProvider.getRowCount() > 0) {
            // type could not be retrieved from the IRowIdAccessor
            // maybe it is a lambda or method reference
            // try to get the type from the collection
            this.clazz = rowDataProvider.getRowObject(0).getClass();
        }

        if (this.clazz == null) {
            LOG.error("row object type could not be retrieved via reflection, use constructor with type parameter!"); //$NON-NLS-1$
        }
    }

    /**
     *
     * @param rowDataProvider
     *            The {@link IRowDataProvider} needed to retrieve the modified
     *            row object.
     * @param rowIdAccessor
     *            The {@link IRowIdAccessor} needed to retrieve the row object
     *            id.
     * @param type
     *            The type of objects handled by the IRowDataProvider and the
     *            IRowIdAcccessor.
     * @since 2.1
     */
    public IdIndexKeyHandler(IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor, Class<T> type) {
        this.rowDataProvider = rowDataProvider;
        this.rowIdAccessor = rowIdAccessor;
        this.clazz = type;
    }

    @Override
    public IdIndexIdentifier<T> getKey(int columnIndex, int rowIndex) {
        if (rowIndex >= 0 && rowIndex < this.rowDataProvider.getRowCount()) {
            T rowObject = this.rowDataProvider.getRowObject(rowIndex);
            // only generate the key via row ID if the row object is of the type
            // of the IRowIdAccessor
            // Needed for example in case of GroupBy where GroupByObjects are
            // added to the underlying list
            if (rowObject.getClass().equals(this.clazz)) {
                Object rowId = this.rowIdAccessor.getRowId(rowObject);
                return new IdIndexIdentifier<>(columnIndex, rowId, rowObject);
            }
        }
        return null;
    }

    @Override
    public IdIndexIdentifier<T> getKeyWithColumnUpdate(IdIndexIdentifier<T> oldKey, int columnIndex) {
        return new IdIndexIdentifier<>(columnIndex, oldKey.rowId, oldKey.rowObject);
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
