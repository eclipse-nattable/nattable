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
package org.eclipse.nebula.widgets.nattable.data;

import java.util.List;

/**
 * Enables the use of a {@link List} containing POJO(s) as a backing data
 * source.
 *
 * By default a bean at position 'X' in the list is displayed in row 'X' in the
 * table. The properties of the bean are used to populate the columns. A
 * {@link IColumnPropertyResolver} is used to retrieve column data from the bean
 * properties.
 *
 * @param <T>
 *            type of the Objects in the backing list.
 * @see IColumnPropertyResolver
 */
public class ListDataProvider<T> implements IRowDataProvider<T> {

    protected List<T> list;
    protected IColumnAccessor<T> columnAccessor;

    public ListDataProvider(List<T> list, IColumnAccessor<T> columnAccessor) {
        this.list = list;
        this.columnAccessor = columnAccessor;
    }

    @Override
    public int getColumnCount() {
        return this.columnAccessor.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return this.list.size();
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        T rowObj = this.list.get(rowIndex);
        return this.columnAccessor.getDataValue(rowObj, columnIndex);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        T rowObj = this.list.get(rowIndex);
        this.columnAccessor.setDataValue(rowObj, columnIndex, newValue);
    }

    @Override
    public T getRowObject(int rowIndex) {
        return this.list.get(rowIndex);
    }

    @Override
    public int indexOfRowObject(T rowObject) {
        return this.list.indexOf(rowObject);
    }

    public List<T> getList() {
        return this.list;
    }

}
