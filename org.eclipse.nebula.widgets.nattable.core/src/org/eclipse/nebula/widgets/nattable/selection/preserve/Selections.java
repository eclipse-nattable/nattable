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
    private Map<Serializable, Row> selectedRows = new HashMap<Serializable, Row>();

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
        Row row = retrieveRow(rowId, rowObject);
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
        Row row = getSelectedColumns(rowId);
        if (row != null) {
            row.removeItem(columnPosition);
            if (!row.hasSelection()) {
                selectedRows.remove(rowId);
            }
        }

        Column column = getSelectedRows(columnPosition);
        if (column != null) {
            column.removeItem(rowId);
            if (!column.hasSelection()) {
                selectedColumns.remove(columnPosition);
            }
        }
    }

    /**
     * Removes all cell selections.
     */
    void clear() {
        selectedRows.clear();
        selectedColumns.clear();
    }

    /**
     * Retrieves all rows that have selected cells.
     * 
     * @return all rows that have selected cells
     */
    Collection<Row> getRows() {
        return selectedRows.values();
    }

    /**
     * Retrieves the column positions of all columns with selected cells. The
     * positions are naturally sorted.
     * 
     * @return all columns positions with selected cells
     */
    List<Integer> getColumnPositions() {
        List<Integer> keys = new ArrayList<Integer>(selectedColumns.keySet());
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
        return selectedColumns.get(columnPosition);
    }

    /**
     * Retrieves the selected columns of a row
     * 
     * @param rowId
     *            row ID for retrieving selected columns
     * @return selected columns of rowId, or null if no selected columns in that
     *         row
     */
    Row getSelectedColumns(Serializable rowId) {
        return selectedRows.get(rowId);
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
        for (Row row : selectedRows.values()) {
            for (Integer columnPosition : row.getItems()) {
                CellPosition<T> cell = new CellPosition<T>(row.getRowObject(),
                        columnPosition);
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
        return selectedRows.containsKey(rowId);
    }

    /**
     * Determines whether there are selected cells
     * 
     * @return whether there are selected cells
     */
    boolean isEmpty() {
        return selectedRows.isEmpty();
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
    private Row retrieveRow(Serializable rowId, T rowObject) {
        Row row = getSelectedColumns(rowId);
        if (row == null) {
            row = new Row(rowId, rowObject);
            selectedRows.put(rowId, row);
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
            selectedColumns.put(columnPosition, column);
        }
        return column;
    }

    /**
     * A collection of selected columns for a row. Row is a Line<Serializable,
     * Integer> where <Serializable> denotes the row ID of the row and <Integer>
     * denotes the type of the selected columns (column positions).
     */
    class Row extends Line<Serializable, Integer> {
        /**
         * The underlying row object
         */
        private final T rowObject;

        /**
         * Creates a row with the specified row
         * 
         * @param rowId
         *            ID of the row
         * @param rowObject
         *            underlying row object
         */
        Row(Serializable rowId, T rowObject) {
            super(rowId);
            this.rowObject = rowObject;
        }

        /**
         * Retrieves the underlying row object
         * 
         * @return the underlying row object
         */
        T getRowObject() {
            return rowObject;
        }
    }

    /**
     * A collection of selected rows for a column. Column is a Line<Integer,
     * Serializable> where <Integer> denotes the column position of the column
     * and <Serializable> denotes the type of the selected rows (row ID).
     */
    class Column extends Line<Integer, Serializable> {
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
            return content;
        }

        /**
         * Adds an item to the selected items
         * 
         * @param item
         *            item to add
         */
        void addItem(S item) {
            content.add(item);
        }

        /**
         * Removes an item from the selected items
         * 
         * @param item
         *            item to remove
         */
        void removeItem(S item) {
            content.remove(item);
        }

        /**
         * Determines whether a certain item is selected in the line
         * 
         * @param item
         *            item to look for
         * @return whether the item is selected in the line
         */
        boolean contains(S item) {
            return content.contains(item);
        }

        /**
         * Determines whether the line has any selections
         * 
         * @return whether the line has any selections
         */
        boolean hasSelection() {
            return !content.isEmpty();
        }

        /**
         * Retrieves the line identifier
         * 
         * @return the line identifier
         */
        I getId() {
            return lineId;
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
            result = prime * result + columnPosition;
            result = prime * result
                    + ((rowObject == null) ? 0 : rowObject.hashCode());
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
            if (columnPosition != other.columnPosition)
                return false;
            if (rowObject == null) {
                if (other.rowObject != null)
                    return false;
            } else if (!rowObject.equals(other.rowObject))
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
            return rowObject;
        }

        /**
         * Retrieves the column position
         * 
         * @return the column position
         */
        Integer getColumnPosition() {
            return columnPosition;
        }
    }

}
