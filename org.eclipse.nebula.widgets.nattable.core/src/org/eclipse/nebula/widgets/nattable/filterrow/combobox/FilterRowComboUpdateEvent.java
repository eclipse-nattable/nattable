/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.Collection;

/**
 * Event class that is used to inform about updates to the filter row combo box
 * items.
 *
 * @author Dirk Fauth
 *
 */
public class FilterRowComboUpdateEvent {

    /**
     * The column index of the column for which the filter row combo value cache
     * was updated.
     */
    private final int columnIndex;
    /**
     * The items that was added to the value cache for the set column index.
     */
    private final Collection<?> addedItems;
    /**
     * The items that was removed from the value cache for the set column index.
     */
    private final Collection<?> removedItems;

    /**
     *
     * @param columnIndex
     *            The column index of the column for which the filter row combo
     *            value cache was updated.
     * @param addedItems
     *            The items that was added to the value cache for the set column
     *            index.
     * @param removedItems
     *            The items that was removed from the value cache for the set
     *            column index.
     */
    public FilterRowComboUpdateEvent(int columnIndex, Collection<?> addedItems,
            Collection<?> removedItems) {
        this.columnIndex = columnIndex;
        this.addedItems = addedItems;
        this.removedItems = removedItems;
    }

    /**
     * @return The column index of the column for which the filter row combo
     *         value cache was updated.
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * @return The items that was added to the value cache for the set column
     *         index.
     */
    public Collection<?> getAddedItems() {
        return this.addedItems;
    }

    /**
     * @return The items that was removed from the value cache for the set
     *         column index.
     */
    public Collection<?> getRemovedItems() {
        return this.removedItems;
    }

}
