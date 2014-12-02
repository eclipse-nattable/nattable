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
 * By implementing filter logic within
 * {@link AbstractFilterListDataProvider#show(Object)} it is possible to create
 * a static filter. All data access methods will skip invisible items within the
 * wrapped list and delegate access to the visible items.
 *
 * NOTE: This way of static filtering can cause performance issues for huge data
 * sets where a lot of items are filtered, because data access will always
 * calculate the visible row position. Trying to use a caching mechanism would
 * create some issues for deleting or inserting new data to the wrapped list.
 *
 * TODO add caching that reacts on insert/delete actions on the wrapped list
 *
 * @param <T>
 *            type of the Objects in the backing list.
 * @see IColumnPropertyResolver
 */
public abstract class AbstractFilterListDataProvider<T> extends
        ListDataProvider<T> {

    public AbstractFilterListDataProvider(List<T> list,
            IColumnAccessor<T> columnAccessor) {
        super(list, columnAccessor);
    }

    /**
     * Iterates over the whole list of data objects and checks the visibility
     * for every object. The number of non visible items will be subtracted from
     * the size of the wrapped list to return the number of visible items.
     */
    @Override
    public int getRowCount() {
        int numberOfInvisible = 0;
        for (T object : this.list) {
            if (!show(object)) {
                numberOfInvisible++;
            }
        }
        return this.list.size() - numberOfInvisible;
    }

    /**
     * Get the data value for the columnIndex and the visible rowIndex.
     */
    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        T rowObj = getRowObject(rowIndex);
        return this.columnAccessor.getDataValue(rowObj, columnIndex);
    }

    /**
     * Set the data value for the columnIndex and the visible rowIndex.
     */
    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        T rowObj = getRowObject(rowIndex);
        this.columnAccessor.setDataValue(rowObj, columnIndex, newValue);
    }

    /**
     * Returns the object for the visible rowIndex. To do this it is iterated
     * over the wrapped list, taking the invisible items into account, so the
     * real row index for the given visible row index is calculated.
     */
    @Override
    public T getRowObject(int rowIndex) {
        T object = null;
        int count = 0;
        int realRowIndex = 0;
        while (count <= rowIndex) {
            object = this.list.get(realRowIndex);
            if (show(object)) {
                count++;
            }
            realRowIndex++;
        }

        return object;
    }

    /**
     * Returns the visible rowIndex for the given object. To do this the real
     * row index for the object within the wrapped list is searched and then all
     * invisible items are subtracted from the real row index to calculate the
     * visible row index.
     */
    @Override
    public int indexOfRowObject(T rowObject) {
        int realRowIndex = this.list.indexOf(rowObject);
        int filteredIndex = realRowIndex;
        // now find number of not visible items
        T vf = null;
        for (int i = 0; i <= realRowIndex; i++) {
            vf = this.list.get(i);
            if (!show(vf)) {
                filteredIndex--;
            }
        }
        return filteredIndex;
    }

    /**
     * Within this method the filter logic should be applied. Return false if
     * the object should not be visible within the grid. Return true if it
     * should be visible.
     *
     * @param object
     *            The object that should be checked.
     * @return true if the object should be visible, false if not
     */
    protected abstract boolean show(T object);
}
