/*******************************************************************************
 * Copyright (c) 2013, 2023 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Event class that is used to inform about updates to the filter row combo box
 * items.
 */
public class FilterRowComboUpdateEvent {

    class UpdateContent {
        /**
         * The column index of the column for which the filter row combo value
         * cache was updated.
         */
        private final int columnIndex;
        /**
         * The items that was added to the value cache for the set column index.
         */
        private final Collection<?> addedItems;
        /**
         * The items that was removed from the value cache for the set column
         * index.
         */
        private final Collection<?> removedItems;

        UpdateContent(int columnIndex, Collection<?> addedItems, Collection<?> removedItems) {
            this.columnIndex = columnIndex;
            this.addedItems = addedItems;
            this.removedItems = removedItems;
        }
    }

    private ArrayList<UpdateContent> updates = new ArrayList<>();

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
    public FilterRowComboUpdateEvent(int columnIndex, Collection<?> addedItems, Collection<?> removedItems) {
        this.updates.add(new UpdateContent(columnIndex, addedItems, removedItems));
    }

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
     * @since 2.1
     */
    public void addUpdate(int columnIndex, Collection<?> addedItems, Collection<?> removedItems) {
        this.updates.add(new UpdateContent(columnIndex, addedItems, removedItems));
    }

    /**
     * @return The column index of the column for which the filter row combo
     *         value cache was updated.
     */
    public int getColumnIndex() {
        return this.updates.get(0).columnIndex;
    }

    /**
     * @return The items that was added to the value cache for the set column
     *         index.
     */
    public Collection<?> getAddedItems() {
        return this.updates.get(0).addedItems;
    }

    /**
     * @return The items that was removed from the value cache for the set
     *         column index.
     */
    public Collection<?> getRemovedItems() {
        return this.updates.get(0).removedItems;
    }

    /**
     * @param update
     *            The index of the update content transfered by this event.
     * @return The column index of the column for which the filter row combo
     *         value cache was updated.
     * @since 2.1
     */
    public int getColumnIndex(int update) {
        return this.updates.get(update).columnIndex;
    }

    /**
     * @param update
     *            The index of the update content transfered by this event.
     * @return The items that was added to the value cache for the set column
     *         index.
     * @since 2.1
     */
    public Collection<?> getAddedItems(int update) {
        return this.updates.get(update).addedItems;
    }

    /**
     * @param update
     *            The index of the update content transfered by this event.
     * @return The items that was removed from the value cache for the set
     *         column index.
     * @since 2.1
     */
    public Collection<?> getRemovedItems(int update) {
        return this.updates.get(update).removedItems;
    }

    /**
     *
     * @return The number of update contents transfered by this event.
     * @since 2.1
     */
    public int updateContentSize() {
        return this.updates.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("FilterRowComboUpdateEvent [").append(System.lineSeparator()); //$NON-NLS-1$
        for (UpdateContent content : this.updates) {
            builder.append("\tcolumnIndex=").append(content.columnIndex) //$NON-NLS-1$
                    .append(", addedItems=").append(content.addedItems) //$NON-NLS-1$
                    .append(", removedItems=").append(content.removedItems) //$NON-NLS-1$
                    .append(System.lineSeparator());
        }
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }
}