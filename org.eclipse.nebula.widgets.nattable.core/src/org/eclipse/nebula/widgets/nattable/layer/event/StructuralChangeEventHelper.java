/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.collections.api.collection.primitive.MutableIntCollection;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Helper class providing support for modifying cached index lists for
 * IStructuralChangeEvents.
 */
public class StructuralChangeEventHelper {

    /**
     * Will check for events that indicate that rows has been deleted. In that
     * case the given cached indexes for the given layer need to be updated
     * because the index of the rows might have changed. E.g. Row with index 3
     * is hidden in the given layer, deleting row at index 1 will cause the row
     * at index 3 to be moved at index 2. Without transforming the index
     * regarding the delete event, the wrong row would be hidden.
     *
     * @param rowDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported row positions to indexes,
     *            because the conversion to the layer who caches the index is
     *            done before it is fired further in the layer stack
     * @param cachedRowIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param handleNotFound
     *            flag to tell whether the not found row indexes should be taken
     *            into account or not. Needed for last row checks
     */
    public static void handleRowDelete(
            Collection<StructuralDiff> rowDiffs, ILayer underlyingLayer,
            Collection<Integer> cachedRowIndexes, boolean handleNotFound) {

        // the number of all deleted rows that don't have a corresponding index
        // anymore (last row cases)
        int numberOfNoIndex = 0;
        ArrayList<Integer> toRemove = new ArrayList<>();
        for (StructuralDiff rowDiff : rowDiffs) {
            if (rowDiff.getDiffType() != null
                    && rowDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = rowDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;// underlyingLayer.getRowIndexByPosition(i);
                    if (index >= 0) {
                        toRemove.add(index);
                    } else {
                        numberOfNoIndex++;
                    }
                }
            }
        }
        // remove the row indexes that are deleted
        cachedRowIndexes.removeAll(toRemove);

        // modify row indexes regarding the deleted rows
        ArrayList<Integer> modifiedRows = new ArrayList<>();
        for (Integer row : cachedRowIndexes) {
            // check number of removed indexes that are lower than the current
            // one
            int deletedBefore = handleNotFound ? numberOfNoIndex : 0;
            for (Integer removed : toRemove) {
                if (removed < row) {
                    deletedBefore++;
                }
            }
            int modRow = row - deletedBefore;
            if (modRow >= 0) {
                modifiedRows.add(modRow);
            }
        }
        cachedRowIndexes.clear();
        cachedRowIndexes.addAll(modifiedRows);
    }

    /**
     * Will check for events that indicate that rows has been deleted. In that
     * case the given cached indexes for the given layer need to be updated
     * because the index of the rows might have changed. E.g. Row with index 3
     * is hidden in the given layer, deleting row at index 1 will cause the row
     * at index 3 to be moved at index 2. Without transforming the index
     * regarding the delete event, the wrong row would be hidden.
     *
     * @param rowDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported row positions to indexes,
     *            because the conversion to the layer who caches the index is
     *            done before it is fired further in the layer stack
     * @param cachedRowIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param handleNotFound
     *            flag to tell whether the not found row indexes should be taken
     *            into account or not. Needed for last row checks
     *
     * @since 2.0
     */
    public static void handleRowDelete(
            Collection<StructuralDiff> rowDiffs, ILayer underlyingLayer,
            MutableIntCollection cachedRowIndexes, boolean handleNotFound) {

        // the number of all deleted rows that don't have a corresponding index
        // anymore (last row cases)
        int numberOfNoIndex = 0;
        MutableIntList toRemove = IntLists.mutable.empty();
        for (StructuralDiff rowDiff : rowDiffs) {
            if (rowDiff.getDiffType() != null
                    && rowDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = rowDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;// underlyingLayer.getRowIndexByPosition(i);
                    if (index >= 0) {
                        toRemove.add(index);
                    } else {
                        numberOfNoIndex++;
                    }
                }
            }
        }
        final int noIndexCount = numberOfNoIndex;

        // remove the row indexes that are deleted
        cachedRowIndexes.removeAll(toRemove);

        // modify row indexes regarding the deleted rows
        MutableIntList modifiedRows = IntLists.mutable.empty();
        cachedRowIndexes.forEach(row -> {
            // check number of removed indexes that are lower than the current
            // one
            int deletedBefore = handleNotFound ? noIndexCount : 0;
            deletedBefore += toRemove.count(removed -> removed < row);

            int modRow = row - deletedBefore;
            if (modRow >= 0) {
                modifiedRows.add(modRow);
            }
        });
        cachedRowIndexes.clear();
        cachedRowIndexes.addAll(modifiedRows);
    }

    /**
     * Will check for events that indicate that rows are added. In that case the
     * given cached indexes need to be updated because the index of the rows
     * might have changed. E.g. Row with index 3 is hidden in the given layer,
     * adding a row at index 1 will cause the row at index 3 to be moved to
     * index 4. Without transforming the index regarding the add event, the
     * wrong row would be hidden.
     *
     * @param rowDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported row positions to indexes,
     *            because the conversion to the layer who caches the index is
     *            done before it is fired further in the layer stack
     * @param cachedRowIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param addToCache
     *            Flag to configure if the added value should be added to the
     *            cache or not. This is necessary to differ whether
     *            cachedRowIndexes are a collection of all indexes that need to
     *            be updated (e.g. row reordering) or just a collection of
     *            indexes that are applied for a specific state (e.g. row hide
     *            state)
     */
    public static void handleRowInsert(
            Collection<StructuralDiff> rowDiffs, ILayer underlyingLayer,
            Collection<Integer> cachedRowIndexes, boolean addToCache) {

        for (StructuralDiff rowDiff : rowDiffs) {
            if (rowDiff.getDiffType() != null
                    && rowDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                Range beforePositionRange = rowDiff.getBeforePositionRange();
                ArrayList<Integer> modifiedRows = new ArrayList<>();
                int beforeIndex = underlyingLayer.getRowIndexByPosition(beforePositionRange.start);
                for (Integer row : cachedRowIndexes) {
                    if (row >= beforeIndex) {
                        modifiedRows.add(row + 1);
                    } else {
                        modifiedRows.add(row);
                    }
                }

                if (addToCache) {
                    modifiedRows.add(beforeIndex, beforePositionRange.start);
                }

                cachedRowIndexes.clear();
                cachedRowIndexes.addAll(modifiedRows);
            }
        }
    }

    /**
     * Will check for events that indicate that rows are added. In that case the
     * given cached indexes need to be updated because the index of the rows
     * might have changed. E.g. Row with index 3 is hidden in the given layer,
     * adding a row at index 1 will cause the row at index 3 to be moved to
     * index 4. Without transforming the index regarding the add event, the
     * wrong row would be hidden.
     *
     * @param rowDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported row positions to indexes,
     *            because the conversion to the layer who caches the index is
     *            done before it is fired further in the layer stack
     * @param cachedRowIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param addToCache
     *            Flag to configure if the added value should be added to the
     *            cache or not. This is necessary to differ whether
     *            cachedRowIndexes are a collection of all indexes that need to
     *            be updated (e.g. row reordering) or just a collection of
     *            indexes that are applied for a specific state (e.g. row hide
     *            state)
     * @since 2.0
     */
    public static void handleRowInsert(
            Collection<StructuralDiff> rowDiffs, ILayer underlyingLayer,
            MutableIntCollection cachedRowIndexes, boolean addToCache) {

        for (StructuralDiff rowDiff : rowDiffs) {
            if (rowDiff.getDiffType() != null
                    && rowDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                Range beforePositionRange = rowDiff.getBeforePositionRange();
                MutableIntList modifiedRows = IntLists.mutable.empty();
                int beforeIndex = underlyingLayer.getRowIndexByPosition(beforePositionRange.start);
                cachedRowIndexes.forEach(row -> {
                    if (row >= beforeIndex) {
                        modifiedRows.add(row + 1);
                    } else {
                        modifiedRows.add(row);
                    }
                });

                if (addToCache) {
                    modifiedRows.addAtIndex(beforeIndex, beforePositionRange.start);
                }

                cachedRowIndexes.clear();
                cachedRowIndexes.addAll(modifiedRows);
            }
        }
    }

    /**
     * Will check for events that indicate that columns has been deleted. In
     * that case the given cached indexes for the given layer need to be updated
     * because the index of the columns might have changed. E.g. Column with
     * index 3 is hidden in the given layer, deleting column at index 1 will
     * cause the column at index 3 to be moved at index 2. Without transforming
     * the index regarding the delete event, the wrong column would be hidden.
     *
     * @param columnDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported column positions to
     *            indexes, because the conversion to the layer who caches the
     *            index is done before it is fired further in the layer stack
     * @param cachedColumnIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param handleNotFound
     *            flag to tell whether the not found column indexes should be
     *            taken into account or not. Needed for last column checks
     */
    public static void handleColumnDelete(
            Collection<StructuralDiff> columnDiffs, ILayer underlyingLayer,
            Collection<Integer> cachedColumnIndexes, boolean handleNotFound) {

        // the number of all deleted columns that don't have a corresponding
        // index anymore (last column cases)
        int numberOfNoIndex = 0;
        ArrayList<Integer> toRemove = new ArrayList<>();
        for (StructuralDiff columnDiff : columnDiffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;// underlyingLayer.getColumnIndexByPosition(i);
                    if (index >= 0) {
                        toRemove.add(index);
                    } else {
                        numberOfNoIndex++;
                    }
                }
            }
        }
        // remove the column indexes that are deleted
        cachedColumnIndexes.removeAll(toRemove);

        // modify column indexes regarding the deleted columns
        ArrayList<Integer> modifiedColumns = new ArrayList<>();
        for (Integer column : cachedColumnIndexes) {
            // check number of removed indexes that are lower than the current
            // one
            int deletedBefore = handleNotFound ? numberOfNoIndex : 0;
            for (Integer removed : toRemove) {
                if (removed < column) {
                    deletedBefore++;
                }
            }
            int modColumn = column - deletedBefore;
            if (modColumn >= 0) {
                modifiedColumns.add(modColumn);
            }
        }
        cachedColumnIndexes.clear();
        cachedColumnIndexes.addAll(modifiedColumns);
    }

    /**
     * Will check for events that indicate that columns has been deleted. In
     * that case the given cached indexes for the given layer need to be updated
     * because the index of the columns might have changed. E.g. Column with
     * index 3 is hidden in the given layer, deleting column at index 1 will
     * cause the column at index 3 to be moved at index 2. Without transforming
     * the index regarding the delete event, the wrong column would be hidden.
     *
     * @param columnDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported column positions to
     *            indexes, because the conversion to the layer who caches the
     *            index is done before it is fired further in the layer stack
     * @param cachedColumnIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param handleNotFound
     *            flag to tell whether the not found column indexes should be
     *            taken into account or not. Needed for last column checks.
     * @since 2.0
     */
    public static void handleColumnDelete(
            Collection<StructuralDiff> columnDiffs, ILayer underlyingLayer,
            MutableIntCollection cachedColumnIndexes, boolean handleNotFound) {

        // the number of all deleted columns that don't have a corresponding
        // index anymore (last column cases)
        int numberOfNoIndex = 0;
        MutableIntList toRemove = IntLists.mutable.empty();
        for (StructuralDiff columnDiff : columnDiffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;// underlyingLayer.getColumnIndexByPosition(i);
                    if (index >= 0) {
                        toRemove.add(index);
                    } else {
                        numberOfNoIndex++;
                    }
                }
            }
        }
        final int noIndexCount = numberOfNoIndex;

        // remove the column indexes that are deleted
        cachedColumnIndexes.removeAll(toRemove);

        // modify column indexes regarding the deleted columns
        MutableIntList modifiedColumns = IntLists.mutable.empty();
        cachedColumnIndexes.forEach(column -> {
            // check number of removed indexes that are lower than the current
            // one
            int deletedBefore = handleNotFound ? noIndexCount : 0;
            deletedBefore += toRemove.count(removed -> removed < column);

            int modColumn = column - deletedBefore;
            if (modColumn >= 0) {
                modifiedColumns.add(modColumn);
            }
        });
        cachedColumnIndexes.clear();
        cachedColumnIndexes.addAll(modifiedColumns);
    }

    /**
     * Will check for events that indicate that columns are added. In that case
     * the given cached indexes need to be updated because the index of the
     * columns might have changed. E.g. Column with index 3 is hidden in the
     * given layer, adding a column at index 1 will cause the column at index 3
     * to be moved to index 4. Without transforming the index regarding the add
     * event, the wrong column would be hidden.
     *
     * @param columnDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported column positions to
     *            indexes, because the conversion to the layer who caches the
     *            index is done before it is fired further in the layer stack
     * @param cachedColumnIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param addToCache
     *            Flag to configure if the added value should be added to the
     *            cache or not. This is necessary to differ whether
     *            cachedColumnIndexes are a collection of all indexes that need
     *            to be updated (e.g. column reordering) or just a collection of
     *            indexes that are applied for a specific state (e.g. column
     *            hide state)
     */
    public static void handleColumnInsert(
            Collection<StructuralDiff> columnDiffs, ILayer underlyingLayer,
            Collection<Integer> cachedColumnIndexes, boolean addToCache) {

        for (StructuralDiff columnDiff : columnDiffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                ArrayList<Integer> modifiedColumns = new ArrayList<>();
                int beforeIndex = underlyingLayer.getColumnIndexByPosition(beforePositionRange.start);
                for (Integer column : cachedColumnIndexes) {
                    if (column >= beforeIndex) {
                        modifiedColumns.add(column + 1);
                    } else {
                        modifiedColumns.add(column);
                    }
                }

                if (addToCache) {
                    modifiedColumns.add(beforeIndex, beforePositionRange.start);
                }

                cachedColumnIndexes.clear();
                cachedColumnIndexes.addAll(modifiedColumns);
            }
        }
    }

    /**
     * Will check for events that indicate that columns are added. In that case
     * the given cached indexes need to be updated because the index of the
     * columns might have changed. E.g. Column with index 3 is hidden in the
     * given layer, adding a column at index 1 will cause the column at index 3
     * to be moved to index 4. Without transforming the index regarding the add
     * event, the wrong column would be hidden.
     *
     * @param columnDiffs
     *            The collection of {@link StructuralDiff}s to handle
     * @param underlyingLayer
     *            The underlying layer of the layer who caches the indexes.
     *            Needed to translate the transported column positions to
     *            indexes, because the conversion to the layer who caches the
     *            index is done before it is fired further in the layer stack
     * @param cachedColumnIndexes
     *            The collection of indexes that is cached by the layer that
     *            needs transformation
     * @param addToCache
     *            Flag to configure if the added value should be added to the
     *            cache or not. This is necessary to differ whether
     *            cachedColumnIndexes are a collection of all indexes that need
     *            to be updated (e.g. column reordering) or just a collection of
     *            indexes that are applied for a specific state (e.g. column
     *            hide state)
     * @since 2.0
     */
    public static void handleColumnInsert(
            Collection<StructuralDiff> columnDiffs, ILayer underlyingLayer,
            MutableIntCollection cachedColumnIndexes, boolean addToCache) {

        for (StructuralDiff columnDiff : columnDiffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                MutableIntList modifiedColumns = IntLists.mutable.empty();
                int beforeIndex = underlyingLayer.getColumnIndexByPosition(beforePositionRange.start);
                cachedColumnIndexes.forEach(column -> {
                    if (column >= beforeIndex) {
                        modifiedColumns.add(column + 1);
                    } else {
                        modifiedColumns.add(column);
                    }
                });

                if (addToCache) {
                    modifiedColumns.addAtIndex(beforeIndex, beforePositionRange.start);
                }

                cachedColumnIndexes.clear();
                cachedColumnIndexes.addAll(modifiedColumns);
            }
        }
    }

    /**
     * Method to indicate if the collection of StructuralDiffs marks a reorder
     * event. This is necessary because reordering itself contains out of two
     * diffs, one for deleting columns/rows, one for adding them at another
     * position. As these diffs are completely separated from each other, but
     * the handling does have impact, this check is added. Also on reordering
     * there is no need for special handling of diffs in the layers that call
     * this method.
     * <p>
     * Here is a small example to explain the impact on handling:
     * <ul>
     * <li>column at position 2 gets hidden</li>
     * <li>reorder column at index 0 to index 4</li>
     * <li>on handling the deletion of index 0 the hidden index 2 would be
     * reduced to 1 (which is wrong because it is reordered not really
     * deleted)</li>
     * <li>adding the column at index 4 would undo the hidden index
     * modification, because the insertion is handled separately and does not
     * know about the former deletion</li>
     * </ul>
     *
     * @param structuralDiffs
     *            The collection of StructuralDiffs to check for reordering
     * @return <code>true</code> if the diff indicates a reordering happened,
     *         <code>false</code> if if was not a reordering based on the
     *         explanation above.
     */
    public static boolean isReorder(Collection<StructuralDiff> structuralDiffs) {
        // if there is a diff that deletes columns and a diff that adds the same
        // amount of columns at once
        // it seems to be a reordering and therefore it is not a real
        // deletion/insertion so we don't need to handle
        if (structuralDiffs != null && (structuralDiffs.size() % 2) == 0) {
            int numberOfDeleteCols = 0;
            int numberOfInsertCols = 0;
            for (StructuralDiff columnDiff : structuralDiffs) {
                if (columnDiff.getDiffType() != null
                        && columnDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                    numberOfDeleteCols = columnDiff.getBeforePositionRange().end
                            - columnDiff.getBeforePositionRange().start;
                }
                if (columnDiff.getDiffType() != null
                        && columnDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                    numberOfInsertCols = columnDiff.getAfterPositionRange().end
                            - columnDiff.getAfterPositionRange().start;
                }
            }

            if (numberOfDeleteCols == numberOfInsertCols) {
                return true;
            }
        }

        return false;
    }

}
