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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.coordinate.Rectangle;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;


/**
 * Tracks the selections made in the table. All selections are tracked in terms of
 * Rectangles.
 *  
 * For example if the table has 10 rows and column 2 is selected, the 
 * Rectangle tracked is (0, 2, 10, 1)
 * 
 * Coordinates are in <i>Selection Layer positions</i>
 * 
 * @see SelectionLayer
 */
public class SelectionModel implements ISelectionModel {
	
	
	//-- Utility --//
	
	private static final Rectangle getLeftSelection(final Rectangle intersection, final Rectangle selection) {
		if (intersection.x > selection.x) {
			return new Rectangle(selection.x, selection.y,
					intersection.x - selection.x, selection.height);
		}
		return null;
	}
	
	private static final Rectangle getRightSelection(final Rectangle intersection, final Rectangle selection) {
		final int newX = intersection.x + intersection.width;
		if (newX < selection.x + selection.width) {
			return new Rectangle(newX, selection.y,
					selection.x + selection.width - newX, selection.height);
		}
		return null;
	}
	
	private static final Rectangle getTopSelection(final Rectangle intersection, final Rectangle selection) {
		if (intersection.y > selection.y) {
			return new Rectangle(selection.x, selection.y,
					selection.width, intersection.y - selection.y);
		}
		return null;
	}
	
	private static final Rectangle getBottomSelection(final Rectangle intersection, final Rectangle selection) {
		final int newY = intersection.y + intersection.height;
		if (newY < selection.y + selection.height) {
			return new Rectangle(selection.x, newY,
					selection.width, selection.y + selection.height - newY);
		}
		return null;
	}
	
	
	private final ILayer selectionLayer;
	
	private boolean multipleSelectionAllowed;
	
	private final List<Rectangle> selections;
	private final ReadWriteLock selectionsLock;
	
	
	public SelectionModel(/*@NotNull*/ final ILayer selectionLayer) {
		this(selectionLayer, true);
	}
	
	public SelectionModel(/*@NotNull*/ final ILayer selectionLayer, final boolean multipleSelectionAllowed) {
		if (selectionLayer == null) {
			throw new NullPointerException("selectionLayer"); //$NON-NLS-1$
		}
		this.selectionLayer = selectionLayer;
		this.multipleSelectionAllowed = multipleSelectionAllowed;
		
		this.selections = new LinkedList<Rectangle>();
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
		addSelectionIntoList(new Rectangle(columnPosition, rowPosition, 1, 1));
	}
	
	@Override
	public void addSelection(final Rectangle positions) {
		if (positions != null) {
			addSelectionIntoList(positions);
		}
	}
	
	private void addSelectionIntoList(final Rectangle selection) {
		this.selectionsLock.writeLock().lock();
		try {
			if (this.multipleSelectionAllowed) {
				ArrayList<Rectangle> itemsToRemove = null;
				for (final Rectangle r : this.selections) {
					if (selection.intersects(r)) {
						if (r.equals(selection)) {
							break;
						}
						
						final Rectangle intersection = selection.intersection(r);
						if (intersection.equals(r)) {
							// r is a subset of intersection
							if (itemsToRemove == null) {
								itemsToRemove = new ArrayList<Rectangle>();
							}
							
							itemsToRemove.add(r);
						} else if (intersection.equals(selection)) {
							// selection is a subset of r
							break;
						}
					}
				}
				
				if (itemsToRemove != null) {
					this.selections.removeAll(itemsToRemove);
				}
			} else {
				this.selections.clear();
				//as no multiple selection is allowed, ensure that only one column 
				//and one row will be selected
				selection.height = 1;
				selection.width = 1;
			}
			
			this.selections.add(selection);
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
	public void clearSelection(final int columnPosition, final int rowPosition) {
		clearSelection(new Rectangle(columnPosition, rowPosition, 1, 1));
	}
	
	@Override
	public void clearSelection(final Rectangle positions) {
		final List<Rectangle> removedItems = new LinkedList<Rectangle>();
		final List<Rectangle> addedItems = new LinkedList<Rectangle>();
		
		this.selectionsLock.readLock().lock();
		try {
			for (final Rectangle r : this.selections) {
				if (r.intersects(positions)) {
					final Rectangle intersection = positions.intersection(r);
					removedItems.add(r);
					
					final Rectangle topSelection = getTopSelection(intersection, r);
					if (topSelection != null) {
						addedItems.add(topSelection);
					}
					final Rectangle rightSelection = getRightSelection(intersection, r);
					if (rightSelection != null) {
						addedItems.add(rightSelection);
					}
					final Rectangle leftSelection = getLeftSelection(intersection, r);
					if (leftSelection != null) {
						addedItems.add(leftSelection);
					}
					final Rectangle bottomSelection = getBottomSelection(intersection, r);
					if (bottomSelection != null) {
						addedItems.add(bottomSelection);
					}
				}
			}
		} finally {
			this.selectionsLock.readLock().unlock();
		}
		
		if (removedItems.size() > 0) {
			this.selectionsLock.writeLock().lock();
			try {
				this.selections.removeAll(removedItems);
			} finally {
				this.selectionsLock.writeLock().unlock();
			}
			
			removedItems.clear();
		}
		
		if (addedItems.size() > 0) {
			this.selectionsLock.writeLock().lock();
			try {
				this.selections.addAll(addedItems);
			} finally {
				this.selectionsLock.writeLock().unlock();
			}
			
			addedItems.clear();
		}
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
		return this.selections;
	}
	
	// Cell features
	
	@Override
	public boolean isCellPositionSelected(final int columnPosition, final int rowPosition) {
		this.selectionsLock.readLock().lock();
		try {
			final ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition, rowPosition);
			final Rectangle cellRectangle = new Rectangle(
					cell.getOriginColumnPosition(),
					cell.getOriginRowPosition(),
					cell.getColumnSpan(),
					cell.getRowSpan());
			
			for (final Rectangle selectionRectangle : this.selections) {
				if (selectionRectangle.intersects(cellRectangle)) {
					return true;
				}
			}
			
			return false;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	// Column features
	
	@Override
	public RangeList getSelectedColumnPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			final int columnCount = this.selectionLayer.getColumnCount();
			
			for (final Rectangle r : this.selections) {
				if (r.x < columnCount) {
					selected.add(new Range(r.x, Math.min(r.x + r.width, columnCount)));
				}
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
			final int columnCount = this.selectionLayer.getColumnCount();
			
			if (columnPosition >= 0 && columnPosition < columnCount) {
				for (final Rectangle r : this.selections) {
					if (columnPosition >= r.x && columnPosition < r.x + r.width) {
						return true;
					}
				}
			}
			
			return false;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public RangeList getFullySelectedColumnPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			final int rowCount = this.selectionLayer.getRowCount();
			
			if (rowCount > 0) {
				final RangeList selectedColumns = getSelectedColumnPositions();
				for (final Range range : selectedColumns) {
					for (int position = range.start; position < range.end; position++) {
						if (isColumnPositionFullySelected(position, rowCount)) {
							selected.values().add(position);
						}
					}
				}
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
			final int rowCount = this.selectionLayer.getRowCount();
			
			return ((rowCount > 0)
					&& isColumnPositionFullySelected(columnPosition, rowCount) );
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	private boolean isColumnPositionFullySelected(final int columnPosition, final int rowCount) {
		// Aggregate all rows of selection rectangles including the column
		final RangeList selectedRowsInColumn = new RangeList();
		
		for (final Rectangle r : this.selections) {
			if (columnPosition >= r.x && columnPosition < r.x + r.width) {
				selectedRowsInColumn.add(new Range(r.y, r.y + r.height));
			}
		}
		
		final Range range = selectedRowsInColumn.values().getRangeOf(0);
		return (range != null && range.end >= rowCount);
	}
	
	// Row features
	
	@Override
	public int getSelectedRowCount() {
		return getSelectedRowPositions().values().size();
	}
	
	@Override
	public RangeList getSelectedRowPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			final int rowCount = this.selectionLayer.getRowCount();
			
			for (final Rectangle r : this.selections) {
				if (r.y < rowCount) {
					selected.add(new Range(r.y, Math.min(r.y + r.height, rowCount)));
				}
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
			final int rowCount = this.selectionLayer.getRowCount();
			
			if (rowPosition >= 0 && rowPosition < rowCount) {
				for (final Rectangle r : this.selections) {
					if (rowPosition >= r.y && rowPosition < r.y + r.height) {
						return true;
					}
				}
			}
			
			return false;
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public RangeList getFullySelectedRowPositions() {
		this.selectionsLock.readLock().lock();
		try {
			final RangeList selected = new RangeList();
			final int columnCount = this.selectionLayer.getColumnCount();
			
			if (columnCount > 0) {
				final RangeList selectedRows = getSelectedRowPositions();
				for (final Range range : selectedRows) {
					for (int position = range.start; position < range.end; position++) {
						if (isRowPositionFullySelected(position, columnCount)) {
							selected.values().add(position);
						}
					}
				}
			}
			
			return selected;
		}
		finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	@Override
	public boolean isRowPositionFullySelected(final int rowPosition) {
		this.selectionsLock.readLock().lock();
		try {
			final int columnCount = this.selectionLayer.getColumnCount();
			
			return ((columnCount > 0)
					&& isRowPositionFullySelected(rowPosition, columnCount) );
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
	private boolean isRowPositionFullySelected(final int rowPosition, final int columnCount) {
		// Aggregate all columns of selection rectangles including the row
		final RangeList selectedColumnsInRow = new RangeList();
		
		for (final Rectangle r : this.selections) {
			if (rowPosition >= r.y && rowPosition < r.y + r.height) {
				selectedColumnsInRow.add(new Range(r.x, r.x + r.width));
			}
		}
		
		final Range range = selectedColumnsInRow.values().getRangeOf(0);
		return (range != null && range.end >= columnCount);
	}
	
	//-- Object methods --//
	
	@Override
	public String toString() {
		this.selectionsLock.readLock().lock();
		try {
			return this.selections.toString();
		} finally {
			this.selectionsLock.readLock().unlock();
		}
	}
	
}
