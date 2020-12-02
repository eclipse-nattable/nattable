/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.asIntArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

public final class ColumnChooserUtils {

    private ColumnChooserUtils() {
        // private default constructor for helper class
    }

    public static final String RENAMED_COLUMN_INDICATOR = "*"; //$NON-NLS-1$

    public static void hideColumnEntries(List<ColumnEntry> removedItems, ColumnHideShowLayer hideShowLayer) {
        MultiColumnHideCommand hideCommand = new MultiColumnHideCommand(
                hideShowLayer,
                asIntArray(getColumnEntryPositions(removedItems)));
        hideShowLayer.doCommand(hideCommand);
    }

    public static void hideColumnPositions(List<Integer> removedPositions, ColumnHideShowLayer hideShowLayer) {
        MultiColumnHideCommand hideCommand = new MultiColumnHideCommand(
                hideShowLayer,
                asIntArray(removedPositions));
        hideShowLayer.doCommand(hideCommand);
    }

    public static void showColumnEntries(List<ColumnEntry> addedItems, ColumnHideShowLayer hideShowLayer) {
        hideShowLayer.doCommand(new MultiColumnShowCommand(getColumnEntryIndexes(addedItems)));
    }

    public static void showColumnIndexes(List<Integer> addedColumnIndexes, ColumnHideShowLayer hideShowLayer) {
        hideShowLayer.doCommand(new MultiColumnShowCommand(addedColumnIndexes));
    }

    public static List<ColumnEntry> getHiddenColumnEntries(
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer) {

        int[] hiddenColumnIndexes = columnHideShowLayer.getHiddenColumnIndexesArray();
        ArrayList<ColumnEntry> hiddenColumnEntries = new ArrayList<>();

        for (int hiddenColumnIndex : hiddenColumnIndexes) {
            String label = getColumnLabel(columnHeaderLayer, columnHeaderDataLayer, hiddenColumnIndex);
            ColumnEntry columnEntry = new ColumnEntry(label, hiddenColumnIndex, -1);
            hiddenColumnEntries.add(columnEntry);
        }

        return hiddenColumnEntries;
    }

    /**
     *
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} to retrieve a possible renamed
     *            column header label.
     * @param columnHeaderDataLayer
     *            The column header {@link DataLayer} to retrieve the column
     *            header label from.
     * @param columnIndex
     *            The column index of the column whose label is requested.
     * @return The renamed column header name for the given column index (if the
     *         column has been renamed), the original column name otherwise.
     */
    public static String getColumnLabel(
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer,
            Integer columnIndex) {

        String label = ""; //$NON-NLS-1$
        if (columnHeaderLayer.isColumnRenamed(columnIndex)) {
            label = columnHeaderLayer.getRenamedColumnLabelByIndex(columnIndex)
                    + RENAMED_COLUMN_INDICATOR;
        } else {
            int position = columnHeaderDataLayer.getColumnPositionByIndex(columnIndex.intValue());
            label = columnHeaderDataLayer.getDataValueByPosition(position, 0).toString();
        }
        return label;
    }

    /**
     * Get all visible columns and the corresponding labels in the header.
     *
     * @param columnHideShowLayer
     *            The {@link ColumnHideShowLayer} to get all visible columns.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} to retrieve a possible renamed
     *            column header label.
     * @param columnHeaderDataLayer
     *            The column header {@link DataLayer} to retrieve the column
     *            header label from.
     * @return All visible columns and the corresponding labels in the header.
     */
    public static List<ColumnEntry> getVisibleColumnsEntries(
            ColumnHideShowLayer columnHideShowLayer,
            ColumnHeaderLayer columnHeaderLayer,
            DataLayer columnHeaderDataLayer) {

        int visibleColumnCount = columnHideShowLayer.getColumnCount();
        ArrayList<ColumnEntry> visibleColumnEntries = new ArrayList<>();

        for (int i = 0; i < visibleColumnCount; i++) {
            int index = columnHideShowLayer.getColumnIndexByPosition(i);
            String label = getColumnLabel(columnHeaderLayer, columnHeaderDataLayer, index);
            ColumnEntry columnEntry = new ColumnEntry(label, index, i);
            visibleColumnEntries.add(columnEntry);
        }
        return visibleColumnEntries;
    }

    /**
     * Search the collection for the entry with the given index.
     *
     * @param entries
     *            The collection of {@link ColumnEntry} objects.
     * @param indexToFind
     *            The column index to find.
     * @return The {@link ColumnEntry} for the given column index.
     */
    public static ColumnEntry find(List<ColumnEntry> entries, int indexToFind) {
        return entries.stream().filter(entry -> entry.getIndex() == indexToFind).findFirst().orElse(null);
    }

    /**
     * Get ColumnEntry positions for the ColumnEntry objects.
     *
     * @param columnEntries
     *            The {@link ColumnEntry} objects.
     * @return The column positions of the provided {@link ColumnEntry} objects.
     */
    public static List<Integer> getColumnEntryPositions(List<ColumnEntry> columnEntries) {
        return columnEntries.stream().map(ColumnEntry::getPosition).collect(Collectors.toList());
    }

    /**
     * Get ColumnEntry indexes for the ColumnEntry objects.
     *
     * @param columnEntries
     *            The {@link ColumnEntry} objects.
     * @return The column indexes of the provided {@link ColumnEntry} objects.
     */
    public static List<Integer> getColumnEntryIndexes(List<ColumnEntry> columnEntries) {
        return columnEntries.stream().map(ColumnEntry::getIndex).collect(Collectors.toList());
    }

    /**
     *
     * @param entries
     *            The collection of {@link ColumnEntry} objects.
     * @param indexToFind
     *            The column index to find.
     * @return <code>true</code> if the list contains an entry with the given
     *         index.
     */
    public static boolean containsIndex(List<ColumnEntry> entries, int indexToFind) {
        return entries.stream().anyMatch(entry -> entry.getIndex() == indexToFind);
    }

}
