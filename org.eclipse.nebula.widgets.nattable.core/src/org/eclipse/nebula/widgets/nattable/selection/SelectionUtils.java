/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * Helper class to operate with selections.
 */
public class SelectionUtils {

    /**
     *
     * @param withShiftMask
     *            flag to indicate whether the shift masked is active
     * @param withControlMask
     *            flag to indicate whether the control masked is active
     * @return <code>true</code> if both, shift and control mask, are not active
     */
    public static boolean noShiftOrControl(boolean withShiftMask, boolean withControlMask) {
        return !withShiftMask && !withControlMask;
    }

    /**
     *
     * @param withShiftMask
     *            flag to indicate whether the shift masked is active
     * @param withControlMask
     *            flag to indicate whether the control masked is active
     * @return <code>true</code> if both, shift and control mask are active
     */
    public static boolean bothShiftAndControl(boolean withShiftMask, boolean withControlMask) {
        return withShiftMask && withControlMask;
    }

    /**
     *
     * @param withShiftMask
     *            flag to indicate whether the shift masked is active
     * @param withControlMask
     *            flag to indicate whether the control masked is active
     * @return <code>true</code> if only the control mask is active
     */
    public static boolean isControlOnly(boolean withShiftMask, boolean withControlMask) {
        return !withShiftMask && withControlMask;
    }

    /**
     *
     * @param withShiftMask
     *            flag to indicate whether the shift masked is active
     * @param withControlMask
     *            flag to indicate whether the control masked is active
     * @return <code>true</code> if only the shift mask is active
     */
    public static boolean isShiftOnly(boolean withShiftMask, boolean withControlMask) {
        return withShiftMask && !withControlMask;
    }

    /**
     * Test if the numbers in the given array are consecutive. If there are
     * duplicates or gaps in the array, <code>false</code> will be returned.
     *
     * @param pos
     *            the array of numbers to check
     * @return <code>true</code> if the numbers are consecutive,
     *         <code>false</code> if there are duplicates or gaps.
     *
     * @since 1.4
     */
    public static boolean isConsecutive(int[] pos) {
        for (int i = 1; i < pos.length; i++) {
            if (pos[i - 1] + 1 != pos[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the selected region tracked by the given
     * {@link SelectionLayer} is consecutive or not.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} that tracks the selection.
     * @return <code>true</code> if the current selection is consecutive,
     *         <code>false</code> if not.
     *
     * @since 1.4
     */
    public static boolean hasConsecutiveSelection(SelectionLayer selectionLayer) {
        if (selectionLayer != null
                && SelectionUtils.isConsecutive(selectionLayer.getSelectedColumnPositions())) {

            Map<Integer, Set<Integer>> positions = new HashMap<Integer, Set<Integer>>();
            int column = -1;
            int row = -1;

            // collect the selection information
            for (PositionCoordinate coord : selectionLayer.getSelectedCellPositions()) {
                Set<Integer> rows = positions.get(coord.columnPosition);
                if (rows == null) {
                    rows = new LinkedHashSet<Integer>();
                    positions.put(coord.columnPosition, rows);
                }
                rows.add(coord.rowPosition);

                column = Math.max(column, coord.columnPosition);
                row = Math.max(row, coord.rowPosition);
            }

            // check if the selected region is a rectangle
            // every row collection for each column needs to have the same size
            // and the same content
            Set<Integer> previous = null;
            for (Set<Integer> rows : positions.values()) {
                if (previous != null && !previous.equals(rows)) {
                    return false;
                }
                previous = rows;
            }

            // test if rows are consecutive
            if (previous != null) {
                int[] rowPositions = new int[previous.size()];
                int i = 0;
                for (Integer rowPos : previous) {
                    rowPositions[i++] = rowPos;
                }
                if (SelectionUtils.isConsecutive(rowPositions)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the bottom right cell of a selected region. Will only return an
     * ILayerCell if the selected region is consecutive. Otherwise
     * <code>null</code> will be returned.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the selection.
     *
     * @return The bottom right cell of a selected region or <code>null</code>
     *         if the selected region is not consecutive.
     *
     * @since 1.4
     */
    public static ILayerCell getBottomRightCellInSelection(SelectionLayer selectionLayer) {
        if (hasConsecutiveSelection(selectionLayer)) {
            int column = -1;
            int row = -1;
            for (PositionCoordinate coord : selectionLayer.getSelectedCellPositions()) {
                column = Math.max(column, coord.columnPosition);
                row = Math.max(row, coord.rowPosition);
            }
            return selectionLayer.getCellByPosition(column, row);
        }
        return null;
    }

    /**
     * Inspects the current selection on the given {@link SelectionLayer} and
     * returns a list of the corresponding list item objects. Uses the
     * {@link IRowDataProvider} to be able to determine the row objects per
     * selected row position.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} to retrieve the selected row
     *            indexes from.
     * @param rowDataProvider
     *            The {@link IRowDataProvider} to retrieve the object for the
     *            row index.
     * @param fullySelectedRowsOnly
     *            Flag to determine if only fully selected rows should be taken
     *            into account.
     * @return The list of all objects that are currently marked as selected.
     *         Never <code>null</code>.
     *
     * @since 1.4
     */
    public static <T> List<T> getSelectedRowObjects(
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider,
            boolean fullySelectedRowsOnly) {

        List<RowObjectIndexHolder<T>> rows = new ArrayList<RowObjectIndexHolder<T>>();

        if (selectionLayer != null) {
            if (fullySelectedRowsOnly) {
                for (int rowPosition : selectionLayer.getFullySelectedRowPositions()) {
                    addToSelection(rows, rowPosition, selectionLayer, rowDataProvider);
                }
            } else {
                Set<Range> rowRanges = selectionLayer.getSelectedRowPositions();
                for (Range rowRange : rowRanges) {
                    for (int rowPosition = rowRange.start; rowPosition < rowRange.end; rowPosition++) {
                        addToSelection(rows, rowPosition, selectionLayer, rowDataProvider);
                    }
                }
            }
        }
        Collections.sort(rows);
        List<T> rowObjects = new ArrayList<T>();
        for (RowObjectIndexHolder<T> holder : rows) {
            rowObjects.add(holder.getRow());
        }
        return rowObjects;
    }

    private static <T> void addToSelection(
            List<RowObjectIndexHolder<T>> rows,
            int rowPosition,
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider) {

        int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
        if (rowIndex >= 0 && rowIndex < rowDataProvider.getRowCount()) {
            T rowObject = rowDataProvider.getRowObject(rowIndex);
            rows.add(new RowObjectIndexHolder<T>(rowIndex, rowObject));
        }
    }

}
