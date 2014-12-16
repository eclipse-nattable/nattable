/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 448115, 449361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;

/**
 * {@link IColumnAccessor} that wraps an instance of {@link IColumnAccessor} and
 * adds the ability to handle {@link GroupByObject}s additionally to the
 * underlying data structure. Necessary to be able to introduce groupBy
 * structures.
 *
 * @param <T>
 *            The type of the underlying data model
 */
public class GroupByColumnAccessor<T> implements IColumnAccessor<Object> {

    protected final IColumnAccessor<T> columnAccessor;

    public GroupByColumnAccessor(IColumnAccessor<T> columnAccessor) {
        this.columnAccessor = columnAccessor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getDataValue(Object rowObject, int columnIndex) {
        if (rowObject instanceof GroupByObject) {
            // we return the GroupByObject for every column because we are
            // called by the bottom most layer, therefore we don't know which
            // column is currently treated as the tree column
            // the GroupByDisplayConverter is used to determine
            // whether the groupBy value, the groupBy summary value or nothing
            // is rendered
            return rowObject;
        } else {
            return this.columnAccessor.getDataValue((T) rowObject, columnIndex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setDataValue(Object rowObject, int columnIndex, Object newValue) {
        if (rowObject instanceof GroupByObject) {
            // do nothing
        } else {
            this.columnAccessor.setDataValue((T) rowObject, columnIndex, newValue);
        }
    }

    @Override
    public int getColumnCount() {
        return this.columnAccessor.getColumnCount();
    }
}
