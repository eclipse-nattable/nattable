/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionModel;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SelectionModelTest {

	private SelectionModel model;

	@Before
	public void before() {
		SelectionLayer selectionLayer = new SelectionLayer(new DataLayerFixture(100, 100, 100, 40));
		model = new SelectionModel(selectionLayer);
	}
	
	@Test
	public void isCellSelected() {
		assertFalse("Empty model should have no selection.", model.isCellPositionSelected(0, 0));
	}
	
	@Test
	public void addCellSelection() {
		model.addSelection(0,0);
		
		assertTrue("Failed to add selection.", model.isCellPositionSelected(0, 0));
	}

	@Test
	public void isColumnFullySelected() throws Exception {
		model.addSelection(new Rectangle(3, 0, 10, 10));

		assertFalse(model.isColumnPositionFullySelected(3, 11));
		assertTrue(model.isColumnPositionFullySelected(3, 10));

		model.clearSelection(3, 1);
		assertFalse(model.isColumnPositionFullySelected(3, 10));
	}

	@Test
	public void isColumnFullySelectedForContiguousRectangles() throws Exception {
		model.addSelection(new Rectangle(0, 0, 10, 10));
		model.addSelection(new Rectangle(5, 10, 10, 10));

		assertTrue(model.isColumnPositionFullySelected(5, 20));
	}

	@Test
	public void isColumnFullySelectedForNonContiguousRectangles() throws Exception {
		model.addSelection(new Rectangle(0, 0, 10, 10));
		model.addSelection(new Rectangle(5, 5, 10, 8));
		model.addSelection(new Rectangle(5, 15, 10, 5));

		assertFalse(model.isColumnPositionFullySelected(5, 20));
	}

	@Test
	public void isColumnFullySelectedForOverlapingRectangles() throws Exception {
		model.addSelection(new Rectangle(0, 0, 10, 10));
		model.addSelection(new Rectangle(5, 5, 10, 8));

		assertTrue(model.isColumnPositionFullySelected(5, 13));
	}

	@Test
	public void isColumnFullySelectedWhenIndividualCellsSelected() throws Exception {
		model.addSelection(new Rectangle(1, 5, 1, 1));
		model.addSelection(new Rectangle(1, 10, 1, 1));

		assertFalse(model.isColumnPositionFullySelected(1, 10));
	}

	@Test
	public void isColumnFullySelectedWhenLastCellSelected() throws Exception {
		model.addSelection(new Rectangle(1, 5, 1, 1));

		assertFalse(model.isColumnPositionFullySelected(1, 6));
		assertFalse(model.isColumnPositionFullySelected(1, 1000000));
	}
	
	@Test
	public void isRowFullySelected() throws Exception {
		model.addSelection(new Rectangle(0,3,10,10));
		assertTrue(model.isRowPositionFullySelected(3, 10));
	}

	@Test
	public void isRowFullySelectedWhenMultipleRowsAndColumnsAreSelected() throws Exception {
		//Rows 3, 6 fully selected
		model.addSelection(new Rectangle(0,3,10,1));
		model.addSelection(new Rectangle(0,6,10,1));
		
		//Column 2, 5 fully selected
		model.addSelection(new Rectangle(2, 0,1,10));
		model.addSelection(new Rectangle(5, 0,1,10));
		
		assertTrue(model.isRowPositionFullySelected(6, 10));
		assertTrue(model.isRowPositionFullySelected(3, 10));
		
		// Remove Column 2
		model.clearSelection(new Rectangle(2, 0,1,10));
		assertFalse(model.isRowPositionFullySelected(6, 10));
		assertFalse(model.isRowPositionFullySelected(3, 10));
		
		//Add column 2 again
		model.addSelection(new Rectangle(2, 0,1,10));
		assertTrue(model.isRowPositionFullySelected(6, 10));
		assertTrue(model.isRowPositionFullySelected(3, 10));
	}

	@Test
	public void isRowNotFullySelected() throws Exception {
		model.addSelection(new Rectangle(0,3,10,10));
		
		assertFalse(model.isRowPositionFullySelected(3, 11));
	}
	
	@Test
	public void contains() throws Exception {
		assertTrue(model.contains(new Rectangle(0, 0, 10, 10), new Rectangle(1, 1, 5, 5)));
		assertTrue(model.contains(new Rectangle(0, 0, 10, 1), new Rectangle(5, 0, 1, 1)));
		
		assertFalse(model.contains(new Rectangle(0, 6, 0, 0), new Rectangle(2, 6, 1, 1)));
	}
	
	@Test
	public void isMltipleCol() throws Exception {
		model.addSelection(new Rectangle(1,0,1,20));
		model.addSelection(new Rectangle(2,0,1,20));
		model.addSelection(new Rectangle(3,0,1,20));
		
		assertFalse(model.isColumnPositionFullySelected(1, 21));
		assertTrue(model.isColumnPositionFullySelected(2, 20));
	}
	
	@Test
	public void shouldReturnListOfFullySelectedColumns() throws Exception {
		model.addSelection(new Rectangle(1,0,1,20));
		model.addSelection(new Rectangle(2,10,1,4));
		model.addSelection(new Rectangle(3,0,1,20));
		
		int[] fullySelectedColumns = model.getFullySelectedColumnPositions(20);
		Assert.assertEquals(2, fullySelectedColumns.length);
		Assert.assertEquals(1, fullySelectedColumns[0]);
		Assert.assertEquals(3, fullySelectedColumns[1]);
	}
	
	@Test 
	public void shouldReturnListOfFullySelectedRows() throws Exception {
		int[] fullySelectedRows = model.getFullySelectedRowPositions(20);
		Assert.assertEquals(0, fullySelectedRows.length);

		model.addSelection(new Rectangle(0,1,20,1));
		model.addSelection(new Rectangle(3,2,4,1));
		model.addSelection(new Rectangle(0,3,20,1));
		
		fullySelectedRows = model.getFullySelectedRowPositions(20);
		Assert.assertEquals(2, fullySelectedRows.length);
		Assert.assertEquals(1, fullySelectedRows[0]);
		Assert.assertEquals(3, fullySelectedRows[1]);
	}
	
	@Test
	public void addMultipleAdjacentCellSelection() {
		
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				model.addSelection(col, row);
			}
		}
		
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				assertTrue("Failed to add selection [" + row + ", " + col + "]", model.isCellPositionSelected(col, row));
			}
		}
		
	}
	
	@Test
	public void addMultipleDisjointCellSelection() {
		model.addSelection(0, 0);
		model.addSelection(2, 0);
		model.addSelection(0, 2);
		
		assertTrue("Failed to add selection [0, 0]", model.isCellPositionSelected(0, 0));
		assertTrue("Failed to add selection [0, 2]", model.isCellPositionSelected(2, 0));
		assertTrue("Failed to add selection [2, 0]", model.isCellPositionSelected(0, 2));
		
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(1, 0));
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(0, 1));
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(1, 1));
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(2, 1));
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(1, 2));
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(2, 2));
		assertFalse("Added too many cells to the range", model.isCellPositionSelected(3, 3));
	}
	
	@Test
	public void clearSelection() {
		model.addSelection(0, 0);
		
		model.clearSelection();
		
		assertFalse("Failed to clear selection", model.isCellPositionSelected(0, 0));
	}
	
	@Test
	public void addRangeSelection() {
		int col = 2;
		int row = 3;
		int numCols = 4;
		int numRows = 5;
		model.addSelection(new Rectangle(col, row, numCols, numRows));
		
		for (int rowIndex = row; rowIndex < row + numRows; rowIndex++) {
			for (int colIndex = col; colIndex < col + numCols; colIndex++) {
				assertTrue("Failed to add range [" + rowIndex + ", " + colIndex + "]", model.isCellPositionSelected(colIndex, rowIndex));
			}
		}
		
		assertFalse("Added too many cells from range", model.isCellPositionSelected(col, row + numRows));
	}

	@Test
	public void addNullRangeSelection() {
		model.addSelection(null);
		
		assertTrue(model.isEmpty());
	}
	
	@Test
	public void removeSingleCell() {
		model.addSelection(0, 0);
		model.clearSelection(0, 0);
		
		assertFalse("Failed to remove selection [0, 0].", model.isCellPositionSelected(0, 0));
	}
	
	@Test
	public void removeSingleCellAfterMultipleAdds() {
		model.addSelection(0, 0);
		model.addSelection(1, 1);
		model.addSelection(2, 1);
		model.addSelection(2, 3);
		model.clearSelection(1, 1);
		
		assertFalse("Failed to remove selection [1, 1].", model.isCellPositionSelected(1, 1));
	}
	
	private void removeSingleCellFromRange(int col, int row, int numCols, int numRows, int removedRow, int removedColumn) {
		model.addSelection(new Rectangle(col, row, numCols, numRows));

		model.clearSelection(removedColumn, removedRow);
		
		assertFalse("Failed to remove selection [" + removedRow + ", " + removedColumn + "]", model.isCellPositionSelected(removedColumn, removedRow));
		
		for (int rowIndex = row; rowIndex < row + numRows; rowIndex++) {
			for (int colIndex = col; colIndex < col + numCols; colIndex++) {
				
				if (!(rowIndex == removedRow && colIndex == removedColumn))
					assertTrue("Failed to add range [" + rowIndex + ", " + colIndex + "]", model.isCellPositionSelected(colIndex, rowIndex));
			}
		}
	}
	
	private void removeRangeFromRange(int col, int row, int numCols,
			int numRows, int removedRow, int removedColumn, int removedNumCols,
			int removedNumRows) {
		model.addSelection(new Rectangle(col, row, numCols, numRows));
		
		Rectangle removedSelection = new Rectangle(removedColumn, removedRow, removedNumCols, removedNumRows);
		model.clearSelection(removedSelection);
		
		for (int rowIndex = removedRow; rowIndex < removedRow + removedNumRows; rowIndex++) {
			for (int colIndex = removedColumn; colIndex < removedColumn + removedNumCols; colIndex++) {
				assertFalse("Failed to remove selection [" + rowIndex + ", " + colIndex + "]", model.isCellPositionSelected(colIndex, rowIndex));
			}
		}
		
		for (int rowIndex = row; rowIndex < row + numRows; rowIndex++) {
			for (int colIndex = col; colIndex < col + numCols; colIndex++) {
				
				if (!removedSelection.contains(colIndex, rowIndex))
					assertTrue("Failed to add range [" + rowIndex + ", " + colIndex + "]", model.isCellPositionSelected(colIndex, rowIndex));
			}
		}
	}
	
	@Test
	public void removeSingleCellFrom1x1Range() {
		int col = 2;
		int row = 3;
		int numCols = 1;
		int numRows = 1;
		
		int removedColumn = 2;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeSingleCellFromBeginning1x10Range() {
		int col = 2;
		int row = 3;
		int numCols = 10;
		int numRows = 1;
		
		int removedColumn = 2;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeSingleCellFromEnd1x10Range() {
		int col = 2;
		int row = 3;
		int numCols = 10;
		int numRows = 1;
		
		int removedColumn = 11;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}

	@Test
	public void removeSingleCellFromMiddle1x10Range() {
		int col = 2;
		int row = 3;
		int numCols = 10;
		int numRows = 1;
		
		int removedColumn = 8;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeSingleCellFromBeginning10x1Range() {
		int col = 2;
		int row = 3;
		int numCols = 1;
		int numRows = 10;
		
		int removedColumn = 2;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeSingleCellFromEnd10x1Range() {
		int col = 2;
		int row = 3;
		int numCols = 1;
		int numRows = 10;
		
		int removedColumn = 2;
		int removedRow = 12;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}

	@Test
	public void removeSingleCellFromMiddle10x1Range() {
		int col = 2;
		int row = 3;
		int numCols = 1;
		int numRows = 10;
		
		int removedColumn = 2;
		int removedRow = 7;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeTopLeftFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 2;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeTopRightFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 4;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeTopMiddleFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 3;
		int removedRow = 3;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeBottomLeftFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 2;
		int removedRow = 6;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeBottomRightFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 4;
		int removedRow = 6;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeBottomMiddleFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 3;
		int removedRow = 6;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeMidLeftFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 2;
		int removedRow = 5;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeMidRightFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 4;
		int removedRow = 5;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void removeMidMiddleFrom3x4Range() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 3;
		int removedRow = 5;
		
		removeSingleCellFromRange(col, row, numCols, numRows, removedRow, removedColumn);
	}
	
	@Test
	public void getSelectedRows() {
		model.addSelection(10, 1);
		model.addSelection(10, 2);
		model.addSelection(10, 7);
		model.addSelection(new Rectangle(10, 50, 5, 10));

		List<Range> selectedRows = ObjectUtils.asList(model.getSelectedRowPositions());

		assertTrue(selectedRows.contains(new Range(1, 3)));
		assertTrue(selectedRows.contains(new Range(7, 8)));
		assertTrue(selectedRows.contains(new Range(50, 60)));
	}

	@Test
	public void getSelectedRowsForOverlapingSelections() {
		model.addSelection(new Rectangle(10, 3, 5, 2));
		model.addSelection(new Rectangle(10, 4, 5, 10));
		model.addSelection(new Rectangle(10, 20, 5, 10));
		
		List<Range> selectedRows = ObjectUtils.asList(model.getSelectedRowPositions());
		assertEquals(2, selectedRows.size());
		
		assertTrue(selectedRows.contains(new Range(3, 14)));
		assertTrue(selectedRows.contains(new Range(20, 30)));
	}

	@Test
	public void getSelectedRowsForLargeNumberOfSelections() {
		model.addSelection(1, 10);
		model.addSelection(new Rectangle(5, 1, 1, 100));

		List<Range> selectedRows = ObjectUtils.asList(model.getSelectedRowPositions());
		assertEquals(1, selectedRows.size());

		assertTrue(selectedRows.contains(new Range(1, 100)));
	}
	
	@Test
	public void getSelectedRowCount() throws Exception {
		model.addSelection(new Rectangle(10, 3, 1, 1));
		model.addSelection(new Rectangle(10, 10, 1, 1));
		model.addSelection(new Rectangle(10, 5, 1, 20));
		
		assertEquals(21, model.getSelectedRowCount());
	}

	@Test
	public void isRowPositionSelected() throws Exception {
		model.addSelection(1, 10);
		model.addSelection(new Rectangle(5, 1, 100, 10000000));

		assertTrue(model.isRowPositionSelected(10));
		assertTrue(model.isRowPositionSelected(99));
	}
	
	@Test
	public void getSelectedColumns() {
		int [] columns = new int [] {
			1,4,3
		};
		
		for (int column : columns) {
			model.addSelection(column, column % 3);
		}
		
		int [] selectedColumns = model.getSelectedColumnPositions();
		
		Arrays.sort(columns);
		
		assertEquals(Arrays.toString(columns), Arrays.toString(selectedColumns));
	}
	
	@Test
	public void removeFromEmptySelection() {
		Rectangle removedSelection = new Rectangle(0, 0, 10, 10);
		model.clearSelection(removedSelection);
		
		assertTrue("Removal from empty selection failed.", model.isEmpty());
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				assertFalse("Selection was not removed [" + i + ", " + j + "]", model.isCellPositionSelected(j, i));
			}
		}
	}
	
	@Test
	public void removeTopRange() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 4;
		
		int removedColumn = 2;
		int removedRow = 2;
		int removedNumCols = 3;
		int removedNumRows = 3;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
	}

	@Test
	public void removeMidRange() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 10;
		
		int removedColumn = 2;
		int removedRow = 7;
		int removedNumCols = 3;
		int removedNumRows = 3;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
	}
	
	@Test
	public void removeBottomRange() {
		int col = 2;
		int row = 3;
		int numCols = 3;
		int numRows = 10;
		
		int removedColumn = 2;
		int removedRow = 7;
		int removedNumCols = 3;
		int removedNumRows = 7;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
	}

	@Test
	public void removeLeftRange() {
		int col = 2;
		int row = 3;
		int numCols = 6;
		int numRows = 4;
		
		int removedColumn = 1;
		int removedRow = 3;
		int removedNumCols = 3;
		int removedNumRows = 4;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
	}
	
	@Test
	public void removeMiddleRange() {
		int col = 2;
		int row = 3;
		int numCols = 6;
		int numRows = 4;
		
		int removedColumn = 4;
		int removedRow = 3;
		int removedNumCols = 2;
		int removedNumRows = 4;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
		
	}

	@Test
	public void removeRightRange() {
		int col = 2;
		int row = 3;
		int numCols = 6;
		int numRows = 4;
		
		int removedColumn = 5;
		int removedRow = 3;
		int removedNumCols = 5;
		int removedNumRows = 4;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
		
	}
	
	@Test
	public void removeTopFromTwoRows() {
		int col = 0;
		int row = 0;
		int numCols = 10;
		int numRows = 2;
		
		int removedColumn = col;
		int removedRow = row;
		int removedNumCols = numCols;
		int removedNumRows = 1;
		
		removeRangeFromRange(col, row, numCols, numRows, removedRow, removedColumn, removedNumCols, removedNumRows);
	}
	
	@Test
	public void sortByY() throws Exception {
		List<Rectangle> rectangles = new ArrayList<Rectangle>();
		rectangles.add(new Rectangle(0, 3, 1, 1));
		rectangles.add(new Rectangle(0, 5, 1, 1));
		rectangles.add(new Rectangle(0, 1, 1, 1));
		rectangles.add(new Rectangle(0, 13, 1, 1));

		model.sortByY(rectangles);
		
		Assert.assertEquals(1, rectangles.get(0).y);
		Assert.assertEquals(3, rectangles.get(1).y);
		Assert.assertEquals(5, rectangles.get(2).y);
		Assert.assertEquals(13, rectangles.get(3).y);
	}
}
