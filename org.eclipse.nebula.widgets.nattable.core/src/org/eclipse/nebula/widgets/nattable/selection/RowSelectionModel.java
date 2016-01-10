/*******************************************************************************
 * Copyright (c) 2012, 2014, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 447259, 446275, 447394, 446276
 *     Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 478622
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

public class RowSelectionModel<R> implements IRowSelectionModel<R> {

    protected final SelectionLayer selectionLayer;
    protected final IRowDataProvider<R> rowDataProvider;
    protected final IRowIdAccessor<R> rowIdAccessor;
    private boolean multipleSelectionAllowed;

    protected Map<Serializable, R> selectedRows;
    /**
     * *live* reference to last range parameter used in addSelection(range)
     */
    protected Rectangle lastSelectedRange;
    protected Set<Serializable> lastSelectedRowIds;
    protected final ReadWriteLock selectionsLock;

    public RowSelectionModel(SelectionLayer selectionLayer,
            IRowDataProvider<R> rowDataProvider, IRowIdAccessor<R> rowIdAccessor) {
        this(selectionLayer, rowDataProvider, rowIdAccessor, true);
    }

    public RowSelectionModel(SelectionLayer selectionLayer,
            IRowDataProvider<R> rowDataProvider,
            IRowIdAccessor<R> rowIdAccessor, boolean multipleSelectionAllowed) {
        this.selectionLayer = selectionLayer;
        this.rowDataProvider = rowDataProvider;
        this.rowIdAccessor = rowIdAccessor;
        this.multipleSelectionAllowed = multipleSelectionAllowed;

        this.selectedRows = new HashMap<Serializable, R>();
        this.selectionsLock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean isMultipleSelectionAllowed() {
        return this.multipleSelectionAllowed;
    }

    @Override
    public void setMultipleSelectionAllowed(boolean multipleSelectionAllowed) {
        this.multipleSelectionAllowed = multipleSelectionAllowed;
    }

    @Override
    public void addSelection(int columnPosition, int rowPosition) {
        this.selectionsLock.writeLock().lock();

        try {
            if (!this.multipleSelectionAllowed) {
                this.selectedRows.clear();
            }

            R rowObject = getRowObjectByPosition(rowPosition);
            if (rowObject != null) {
                Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
                this.selectedRows.put(rowId, rowObject);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void addSelection(Rectangle range) {
        this.selectionsLock.writeLock().lock();

        try {
            if (!this.multipleSelectionAllowed) {
                // as no multiple selection is allowed, ensure that only one row
                // will be selected
                this.selectedRows.clear();
                range.height = 1;
            }

            Map<Serializable, R> rowsToSelect = new HashMap<Serializable, R>();

            int maxY = Math.min(range.y + range.height, this.selectionLayer.getRowCount());
            for (int rowPosition = range.y; rowPosition < maxY; rowPosition++) {
                R rowObject = getRowObjectByPosition(rowPosition);
                if (rowObject != null) {
                    Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
                    rowsToSelect.put(rowId, rowObject);
                }
            }

            this.selectedRows.putAll(rowsToSelect);

            if (range.equals(this.lastSelectedRange)) {
                this.lastSelectedRowIds = rowsToSelect.keySet();
            } else {
                this.lastSelectedRowIds = null;
            }

            this.lastSelectedRange = range;
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection() {
        this.selectionsLock.writeLock().lock();
        try {
            this.selectedRows.clear();
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection(int columnPosition, int rowPosition) {
        this.selectionsLock.writeLock().lock();

        try {
            Serializable rowId = getRowIdByPosition(rowPosition);
            this.selectedRows.remove(rowId);
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection(Rectangle removedSelection) {
        this.selectionsLock.writeLock().lock();

        try {
            int maxY = Math.min(
                    removedSelection.y + removedSelection.height,
                    this.selectionLayer.getRowCount());
            for (int rowPosition = removedSelection.y; rowPosition < maxY; rowPosition++) {
                clearSelection(0, rowPosition);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection(R rowObject) {
        this.selectionsLock.writeLock().lock();

        try {
            this.selectedRows.remove(this.rowIdAccessor.getRowId(rowObject));
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    };

    @Override
    public boolean isEmpty() {
        this.selectionsLock.readLock().lock();

        try {
            return this.selectedRows.isEmpty();
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public List<Rectangle> getSelections() {
        List<Rectangle> selectionRectangles = new ArrayList<Rectangle>();

        this.selectionsLock.readLock().lock();

        try {
            int width = this.selectionLayer.getColumnCount();
            for (Serializable rowId : this.selectedRows.keySet()) {
                int rowPosition = getRowPositionById(rowId);
                selectionRectangles.add(new Rectangle(0, rowPosition, width, 1));
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }

        return selectionRectangles;
    }

    // Cell features

    @Override
    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition, rowPosition);
        if (cell != null) {
            int cellOriginRowPosition = cell.getOriginRowPosition();
            for (int testRowPosition = cellOriginRowPosition; testRowPosition < cellOriginRowPosition + cell.getRowSpan(); testRowPosition++) {
                if (isRowPositionSelected(testRowPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Column features

    @Override
    public int[] getSelectedColumnPositions() {
        if (!isEmpty()) {
            this.selectionsLock.readLock().lock();

            int columnCount;

            try {
                columnCount = this.selectionLayer.getColumnCount();
            } finally {
                this.selectionsLock.readLock().unlock();
            }

            int[] columns = new int[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = i;
            }
            return columns;
        }
        return new int[] {};
    }

    @Override
    public boolean isColumnPositionSelected(int columnPosition) {
        this.selectionsLock.readLock().lock();

        try {
            return !this.selectedRows.isEmpty();
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public int[] getFullySelectedColumnPositions(int fullySelectedColumnRowCount) {
        this.selectionsLock.readLock().lock();

        try {
            if (isColumnPositionFullySelected(0, fullySelectedColumnRowCount)) {
                return getSelectedColumnPositions();
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }

        return new int[] {};
    }

    @Override
    public boolean isColumnPositionFullySelected(
            int columnPosition, int fullySelectedColumnRowCount) {
        this.selectionsLock.readLock().lock();

        try {
            int selectedRowCount = this.selectedRows.size();

            if (selectedRowCount == 0) {
                return false;
            }

            return selectedRowCount == fullySelectedColumnRowCount;
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    // Row features

    @Override
    public List<R> getSelectedRowObjects() {
        final List<R> rowObjects = new ArrayList<R>();

        this.selectionsLock.readLock().lock();
        try {
            rowObjects.addAll(this.selectedRows.values());
        } finally {
            this.selectionsLock.readLock().unlock();
        }

        return rowObjects;
    }

    @Override
    public int getSelectedRowCount() {
        this.selectionsLock.readLock().lock();

        try {
            return this.selectedRows.size();
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public Set<Range> getSelectedRowPositions() {
        Set<Range> selectedRowRanges = new HashSet<Range>();

        this.selectionsLock.readLock().lock();

        try {
            for (Serializable rowId : this.selectedRows.keySet()) {
                int rowPosition = getRowPositionById(rowId);
                selectedRowRanges.add(new Range(rowPosition, rowPosition + 1));
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }

        return selectedRowRanges;
    }

    @Override
    public boolean isRowPositionSelected(int rowPosition) {
        this.selectionsLock.readLock().lock();

        try {
            Serializable rowId = getRowIdByPosition(rowPosition);
            return this.selectedRows.containsKey(rowId);
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public int[] getFullySelectedRowPositions(int rowWidth) {
        this.selectionsLock.readLock().lock();

        try {
            int selectedRowCount = this.selectedRows.size();
            int[] selectedRowPositions = new int[selectedRowCount];
            int i = 0;
            for (Serializable rowId : this.selectedRows.keySet()) {
                selectedRowPositions[i] = getRowPositionById(rowId);
                i++;
            }
            return selectedRowPositions;
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public boolean isRowPositionFullySelected(int rowPosition, int rowWidth) {
        return isRowPositionSelected(rowPosition);
    }

    private Serializable getRowIdByPosition(int rowPosition) {
        R rowObject = getRowObjectByPosition(rowPosition);
        if (rowObject != null) {
            Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
            return rowId;
        }
        return null;
    }

    private R getRowObjectByPosition(int rowPosition) {
        this.selectionsLock.readLock().lock();

        try {
            int rowIndex = this.selectionLayer.getRowIndexByPosition(rowPosition);
            if (rowIndex >= 0) {
                try {
                    R rowObject = this.rowDataProvider.getRowObject(rowIndex);
                    return rowObject;
                } catch (Exception e) {
                    // row index is invalid for the data provider
                }
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }

        return null;
    }

    private int getRowPositionById(Serializable rowId) {
        this.selectionsLock.readLock().lock();

        try {
            R rowObject = this.selectedRows.get(rowId);
            int rowIndex = this.rowDataProvider.indexOfRowObject(rowObject);
            if (rowIndex == -1) {
                return -1;
            }
            int rowPosition = this.selectionLayer.getRowPositionByIndex(rowIndex);
            return rowPosition;
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public void handleLayerEvent(IStructuralChangeEvent event) {
        // handling for deleting rows
        if (event.isVerticalStructureChanged()) {
            // the change is already done and we don't know about indexes, so we
            // need to check if the selected objects still exist
            Collection<Serializable> keysToRemove = new ArrayList<Serializable>();
            for (Map.Entry<Serializable, R> entry : this.selectedRows.entrySet()) {
                int rowIndex = this.rowDataProvider.indexOfRowObject(entry.getValue());
                if (rowIndex == -1) {
                    keysToRemove.add(entry.getKey());
                }
            }

            this.selectionsLock.readLock().lock();
            try {
                for (Serializable toRemove : keysToRemove) {
                    this.selectedRows.remove(toRemove);
                }
            } finally {
                this.selectionsLock.readLock().unlock();
            }

            // fire row selection event
            // since we are not able to identify the row position of the deleted
            // selection we use all rows in the event to indicate the selection
            // change for all deleted rows
            if (!keysToRemove.isEmpty()) {
                Collection<Integer> rowPositions = new HashSet<Integer>();
                Collection<StructuralDiff> diffs = event.getRowDiffs();
                if (diffs != null) {
                    for (StructuralDiff rowDiff : diffs) {
                        if (rowDiff.getDiffType() != null
                                && rowDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                            Range beforePositionRange = rowDiff.getBeforePositionRange();
                            for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                                rowPositions.add(i);
                            }
                        }
                    }
                }
                // if there is no diff in the event we assume everything has
                // changed, in such a case we are not able to fire an
                // appropriate event the layer stack upwards since it will be
                // stopped while converting it to the target layer
                // for the RowSelectionProvider this is sufficient because it
                // registers itself as a listener to the SelectionLayer and
                // therefore gets informed about the selection change
                this.selectionLayer.fireLayerEvent(
                        new RowSelectionEvent(this.selectionLayer, rowPositions, -1, false, false));
            }
        }
    }

    @Override
    public Class<IStructuralChangeEvent> getLayerEventClass() {
        return IStructuralChangeEvent.class;
    }
}
