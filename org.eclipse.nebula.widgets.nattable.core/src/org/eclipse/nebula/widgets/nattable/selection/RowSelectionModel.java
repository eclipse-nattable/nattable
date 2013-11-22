/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.coordinate.Rectangle;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;


public class RowSelectionModel<R> implements IRowSelectionModel<R> {
	
	protected final IUniqueIndexLayer selectionLayer;
	
	protected final IRowDataProvider<R> rowDataProvider;
	protected final IRowIdAccessor<R> rowIdAccessor;
	private boolean multipleSelectionAllowed;
	
	protected Map<Serializable, R> selectedRows;
	protected Rectangle lastSelectedRange;  // *live* reference to last range parameter used in addSelection(range)
	protected Set<Serializable> lastSelectedRowIds;
	protected final ReadWriteLock selectionsLock;
	
	
	public RowSelectionModel(/*@NotNull*/ final IUniqueIndexLayer selectionLayer,
			final IRowDataProvider<R> rowDataProvider, final IRowIdAccessor<R> rowIdAccessor) {
		this(selectionLayer, rowDataProvider, rowIdAccessor, true);
	}
	
	public RowSelectionModel(/*@NotNull*/ final IUniqueIndexLayer selectionLayer,
			final IRowDataProvider<R> rowDataProvider, final IRowIdAccessor<R> rowIdAccessor,
			final boolean multipleSelectionAllowed) {
		if (selectionLayer == null) {
			throw new NullPointerException("selectionLayer"); //$NON-NLS-1$
		}
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
	public void setMultipleSelectionAllowed(final boolean multipleSelectionAllowed) {
		this.multipleSelectionAllowed = multipleSelectionAllowed;
	}
	
	
	@Override
	public void addSelection(final int columnPosition, final int rowPosition) {
		this.selectionsLock.writeLock().lock();
		try {
			if (!this.multipleSelectionAllowed) {
				this.selectedRows.clear();
			}
			
			final R rowObject = getRowObjectByPosition(rowPosition);
			if (rowObject != null) {
				final Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
				this.selectedRows.put(rowId, rowObject);
			}
		} finally {
			this.selectionsLock.writeLock().unlock();
		}
	}
	
	@Override
	public void addSelection(final Rectangle positions) {
		this.selectionsLock.writeLock().lock();
		try {
			if (this.multipleSelectionAllowed) {
				if (positions.equals(this.lastSelectedRange)) {
					// Unselect all previously selected rowIds
					if (this.lastSelectedRowIds != null) {
						for (final Serializable rowId : this.lastSelectedRowIds) {
							this.selectedRows.remove(rowId);
						}
					}
				}
			} else {
				this.selectedRows.clear();
				//as no multiple selection is allowed, ensure that only one row 
				//will be selected
				positions.height = 1;
			}
			
			final Map<Serializable, R> rowsToSelect = new HashMap<Serializable, R>();
			
			final int maxY = Math.min(positions.y + positions.height, this.selectionLayer.getRowCount());
			for (int rowPosition = positions.y; rowPosition < maxY; rowPosition++) {
				final R rowObject = getRowObjectByPosition(rowPosition);
				if (rowObject != null) {
					final Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
					rowsToSelect.put(rowId, rowObject);
				}
			}
			
			this.selectedRows.putAll(rowsToSelect);
			
			if (positions.equals(this.lastSelectedRange)) {
				this.lastSelectedRowIds = rowsToSelect.keySet();
			} else {
				this.lastSelectedRowIds = null;
			}
			
			this.lastSelectedRange = positions;
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
	public void clearSelection(final int columnPosition, final int rowPosition) {
		this.selectionsLock.writeLock().lock();
		try {
			final Serializable rowId = getRowIdByPosition(rowPosition);
			this.selectedRows.remove(rowId);
		} finally {
			this.selectionsLock.writeLock().unlock();
		}
	}
	
	@Override
	public void clearSelection(final Rectangle positions) {
		this.selectionsLock.writeLock().lock();
		try {
			final int maxY = Math.min(positions.y + positions.height, this.selectionLayer.getRowCount());
			for (int rowPosition = positions.y; rowPosition < maxY; rowPosition++) {
				clearSelection(0, rowPosition);
			}
		} finally {
			this.selectionsLock.writeLock().unlock();
		}
	}
	
	@Override
	public void clearSelection(final R rowObject) {
		this.selectionsLock.writeLock().lock();
		
		try {
			this.selectedRows.values().remove(rowObject);
		} finally {
			this.selectionsLock.writeLock().unlock();
		}
	}
	
	
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
		this.selectionsLock.readLock().lock();
		try {
			final List<Rectangle> selectionRectangles = new ArrayList<Rectangle>();
			final int width = this.selectionLayer.getColumnCount();
			
			for (final Serializable rowId : this.selectedRows.keySet()) {
				final int rowPosition = getRowPositionById(rowId);
				selectionRectangles.add(new Rectangle(0, rowPosition, width, 1));
			}
			
			return selectionRectangles;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	// Cell features
	
	@Override
	public boolean isCellPositionSelected(final int columnPosition, final int rowPosition) {
		final ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition, rowPosition);
		final int cellOriginRowPosition = cell.getOriginRowPosition();
		for (int testRowPosition = cellOriginRowPosition; testRowPosition < cellOriginRowPosition + cell.getRowSpan(); testRowPosition++) {
			if (isRowPositionSelected(testRowPosition)) {
				return true;
			}
		}
		return false;
	}
	
	// Column features
	
	@Override
	public RangeList getSelectedColumnPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			
			if (!this.selectedRows.isEmpty()) {
				selected.add(new Range(0, this.selectionLayer.getColumnCount()));
			}
			
			return selected;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public boolean isColumnPositionSelected(final int columnPosition) {
		this.selectionsLock.readLock().lock();
		try {
			return !this.selectedRows.isEmpty();
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public RangeList getFullySelectedColumnPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			
			if (isFullySelected()) {
				selected.add(new Range(0, this.selectionLayer.getColumnCount()));
			}
			
			return selected;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public boolean isColumnPositionFullySelected(final int columnPosition) {
		this.selectionsLock.readLock().lock();
		try {
			return isFullySelected();
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	private boolean isFullySelected() {
		final int rowCount = this.selectionLayer.getRowCount();
		final int selectedRowCount = this.selectedRows.size();
		
		return (rowCount > 0 && selectedRowCount == rowCount);
	}
	
	// Row features
	
	@Override
	public List<R> getSelectedRowObjects() {
		this.selectionsLock.readLock().lock();
		try {
			return new ArrayList<R>(this.selectedRows.values());
		} finally {
			this.selectionsLock.readLock().unlock();
		}
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
	public RangeList getSelectedRowPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			
			for (final Serializable rowId : this.selectedRows.keySet()) {
				selected.values().add(getRowPositionById(rowId));
			}
			
			return selected;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public boolean isRowPositionSelected(final int rowPosition) {
		this.selectionsLock.readLock().lock();
		try {
			final Serializable rowId = getRowIdByPosition(rowPosition);
			
			return this.selectedRows.containsKey(rowId);
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public RangeList getFullySelectedRowPositions() {
		return getSelectedRowPositions();
	}
	
	@Override
	public boolean isRowPositionFullySelected(final int rowPosition) {
		return isRowPositionSelected(rowPosition);
	}
	
	private Serializable getRowIdByPosition(final int rowPosition) {
		final R rowObject = getRowObjectByPosition(rowPosition);
		if (rowObject != null) {
			final Serializable rowId = this.rowIdAccessor.getRowId(rowObject);
			return rowId;
		}
		return null;
	}
	
	private R getRowObjectByPosition(final int rowPosition) {
		this.selectionsLock.readLock().lock();
		try {
			final int rowIndex = this.selectionLayer.getRowIndexByPosition(rowPosition);
			if (rowIndex >= 0) {
				try {
					final R rowObject = this.rowDataProvider.getRowObject(rowIndex);
					return rowObject;
				} catch (final Exception e) {
					// row index is invalid for the data provider
				}
			}
		} finally {
			this.selectionsLock.readLock().unlock();
		}
		
		return null;
	}
	
	private int getRowPositionById(final Serializable rowId) {
		this.selectionsLock.readLock().lock();
		try {
			final R rowObject = this.selectedRows.get(rowId);
			final int rowIndex = this.rowDataProvider.indexOfRowObject(rowObject);
			if (rowIndex < 0) {
				return -1;
			}
			
			return this.selectionLayer.getRowPositionByIndex(rowIndex);
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	//-- Object methods --//
	
	@Override
	public String toString() {
		this.selectionsLock.readLock().lock();
		try {
			return this.selectedRows.toString();
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
}
