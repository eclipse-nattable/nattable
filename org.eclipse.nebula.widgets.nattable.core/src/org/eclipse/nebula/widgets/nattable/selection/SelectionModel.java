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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.graphics.Rectangle;

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

	private final SelectionLayer selectionLayer;
	private boolean multipleSelectionAllowed;
	
	private final List<Rectangle> selections;
	private final ReadWriteLock selectionsLock;

	public SelectionModel(SelectionLayer selectionLayer) {
		this(selectionLayer, true);
	}
	
	public SelectionModel(SelectionLayer selectionLayer, boolean multipleSelectionAllowed) {
		this.selectionLayer = selectionLayer;
		this.multipleSelectionAllowed = multipleSelectionAllowed;
		
		selections = new LinkedList<Rectangle>();
		selectionsLock = new ReentrantReadWriteLock();
	}

	@Override
	public boolean isMultipleSelectionAllowed() {
		return multipleSelectionAllowed;
	}
	
	@Override
	public void setMultipleSelectionAllowed(boolean multipleSelectionAllowed) {
		this.multipleSelectionAllowed = multipleSelectionAllowed;
	}
	
	@Override
	public void addSelection(int columnPosition, int rowPosition) {
		addSelectionIntoList(new Rectangle(columnPosition, rowPosition, 1, 1));
	}

	@Override
	public void addSelection(final Rectangle range) {
		if (range != null) {
			addSelectionIntoList(range);
		}

	}

	private void addSelectionIntoList(Rectangle selection) {
		selectionsLock.writeLock().lock();
		try {
			if (multipleSelectionAllowed) {
				ArrayList<Rectangle> itemsToRemove = null;
				for (Rectangle r : selections) {
					if (selection.intersects(r)) {
						if (r.equals(selection)) {
							break;
						}
	
						Rectangle intersection = selection.intersection(r);
						if (intersection.equals(r)) {
							// r is a subset of intersection
							if (itemsToRemove == null)
								itemsToRemove = new ArrayList<Rectangle>();
	
							itemsToRemove.add(r);
						} else if (intersection.equals(selection)) {
							// selection is a subset of r
							break;
						}
					}
				}
	
				if (itemsToRemove != null) {
					selections.removeAll(itemsToRemove);
				}
			} else {
				selections.clear();
				//as no multiple selection is allowed, ensure that only one column 
				//and one row will be selected
				selection.height = 1;
				selection.width = 1;
			}

			selections.add(selection);
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
		clearSelection(new Rectangle(columnPosition, rowPosition, 1, 1));
	}

	@Override
	public void clearSelection(Rectangle removedSelection) {

		List<Rectangle> removedItems = new LinkedList<Rectangle>();
		List<Rectangle> addedItems = new LinkedList<Rectangle>();

		selectionsLock.readLock().lock();

		try {
			for (Rectangle r : selections) {
				if (r.intersects(removedSelection)) {
					Rectangle intersection = removedSelection.intersection(r);
					removedItems.add(r);

					Rectangle topSelection = getTopSelection(intersection, r);
					if (topSelection != null) {
						addedItems.add(topSelection);
					}

					Rectangle rightSelection = getRightSelection(intersection, r);
					if (rightSelection != null)
						addedItems.add(rightSelection);

					Rectangle leftSelection = getLeftSelection(intersection, r);
					if (leftSelection != null)
						addedItems.add(leftSelection);

					Rectangle bottomSelection = getBottomSelection(intersection, r);
					if (bottomSelection != null)
						addedItems.add(bottomSelection);
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}

		if (removedItems.size() > 0) {
			selectionsLock.writeLock().lock();
			try {
				selections.removeAll(removedItems);
			} finally {
				selectionsLock.writeLock().unlock();
			}

			removedItems.clear();
		}

		if (addedItems.size() > 0) {
			selectionsLock.writeLock().lock();
			try {
				selections.addAll(addedItems);
			} finally {
				selectionsLock.writeLock().unlock();
			}

			addedItems.clear();
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
		return selections;
	}
	
	// Cell features

	@Override
	public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
		selectionsLock.readLock().lock();

		try {
			ILayerCell cell = selectionLayer.getCellByPosition(columnPosition, rowPosition);
			Rectangle cellRectangle =
					new Rectangle(
							cell.getOriginColumnPosition(),
							cell.getOriginRowPosition(),
							cell.getColumnSpan(),
							cell.getRowSpan());
			
			for (Rectangle selectionRectangle : selections) {
				if (selectionRectangle.intersects(cellRectangle))
					return true;
			}
		} finally {
			selectionsLock.readLock().unlock();
		}

		return false;
	}
	
	// Column features

	@Override
	public int[] getSelectedColumnPositions() {
		TreeSet<Integer> selectedColumns = new TreeSet<Integer>();

		selectionsLock.readLock().lock();

		int columnCount = selectionLayer.getColumnCount();
		try {
			for (Rectangle r : selections) {
				int startColumn = r.x;
				if (startColumn < columnCount) {
					int numColumns = (r.x + r.width <= columnCount) ? r.width : columnCount - r.x;
	
					// Change from row < startRow to row < startRow+numRows
					for (int column = startColumn; column < startColumn + numColumns; column++) {
						selectedColumns.add(Integer.valueOf(column));
					}
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}

		// Convert to array
		return ObjectUtils.asIntArray(selectedColumns);
	}
	
	@Override
	public boolean isColumnPositionSelected(int columnPosition) {
		selectionsLock.readLock().lock();

		try {
			for (int column : getSelectedColumnPositions()) {
				if (column == columnPosition) {
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
		final int[] selectedColumns = getSelectedColumnPositions();
		int[] columnsToHide = new int[selectedColumns.length];
		int index = 0;
		for (int columnPosition : selectedColumns) {
			if (isColumnPositionFullySelected(columnPosition, columnHeight)) {
				columnsToHide[index++] = columnPosition;
			}
		}

		return index > 0  ? ArrayUtils.subarray(columnsToHide, 0, index) : new int[0];
	}

	/**
	 * Are all cells in this column selected?
	 * Different selection rectangles might aggregate to cover the entire column.
	 * We need to take into account any overlapping selections or any selection rectangles
	 * contained within each other.
	 * 
	 * See the related tests for a better understanding.
	 */
	@Override
	public boolean isColumnPositionFullySelected(int columnPosition, int columnHeight) {
		selectionsLock.readLock().lock();

		try {
			// Aggregate all rectangles in the column which are in the selection model
			List<Rectangle> selectedRectanglesInColumn = new ArrayList<Rectangle>();

			// If X is same add up the height of the selected area
			for (Rectangle r : selections) {
				// Column is within the bounds of the selcted rectangle
				if (columnPosition >= r.x && columnPosition < r.x + r.width) {
					selectedRectanglesInColumn.add(new Rectangle(columnPosition, r.y, 1, r.height));
				}
			}
			if (selectedRectanglesInColumn.isEmpty()) {
				return false;
			}
			sortByY(selectedRectanglesInColumn);
			Rectangle finalRectangle = new Rectangle(columnPosition, selectedRectanglesInColumn.get(0).y, 0, 0);

			// Ensure that selections in the column are contiguous and cover the entire column
			for (int i = 0; i < selectedRectanglesInColumn.size(); i++) {
				Rectangle rectangle = selectedRectanglesInColumn.get(i);
				if(contains(finalRectangle, rectangle)){
					continue;
				}
				if (i > 0) {
					Rectangle previousRect = selectedRectanglesInColumn.get(i - 1);
					if (rectangle.union(previousRect).height > (rectangle.height + previousRect.height)) {
						// Rectangles not contiguous
						return false;
					}
				}
				// Union will resolve any overlaping area
				finalRectangle = finalRectangle.union(rectangle);
			}
			return finalRectangle.height >= columnHeight;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}
	
	// Row features

	@Override
	public int getSelectedRowCount() {
		Set<Range> selectedRows = getSelectedRowPositions();
		int count = 0;
		for (Range range : selectedRows) {
			count += range.end - range.start;
		}
		return count;
	}

	@Override
	public Set<Range> getSelectedRowPositions() {
		Set<Range> selectedRowsRange = new HashSet<Range>();

		selectionsLock.readLock().lock();

		int rowCount = selectionLayer.getRowCount();
		try {
			for (Rectangle r : selections) {
				if (r.y < rowCount) {
					int height = (r.y + r.height <= rowCount) ? r.height : rowCount - r.y;
					selectedRowsRange.add(new Range(r.y, r.y + height));
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}

		ArrayList<Range> ranges = new ArrayList<Range>(selectedRowsRange);
		Range.sortByStart(ranges);
		List<Range> uniqueRanges = new ArrayList<Range>();

		// Adjust for overlaps - between consecutive selections
		for (int i = 0; i < ranges.size(); i++) {
			if (i > 0) {
				Range previousRange = ranges.get(i - 1);
				Range currentRange = ranges.get(i);
				if (previousRange.overlap(currentRange) || (previousRange.end == currentRange.start)) {
					int largerRangeEnd = (previousRange.end > currentRange.end) ? previousRange.end : currentRange.end;
					uniqueRanges.get(uniqueRanges.size() - 1).end = largerRangeEnd;
					ranges.get(i).end = largerRangeEnd;
				} else {
					uniqueRanges.add(ranges.get(i));
				}
			} else {
				uniqueRanges.add(ranges.get(i));
			}
		}
		return new HashSet<Range>(uniqueRanges);
	}

	@Override
	public boolean isRowPositionSelected(int rowPosition) {
		selectionsLock.readLock().lock();

		try {
			for (Range rowRange : getSelectedRowPositions()) {
				if (rowRange.contains(rowPosition)) {
					return true;
				}
			}
		} finally {
			selectionsLock.readLock().unlock();
		}

		return false;
	}

	@Override
	public int[] getFullySelectedRowPositions(int rowWidth) {
		final Set<Range> selectedRows = getSelectedRowPositions();
		int[] fullySelectedRows = new int[getSelectedRowCount()];
		int index = 0;

		for (Range rowRange : selectedRows) {
			for (int i = rowRange.start; i < rowRange.end; i++) {
				if (isRowPositionFullySelected(i, rowWidth)) {
					fullySelectedRows[index++] = i;
				}
			}
		}

		return index > 0 ? ArrayUtils.subarray(fullySelectedRows, 0, index) : new int[0];
	}

	@Override
	public boolean isRowPositionFullySelected(int rowPosition, int rowWidth) {
		selectionsLock.readLock().lock();

		try {
			// Aggregate all rectangles in the row which are in the selection model
			List<Rectangle> selectedRectanglesInRow = new ArrayList<Rectangle>();

			// If X is same add up the width of the selected area
			for (Rectangle r : selections) {
				// Row is within the bounds of the selcted rectangle
				if (rowPosition >= r.y && rowPosition < r.y + r.height) {
					selectedRectanglesInRow.add(new Rectangle(r.x, rowPosition, r.width, 1));
				}
			}
			if (selectedRectanglesInRow.isEmpty()) {
				return false;
			}
			sortByX(selectedRectanglesInRow);
			Rectangle finalRectangle = new Rectangle(selectedRectanglesInRow.get(0).x, rowPosition, 0, 0);

			// Ensure that selections in the row are contiguous and cover the entire row
			for (int i = 0; i < selectedRectanglesInRow.size(); i++) {
				Rectangle rectangle = selectedRectanglesInRow.get(i);
				if(contains(finalRectangle, rectangle)){
					continue;
				}
				if (i > 0) {
					Rectangle previousRect = selectedRectanglesInRow.get(i - 1);
					if (rectangle.union(previousRect).width > (rectangle.width + previousRect.width)) {
						// Rectangles not contiguous
						return false;
					}
				}
				// Union will resolve any overlaping area
				finalRectangle = finalRectangle.union(rectangle);
			}
			return finalRectangle.width >= rowWidth;
		} finally {
			selectionsLock.readLock().unlock();
		}
	}

	protected boolean contains(Rectangle containerRectangle, Rectangle rectangle) {
		Rectangle union = containerRectangle.union(rectangle);
		return union.equals(containerRectangle); 
	}

	protected void sortByX(List<Rectangle> selectionRectanglesInRow) {
		Collections.sort(selectionRectanglesInRow, new Comparator<Rectangle>(){
			@Override
			public int compare(Rectangle rectangle1, Rectangle rectangle2) {
				return new Integer(rectangle1.x).compareTo(new Integer(rectangle2.x)) ;
			}
		});
	}

	protected void sortByY(List<Rectangle> selectionRectanglesInColumn) {
		Collections.sort(selectionRectanglesInColumn, new Comparator<Rectangle>(){
			@Override
			public int compare(Rectangle rectangle1, Rectangle rectangle2) {
				return new Integer(rectangle1.y).compareTo(new Integer(rectangle2.y)) ;
			}
		});
	}

	private Rectangle getLeftSelection(Rectangle intersection, Rectangle selection) {
		if (intersection.x > selection.x) {
			Rectangle leftSelection = new Rectangle(selection.x, selection.y,
					intersection.x - selection.x, selection.height);
			return leftSelection;
		}

		return null;
	}

	private Rectangle getRightSelection(Rectangle intersection, Rectangle selection) {
		int newX = intersection.x + intersection.width;

		if (newX < selection.x + selection.width) {
			Rectangle rightSelection = new Rectangle(newX, selection.y,
					selection.x + selection.width - newX, selection.height);

			return rightSelection;
		}

		return null;
	}

	private Rectangle getTopSelection(Rectangle intersection,
			Rectangle selectoin) {
		if (intersection.y > selectoin.y) {
			Rectangle topSelection = new Rectangle(selectoin.x, selectoin.y,
					selectoin.width, intersection.y - selectoin.y);
			return topSelection;
		}
		return null;
	}

	private Rectangle getBottomSelection(Rectangle intersection,
			Rectangle selection) {
		int newY = intersection.y + intersection.height;

		if (newY < selection.y + selection.height) {
			Rectangle bottomSelection = new Rectangle(selection.x, newY,
					selection.width, selection.y + selection.height - newY);
			return bottomSelection;
		}

		return null;
	}

	// Object methods

	@Override
	public String toString() {
		selectionsLock.readLock().lock();

		try {
			return selections.toString();
		} finally {
			selectionsLock.readLock().unlock();
		}
	}
	
}
