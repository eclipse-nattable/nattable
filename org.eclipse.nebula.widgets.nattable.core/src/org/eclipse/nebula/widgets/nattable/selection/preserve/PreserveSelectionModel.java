/*******************************************************************************
 * Copyright (c) 2014, 2018 Jonas Hugo, Markus Wahl, Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453851, 446275, 447396
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ResizeStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.nebula.widgets.nattable.selection.IMarkerSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionUtils;
import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.CellPosition;
import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.Row;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Individual cell selection model that copes with the reordering of rows.
 *
 * @param <T>
 *            the type of object underlying each row
 */
public class PreserveSelectionModel<T> implements IMarkerSelectionModel {

    /**
     * Provider of cell information.
     *
     * @since 1.6
     */
    protected final IUniqueIndexLayer selectionLayer;

    /**
     * Provider of underlying row objects
     */
    private final IRowDataProvider<T> rowDataProvider;

    /**
     * Provider of unique IDs for the rows
     */
    private final IRowIdAccessor<T> rowIdAccessor;

    /**
     * Whether to allow multiple selections
     */
    private boolean allowMultiSelection;

    /**
     * The selected cells
     */
    private Selections<T> selections = new Selections<T>();

    /**
     * Lock for ensuring thread safety
     */
    private final ReadWriteLock selectionsLock;

    /**
     * Position of the selection anchor marker, expressed in row object and
     * column position
     */
    CellPosition<T> selectionAnchor;

    /**
     * The selection anchor point calculated out of the selection anchor marker.
     * Tracked here to reduce the number of calculations on rendering.
     */
    Point selectionAnchorPoint;

    /**
     * Position of the last selected cell marker, expressed in row object and
     * column position
     */
    CellPosition<T> lastSelectedCell;

    /**
     * Area of the last selected region marker, expressed in row position,
     * column position, number of column width and number of rows height
     */
    Rectangle lastSelectedRegion;

    /**
     * The row object of the origin of the last selected region
     */
    T lastSelectedRegionOriginRowObject;

    /**
     * Creates a row sortable selection model
     *
     * @param selectionLayer
     *            provider of cell information
     * @param rowDataProvider
     *            provider of underlying row objects
     * @param rowIdAccessor
     *            provider of unique IDs for the rows
     */
    public PreserveSelectionModel(
            IUniqueIndexLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider,
            IRowIdAccessor<T> rowIdAccessor) {
        this.selectionLayer = selectionLayer;
        this.rowDataProvider = rowDataProvider;
        this.rowIdAccessor = rowIdAccessor;
        this.allowMultiSelection = true;
        this.selectionsLock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean isMultipleSelectionAllowed() {
        return this.allowMultiSelection;
    }

    @Override
    public void setMultipleSelectionAllowed(boolean multipleSelectionAllowed) {
        this.allowMultiSelection = multipleSelectionAllowed;
    }

    @Override
    public void addSelection(int columnPosition, int rowPosition) {
        this.selectionsLock.writeLock().lock();
        try {
            if (!this.allowMultiSelection) {
                clearSelection();
            }

            Serializable rowId = getRowIdByPosition(rowPosition);
            if (rowId != null) {
                T rowObject = getRowObjectByPosition(rowPosition);
                this.selections.select(rowId, rowObject, columnPosition);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void addSelection(Rectangle range) {
        this.selectionsLock.writeLock().lock();
        try {
            if (!this.allowMultiSelection) {
                clearSelection();
            }

            int startColumnPosition = range.x;
            int startRowPosition = range.y;
            if (startColumnPosition < this.selectionLayer.getColumnCount() && startRowPosition < this.selectionLayer.getRowCount()) {
                int numberOfVisibleColumnsToBeSelected = getNumberOfColumnsToBeSelected(range);
                int numberOfVisibleRowsToBeSelected = getNumberOfRowsToBeSelected(range);

                for (int rowPosition = startRowPosition; rowPosition < startRowPosition + numberOfVisibleRowsToBeSelected; rowPosition++) {
                    Serializable rowId = getRowIdByPosition(rowPosition);
                    if (rowId != null) {
                        T rowObject = getRowObjectByPosition(rowPosition);
                        for (int columnPosition = startColumnPosition; columnPosition < (startColumnPosition + numberOfVisibleColumnsToBeSelected); columnPosition++) {
                            this.selections.select(rowId, rowObject, columnPosition);
                        }
                    }
                }
            }

        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection() {
        this.selectionsLock.writeLock().lock();
        try {
            this.selections.clear();
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection(int columnPosition, int rowPosition) {
        this.selectionsLock.writeLock().lock();
        try {
            Serializable rowId = getRowIdByPosition(rowPosition);
            if (rowId != null) {
                this.selections.deselect(rowId, columnPosition);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void clearSelection(Rectangle removedSelection) {
        this.selectionsLock.writeLock().lock();
        try {
            int startColumnPosition = removedSelection.x;
            int startRowPosition = removedSelection.y;
            if (startColumnPosition < this.selectionLayer.getColumnCount() && startRowPosition < this.selectionLayer.getRowCount()) {
                int numberOfVisibleColumnsToBeSelected = getNumberOfColumnsToBeSelected(removedSelection);
                int numberOfVisibleRowsToBeSelected = getNumberOfRowsToBeSelected(removedSelection);

                for (int rowPosition = startRowPosition; rowPosition < startRowPosition + numberOfVisibleRowsToBeSelected; rowPosition++) {
                    Serializable rowId = getRowIdByPosition(rowPosition);
                    if (rowId != null) {
                        for (int columnPosition = startColumnPosition; columnPosition < startColumnPosition + numberOfVisibleColumnsToBeSelected; columnPosition++) {
                            this.selections.deselect(rowId, columnPosition);
                        }
                    }
                }
            }

        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    /**
     * Return the number of columns to select. Determines the number based on
     * the columns that are known. Needed in case of full row selection as the
     * range will be from 0 to {@link Integer#MAX_VALUE}, where only the range
     * from 0 to total column count is needed.
     *
     * @param selection
     *            The rectangle that should be selected.
     * @return The number of columns to select.
     */
    private int getNumberOfColumnsToBeSelected(Rectangle selection) {
        int columnCount = this.selectionLayer.getColumnCount();
        return (selection.x + selection.width <= columnCount) ? selection.width : columnCount;
    }

    /**
     * Return the number of rows to select. Determines the number based on the
     * rows that are known. Needed in case of full column selection as the range
     * will be from 0 to {@link Integer#MAX_VALUE}, where only the range from 0
     * to total row count is needed.
     *
     * @param selection
     *            The rectangle that should be selected.
     * @return The number of rows to select.
     */
    private int getNumberOfRowsToBeSelected(Rectangle selection) {
        int rowCount = this.selectionLayer.getRowCount();
        return (selection.y + selection.height <= rowCount) ? selection.height : rowCount;
    }

    @Override
    public boolean isEmpty() {
        this.selectionsLock.readLock().lock();
        try {
            return this.selections.isEmpty();
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public List<Rectangle> getSelections() {
        ArrayList<Rectangle> selectedCells = null;

        this.selectionsLock.readLock().lock();
        try {
            Collection<CellPosition<T>> selectedPositions = this.selections.getSelections();
            selectedCells = new ArrayList<Rectangle>(selectedPositions.size());
            for (CellPosition<T> cellPosition : selectedPositions) {
                int rowPosition = getRowPositionByRowObject(cellPosition.getRowObject());
                if (isRowVisible(rowPosition)) {
                    Integer columnPosition = cellPosition.getColumnPosition();
                    Rectangle selectedCell = new Rectangle(columnPosition, rowPosition, 1, 1);
                    selectedCells.add(selectedCell);
                }
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }
        return selectedCells != null ? selectedCells : new ArrayList<Rectangle>();
    }

    /**
     * Determines if rowPosition represents a visible row
     *
     * @param rowPosition
     *            position of row to inspect
     * @return whether rowPosition represents a visible row
     */
    private boolean isRowVisible(int rowPosition) {
        return rowPosition != -1;
    }

    @Override
    public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
        this.selectionsLock.readLock().lock();

        try {
            ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition, rowPosition);
            int cellOriginRowPosition = cell.getOriginRowPosition();

            for (int candidateRowPosition = cellOriginRowPosition; candidateRowPosition < cellOriginRowPosition + cell.getRowSpan(); candidateRowPosition++) {
                Serializable rowId = getRowIdByPosition(candidateRowPosition);

                int cellOriginColumnPosition = cell.getOriginColumnPosition();
                for (int candidateColumnPosition = cellOriginColumnPosition; candidateColumnPosition < cellOriginColumnPosition + cell.getColumnSpan(); candidateColumnPosition++) {
                    if (this.selections.isSelected(rowId, candidateColumnPosition)) {
                        return true;
                    }

                }
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }
        return false;
    }

    @Override
    public int[] getSelectedColumnPositions() {
        this.selectionsLock.readLock().lock();
        try {
            Collection<Integer> columnPositions = this.selections.getColumnPositions();
            return ArrayUtil.asIntArray(columnPositions);
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public boolean isColumnPositionSelected(int columnPosition) {
        this.selectionsLock.readLock().lock();
        try {
            for (Selections.Row<T> row : this.selections.getRows()) {
                if (row.contains(columnPosition)) {
                    return true;
                }
            }

        } finally {
            this.selectionsLock.readLock().unlock();
        }
        return false;
    }

    @Override
    public int[] getFullySelectedColumnPositions(int columnHeight) {
        this.selectionsLock.readLock().lock();
        try {
            List<Integer> fullySelectedColumnPositions = new ArrayList<Integer>();
            for (Integer selectedColumn : this.selections.getColumnPositions()) {
                if (isColumnPositionFullySelected(selectedColumn, columnHeight)) {
                    fullySelectedColumnPositions.add(selectedColumn);
                }
            }
            return ArrayUtil.asIntArray(fullySelectedColumnPositions);
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public boolean isColumnPositionFullySelected(int columnPosition, int columnHeight) {
        this.selectionsLock.readLock().lock();
        try {
            Selections.Column selectedRowsInColumn = this.selections.getSelectedRows(columnPosition);
            if (hasColumnsSelectedRows(selectedRowsInColumn)) {
                return selectedRowsInColumn.getItems().size() >= columnHeight;
            }
            return false;
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    /**
     * Determines if there are selected cells in a column
     *
     * @param column
     *            collections of selected cells for a column
     * @return whether there are selected cells in column
     */
    private boolean hasColumnsSelectedRows(Selections.Column column) {
        return column != null;
    }

    @Override
    public int getSelectedRowCount() {
        this.selectionsLock.readLock().lock();
        try {
            return this.selections.getRows().size();
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public Set<Range> getSelectedRowPositions() {
        HashSet<Range> visiblySelectedRowPositions = new HashSet<Range>();

        this.selectionsLock.readLock().lock();
        try {
            for (Selections.Row<T> row : this.selections.getRows()) {
                int rowPosition = getRowPositionByRowObject(row.getRowObject());
                if (isRowVisible(rowPosition)) {
                    visiblySelectedRowPositions.add(new Range(rowPosition, rowPosition + 1));
                }
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }
        return visiblySelectedRowPositions;
    }

    @Override
    public boolean isRowPositionSelected(int rowPosition) {
        this.selectionsLock.readLock().lock();
        try {
            Serializable rowId = getRowIdByPosition(rowPosition);
            return this.selections.isRowSelected(rowId);
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public int[] getFullySelectedRowPositions(int rowWidth) {
        this.selectionsLock.readLock().lock();
        try {
            List<Integer> fullySelectedRows = new ArrayList<Integer>();
            for (Selections.Row<T> selectedRow : this.selections.getRows()) {
                T rowObject = selectedRow.getRowObject();
                int rowPosition = getRowPositionByRowObject(rowObject);
                if (isRowVisible(rowPosition)
                        && isRowPositionFullySelected(rowPosition, rowWidth)) {
                    fullySelectedRows.add(rowPosition);
                }
            }
            Collections.sort(fullySelectedRows);
            return ArrayUtil.asIntArray(fullySelectedRows);
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    @Override
    public boolean isRowPositionFullySelected(int rowPosition, int rowWidth) {
        TreeSet<Integer> selectedColumnPositions = new TreeSet<Integer>();

        this.selectionsLock.readLock().lock();
        try {
            Serializable rowId = getRowIdByPosition(rowPosition);
            if (rowId != null) {
                Selections.Row<T> selectedColumnsInRow = this.selections.getSelectedColumns(rowId);
                if (hasRowSelectedColumns(selectedColumnsInRow)) {
                    for (Integer columnPosition : selectedColumnsInRow.getItems()) {
                        selectedColumnPositions.add(columnPosition);
                    }
                }
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }

        return (selectedColumnPositions.size() < rowWidth)
                ? false
                : SelectionUtils.isConsecutive(ArrayUtil.asIntArray(selectedColumnPositions));
    }

    /**
     * Determines if there are selected cells in a row
     *
     * @param row
     *            collections of selected cells for a row
     * @return whether there are selected cells in row
     */
    private boolean hasRowSelectedColumns(Selections.Row<T> row) {
        return row != null;
    }

    /**
     * Retrieves the row ID for a row position
     *
     * @param rowPosition
     *            row position for retrieving row ID
     * @return row ID for rowPosition, or null if undefined
     *
     * @since 1.6
     */
    protected Serializable getRowIdByPosition(int rowPosition) {
        T rowObject = getRowObjectByPosition(rowPosition);
        if (rowObject != null) {
            return this.rowIdAccessor.getRowId(rowObject);
        }
        return null;
    }

    /**
     * Retrieves the row object for a row position
     *
     * @param rowPosition
     *            row position for retrieving row object
     * @return row object for rowPosition, or null if undefined
     */
    private T getRowObjectByPosition(int rowPosition) {
        int rowIndex = this.selectionLayer.getRowIndexByPosition(rowPosition);
        if (rowIndex >= 0) {
            try {
                return this.rowDataProvider.getRowObject(rowIndex);
            } catch (Exception e) {
                // row index is invalid for the data provider
            }
        }
        return null;
    }

    /**
     * Retrieves the row position for a row object
     *
     * @param rowObject
     *            row object for retrieving row position
     * @return row position for rowObject, or -1 if undefined
     *
     * @since 1.6
     */
    protected int getRowPositionByRowObject(T rowObject) {
        int rowIndex = this.rowDataProvider.indexOfRowObject(rowObject);
        if (rowIndex == -1) {
            return -1;
        }
        return this.selectionLayer.getRowPositionByIndex(rowIndex);
    }

    @Override
    public Point getSelectionAnchor() {
        if (this.selectionAnchorPoint == null) {
            this.selectionsLock.readLock().lock();
            try {
                this.selectionAnchorPoint = createMarkerPoint(this.selectionAnchor);
            } finally {
                this.selectionsLock.readLock().unlock();
            }
        }
        return this.selectionAnchorPoint;
    }

    @Override
    public Point getLastSelectedCell() {
        this.selectionsLock.readLock().lock();
        try {
            return createMarkerPoint(this.lastSelectedCell);
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    /**
     * Creates a point from a cell position. The point is expressed in row
     * position and column position. The row position is calculated by
     * translating the row object of the cell position. It uses the column
     * position of the cell position without translation.
     *
     * @param cellPosition
     *            cell position to translate into a point
     * @return cellPosition expressed in row position and column position
     */
    private Point createMarkerPoint(CellPosition<T> cellPosition) {
        if (cellPosition == null
                || (cellPosition.getColumnPosition() == SelectionLayer.NO_SELECTION && cellPosition.getRowObject() == null)) {
            return createUndefinedPoint();
        }
        int rowPosition = getRowPositionByRowObject(cellPosition.getRowObject());
        return new Point(cellPosition.getColumnPosition(), rowPosition);
    }

    /**
     * Creates an undefined point, using the SelectionLayer.NO_SELECTION
     * constant.
     *
     * @return an undefined point
     */
    private Point createUndefinedPoint() {
        return new Point(SelectionLayer.NO_SELECTION, SelectionLayer.NO_SELECTION);
    }

    @Override
    public Rectangle getLastSelectedRegion() {
        this.selectionsLock.readLock().lock();
        try {
            if (this.lastSelectedRegion == null) {
                return null;
            } else {
                correctLastSelectedRegion();
                return this.lastSelectedRegion;
            }
        } finally {
            this.selectionsLock.readLock().unlock();
        }
    }

    /**
     * Corrects the last selected region by moving it so that it is originating
     * on the row position identified by the last selected region origin row
     * object.
     */
    private void correctLastSelectedRegion() {
        this.lastSelectedRegion.y = getRowPositionByRowObject(this.lastSelectedRegionOriginRowObject);
    }

    @Override
    public void setSelectionAnchor(Point coordinate) {
        this.selectionsLock.writeLock().lock();
        try {
            if (coordinate.x == SelectionLayer.NO_SELECTION
                    && coordinate.y == SelectionLayer.NO_SELECTION) {
                this.selectionAnchor = null;
            } else {
                this.selectionAnchor =
                        new CellPosition<T>(getRowObjectByPosition(coordinate.y), coordinate.x);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
            this.selectionAnchorPoint = null;
        }
    }

    @Override
    public void setLastSelectedCell(Point coordinate) {
        this.selectionsLock.writeLock().lock();
        try {
            if (coordinate.x == SelectionLayer.NO_SELECTION
                    && coordinate.y == SelectionLayer.NO_SELECTION) {
                this.lastSelectedCell = null;
            } else {
                this.lastSelectedCell =
                        new CellPosition<T>(getRowObjectByPosition(coordinate.y), coordinate.x);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void setLastSelectedRegion(Rectangle region) {
        // clear the selection in the current last selected region
        if (region != null && this.lastSelectedRegion != null) {
            clearSelection(this.lastSelectedRegion);
        }

        this.selectionsLock.writeLock().lock();
        try {

            this.lastSelectedRegion = region;

            if (region != null) {
                this.lastSelectedRegionOriginRowObject = getRowObjectByPosition(region.y);
            }
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void setLastSelectedRegion(int x, int y, int width, int height) {
        this.selectionsLock.writeLock().lock();
        try {
            this.lastSelectedRegion.x = x;
            this.lastSelectedRegion.y = y;
            this.lastSelectedRegion.width = width;
            this.lastSelectedRegion.height = height;

            this.lastSelectedRegionOriginRowObject = getRowObjectByPosition(y);
        } finally {
            this.selectionsLock.writeLock().unlock();
        }
    }

    @Override
    public void handleLayerEvent(IStructuralChangeEvent event) {
        // we are not interested in resize events
        if (event instanceof ResizeStructuralRefreshEvent
                || event instanceof ColumnResizeEvent
                || event instanceof RowResizeEvent) {
            return;
        }

        // ensure the selection anchor point is calculated on the next access
        this.selectionAnchorPoint = null;

        // handling for deleting columns
        if (event.isHorizontalStructureChanged()) {
            Collection<StructuralDiff> diffs = event.getColumnDiffs();
            if (diffs != null) {
                // first handle deletion, then handle insert
                // this is to avoid mixed operations that might lead to
                // confusing indexes
                List<Integer> removed = new ArrayList<Integer>();
                for (StructuralDiff columnDiff : diffs) {
                    if (columnDiff.getDiffType() != null
                            && columnDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                        Range beforePositionRange = columnDiff.getBeforePositionRange();
                        // first de-select removed columns
                        for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                            if (!(event instanceof ColumnReorderEvent)) {
                                // in case the column was reordered we don't
                                // want to deselect the column
                                this.selections.deselectColumn(i);
                            }
                            removed.add(i);
                        }
                    }
                }
                // now update still visible column selections
                Collections.sort(removed);
                int mod = 0;
                for (int i : removed) {
                    // ask for further column selections that need to be
                    // modified
                    this.selections.updateColumnsForRemoval(i - mod);
                    mod++;
                }

                for (StructuralDiff columnDiff : diffs) {
                    if (columnDiff.getDiffType() != null
                            && columnDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
                        Range afterPositionRange = columnDiff.getAfterPositionRange();
                        for (int i = afterPositionRange.start; i < afterPositionRange.end; i++) {
                            // ask for column selections that need to be
                            // modified
                            this.selections.updateColumnsForAddition(i);
                        }
                    }
                }
            }
        }

        // handling for deleting rows
        if (event.isVerticalStructureChanged()) {
            // the change is already done and we don't know about indexes, so we
            // need to check if the selected objects still exist
            Collection<Serializable> keysToRemove = new ArrayList<Serializable>();
            for (Selections.Row<T> row : this.selections.getRows()) {
                if (!ignoreVerticalChange(row)) {
                    int rowIndex = this.rowDataProvider.indexOfRowObject(row.getRowObject());
                    if (rowIndex == -1 || this.selectionLayer.getRowPositionByIndex(rowIndex) == -1) {
                        keysToRemove.add(row.getId());
                    }
                }
            }

            for (Serializable toRemove : keysToRemove) {
                this.selections.deselectRow(toRemove);
            }
        }
    }

    /**
     * Check if the default handling for vertical structure changes should be
     * performed for the given {@link Row}, or if it should be skipped. Skipping
     * for example would make sense for selections that are stored for rows that
     * have no row data in the backing data structure, e.g. a summary row
     * selection.
     * 
     * @param row
     *            The internal selected row representation.
     * @return <code>false</code> if the default handling for vertical changes
     *         should be performed, <code>true</code> if the default handling
     *         should be skipped.
     *
     * @since 1.6
     */
    protected boolean ignoreVerticalChange(Selections.Row<T> row) {
        return false;
    }

    @Override
    public Class<IStructuralChangeEvent> getLayerEventClass() {
        return IStructuralChangeEvent.class;
    }
}