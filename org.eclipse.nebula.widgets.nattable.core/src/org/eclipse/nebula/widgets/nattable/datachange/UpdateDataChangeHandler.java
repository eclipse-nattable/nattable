/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Abstract implementation of {@link DataChangeHandler} to handle data updates.
 *
 * @since 1.6
 */
public abstract class UpdateDataChangeHandler<T extends UpdateDataChange> extends AbstractDataChangeHandler<T> {

    /**
     * The column indexes of columns that contain dirty cells.
     */
    protected final Set<Integer> changedColumns = new HashSet<Integer>();

    /**
     * The row indexes of rows that contain dirty cells.
     */
    protected final Set<Integer> changedRows = new HashSet<Integer>();

    /**
     * Flag to configure if the tracked changes in the {@link DataChangeLayer}
     * should be updated on horizontal/column structural changes.
     */
    private boolean updateOnHorizontalChanges = true;

    /**
     * Flag to configure if the tracked changes in the {@link DataChangeLayer}
     * should be updated on vertical/row structural changes.
     */
    private boolean updateOnVerticalChanges = true;

    /**
     * Creates an {@link PersistenceUpdateDataChangeHandler} to handle
     * {@link DataUpdateEvent}s to be able to track and revert data changes.
     *
     * @param layer
     *            The {@link DataChangeLayer} this handler should be assigned
     *            to.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to store data changes
     *            for a specific key.
     */
    public UpdateDataChangeHandler(DataChangeLayer layer, CellKeyHandler<?> keyHandler, Map<Object, T> dataChanges) {
        super(layer, keyHandler, dataChanges);
    }

    @Override
    public void handleStructuralChange(IStructuralChangeEvent structuralChangeEvent) {
        if (structuralChangeEvent.isHorizontalStructureChanged()
                && structuralChangeEvent.getColumnDiffs() != null) {

            if (this.keyHandler.updateOnHorizontalStructuralChange()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getColumnDiffs();
                handleColumnDelete(structuralDiffs);
                handleColumnInsert(structuralDiffs);
            } else {
                removeChangesForDeletedColumnObjects();
            }
        } else if (structuralChangeEvent.isVerticalStructureChanged()
                && structuralChangeEvent.getRowDiffs() != null) {

            if (this.keyHandler.updateOnVerticalStructuralChange()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getRowDiffs();
                handleRowDelete(structuralDiffs);
                handleRowInsert(structuralDiffs);
            } else {
                removeChangesForDeletedRowObjects();
            }
        }
        rebuildPositionCollections();
    }

    /**
     * Will check for events that indicate that rows have been deleted. In that
     * case the cached dataChanges need to be updated because the index of the
     * rows might have changed. E.g. cell with row at index 3 is changed in the
     * given layer, deleting row at index 1 will cause the row at index 3 to be
     * moved to index 2. Without transforming the index regarding the delete
     * event, the wrong cell at the incorrect row would be shown as changed.
     *
     * @param rowDiffs
     *            The collection of {@link StructuralDiff}s to handle.
     */
    @SuppressWarnings("unchecked")
    private void handleRowDelete(Collection<StructuralDiff> rowDiffs) {
        // for correct calculation the diffs need to be processed from lowest
        // position to highest
        List<StructuralDiff> diffs = new ArrayList<StructuralDiff>(rowDiffs);
        Collections.sort(diffs, (o1, o2) -> o1.getBeforePositionRange().start - o2.getBeforePositionRange().start);

        List<Integer> toRemove = new ArrayList<Integer>();
        for (StructuralDiff rowDiff : diffs) {
            if (rowDiff.getDiffType() != null
                    && rowDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = rowDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;
                    if (index >= 0) {
                        toRemove.add(index);
                    }
                }
            }
        }

        if (!toRemove.isEmpty()) {
            // modify row indexes regarding the deleted rows
            Map<Object, T> modifiedRows = new HashMap<Object, T>();
            for (Map.Entry<Object, T> entry : this.dataChanges.entrySet()) {
                int rowIndex = this.keyHandler.getRowIndex(entry.getKey());
                if (!toRemove.contains(rowIndex)) {
                    // check number of removed indexes that are lower than the
                    // current one
                    int deletedBefore = 0;
                    for (Integer removed : toRemove) {
                        if (removed < rowIndex) {
                            deletedBefore++;
                        }
                    }
                    int modRow = rowIndex - deletedBefore;
                    if (modRow >= 0) {
                        Object oldKey = entry.getKey();
                        Object updatedKey = this.keyHandler.getKeyWithRowUpdate(oldKey, modRow);
                        entry.getValue().updateKey(updatedKey);
                        modifiedRows.put(updatedKey, entry.getValue());

                        if (this.updateOnVerticalChanges) {
                            // update the changes in the DataChangeLayer too
                            synchronized (this.layer.dataChanges) {
                                for (Iterator<DataChange> it = this.layer.dataChanges.iterator(); it.hasNext();) {
                                    DataChange change = it.next();
                                    if (change.getClass().equals(entry.getValue().getClass())
                                            && change.getKey().equals(oldKey)) {
                                        Object uk = this.keyHandler.getKeyWithRowUpdate(oldKey, modRow);
                                        change.updateKey(uk);
                                    }
                                }
                            }
                        }
                    }
                } else if (this.updateOnVerticalChanges) {
                    synchronized (this.layer.dataChanges) {
                        for (Iterator<DataChange> it = this.layer.dataChanges.iterator(); it.hasNext();) {
                            DataChange change = it.next();
                            if (change.getClass().equals(entry.getValue().getClass())
                                    && this.keyHandler.getRowIndex(change.getKey()) == this.keyHandler.getRowIndex(entry.getValue().getKey())) {
                                it.remove();
                            }
                        }
                    }
                }
            }

            this.dataChanges.clear();
            this.dataChanges.putAll(modifiedRows);
        }
    }

    /**
     * Will check for events that indicate that rows are added. In that case the
     * cached dataChanges need to be updated because the index of the rows might
     * have changed. E.g. Row with index 3 is hidden in the given layer, adding
     * a row at index 1 will cause the row at index 3 to be moved to index 4.
     * Without transforming the index regarding the add event, the wrong row
     * would be hidden.
     *
     * @param rowDiffs
     *            The collection of {@link StructuralDiff}s to handle.
     */
    @SuppressWarnings("unchecked")
    private void handleRowInsert(Collection<StructuralDiff> rowDiffs) {
        // for correct calculation the diffs need to be processed from highest
        // position to lowest
        List<StructuralDiff> diffs = new ArrayList<StructuralDiff>(rowDiffs);
        Collections.sort(diffs, (o1, o2) -> o2.getBeforePositionRange().start - o1.getBeforePositionRange().start);

        for (StructuralDiff rowDiff : diffs) {
            if (rowDiff.getDiffType() != null
                    && rowDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                Range beforePositionRange = rowDiff.getBeforePositionRange();
                // modify row indexes regarding the inserted rows
                Map<Object, T> modifiedRows = new HashMap<Object, T>();
                for (Map.Entry<Object, T> entry : this.dataChanges.entrySet()) {
                    int rowIndex = this.keyHandler.getRowIndex(entry.getKey());

                    Object oldKey = entry.getKey();
                    int modRow = -1;
                    if (rowIndex >= beforePositionRange.start) {
                        modRow = rowIndex + 1;
                    } else {
                        modRow = rowIndex;
                    }

                    Object updatedKey = this.keyHandler.getKeyWithRowUpdate(entry.getKey(), modRow);
                    entry.getValue().updateKey(updatedKey);
                    modifiedRows.put(updatedKey, entry.getValue());

                    if (this.updateOnVerticalChanges) {
                        // update the changes in the DataChangeLayer too
                        synchronized (this.layer.dataChanges) {
                            for (Iterator<DataChange> it = this.layer.dataChanges.iterator(); it.hasNext();) {
                                DataChange change = it.next();
                                if (change.getClass().equals(entry.getValue().getClass())
                                        && change.getKey().equals(oldKey)) {
                                    Object uk = this.keyHandler.getKeyWithRowUpdate(oldKey, modRow);
                                    change.updateKey(uk);
                                }
                            }
                        }
                    }
                }

                this.dataChanges.clear();
                this.dataChanges.putAll(modifiedRows);
            }
        }
    }

    /**
     * Will check for events that indicate that columns have been deleted. In
     * that case the cached dataChanges need to be updated because the index of
     * the columns might have changed. E.g. cell with column at index 3 is
     * changed in the given layer, deleting column at index 1 will cause the
     * column at index 3 to be moved to index 2. Without transforming the index
     * regarding the delete event, the wrong cell at the incorrect column would
     * be shown as changed.
     *
     * @param columnDiffs
     *            The collection of {@link StructuralDiff}s to handle.
     */
    @SuppressWarnings("unchecked")
    public void handleColumnDelete(Collection<StructuralDiff> columnDiffs) {
        // for correct calculation the diffs need to be processed from lowest
        // position to highest
        List<StructuralDiff> diffs = new ArrayList<StructuralDiff>(columnDiffs);
        Collections.sort(diffs, (o1, o2) -> o1.getBeforePositionRange().start - o2.getBeforePositionRange().start);

        List<Integer> toRemove = new ArrayList<Integer>();
        for (StructuralDiff columnDiff : diffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;
                    if (index >= 0) {
                        toRemove.add(index);
                    }
                }
            }
        }

        // only perform modifications if items where deleted
        if (!toRemove.isEmpty()) {
            // modify column indexes regarding the deleted column
            Map<Object, T> modifiedColumns = new HashMap<Object, T>();
            for (Map.Entry<Object, T> entry : this.dataChanges.entrySet()) {
                int columnIndex = this.keyHandler.getColumnIndex(entry.getKey());
                if (!toRemove.contains(columnIndex)) {
                    // check number of removed indexes that are lower than the
                    // current one
                    int deletedBefore = 0;
                    for (Integer removed : toRemove) {
                        if (removed < columnIndex) {
                            deletedBefore++;
                        }
                    }
                    int modColumn = columnIndex - deletedBefore;
                    if (modColumn >= 0) {
                        Object oldKey = entry.getKey();
                        Object updatedKey = this.keyHandler.getKeyWithColumnUpdate(oldKey, modColumn);
                        entry.getValue().updateKey(updatedKey);
                        modifiedColumns.put(updatedKey, entry.getValue());

                        if (this.updateOnVerticalChanges) {
                            // update the changes in the DataChangeLayer too
                            synchronized (this.layer.dataChanges) {
                                for (Iterator<DataChange> it = this.layer.dataChanges.iterator(); it.hasNext();) {
                                    DataChange change = it.next();
                                    if (change.getClass().equals(entry.getValue().getClass())
                                            && change.getKey().equals(oldKey)) {
                                        Object uk = this.keyHandler.getKeyWithColumnUpdate(oldKey, modColumn);
                                        change.updateKey(uk);
                                    }
                                }
                            }
                        }
                    }
                } else if (this.updateOnHorizontalChanges) {
                    for (Iterator<DataChange> it = this.layer.dataChanges.iterator(); it.hasNext();) {
                        DataChange change = it.next();
                        if (change.getClass().equals(entry.getValue().getClass())
                                && this.keyHandler.getColumnIndex(change.getKey()) == this.keyHandler.getColumnIndex(entry.getValue().getKey())) {
                            it.remove();
                        }
                    }
                }
            }

            this.dataChanges.clear();
            this.dataChanges.putAll(modifiedColumns);
        }
    }

    /**
     * Will check for events that indicate that columns are added. In that case
     * the cached dataChanges need to be updated because the index of the
     * columns might have changed. E.g. column with index 3 is hidden in the
     * given layer, adding a column at index 1 will cause the column at index 3
     * to be moved to index 4. Without transforming the index regarding the add
     * event, the wrong column would be hidden.
     *
     * @param columnDiffs
     *            The collection of {@link StructuralDiff}s to handle.
     */
    @SuppressWarnings("unchecked")
    public void handleColumnInsert(Collection<StructuralDiff> columnDiffs) {
        // for correct calculation the diffs need to be processed from highest
        // position to lowest
        List<StructuralDiff> diffs = new ArrayList<StructuralDiff>(columnDiffs);
        Collections.sort(diffs, (o1, o2) -> o2.getBeforePositionRange().start - o1.getBeforePositionRange().start);

        for (StructuralDiff columnDiff : diffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                // modify column indexes regarding the inserted columns
                Map<Object, T> modifiedColumns = new HashMap<Object, T>();
                for (Map.Entry<Object, T> entry : this.dataChanges.entrySet()) {
                    int columnIndex = this.keyHandler.getColumnIndex(entry.getKey());

                    Object oldKey = entry.getKey();
                    int modColumn = -1;
                    if (columnIndex >= beforePositionRange.start) {
                        modColumn = columnIndex + 1;
                    } else {
                        modColumn = columnIndex;
                    }

                    Object updatedKey = this.keyHandler.getKeyWithColumnUpdate(entry.getKey(), modColumn);
                    entry.getValue().updateKey(updatedKey);
                    modifiedColumns.put(updatedKey, entry.getValue());

                    if (this.updateOnHorizontalChanges) {
                        // update the changes in the DataChangeLayer too
                        synchronized (this.layer.dataChanges) {
                            for (Iterator<DataChange> it = this.layer.dataChanges.iterator(); it.hasNext();) {
                                DataChange change = it.next();
                                if (change.getClass().equals(entry.getValue().getClass())
                                        && change.getKey().equals(oldKey)) {
                                    Object uk = this.keyHandler.getKeyWithColumnUpdate(oldKey, modColumn);
                                    change.updateKey(uk);
                                }
                            }
                        }
                    }
                }

                this.dataChanges.clear();
                this.dataChanges.putAll(modifiedColumns);
            }
        }
    }

    /**
     * Clear the locally stored changes.
     */
    @Override
    public void clearDataChanges() {
        super.clearDataChanges();
        this.changedColumns.clear();
        this.changedRows.clear();
    }

    /**
     * Iterates over the locally stored data changes and checks if the
     * referenced object does still exist. If not the data change is removed.
     * <p>
     * This method is intended to be used with {@link CellKeyHandler}
     * implementations whose created keys do not need to be updated on
     * structural changes as they update automatically, e.g. via unique
     * identifier.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected void removeChangesForDeletedColumnObjects() {
        // we need to ensure that changes for deleted rows are
        // removed from the data changes collection
        for (Iterator<Object> it = this.dataChanges.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            int columnIndex = this.keyHandler.getColumnIndex(key);
            if (columnIndex < 0) {
                if (this.updateOnHorizontalChanges) {
                    T localChange = this.dataChanges.get(key);
                    for (Iterator<DataChange> iterator = this.layer.dataChanges.iterator(); iterator.hasNext();) {
                        DataChange change = iterator.next();
                        if (change.getClass().equals(localChange.getClass())
                                && this.keyHandler.getColumnIndex(change.getKey()) == this.keyHandler.getColumnIndex(localChange.getKey())) {
                            iterator.remove();
                        }
                    }
                }
                it.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void removeChangesForDeletedRowObjects() {
        // we need to ensure that changes for deleted rows are
        // removed from the data changes collection
        for (Iterator<Object> it = this.dataChanges.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            int rowIndex = this.keyHandler.getRowIndex(key);
            if (rowIndex < 0) {
                if (this.updateOnVerticalChanges) {
                    T localChange = this.dataChanges.get(key);
                    for (Iterator<DataChange> iterator = this.layer.dataChanges.iterator(); iterator.hasNext();) {
                        DataChange change = iterator.next();
                        if (change.getClass().equals(localChange.getClass())
                                && this.keyHandler.getRowIndex(change.getKey()) == this.keyHandler.getRowIndex(localChange.getKey())) {
                            iterator.remove();
                        }
                    }

                }
                it.remove();
            }
        }
    }

    /**
     * Rebuilds the {@link #changedColumns} and {@link #changedRows} collections
     * based on the updated {@link #dataChanges} map.
     */
    @SuppressWarnings("unchecked")
    protected void rebuildPositionCollections() {
        this.changedColumns.clear();
        this.changedRows.clear();
        for (Iterator<Object> it = this.dataChanges.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            int columnIndex = this.keyHandler.getColumnIndex(key);
            int rowIndex = this.keyHandler.getRowIndex(key);
            if (columnIndex >= 0 && rowIndex >= 0) {
                this.changedColumns.add(columnIndex);
                this.changedRows.add(rowIndex);
            }
        }
    }

    @Override
    public boolean isColumnDirty(int columnPosition) {
        return this.changedColumns.contains(columnPosition);
    }

    @Override
    public boolean isRowDirty(int rowPosition) {
        return this.changedRows.contains(rowPosition);
    }

    @Override
    public boolean isCellDirty(int columnPosition, int rowPosition) {
        Object key = this.keyHandler.getKey(columnPosition, rowPosition);
        if (key != null) {
            return this.dataChanges.containsKey(key);
        }
        return false;
    }

    /**
     * Configure if the changes tracked by the {@link DataChangeLayer} should
     * also be updated on a horizontal/column structural changes. The update is
     * needed in case the change does not cause a {@link DataChange} that is
     * created by some other handler and performed in the correct order.
     *
     * @param update
     *            <code>true</code> if the changes tracked by the
     *            {@link DataChangeLayer} should be updated, <code>false</code>
     *            if not.
     */
    public void setUpdateOnHorizontalChanges(boolean update) {
        this.updateOnHorizontalChanges = update;
    }

    /**
     * Configure if the changes tracked by the {@link DataChangeLayer} should
     * also be updated on a vertical/row structural changes. The update is
     * needed in case the change does not cause a {@link DataChange} that is
     * created by some other handler and performed in the correct order.
     *
     * @param update
     *            <code>true</code> if the changes tracked by the
     *            {@link DataChangeLayer} should be updated, <code>false</code>
     *            if not.
     */
    public void setUpdateOnVerticalChanges(boolean update) {
        this.updateOnVerticalChanges = update;
    }
}
