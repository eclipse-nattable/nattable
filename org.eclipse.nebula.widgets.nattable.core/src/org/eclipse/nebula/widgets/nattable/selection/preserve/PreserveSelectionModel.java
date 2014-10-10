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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.IMarkerSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.CellPosition;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Individual cell selection model that copes with the reordering of rows.
 *
 * @param <T> the type of object underlying each row
 */
public class PreserveSelectionModel<T> implements IMarkerSelectionModel {

	/**
	 * Provider of cell information
	 */
	private final IUniqueIndexLayer selectionLayer;

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
	 * Position of the selection anchor marker, expressed in row object and column position
	 */
	CellPosition<T> selectionAnchor;

	/**
	 * Position of the last selected cell marker, expressed in row object and column position
	 */
	CellPosition<T> lastSelectedCell;

	/**
	 * Area of the last selected region marker, expressed in row position, column position,
	 * number of column width and number of rows height
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
	public PreserveSelectionModel(IUniqueIndexLayer selectionLayer, IRowDataProvider<T> rowDataProvider, IRowIdAccessor<T> rowIdAccessor) {
		this.selectionLayer = selectionLayer;
		this.rowDataProvider = rowDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		this.allowMultiSelection = true;
		selectionsLock = new ReentrantReadWriteLock();
	}

	@Override
	public boolean isMultipleSelectionAllowed() {
		return allowMultiSelection;
	}

	@Override
	public void setMultipleSelectionAllowed(boolean multipleSelectionAllowed) {
		allowMultiSelection = multipleSelectionAllowed;
	}

	@Override
	public void addSelection(int columnPosition, int rowPosition) {
		selectionsLock.writeLock().lock();
		try {
			if (!allowMultiSelection) {
				clearSelection();
			}

			T rowObject = getRowObjectByPosition(rowPosition);
			if (rowObject != null) {
				Serializable rowId = rowIdAccessor.getRowId(rowObject);
				selections.select(rowId, rowObject, columnPosition);
			}
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void addSelection(Rectangle range) {
		selectionsLock.writeLock().lock();
		try {
			SelectionOperation addSelectionOperation = new SelectionOperation() {

				@Override
				public void run(int columnPosition, int rowPosition) {
					addSelection(columnPosition, rowPosition);
				}
			};
			performOnKnownCells(range, addSelectionOperation);
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void clearSelection() {
		selectionsLock.writeLock().lock();
		try {
			selections.clear();
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void clearSelection(int columnPosition, int rowPosition) {
		selectionsLock.writeLock().lock();
		try {
			T rowObject = getRowObjectByPosition(rowPosition);
			if (rowObject != null) {
				Serializable rowId = rowIdAccessor.getRowId(rowObject);
				selections.deselect(rowId, columnPosition);
			}
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void clearSelection(Rectangle removedSelection) {
		selectionsLock.writeLock().lock();
		try {
			SelectionOperation clearSelectionOperation = new SelectionOperation() {

				@Override
				public void run(int columnPosition, int rowPosition) {
					clearSelection(columnPosition, rowPosition);
				}
			};
			performOnKnownCells(removedSelection, clearSelectionOperation);
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	/**
	 * Only perform selection operations on cells which are known.
	 *
	 * For example selecting a full row operation will have a range from 0 to Integer.Max_Value.
	 * But only needed to operate on a range from 0 to total-column-count
	 *
	 * @param selection
	 *            area which the operation should be run on.
	 * @param selectionOperation
	 *            the operation to be perform on every cell in the area.
	 */
	private void performOnKnownCells(Rectangle selection, SelectionOperation selectionOperation) {
		int columnCount = selectionLayer.getColumnCount();
		int rowCount = selectionLayer.getRowCount();
		int startColumnPosition = selection.x;
		int startRowPosition = selection.y;
		if (startColumnPosition < columnCount && startRowPosition < rowCount) {

			int columnStartIndex = selectionLayer.getColumnIndexByPosition(selection.x);
			int numberOfVisibleColumnsToBeSelected = (selection.x + selection.width <= columnCount) ? selection.width : columnCount - columnStartIndex;
			int rowStartIndex = selectionLayer.getRowIndexByPosition(selection.y);
			int numberOfVisibleRowsToBeSelected = (selection.y + selection.height <= rowCount) ? selection.height : rowCount - rowStartIndex;

			for (int columnPosition = startColumnPosition; columnPosition < startColumnPosition + numberOfVisibleColumnsToBeSelected; columnPosition++) {
				for (int rowPosition = startRowPosition; rowPosition < startRowPosition + numberOfVisibleRowsToBeSelected; rowPosition++) {
					selectionOperation.run(columnPosition, rowPosition);
				}
			}
		}
	}

	@Override
	public boolean isEmpty() {
		selectionsLock.readLock().lock();
		try {
			return selections.isEmpty();
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public List<Rectangle> getSelections() {
		ArrayList<Rectangle> selectedCells = new ArrayList<Rectangle>();

		selectionsLock.readLock().lock();
		try {
			for (CellPosition<T> cellPosition : selections.getSelections()) {
				int rowPosition = getRowPositionByRowObject(cellPosition.getRowObject());
				if (isRowVisible(rowPosition)) {
					Integer columnPosition = cellPosition.getColumnPosition();
					Rectangle selectedCell = new Rectangle(columnPosition, rowPosition, 1, 1);
					selectedCells.add(selectedCell);
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		return selectedCells;
	}

	/**
	 * Determines if rowPosition represents a visible row
	 * @param rowPosition position of row to inspect
	 * @return whether rowPosition represents a visible row
	 */
	private boolean isRowVisible(int rowPosition) {
		return rowPosition != -1;
	}

	@Override
	public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
		selectionsLock.readLock().lock();

		try {
			ILayerCell cell = selectionLayer.getCellByPosition(columnPosition, rowPosition);
			int cellOriginRowPosition = cell.getOriginRowPosition();

			for (int candidateRowPosition = cellOriginRowPosition; candidateRowPosition < cellOriginRowPosition + cell.getRowSpan(); candidateRowPosition++) {
				Serializable rowId = getRowIdByPosition(candidateRowPosition);

				int cellOriginColumnPosition = cell.getOriginColumnPosition();
				for (int candidateColumnPosition = cellOriginColumnPosition; candidateColumnPosition < cellOriginColumnPosition + cell.getColumnSpan(); candidateColumnPosition++) {
					if (selections.isSelected(rowId, candidateColumnPosition)) {
						return true;
					}

				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		return false;
	}

	@Override
	public int[] getSelectedColumnPositions() {
		selectionsLock.readLock().lock();
		try {
			Collection<Integer> columnPositions = selections.getColumnPositions();
			return ArrayUtils.toPrimitive(columnPositions.toArray(new Integer[columnPositions.size()]));
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public boolean isColumnPositionSelected(int columnPosition) {
		selectionsLock.readLock().lock();
		try {
			for (Selections<T>.Row row : selections.getRows()) {
				if (row.contains(columnPosition)) {
					return true;
				}
			}

		} finally {
			selectionsLock.readLock().unlock();
		}
		return false;
	}

	@Override
	public int[] getFullySelectedColumnPositions(int columnHeight) {
		selectionsLock.readLock().lock();
		try {
			List<Integer> fullySelectedColumnPositions = new ArrayList<Integer>();
			for (Integer selectedColumn : selections.getColumnPositions()) {
				if (isColumnPositionFullySelected(selectedColumn, columnHeight)) {
					fullySelectedColumnPositions.add(selectedColumn);
				}
			}
			return ArrayUtils.toPrimitive(fullySelectedColumnPositions.toArray(new Integer[fullySelectedColumnPositions.size()]));
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public boolean isColumnPositionFullySelected(int columnPosition, int columnHeight) {
		TreeSet<Integer> selectedRowIndices = new TreeSet<Integer>();
		selectionsLock.readLock().lock();
		try {
			Selections<T>.Column selectedRowsInColumn = selections.getSelectedRows(columnPosition);
			if (hasColumnsSelectedRows(selectedRowsInColumn)) {
				for (Serializable rowId : selectedRowsInColumn.getItems()) {
					Selections<T>.Row row = selections.getSelectedColumns(rowId);
					T rowObject = row.getRowObject();
					int rowIndex = rowDataProvider.indexOfRowObject(rowObject);
					selectedRowIndices.add(rowIndex);
				}
			}
			return hasContinuousSection(selectedRowIndices, columnHeight);
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	/**
	 * Determines if there are selected cells in a column
	 * @param column collections of selected cells for a column
	 * @return whether there are selected cells in column
	 */
	private boolean hasColumnsSelectedRows(Selections<T>.Column column) {
		return column != null;
	}

	/**
	 * Determines if there is a long enough continuous section of integers in the
	 * sequence. The continuous section must be at least sectionSize long.
	 * @param sequence sequence of integers to inspect
	 * @param minimumLength minimum length of continuous section
	 * @return whether there is a long enough continuous section of integers in
	 *         sequence
	 */
	private boolean hasContinuousSection(TreeSet<Integer> sequence, int minimumLength) {
		int counter = 0;
		Integer previousValue = null;
		for (Integer index : sequence) {
			if (previousValue != null) {
				// Not first measurement.
				if (index != previousValue + 1) {
					// Restart measurement:
					counter = 0;
				}
			}
			// Continuous measurement:
			previousValue = index;
			counter += 1;
			if (counter == minimumLength) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getSelectedRowCount() {
		selectionsLock.readLock().lock();
		try {
			return selections.getRows().size();
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public Set<Range> getSelectedRowPositions() {
		HashSet<Range> visiblySelectedRowPositions = new HashSet<Range>();

		selectionsLock.readLock().lock();
		try {
			for (Selections<T>.Row row : selections.getRows()) {
				int rowPosition = getRowPositionByRowObject(row.getRowObject());
				if (isRowVisible(rowPosition)) {
					visiblySelectedRowPositions.add(new Range(rowPosition, rowPosition + 1));
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		return visiblySelectedRowPositions;
	}

	@Override
	public boolean isRowPositionSelected(int rowPosition) {
		selectionsLock.readLock().lock();
		try {
			Serializable rowId = getRowIdByPosition(rowPosition);
			return selections.isRowSelected(rowId);
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public int[] getFullySelectedRowPositions(int rowWidth) {
		selectionsLock.readLock().lock();
		try {
			List<Integer> fullySelectedRows = new ArrayList<Integer>();
			for (Selections<T>.Row selectedRow : selections.getRows()) {
				T rowObject = selectedRow.getRowObject();
				int rowPosition = getRowPositionByRowObject(rowObject);
				if (isRowVisible(rowPosition) && isRowPositionFullySelected(rowPosition, rowWidth)) {
					fullySelectedRows.add(rowPosition);
				}
			}
			Collections.sort(fullySelectedRows);
			return ArrayUtils.toPrimitive(fullySelectedRows.toArray(new Integer[fullySelectedRows.size()]));
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public boolean isRowPositionFullySelected(int rowPosition, int rowWidth) {
		TreeSet<Integer> selectedColumnPositions = new TreeSet<Integer>();

		selectionsLock.readLock().lock();
		try {
			T rowObject = getRowObjectByPosition(rowPosition);
			if (rowObject != null) {
				Serializable rowId = rowIdAccessor.getRowId(rowObject);
				Selections<T>.Row selectedColumnsInRow = selections.getSelectedColumns(rowId);
				if (hasRowSelectedColumns(selectedColumnsInRow)) {
					for (Integer columnPosition : selectedColumnsInRow.getItems()) {
						selectedColumnPositions.add(columnPosition);
					}
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
		return hasContinuousSection(selectedColumnPositions, rowWidth);
	}

	/**
	 * Determines if there are selected cells in a row
	 * @param row collections of selected cells for a row
	 * @return whether there are selected cells in row
	 */
	private boolean hasRowSelectedColumns(Selections<T>.Row row) {
		return row != null;
	}

	/**
	 * Retrieves the row ID for a row position
	 * @param rowPosition row position for retrieving row ID
	 * @return row ID for rowPosition, or null if undefined
	 */
	private Serializable getRowIdByPosition(int rowPosition) {
		T rowObject = getRowObjectByPosition(rowPosition);
		if (rowObject != null) {
			return rowIdAccessor.getRowId(rowObject);
		}
		return null;
	}

	/**
	 * Retrieves the row object for a row position
	 * @param rowPosition row position for retrieving row object
	 * @return row object for rowPosition, or null if undefined
	 */
	private T getRowObjectByPosition(int rowPosition) {
		int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
		if (rowIndex >= 0) {
			try {
				return rowDataProvider.getRowObject(rowIndex);
			} catch (Exception e) {
				// row index is invalid for the data provider
			}
		}
		return null;
	}

	/**
	 * Retrieves the row position for a row object
	 * @param rowObject row object for retrieving row position
	 * @return row position for rowObject, or -1 if undefined
	 */
	private int getRowPositionByRowObject(T rowObject) {
		int rowIndex = rowDataProvider.indexOfRowObject(rowObject);
		if (rowIndex == -1) {
			return -1;
		}
		return selectionLayer.getRowPositionByIndex(rowIndex);
	}

	@Override
	public Point getSelectionAnchor() {
		selectionsLock.readLock().lock();
		try {
			return createMarkerPoint(selectionAnchor);
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	@Override
	public Point getLastSelectedCell() {
		selectionsLock.readLock().lock();
		try {
			return createMarkerPoint(lastSelectedCell);
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	/**
	 * Creates a point from a cell position. The point is expressed in row position and
	 * column position. The row position is calculated by translating the row object of
	 * the cell position. It uses the column position of the cell position without
	 * translation.
	 * @param cellPosition cell position to translate into a point
	 * @return cellPosition expressed in row position and column position
	 */
	private Point createMarkerPoint(CellPosition<T> cellPosition) {
		if (cellPosition == null) {
			return createUndefinedPoint();
		}
		int rowPosition = getRowPositionByRowObject(cellPosition.getRowObject());
		return new Point(cellPosition.getColumnPosition(), rowPosition);
	}

	/**
	 * Creates an undefined point, using the SelectionLayer.NO_SELECTION constant.
	 * @return an undefined point
	 */
	private Point createUndefinedPoint() {
		return new Point(SelectionLayer.NO_SELECTION, SelectionLayer.NO_SELECTION);
	}

	@Override
	public Rectangle getLastSelectedRegion() {
		selectionsLock.readLock().lock();
		try {
			if (lastSelectedRegion == null) {
				return null;
			} else {
				correctLastSelectedRegion();
				return lastSelectedRegion;
			}
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	/**
	 * Corrects the last selected region by moving it so that it is originating on the row
	 * position identified by the last selected region origin row object.
	 */
	private void correctLastSelectedRegion() {
		lastSelectedRegion.y = getRowPositionByRowObject(lastSelectedRegionOriginRowObject);
	}

	@Override
	public void setSelectionAnchor(Point coordinate) {
		selectionsLock.writeLock().lock();
		try {
			selectionAnchor = new CellPosition<T>(getRowObjectByPosition(coordinate.y), coordinate.x);
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void setLastSelectedCell(Point coordinate) {
		selectionsLock.writeLock().lock();
		try {
			lastSelectedCell = new CellPosition<T>(getRowObjectByPosition(coordinate.y), coordinate.x);
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void setLastSelectedRegion(Rectangle region) {
		selectionsLock.writeLock().lock();
		try {
			lastSelectedRegion = region;
			if (region != null) {
				lastSelectedRegionOriginRowObject = getRowObjectByPosition(region.y);
			}
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}

	@Override
	public void setLastSelectedRegion(int x, int y, int width, int height) {
		selectionsLock.writeLock().lock();
		try {
			lastSelectedRegion.x = x;
			lastSelectedRegion.y = y;
			lastSelectedRegion.width = width;
			lastSelectedRegion.height = height;

			lastSelectedRegionOriginRowObject = getRowObjectByPosition(y);
		} finally {
			selectionsLock.writeLock().unlock();
		}
	}
	
	@Override
	public void updateSelection() {
		// do nothing, the selection state is held internally by id
	}

	/**
	 * Internal interface to be used for higher order methods.
	 *
	 */
	abstract interface SelectionOperation {
		/**
		 * Performs the operation
		 *
		 * @param columnPosition
		 * @param rowPosition
		 */
		public void run(int columnPosition, int rowPosition);
	}

}
