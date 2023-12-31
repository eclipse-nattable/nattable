/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SelectionModelTest {

    private SelectionModel model;

    @BeforeEach
    public void before() {
        SelectionLayer selectionLayer = new SelectionLayer(
                new DataLayerFixture(100, 100, 100, 40));
        this.model = new SelectionModel(selectionLayer);
    }

    @Test
    public void isCellSelected() {
        assertFalse(this.model.isCellPositionSelected(0, 0), "Empty model should have no selection.");
    }

    @Test
    public void addCellSelection() {
        this.model.addSelection(0, 0);

        assertTrue(this.model.isCellPositionSelected(0, 0), "Failed to add selection.");
    }

    @Test
    public void isColumnFullySelected() {
        this.model.addSelection(new Rectangle(3, 0, 10, 10));

        assertFalse(this.model.isColumnPositionFullySelected(3, 11));
        assertTrue(this.model.isColumnPositionFullySelected(3, 10));

        this.model.clearSelection(3, 1);
        assertFalse(this.model.isColumnPositionFullySelected(3, 10));
    }

    @Test
    public void isColumnFullySelectedForContiguousRectangles() {
        this.model.addSelection(new Rectangle(0, 0, 10, 10));
        this.model.addSelection(new Rectangle(5, 10, 10, 10));

        assertTrue(this.model.isColumnPositionFullySelected(5, 20));
    }

    @Test
    public void isColumnFullySelectedForNonContiguousRectangles() {
        this.model.addSelection(new Rectangle(0, 0, 10, 10));
        this.model.addSelection(new Rectangle(5, 5, 10, 8));
        this.model.addSelection(new Rectangle(5, 15, 10, 5));

        assertFalse(this.model.isColumnPositionFullySelected(5, 20));
    }

    @Test
    public void isColumnFullySelectedForOverlapingRectangles() {
        this.model.addSelection(new Rectangle(0, 0, 10, 10));
        this.model.addSelection(new Rectangle(5, 5, 10, 8));

        assertTrue(this.model.isColumnPositionFullySelected(5, 13));
    }

    @Test
    public void isColumnFullySelectedWhenIndividualCellsSelected() {
        this.model.addSelection(new Rectangle(1, 5, 1, 1));
        this.model.addSelection(new Rectangle(1, 10, 1, 1));

        assertFalse(this.model.isColumnPositionFullySelected(1, 10));
    }

    @Test
    public void isColumnFullySelectedWhenLastCellSelected() {
        this.model.addSelection(new Rectangle(1, 5, 1, 1));

        assertFalse(this.model.isColumnPositionFullySelected(1, 6));
        assertFalse(this.model.isColumnPositionFullySelected(1, 1000000));
    }

    @Test
    public void isRowFullySelected() {
        this.model.addSelection(new Rectangle(0, 3, 10, 10));
        assertTrue(this.model.isRowPositionFullySelected(3, 10));
    }

    @Test
    public void isRowFullySelectedWhenMultipleRowsAndColumnsAreSelected() {
        // Rows 3, 6 fully selected
        this.model.addSelection(new Rectangle(0, 3, 10, 1));
        this.model.addSelection(new Rectangle(0, 6, 10, 1));

        // Column 2, 5 fully selected
        this.model.addSelection(new Rectangle(2, 0, 1, 10));
        this.model.addSelection(new Rectangle(5, 0, 1, 10));

        assertTrue(this.model.isRowPositionFullySelected(6, 10));
        assertTrue(this.model.isRowPositionFullySelected(3, 10));

        // Remove Column 2
        this.model.clearSelection(new Rectangle(2, 0, 1, 10));
        assertFalse(this.model.isRowPositionFullySelected(6, 10));
        assertFalse(this.model.isRowPositionFullySelected(3, 10));

        // Add column 2 again
        this.model.addSelection(new Rectangle(2, 0, 1, 10));
        assertTrue(this.model.isRowPositionFullySelected(6, 10));
        assertTrue(this.model.isRowPositionFullySelected(3, 10));
    }

    @Test
    public void isRowNotFullySelected() {
        this.model.addSelection(new Rectangle(0, 3, 10, 10));

        assertFalse(this.model.isRowPositionFullySelected(3, 11));
    }

    @Test
    public void contains() {
        assertTrue(this.model.contains(new Rectangle(0, 0, 10, 10), new Rectangle(1, 1, 5, 5)));
        assertTrue(this.model.contains(new Rectangle(0, 0, 10, 1), new Rectangle(5, 0, 1, 1)));

        assertFalse(this.model.contains(new Rectangle(0, 6, 0, 0), new Rectangle(2, 6, 1, 1)));
    }

    @Test
    public void isMltipleCol() {
        this.model.addSelection(new Rectangle(1, 0, 1, 20));
        this.model.addSelection(new Rectangle(2, 0, 1, 20));
        this.model.addSelection(new Rectangle(3, 0, 1, 20));

        assertFalse(this.model.isColumnPositionFullySelected(1, 21));
        assertTrue(this.model.isColumnPositionFullySelected(2, 20));
    }

    @Test
    public void shouldReturnListOfFullySelectedColumns() {
        this.model.addSelection(new Rectangle(1, 0, 1, 20));
        this.model.addSelection(new Rectangle(2, 10, 1, 4));
        this.model.addSelection(new Rectangle(3, 0, 1, 20));

        int[] fullySelectedColumns = this.model.getFullySelectedColumnPositions(20);
        assertEquals(2, fullySelectedColumns.length);
        assertEquals(1, fullySelectedColumns[0]);
        assertEquals(3, fullySelectedColumns[1]);
    }

    @Test
    public void shouldReturnListOfFullySelectedRows() {
        int[] fullySelectedRows = this.model.getFullySelectedRowPositions(20);
        assertEquals(0, fullySelectedRows.length);

        this.model.addSelection(new Rectangle(0, 1, 20, 1));
        this.model.addSelection(new Rectangle(3, 2, 4, 1));
        this.model.addSelection(new Rectangle(0, 3, 20, 1));

        fullySelectedRows = this.model.getFullySelectedRowPositions(20);
        assertEquals(2, fullySelectedRows.length);
        assertEquals(1, fullySelectedRows[0]);
        assertEquals(3, fullySelectedRows[1]);
    }

    @Test
    public void addMultipleAdjacentCellSelection() {

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.model.addSelection(col, row);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                assertTrue(this.model.isCellPositionSelected(col, row),
                        "Failed to add selection [" + row + ", " + col + "]");
            }
        }

    }

    @Test
    public void addMultipleDisjointCellSelection() {
        this.model.addSelection(0, 0);
        this.model.addSelection(2, 0);
        this.model.addSelection(0, 2);

        assertTrue(this.model.isCellPositionSelected(0, 0), "Failed to add selection [0, 0]");
        assertTrue(this.model.isCellPositionSelected(2, 0), "Failed to add selection [0, 2]");
        assertTrue(this.model.isCellPositionSelected(0, 2), "Failed to add selection [2, 0]");

        assertFalse(this.model.isCellPositionSelected(1, 0), "Added too many cells to the range");
        assertFalse(this.model.isCellPositionSelected(0, 1), "Added too many cells to the range");
        assertFalse(this.model.isCellPositionSelected(1, 1), "Added too many cells to the range");
        assertFalse(this.model.isCellPositionSelected(2, 1), "Added too many cells to the range");
        assertFalse(this.model.isCellPositionSelected(1, 2), "Added too many cells to the range");
        assertFalse(this.model.isCellPositionSelected(2, 2), "Added too many cells to the range");
        assertFalse(this.model.isCellPositionSelected(3, 3), "Added too many cells to the range");
    }

    @Test
    public void clearSelection() {
        this.model.addSelection(0, 0);

        this.model.clearSelection();

        assertFalse(this.model.isCellPositionSelected(0, 0), "Failed to clear selection");
    }

    @Test
    public void addRangeSelection() {
        int col = 2;
        int row = 3;
        int numCols = 4;
        int numRows = 5;
        this.model.addSelection(new Rectangle(col, row, numCols, numRows));

        for (int rowIndex = row; rowIndex < row + numRows; rowIndex++) {
            for (int colIndex = col; colIndex < col + numCols; colIndex++) {
                assertTrue(this.model.isCellPositionSelected(colIndex, rowIndex),
                        "Failed to add range [" + rowIndex + ", " + colIndex + "]");
            }
        }

        assertFalse(this.model.isCellPositionSelected(col, row + numRows), "Added too many cells from range");
    }

    @Test
    public void addNullRangeSelection() {
        this.model.addSelection(null);

        assertTrue(this.model.isEmpty());
    }

    @Test
    public void removeSingleCell() {
        this.model.addSelection(0, 0);
        this.model.clearSelection(0, 0);

        assertFalse(this.model.isCellPositionSelected(0, 0), "Failed to remove selection [0, 0].");
    }

    @Test
    public void removeSingleCellAfterMultipleAdds() {
        this.model.addSelection(0, 0);
        this.model.addSelection(1, 1);
        this.model.addSelection(2, 1);
        this.model.addSelection(2, 3);
        this.model.clearSelection(1, 1);

        assertFalse(this.model.isCellPositionSelected(1, 1), "Failed to remove selection [1, 1].");
    }

    private void removeSingleCellFromRange(
            int col, int row, int numCols, int numRows,
            int removedRow, int removedColumn) {

        this.model.addSelection(new Rectangle(col, row, numCols, numRows));

        this.model.clearSelection(removedColumn, removedRow);

        assertFalse(this.model.isCellPositionSelected(removedColumn, removedRow),
                "Failed to remove selection [" + removedRow + ", " + removedColumn + "]");

        for (int rowIndex = row; rowIndex < row + numRows; rowIndex++) {
            for (int colIndex = col; colIndex < col + numCols; colIndex++) {

                if (!(rowIndex == removedRow && colIndex == removedColumn))
                    assertTrue(this.model.isCellPositionSelected(colIndex, rowIndex),
                            "Failed to add range [" + rowIndex + ", " + colIndex + "]");
            }
        }
    }

    private void removeRangeFromRange(
            int col, int row, int numCols, int numRows,
            int removedRow, int removedColumn, int removedNumCols, int removedNumRows) {
        this.model.addSelection(new Rectangle(col, row, numCols, numRows));

        Rectangle removedSelection = new Rectangle(removedColumn, removedRow, removedNumCols, removedNumRows);
        this.model.clearSelection(removedSelection);

        for (int rowIndex = removedRow; rowIndex < removedRow + removedNumRows; rowIndex++) {
            for (int colIndex = removedColumn; colIndex < removedColumn
                    + removedNumCols; colIndex++) {
                assertFalse(this.model.isCellPositionSelected(colIndex, rowIndex),
                        "Failed to remove selection [" + rowIndex + ", " + colIndex + "]");
            }
        }

        for (int rowIndex = row; rowIndex < row + numRows; rowIndex++) {
            for (int colIndex = col; colIndex < col + numCols; colIndex++) {

                if (!removedSelection.contains(colIndex, rowIndex))
                    assertTrue(this.model.isCellPositionSelected(colIndex, rowIndex),
                            "Failed to add range [" + rowIndex + ", " + colIndex + "]");
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
        this.model.addSelection(10, 1);
        this.model.addSelection(10, 2);
        this.model.addSelection(10, 7);
        this.model.addSelection(new Rectangle(10, 50, 5, 10));

        List<Range> selectedRows = ObjectUtils.asList(this.model.getSelectedRowPositions());

        assertTrue(selectedRows.contains(new Range(1, 3)));
        assertTrue(selectedRows.contains(new Range(7, 8)));
        assertTrue(selectedRows.contains(new Range(50, 60)));
    }

    @Test
    public void getSelectedRowsForOverlapingSelections() {
        this.model.addSelection(new Rectangle(10, 3, 5, 2));
        this.model.addSelection(new Rectangle(10, 4, 5, 10));
        this.model.addSelection(new Rectangle(10, 20, 5, 10));

        List<Range> selectedRows = ObjectUtils.asList(this.model.getSelectedRowPositions());
        assertEquals(2, selectedRows.size());

        assertTrue(selectedRows.contains(new Range(3, 14)));
        assertTrue(selectedRows.contains(new Range(20, 30)));
    }

    @Test
    public void getSelectedRowsForLargeNumberOfSelections() {
        this.model.addSelection(1, 10);
        this.model.addSelection(new Rectangle(5, 1, 1, 100));

        List<Range> selectedRows = ObjectUtils.asList(this.model.getSelectedRowPositions());
        assertEquals(1, selectedRows.size());

        assertTrue(selectedRows.contains(new Range(1, 100)));
    }

    @Test
    public void getSelectedRowCount() {
        this.model.addSelection(new Rectangle(10, 3, 1, 1));
        this.model.addSelection(new Rectangle(10, 10, 1, 1));
        this.model.addSelection(new Rectangle(10, 5, 1, 20));

        assertEquals(21, this.model.getSelectedRowCount());
    }

    @Test
    public void isRowPositionSelected() {
        this.model.addSelection(1, 10);
        this.model.addSelection(new Rectangle(5, 1, 100, 10000000));

        assertTrue(this.model.isRowPositionSelected(10));
        assertTrue(this.model.isRowPositionSelected(99));
    }

    @Test
    public void getSelectedColumns() {
        int[] columns = new int[] { 1, 4, 3 };

        for (int column : columns) {
            this.model.addSelection(column, column % 3);
        }

        int[] selectedColumns = this.model.getSelectedColumnPositions();

        Arrays.sort(columns);

        assertEquals(Arrays.toString(columns), Arrays.toString(selectedColumns));
    }

    @Test
    public void removeFromEmptySelection() {
        Rectangle removedSelection = new Rectangle(0, 0, 10, 10);
        this.model.clearSelection(removedSelection);

        assertTrue(this.model.isEmpty(), "Removal from empty selection failed.");

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertFalse(this.model.isCellPositionSelected(j, i), "Selection was not removed [" + i + ", " + j + "]");
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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);
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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);
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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);
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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);
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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);

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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);

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

        removeRangeFromRange(col, row, numCols, numRows, removedRow,
                removedColumn, removedNumCols, removedNumRows);
    }

    @Test
    public void sortByY() {
        List<Rectangle> rectangles = new ArrayList<>();
        rectangles.add(new Rectangle(0, 3, 1, 1));
        rectangles.add(new Rectangle(0, 5, 1, 1));
        rectangles.add(new Rectangle(0, 1, 1, 1));
        rectangles.add(new Rectangle(0, 13, 1, 1));

        this.model.sortByY(rectangles);

        assertEquals(1, rectangles.get(0).y);
        assertEquals(3, rectangles.get(1).y);
        assertEquals(5, rectangles.get(2).y);
        assertEquals(13, rectangles.get(3).y);
    }
}
