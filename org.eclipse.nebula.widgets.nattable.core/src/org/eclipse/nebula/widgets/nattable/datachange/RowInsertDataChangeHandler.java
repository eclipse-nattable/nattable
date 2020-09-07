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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.datachange.event.KeyRowInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * {@link DataChangeHandler} to track row insert operations. Registers as
 * {@link ILayerEventHandler} for the {@link RowInsertEvent}. It is intended to
 * be used with a configuration that directly updates the backing data.
 * Temporary data storage is not supported. It therefore is able to perform
 * discard operations and will do nothing on save.
 *
 * @since 1.6
 */
public class RowInsertDataChangeHandler extends AbstractDataChangeHandler<RowInsertDataChange> implements ILayerEventHandler<RowInsertEvent> {

    private static final Log LOG = LogFactory.getLog(RowInsertDataChangeHandler.class);

    /**
     *
     * @param layer
     *            The {@link DataChangeLayer} this handler should be assigned
     *            to.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to store dataChanges
     *            for a specific key.
     */
    public RowInsertDataChangeHandler(DataChangeLayer layer, CellKeyHandler<?> keyHandler) {
        super(layer, keyHandler, new ConcurrentHashMap<Object, RowInsertDataChange>());
    }

    @Override
    public void handleStructuralChange(IStructuralChangeEvent structuralChangeEvent) {
        if (structuralChangeEvent.isVerticalStructureChanged()
                && structuralChangeEvent.getRowDiffs() != null) {

            if (this.keyHandler.updateOnVerticalStructuralChange()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getRowDiffs();
                handleRowDelete(structuralDiffs);
                handleRowInsert(structuralDiffs);
            }
        }
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
            Map<Object, RowInsertDataChange> modifiedRows = new HashMap<Object, RowInsertDataChange>();
            for (Map.Entry<Object, RowInsertDataChange> entry : this.dataChanges.entrySet()) {
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
                Map<Object, RowInsertDataChange> modifiedRows = new HashMap<Object, RowInsertDataChange>();
                for (Map.Entry<Object, RowInsertDataChange> entry : this.dataChanges.entrySet()) {
                    int rowIndex = this.keyHandler.getRowIndex(entry.getKey());

                    int modRow = -1;
                    if (rowIndex >= beforePositionRange.start) {
                        modRow = rowIndex + 1;
                    } else {
                        modRow = rowIndex;
                    }

                    Object updatedKey = this.keyHandler.getKeyWithRowUpdate(entry.getKey(), modRow);
                    entry.getValue().updateKey(updatedKey);
                    modifiedRows.put(updatedKey, entry.getValue());
                }

                this.dataChanges.clear();
                this.dataChanges.putAll(modifiedRows);
            }
        }
    }

    @Override
    public boolean isColumnDirty(int columnPosition) {
        return !this.dataChanges.isEmpty();
    }

    @Override
    public boolean isRowDirty(int rowPosition) {
        // we do not care about the column position,
        // so we use -1 for key generation
        Object key = this.keyHandler.getKey(-1, rowPosition);
        if (key != null) {
            return this.dataChanges.containsKey(key);
        }
        return false;
    }

    @Override
    public boolean isCellDirty(int columnPosition, int rowPosition) {
        return isRowDirty(rowPosition);
    }

    @Override
    public void handleLayerEvent(RowInsertEvent event) {
        if (this.handleDataUpdate) {
            synchronized (this.dataChanges) {
                if (event instanceof KeyRowInsertEvent) {
                    KeyRowInsertEvent e = (KeyRowInsertEvent) event;
                    List<Object> keys = new ArrayList<Object>(e.getKeys());
                    Collections.reverse(keys);
                    for (Object key : keys) {
                        // store the change locally
                        this.dataChanges.put(key, new RowInsertDataChange(key, e.getKeyHandler()));

                        // store the change in the DataChangeLayer
                        this.layer.addDataChange(new RowInsertDataChange(key, e.getKeyHandler()));
                    }
                } else {
                    // we need to ensure that the data changes are in correct
                    // order to ensure that deleting them again delete in the
                    // correct places when discarding backwards
                    int[] positions = PositionUtil.getPositions(event.getRowPositionRanges());
                    for (int i : positions) {
                        Object key = this.keyHandler.getKey(-1, i);
                        if (key != null) {
                            // store the change locally
                            this.dataChanges.put(key, new RowInsertDataChange(key, this.keyHandler));

                            // store the change in the DataChangeLayer
                            this.layer.addDataChange(new RowInsertDataChange(key, this.keyHandler));
                        } else {
                            LOG.warn("key was null for position " + i); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
    }

    @Override
    public Class<RowInsertEvent> getLayerEventClass() {
        return RowInsertEvent.class;
    }

}
