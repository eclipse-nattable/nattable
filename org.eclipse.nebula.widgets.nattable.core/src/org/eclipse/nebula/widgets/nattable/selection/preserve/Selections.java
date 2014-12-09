/*******************************************************************************
 * Copyright (c) 2014 Jonas Hugo, Markus Wahl.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 447396
 *     Dirk Fauth <dirk.fauth@googlemail.com> - made inner classes static for better generic handling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * The selected cells of columns and rows
 *
 * @param <T>
 *            the type of object underlying each row
 */
class Selections<T> {

    /**
     * A map for looking up rows given their row IDs
     */
    private Map<Serializable, Row<T>> selectedRows = new HashMap<Serializable, Row<T>>();

    /**
     * A map for looking up columns given their column positions
     */
    private Map<Integer, Column> selectedColumns = new HashMap<Integer, Column>();

    /**
     * Select the cell at the intersection of the specified row and column.
     *
     * @param rowId
     * @param rowObject
     *            row object with the row rowId
     * @param columnPosition
     */
    void select(Serializable rowId, T rowObject, int columnPosition) {
        Row<T> row = retrieveRow(rowId, rowObject);
        row.addItem(columnPosition);

        Column column = retrieveColumn(columnPosition);
        column.addItem(rowId);
    }

    /**
     * Removes the selection of the cell at the intersection of the specified
     * row and column.
     *
     * @param rowId
     * @param columnPosition
     */
    void deselect(Serializable rowId, int columnPosition) {
        Row<T> row = getSelectedColumns(rowId);
        if (row != null) {
            row.removeItem(columnPosition);
            if (!row.hasSelection()) {
                this.selectedRows.remove(rowId);
            }
        }

        Column column = getSelectedRows(columnPosition);
        if (column != null) {
            column.removeItem(rowId);
            if (!column.hasSelection()) {
                this.selectedColumns.remove(columnPosition);
            }
        }
    }

    /**
     * Removes the selection of all cells for the specified row id.
     *
     * @param rowId
     */
    void deselectRow(Serializable rowId) {
        Row<T> row = getSelectedColumns(rowId);
        if (row != null) {
            row.clearItems();
            this.selectedRows.remove(rowId);
        }

        Collection<Integer> toRemove = new HashSet<Integer>();
        for (Map.Entry<Integer, Column> entry : this.selectedColumns.entrySet()) {
            entry.getValue().removeItem(rowId);
            if (!entry.getValue().hasSelection()) {
                toRemove.add(entry.getKey());
            }
        }

        for (Integer key : toRemove) {
            this.selectedColumns.remove(key);
        }
    }

    /**
     * Removes the selection of all cells for the specified column.
     *
     * @param columnPosition
     */
    void deselectColumn(int columnPosition) {
        Column column = getSelectedRows(columnPosition);
        if (column != null) {
            column.clearItems();
            this.selectedColumns.remove(columnPosition);
        }

        Collection<Serializable> toRemove = new HashSet<Serializable>();
        for (Map.Entry<Serializable, Row<T>> entry : this.selectedRows.entrySet()) {
            entry.getValue().removeItem(columnPosition);
            if (!entry.getValue().hasSelection()) {
                toRemove.add(entry.getKey());
            }
        }

        for (Serializable key : toRemove) {
            this.selectedRows.remove(key);
        }
    }

    void updateColumnsForRemoval(int columnPosition) {
        // find maximum selected column
        int maxColumn = 0;
        for (Integer pos : this.selectedColumns.keySet()) {
            maxColumn = Math.max(maxColumn, pos);
        }

        for (int i = columnPosition + 1; i <= maxColumn; i++) {
            Column column = this.selectedColumns.get(i);
            if (column != null) {
                this.selectedColumns.put(i - 1, new Column(i - 1));
                this.selectedColumns.remove(i);

                // also update the row references
                for (Row<T> row : this.selectedRows.values()) {
                    Collection<Integer> toRemove = new HashSet<Integer>();
                    Collection<Integer> toAdd = new HashSet<Integer>();
                    for (Integer col : row.getItems()) {
                        if (col <= i) {
                            toRemove.add(i);
                            toAdd.add(i - 1);
                        }
                    }
                    row.getItems().removeAll(toRemove);
                    row.getItems().addAll(toAdd);
                }
            }
        }
    }

    void updateColumnsForAddition(int columnPosition) {
        // find maximum selected column
        int maxColumn = 0;
        for (Integer pos : this.selectedColumns.keySet()) {
            maxColumn = Math.max(maxColumn, pos);
        }

        for (int i = maxColumn; i >= columnPosition; i--) {
            Column column = this.selectedColumns.get(i);
            if (column != null) {
                this.selectedColumns.put(i + 1, new Column(i + 1));
                this.selectedColumns.remove(i);

                // also update the row references
                for (Row<T> row : this.selectedRows.values()) {
                    Collection<Integer> toRemove = new HashSet<Integer>();
                    Collection<Integer> toAdd = new HashSet<Integer>();
                    for (Integer col : row.getItems()) {
                        if (col >= i) {
                            toRemove.add(i);
                            toAdd.add(i + 1);
                        }
                    }
                    row.getItems().removeAll(toRemove);
                    row.getItems().addAll(toAdd);
                }
            }
        }
    }

    /**
     * Removes all cell selections.
     */
    void clear() {
        this.selectedRows.clear();
        this.selectedColumns.clear();
    }

    /**
     * Retrieves all rows that have selected cells.
     *
     * @return all rows that have selected cells
     */
    Collection<Row<T>> getRows() {
        return this.selectedRows.values();
    }

    /**
     * Retrieves the column positions of all columns with selected cells. The
     * positions are naturally sorted.
     *
     * @return all columns positions with selected cells
     */
    List<Integer> getColumnPositions() {
        List<Integer> keys = new ArrayList<Integer>(this.selectedColumns.keySet());
        Collections.sort(keys);
        return keys;
    }

    /**
     * Retrieves the selected rows of a column
     *
     * @param columnPosition
     *            column for retrieving selected rows
     * @return selected rows of columnPosition, or null if no selected rows in
     *         that column
     */
    Column getSelectedRows(int columnPosition) {
        return this.selectedColumns.get(columnPosition);
    }

    /**
     * Retrieves the selected columns of a row
     *
     * @param rowId
     *            row ID for retrieving selected columns
     * @return selected columns of rowId, or null if no selected columns in that
     *         row
     */
    Row<T> getSelectedColumns(Serializable rowId) {
        return this.selectedRows.get(rowId);
    }

    /**
     * Retrieves all selected cell positions expressed in row object and column
     * position. The size of the collection is zero when there are no selected
     * cells.
     *
     * @return all selected cell positions
     */
    Collection<CellPosition<T>> getSelections() {
        ArrayList<CellPosition<T>> selectedCells = new ArrayList<CellPosition<T>>();
        for (Row<T> row : this.selectedRows.values()) {
            for (Integer columnPosition : row.getItems()) {
                CellPosition<T> cell = new CellPosition<T>(row.getRowObject(), columnPosition);
                selectedCells.add(cell);
            }
        }
        return selectedCells;
    }

    /**
     * Determines whether a cell is selected
     *
     * @param rowId
     *            row ID of the inspected cell
     * @param columnPosition
     *            column position of the inspected cell
     * @return whether the specified cell is selected
     */
    boolean isSelected(Serializable rowId, int columnPosition) {
        if (isRowSelected(rowId)) {
            return getSelectedColumns(rowId).contains(columnPosition);
        } else {
            return false;
        }
    }

    /**
     * Determines whether a row contains a selected cell
     *
     * @param rowId
     *            row ID to inspect
     * @return whether the specified row contains a selected cell
     */
    boolean isRowSelected(Serializable rowId) {
        return this.selectedRows.containsKey(rowId);
    }

    /**
     * Determines whether there are selected cells
     *
     * @return whether there are selected cells
     */
    boolean isEmpty() {
        return this.selectedRows.isEmpty();
    }

    /**
     * Retrieves a collection of selected columns for a row. The row is
     * specified by row ID, but row object is needed when the row does not
     * already have any other selected cells.
     *
     * @param rowId
     *            row to retrieve columns for
     * @param rowObject
     *            row object with the row rowId
     * @return a collection of selected columns for the row
     */
    private Row<T> retrieveRow(Serializable rowId, T rowObject) {
        Row<T> row = getSelectedColumns(rowId);
        if (row == null) {
            row = new Row<T>(rowId, rowObject);
            this.selectedRows.put(rowId, row);
        }
        return row;
    }

    /**
     * Retrieves a collection of selected rows for a column.
     *
     * @param columnPosition
     *            column to retrieve columns for
     * @return a collection of selected rows for a column
     */
    private Column retrieveColumn(int columnPosition) {
        Column column = getSelectedRows(columnPosition);
        if (column == null) {
            column = new Column(columnPosition);
            this.selectedColumns.put(columnPosition, column);
        }
        return column;
    }

    /**
     * A collection of selected columns for a row. Row is a Line<Serializable,
     * Integer> where <Serializable> denotes the row ID of the row and <Integer>
     * denotes the type of the selected columns (column positions).
     */
    static class Row<R> extends Line<Serializable, Integer> {
        /**
         * The underlying row object
         */
        private final R rowObject;

        /**
         * Creates a row with the specified row
         *
         * @param rowId
         *            ID of the row
         * @param rowObject
         *            underlying row object
         */
        Row(Serializable rowId, R rowObject) {
            super(rowId);
            this.rowObject = rowObject;
        }

        /**
         * Retrieves the underlying row object
         *
         * @return the underlying row object
         */
        R getRowObject() {
            return this.rowObject;
        }
    }

    /**
     * A collection of selected rows for a column. Column is a Line<Integer,
     * Serializable> where <Integer> denotes the column position of the column
     * and <Serializable> denotes the type of the selected rows (row ID).
     */
    static class Column extends Line<Integer, Serializable> {
        /**
         * Creates a column with the specified column
         *
         * @param columnPosition
         *            position of the column
         */
        Column(Integer columnPosition) {
            super(columnPosition);
        }
    }

    /**
     * A collection of selected items in a line.
     *
     * @param <I>
     *            the type of the line identifier
     * @param <S>
     *            the type of the selected items
     */
    static class Line<I, S> {
        /**
         * Identifying the line
         */
        private final I lineId;

        /**
         * The selected items
         */
        private HashSet<S> content = new HashSet<S>();

        /**
         * Creates a line with the specified ID
         *
         * @param lineId
         *            identifying the line
         */
        Line(I lineId) {
            this.lineId = lineId;
        }

        /**
         * Retrieves the selected items of the line
         *
         * @return the selected items
         */
        Collection<S> getItems() {
            return this.content;
        }

        /**
         * Adds an item to the selected items
         *
         * @param item
         *            item to add
         */
        void addItem(S item) {
            this.content.add(item);
        }

        /**
         * Removes an item from the selected items
         *
         * @param item
         *            item to remove
         */
        void removeItem(S item) {
            this.content.remove(item);
        }

        /**
         * Clears the selected items.
         */
        void clearItems() {
            this.content.clear();
        }

        /**
         * Determines whether a certain item is selected in the line
         *
         * @param item
         *            item to look for
         * @return whether the item is selected in the line
         */
        boolean contains(S item) {
            return this.content.contains(item);
        }

        /**
         * Determines whether the line has any selections
         *
         * @return whether the line has any selections
         */
        boolean hasSelection() {
            return !this.content.isEmpty();
        }

        /**
         * Retrieves the line identifier
         *
         * @return the line identifier
         */
        I getId() {
            return this.lineId;
        }
    }

    /**
     * Position of a cell expressed in row object and column position
     *
     * @param <T>
     *            the type of row object
     */
    static class CellPosition<T> {
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.columnPosition;
            result = prime * result
                    + ((this.rowObject == null) ? 0 : this.rowObject.hashCode());
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
            @SuppressWarnings("unchecked")
            CellPosition<T> other = (CellPosition<T>) obj;
            if (this.columnPosition != other.columnPosition)
                return false;
            if (this.rowObject == null) {
                if (other.rowObject != null)
                    return false;
            } else if (!this.rowObject.equals(other.rowObject))
                return false;
            return true;
        }

        /**
         * Column position
         */
        private final int columnPosition;

        /**
         * Row object
         */
        private final T rowObject;

        /**
         * Creates a cell position
         *
         * @param rowObject
         *            row object
         * @param columnPosition
         *            column position
         */
        CellPosition(T rowObject, int columnPosition) {
            this.rowObject = rowObject;
            this.columnPosition = columnPosition;
        }

        /**
         * Retrieves the row object
         *
         * @return the row object
         */
        T getRowObject() {
            return this.rowObject;
        }

        /**
         * Retrieves the column position
         *
         * @return the column position
         */
        Integer getColumnPosition() {
            return this.columnPosition;
        }
    }

}
