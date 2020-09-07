/*******************************************************************************
 * Copyright (c) 2014, 2020 Jonas Hugo, Markus Wahl, Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - initial test
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.CellPosition;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class PreserveSelectionModelTest {

    private TestSelectionLayer selectionLayer;
    private TestRowDataProvider rowDataProvider;
    private TestRowIdAccessor rowIdAccessor;

    /**
     * Each row consist of an array of String, one String for each column, i e
     * each cell is a String.
     */
    private PreserveSelectionModel<String[]> testee;

    private static String[] indexRow4 = new String[] { "row 4 hallo0", "hallo2", "hallo3" };
    private static String[] indexRow5 = new String[] { "row 5 hallo0", "hallo2", "hallo3" };
    private static String[] indexRow6 = new String[] { "row 6 hallo0", "hallo2", "hallo3" };

    private int rowCount = 7, columnCount = 3;

    private TestCell[][] cells = new TestCell[7][3];

    @Before
    public void setUp() throws Exception {

        this.rowDataProvider = new TestRowDataProvider();
        this.rowIdAccessor = new TestRowIdAccessor();
        this.selectionLayer = new TestSelectionLayer(new DataLayer(this.rowDataProvider));
        /*
         * row 0 in the immediate table is row 4 in the underlying virtual table
         * coordinate system:
         *
         * row 0: A B C
         *
         * row 1: _ D E
         *
         * row 2: _ _ F
         */
        mockCells();

        this.testee = new PreserveSelectionModel<>(
                this.selectionLayer, this.rowDataProvider, this.rowIdAccessor);
    }

    /**
     * row 0 in the immediate table is row 4 in the underlying virtual table
     * coordinate system:
     *
     * row 0: A B C
     *
     * row 1: _ D E
     *
     * row 2: _ _ F
     */
    private void mockCells() {
        for (int rowIndex = 0; rowIndex < this.cells.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < this.cells[rowIndex].length; columnIndex++) {
                this.cells[rowIndex][columnIndex] = mockCell(columnIndex, rowIndex);
            }
        }
    }

    private TestCell mockCell(int columnIndex, int rowIndex) {
        int rowPosition = Math.max(rowIndex - 4, -1);
        int columnPosition = columnIndex;
        if (rowPosition != -1) {
            return new TestCell(columnPosition, rowPosition);
        }
        return new TestCell();
    }

    @Test
    public void Multiple_Selection_Is_Enabled_By_Default() {
        assertTrue(this.testee.isMultipleSelectionAllowed());
    }

    @Test
    public void Disabling_Multiple_Selection_Is_Communicated() {
        this.testee.setMultipleSelectionAllowed(false);

        assertFalse(this.testee.isMultipleSelectionAllowed());
    }

    @Test
    public void Disabling_Multiple_Selection_Is_Supported() {
        this.testee.setMultipleSelectionAllowed(false);
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 0);

        assertFalse(isCellSelected(0, 0));
        assertTrue(isCellSelected(2, 0));
    }

    @Test
    public void Multiple_Selection_Is_Supported() {
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 0);

        assertTrue(isCellSelected(0, 0));
        assertTrue(isCellSelected(2, 0));
    }

    private boolean isCellSelected(int columnPosition, int rowPosition) {
        boolean singularVersion = this.testee.isCellPositionSelected(columnPosition, rowPosition);
        boolean rectangleVersion = false;
        for (Rectangle selection : this.testee.getSelections()) {
            if (selection.intersects(columnPosition, rowPosition, 1, 1)) {
                rectangleVersion = true;
            }
        }
        return singularVersion && rectangleVersion;
    }

    @Test
    public void getSelections_Ignores_Invisible_Rows() {
        // A is on row 0:
        this.testee.addSelection(0, 0);

        // D is not on row 0:
        this.testee.addSelection(1, 1);

        // Scroll away so that index row 4 is not visible:
        this.selectionLayer.scrollOffset = 5;

        assertEquals(1, this.testee.getSelections().size());
        assertEquals(new Rectangle(1, 0, 1, 1), this.testee.getSelections().iterator().next());
    }

    @Test
    public void Add_Selection_By_Rectangle_Is_Supported() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));

        assertTrue(isCellSelected(1, 0));
        assertTrue(isCellSelected(1, 1));
        assertTrue(isCellSelected(2, 0));
        assertTrue(isCellSelected(2, 1));

        assertFalse(isCellSelected(0, 0));
        assertFalse(isCellSelected(2, 2));
    }

    @Test
    public void Adding_Infinite_Row_Selection_Is_Truncated_Before_Stored() {
        this.columnCount = 3;
        this.testee.addSelection(new Rectangle(0, 0, Integer.MAX_VALUE, 1));

        assertTrue(isCellSelected(0, 0));
        assertTrue(isCellSelected(1, 0));
        assertTrue(isCellSelected(2, 0));
    }

    @Test
    public void Adding_Infinite_Column_Selection_Is_Truncated_Before_Stored() {
        this.rowCount = 6;
        this.testee.addSelection(new Rectangle(2, 0, 1, Integer.MAX_VALUE));

        assertTrue(isCellSelected(2, 0));
        assertTrue(isCellSelected(2, 1));
        assertTrue(isCellSelected(2, 2));
    }

    @Test
    public void Clear_Selection() {
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 1);
        this.testee.clearSelection();

        assertFalse(isCellSelected(0, 0));
        assertFalse(isCellSelected(2, 1));
    }

    @Test
    public void Clear_Infinity_Long_Row_Only_Clear_Known_Columns() {
        // as only one row is cleared we expect that there is only one access
        // for the rowId
        long expectedLoops = 1;
        this.testee.addSelection(0, 0);
        this.testee.addSelection(1, 0);
        this.rowIdAccessor.numberOfCalls = 0;
        this.testee.clearSelection(new Rectangle(0, 0, Integer.MAX_VALUE, 1));

        assertEquals(expectedLoops, this.rowIdAccessor.numberOfCalls);
        assertFalse(isCellSelected(0, 0));
        assertFalse(isCellSelected(1, 0));
    }

    @Test
    public void Clear_Infinity_Long_Column_Only_Clear_Known_Rows() {
        long expectedLoops = 3;
        this.testee.addSelection(0, 0);
        this.testee.addSelection(0, 1);
        this.rowIdAccessor.numberOfCalls = 0;
        this.testee.clearSelection(new Rectangle(0, 0, 1, Integer.MAX_VALUE));

        assertEquals(expectedLoops, this.rowIdAccessor.numberOfCalls);
        assertFalse(isCellSelected(0, 0));
        assertFalse(isCellSelected(0, 1));
    }

    @Test
    public void Partial_Clear_Selection() {
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 1);
        this.testee.clearSelection(0, 0);

        assertFalse(isCellSelected(0, 0));
        assertTrue(isCellSelected(2, 1));
    }

    @Test
    public void Clear_Selection_By_Rectangle() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.clearSelection(new Rectangle(1, 0, 1, 2));

        assertFalse(isCellSelected(1, 0));
        assertFalse(isCellSelected(1, 1));
        assertTrue(isCellSelected(2, 0));
        assertTrue(isCellSelected(2, 1));

        this.testee.clearSelection(new Rectangle(2, 1, 1, 1));

        assertTrue(isCellSelected(2, 0));
        assertFalse(isCellSelected(2, 1));
    }

    @Test
    public void None_Selected_Cells_Is_Empty() {
        assertTrue(this.testee.isEmpty());
    }

    @Test
    public void Selected_Cell_Is_Not_Empty() {
        this.testee.addSelection(0, 0);

        assertFalse(this.testee.isEmpty());
    }

    @Test
    public void getSelectedColumnPositions() {
        this.testee.addSelection(1, 1);
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 2);

        int[] selectedColumns = this.testee.getSelectedColumnPositions();
        assertEquals(Arrays.toString(new int[] { 0, 1, 2 }),
                Arrays.toString(selectedColumns));
    }

    @Test
    public void isColumnPositionSelected() {
        int column = 0;
        this.testee.addSelection(column, 0);

        assertTrue(this.testee.isColumnPositionSelected(column));
        assertFalse(this.testee.isColumnPositionSelected(2));
    }

    @Test
    public void Changed_Sort_Order_Is_Properly_Reflected() {
        /*
         * row 0: A _ _
         *
         * row 1: _ D _
         *
         * row 2: _ _ F
         */

        // A
        this.testee.addSelection(0, 0);

        // D
        this.testee.addSelection(1, 1);

        // F
        this.testee.addSelection(2, 2);

        assertTrue(isCellSelected(0, 0));
        assertTrue(isCellSelected(1, 1));
        assertTrue(isCellSelected(2, 2));

        /*
         * sort
         *
         * row 0: _ _ F
         *
         * row 1: _ D _
         *
         * row 2: A _ _
         */
        // let index row 4 and index row 6 change places:
        TestCell[] oldRow4 = this.cells[4];
        TestCell[] oldRow6 = this.cells[6];
        this.cells[4] = oldRow6;
        this.cells[6] = oldRow4;
        TestCell cellF = this.cells[4][2];
        TestCell cellA = this.cells[6][0];
        cellF.rowPosition = 0; // Position if index 4
        cellA.rowPosition = 2; // Position if index 6

        this.rowDataProvider.indexOfRow4 = 6;
        this.rowDataProvider.indexOfRow6 = 4;

        assertTrue(isCellSelected(2, 0));
        assertTrue(isCellSelected(1, 1));
        assertTrue(isCellSelected(0, 2));
    }

    @Test
    public void isColumnFullySelected() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));

        assertFalse(this.testee.isColumnPositionFullySelected(1, 3));
        assertTrue(this.testee.isColumnPositionFullySelected(1, 2));
    }

    @Test
    public void isColumnFullySelected_Copes_With_Clear() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.clearSelection(1, 0);

        assertFalse(this.testee.isColumnPositionFullySelected(1, 2));
        assertTrue(this.testee.isColumnPositionFullySelected(1, 1));
    }

    @Test
    public void isColumnFullySelected_Copes_With_Gap() {
        this.testee.addSelection(2, 0);
        this.testee.addSelection(2, 2);
        this.testee.addSelection(2, 3);
        this.testee.addSelection(2, 4);
        this.testee.addSelection(2, 5);
        this.testee.addSelection(2, 6);

        assertFalse(this.testee.isColumnPositionFullySelected(2, this.rowCount));
    }

    @Test
    public void isColumnFullySelected_Copes_With_Overlapping_Regions() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.addSelection(new Rectangle(2, 1, 1, 2));

        assertTrue(this.testee.isColumnPositionFullySelected(2, 3));
    }

    @Test
    public void isColumnFullySelected_Copes_With_Combining_Adjacent_Individually_Selected_Cells() {
        this.testee.addSelection(1, 0);
        this.testee.addSelection(1, 1);

        assertTrue(this.testee.isColumnPositionFullySelected(1, 2));
    }

    @Test
    public void isColumnPositionFullySelected_For_Unselected_Column() {
        assertFalse(this.testee.isColumnPositionFullySelected(1, 2));
    }

    @Test
    public void getFullySelectedColumnPositions() {
        this.testee.addSelection(0, 0);
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.addSelection(new Rectangle(2, 1, 1, 2));

        int[] fullySelectedColumns = this.testee.getFullySelectedColumnPositions(2);
        assertEquals(Arrays.toString(new int[] { 1, 2 }),
                Arrays.toString(fullySelectedColumns));

        fullySelectedColumns = this.testee.getFullySelectedColumnPositions(1);
        assertEquals(Arrays.toString(new int[] { 0, 1, 2 }),
                Arrays.toString(fullySelectedColumns));

        fullySelectedColumns = this.testee.getFullySelectedColumnPositions(3);
        assertEquals(Arrays.toString(new int[] { 2 }),
                Arrays.toString(fullySelectedColumns));
    }

    @Test
    public void getSelectedRowCount() {
        this.testee.addSelection(1, 1);
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 2);

        assertEquals(3, this.testee.getSelectedRowCount());
    }

    @Test
    public void getSelectedRowPositions() {
        this.testee.addSelection(1, 1);
        this.testee.addSelection(0, 0);
        this.testee.addSelection(2, 2);

        HashSet<Range> actualSelectedRowPositions = new HashSet<>(this.testee.getSelectedRowPositions());

        HashSet<Range> expectedSelectedRowPositions = new HashSet<>();
        expectedSelectedRowPositions.add(new Range(0, 1));
        expectedSelectedRowPositions.add(new Range(1, 2));
        expectedSelectedRowPositions.add(new Range(2, 3));

        assertEquals(expectedSelectedRowPositions, actualSelectedRowPositions);
    }

    @Test
    public void isRowPositionSelected() {
        int row = 0;
        this.testee.addSelection(0, row);

        assertTrue(this.testee.isRowPositionSelected(row));
        assertFalse(this.testee.isRowPositionSelected(2));
    }

    @Test
    public void isRowPositionFullySelected() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));

        assertFalse(this.testee.isRowPositionFullySelected(0, 3));
        assertTrue(this.testee.isRowPositionFullySelected(0, 2));
    }

    @Test
    public void isRowPositionFullySelected_Copes_With_Clear() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.clearSelection(1, 0);

        assertFalse(this.testee.isRowPositionFullySelected(0, 2));
        assertTrue(this.testee.isRowPositionFullySelected(0, 1));
    }

    @Test
    public void isRowPositionFullySelected_Copes_With_Gap() {
        this.testee.addSelection(2, 0);
        this.testee.addSelection(0, 0);

        assertFalse(this.testee.isRowPositionFullySelected(0, 2));
    }

    @Test
    public void isRowPositionFullySelected_Copes_With_Overlapping_Regions() {
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.addSelection(new Rectangle(0, 0, 3, 1));

        assertTrue(this.testee.isRowPositionFullySelected(0, 3));
    }

    @Test
    public void isRowPositionFullySelected_Copes_With_Combining_Adjacent_Individually_Selected_Cells() {
        this.testee.addSelection(1, 0);
        this.testee.addSelection(2, 0);

        assertTrue(this.testee.isRowPositionFullySelected(0, 2));
    }

    @Test
    public void isRowPositionFullySelected_For_Unselected_Row() {
        assertFalse(this.testee.isRowPositionFullySelected(0, 3));
    }

    @Test
    public void getFullySelectedRowPositions() {
        this.testee.addSelection(0, 0);
        this.testee.addSelection(new Rectangle(1, 0, 2, 2));
        this.testee.addSelection(new Rectangle(2, 1, 1, 2));

        int[] fullySelectedRows = this.testee.getFullySelectedRowPositions(2);
        assertEquals(Arrays.toString(new int[] { 0, 1 }),
                Arrays.toString(fullySelectedRows));

        fullySelectedRows = this.testee.getFullySelectedRowPositions(1);
        assertEquals(Arrays.toString(new int[] { 0, 1, 2 }),
                Arrays.toString(fullySelectedRows));

        fullySelectedRows = this.testee.getFullySelectedRowPositions(3);
        assertEquals(Arrays.toString(new int[] { 0 }),
                Arrays.toString(fullySelectedRows));
    }

    // Anchor

    @Test
    public void getSelectionAnchor_Copes_With_Missing_Marker() {
        assertEquals(SelectionLayer.NO_SELECTION, this.testee.getSelectionAnchor().x);
        assertEquals(SelectionLayer.NO_SELECTION, this.testee.getSelectionAnchor().y);
    }

    @Test
    public void getSelectionAnchor() {
        this.testee.selectionAnchor = new CellPosition<>(indexRow4, 0);
        assertEquals(0, this.testee.getSelectionAnchor().x);
        assertEquals(0, this.testee.getSelectionAnchor().y);
    }

    @Test
    public void setSelectionAnchor() {
        this.testee.setSelectionAnchor(new Point(0, 0));
        assertSame(indexRow4, this.testee.selectionAnchor.getRowObject());
    }

    // Last selected cell

    @Test
    public void getLastSelectedCell_Copes_With_Missing_Marker() {
        assertEquals(SelectionLayer.NO_SELECTION, this.testee.getLastSelectedCell().x);
        assertEquals(SelectionLayer.NO_SELECTION, this.testee.getLastSelectedCell().y);
    }

    @Test
    public void getLastSelectedCell() {
        this.testee.lastSelectedCell = new CellPosition<>(indexRow4, 0);
        assertEquals(0, this.testee.getLastSelectedCell().x);
        assertEquals(0, this.testee.getLastSelectedCell().y);
    }

    @Test
    public void setLastSelectedCell() {
        this.testee.setLastSelectedCell(new Point(0, 0));
        assertSame(indexRow4, this.testee.lastSelectedCell.getRowObject());
    }

    // Last selected region

    @Test
    public void getLastSelectedRegion_Copes_With_Missing_Marker() {
        assertNull(this.testee.getLastSelectedRegion());
    }

    @Test
    public void getLastSelectedRegion() {
        this.testee.lastSelectedRegion = new Rectangle(0, 0, 1, 1);
        this.testee.lastSelectedRegionOriginRowObject = indexRow6;
        assertEquals(2, this.testee.getLastSelectedRegion().y);
    }

    @Test
    public void setLastSelectedRegion_Overrides_Reference() {
        Rectangle oldReference = new Rectangle(0, 0, 1, 1);
        this.testee.lastSelectedRegion = oldReference;
        Rectangle newReference = new Rectangle(0, 0, 1, 1);
        this.testee.setLastSelectedRegion(newReference);

        assertSame(newReference, this.testee.lastSelectedRegion);
        assertSame(indexRow4, this.testee.lastSelectedRegionOriginRowObject);
    }

    @Test
    public void setLastSelectedRegion_Clears_Region_On_NULL() {
        this.testee.lastSelectedRegion = new Rectangle(0, 0, 1, 1);
        this.testee.setLastSelectedRegion(null);

        assertNull(this.testee.lastSelectedRegion);
    }

    @Test
    public void setLastSelectedRegion_On_Parameters_Copys_Data() {
        Rectangle oldReference = new Rectangle(0, 0, 1, 1);
        this.testee.lastSelectedRegion = oldReference;
        this.testee.setLastSelectedRegion(1, 1, 2, 2);

        assertSame(oldReference, this.testee.lastSelectedRegion);
        assertEquals(new Rectangle(1, 1, 2, 2), this.testee.lastSelectedRegion);
        assertSame(indexRow5, this.testee.lastSelectedRegionOriginRowObject);
    }

    class TestRowDataProvider implements IRowDataProvider<String[]> {

        int indexOfRow4 = 4, indexOfRow5 = 5, indexOfRow6 = 6;

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return PreserveSelectionModelTest.this.columnCount;
        }

        @Override
        public int getRowCount() {
            return PreserveSelectionModelTest.this.rowCount;
        }

        @Override
        public String[] getRowObject(int rowIndex) {
            if (rowIndex == this.indexOfRow4) {
                return indexRow4;
            } else if (rowIndex == this.indexOfRow5) {
                return indexRow5;
            } else if (rowIndex == this.indexOfRow6) {
                return indexRow6;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public int indexOfRowObject(String[] rowObject) {
            if (rowObject == indexRow4) {
                return this.indexOfRow4;
            } else if (rowObject == indexRow5) {
                return this.indexOfRow5;
            } else if (rowObject == indexRow6) {
                return this.indexOfRow6;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    class TestRowIdAccessor implements IRowIdAccessor<String[]> {

        long numberOfCalls = 0;

        @Override
        public Serializable getRowId(String[] rowObject) {
            this.numberOfCalls += 1;
            if (rowObject == indexRow4) {
                return "rowA";
            } else if (rowObject == indexRow5) {
                return "rowB";
            } else if (rowObject == indexRow6) {
                return "rowC";
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    class TestSelectionLayer extends SelectionLayer {

        int scrollOffset = 4;

        public TestSelectionLayer(IUniqueIndexLayer underlyingLayer) {
            super(underlyingLayer);
        }

        @Override
        public int getRowIndexByPosition(int rowPosition) {
            int rowIndex = rowPosition + this.scrollOffset;
            return rowIndex < 0 || rowIndex > 6 ? -1 : rowIndex;
        }

        @Override
        public int getRowPositionByIndex(int rowIndex) {
            int rowPosition = Math.max(rowIndex - this.scrollOffset, -1);
            return rowPosition > 2 ? -1 : rowPosition;
        };

        @Override
        public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
            return PreserveSelectionModelTest.this.cells[getRowIndexByPosition(rowPosition)][columnPosition];
        }
    }

    class TestCell extends LayerCell {

        int columnPosition, rowPosition;

        public TestCell(int columnPosition, int rowPosition) {
            super(null, columnPosition, rowPosition);
            this.columnPosition = columnPosition;
            this.rowPosition = rowPosition;
        }

        public TestCell() {
            super(null, 0, 0, 0, 0, 0, 0);
        }

        @Override
        public int getOriginRowPosition() {
            return this.rowPosition;
        }

        @Override
        public int getOriginColumnPosition() {
            return this.columnPosition;
        }

        @Override
        public int getRowSpan() {
            return super.getRowSpan();
        }

        @Override
        public int getColumnSpan() {
            return super.getColumnSpan();
        }

    }

}
