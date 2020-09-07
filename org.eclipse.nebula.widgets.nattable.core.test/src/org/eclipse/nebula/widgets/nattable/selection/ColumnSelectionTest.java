/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ColumnSelectionTest {
    // Tests for column selection NTBL-225

    private SelectionLayer selectionLayer;
    private SelectColumnCommandHandler commandHandler;

    @Before
    public void setUp() {
        this.selectionLayer = new SelectionLayer(new DataLayerFixture(10, 50, 100, 40));
        // Selection grid origin as starting point
        this.selectionLayer.setSelectedCell(0, 0);
        this.commandHandler = new SelectColumnCommandHandler(this.selectionLayer);
    }

    @After
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    @Test
    public void shouldSelectAllCellsInAColumn() {
        final int rowCount = this.selectionLayer.getRowCount();
        // User has clicked on second column header cell
        final int rowPosition = 25;
        this.commandHandler.selectColumn(2, rowPosition, false, false);

        // Selection anchor should be at row 0, col 2
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(rowPosition, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // Last selected cell should be part of last row
        assertEquals(2, this.selectionLayer.getLastSelectedCellPosition().getColumnPosition());
        assertEquals(rowCount - 1, this.selectionLayer.getLastSelectedCellPosition().getRowPosition());

        // Cells in between should have been selected
        assertEquals(rowCount, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldSelectAllCellsToTheRightWithShiftKeyPressed() {
        this.selectionLayer.selectCell(2, 2, false, false);

        // User selects column using shift key mask
        this.commandHandler.selectColumn(4, 25, true, false);

        // Selection Anchor should not change
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertCellsSelectedBetween(2, 4);
    }

    @Test
    public void shouldSelectAllCellsToTheLeftWithShiftKeyPressed() {
        this.selectionLayer.selectCell(2, 2, false, false);

        // User selects column using shift key mask
        this.commandHandler.selectColumn(0, 25, true, false);

        // Selection Anchor should not change
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertCellsSelectedBetween(0, 2);
    }

    @Test
    public void shouldAppendColumnsToTheRightWithShiftKeyPressed() {
        this.selectionLayer.selectCell(2, 2, false, false);

        this.commandHandler.selectColumn(3, 25, true, false);
        assertCellsSelectedBetween(2, 3);

        this.commandHandler.selectColumn(4, 25, true, false);
        assertCellsSelectedBetween(2, 4);
    }

    @Test
    public void shouldSetAnchorWithInitialShiftKeyPressed() {
        // start from a clear selection state
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, true, false));

        assertEquals(4, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertFalse("column 3 fully selected", this.selectionLayer.isColumnPositionFullySelected(3));
        assertTrue("column 4 not fully selected", this.selectionLayer.isColumnPositionFullySelected(4));
        assertFalse("column 5 fully selected", this.selectionLayer.isColumnPositionFullySelected(5));
    }

    @Test
    public void shouldExtendFromAnchorWithShiftKeyPressed() {
        // start from a clear selection state
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, true, false));
        // select columns to the left
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, true, false));

        for (int col = 2; col <= 4; col++) {
            assertTrue("column " + col + " not fully selected", this.selectionLayer.isColumnPositionFullySelected(col));
        }
        assertFalse("column 5 fully selected", this.selectionLayer.isColumnPositionFullySelected(5));

        // select columns to the right
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 6, 0, true, false));

        assertFalse("column 2 fully selected", this.selectionLayer.isColumnPositionFullySelected(2));
        assertFalse("column 3 fully selected", this.selectionLayer.isColumnPositionFullySelected(3));
        for (int col = 4; col <= 6; col++) {
            assertTrue("column " + col + " not fully selected", this.selectionLayer.isColumnPositionFullySelected(col));
        }
    }

    private void assertCellsSelectedBetween(int startColPosition, int endColPosition) {
        for (int col = startColPosition; col <= endColPosition; col++) {
            for (int row = 0; row <= 6; row++) {
                assertTrue("[" + col + ", " + row + "] not selected",
                        this.selectionLayer.isCellPositionSelected(col, row));
            }
        }
    }

    @Test
    public void shouldExtendSelectionWithAllCellsInAColumnUsingTheCtrlKey() {
        final int rowCount = this.selectionLayer.getRowCount();

        // User has selected 3 non-consecutive cells
        this.selectionLayer.selectCell(2, 2, false, false);
        this.selectionLayer.selectCell(3, 2, false, true);
        this.selectionLayer.selectCell(2, 0, false, true);

        // User has clicked on second column header cell
        final int rowPosition = 25;
        this.commandHandler.selectColumn(1, rowPosition, false, true);

        // Selection anchor should be at row 0, column 1
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(rowPosition, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // Last selected cell should be part of last row
        assertEquals(1, this.selectionLayer.getLastSelectedCellPosition().getColumnPosition());
        final int lastRowPosition = this.selectionLayer.getLastSelectedCellPosition().getRowPosition();
        assertEquals(rowCount - 1, lastRowPosition);

        // Cells in column should have been selected
        assertEquals(rowCount, this.selectionLayer.getSelectedRowCount());
        assertTrue(this.selectionLayer.isCellPositionSelected(1, 4));

        // Test extending column selection to the right of previous column
        // selection
        this.commandHandler.selectColumn(3, rowPosition, false, true);

        // Selection model should contain all previously selected cells
        assertTrue(this.selectionLayer.isCellPositionSelected(2, 2));
        assertTrue(this.selectionLayer.isCellPositionSelected(3, 2));
        assertTrue(this.selectionLayer.isCellPositionSelected(2, 0));
        assertTrue(this.selectionLayer.isCellPositionSelected(1, 6));
        assertTrue(this.selectionLayer.isCellPositionSelected(3, 6));
    }

    @Test
    public void onlyOneCellSelectedAtAnyTime() {
        this.selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));

        Collection<PositionCoordinate> cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions()[0]);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select another column with control mask
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(2, this.selectionLayer.getSelectedColumnPositions()[0]);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select additional columns with shift mask
        // only the last column should be selected afterwards
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 5, 0, true, false));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(5, this.selectionLayer.getSelectedColumnPositions()[0]);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldUpdateAnchorIfDeselected() {
        // start from a clear selection state
        this.selectionLayer.clear();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, true, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, true, false));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getRowPosition());

        for (int col = 2; col <= 4; col++) {
            assertTrue("column " + col + " not fully selected", this.selectionLayer.isColumnPositionFullySelected(col));
        }

        // deselect column 2
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        assertEquals(3, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertFalse("column 2 fully selected", this.selectionLayer.isColumnPositionFullySelected(2));
        for (int col = 3; col <= 4; col++) {
            assertTrue("column " + col + " not fully selected", this.selectionLayer.isColumnPositionFullySelected(col));
        }
    }

    @Test
    public void shouldNotIncludeDeselectedCellsWithCtrlModifier() {
        // test a previous bug where single deselected cells where added to the
        // selection with following modifier selections

        // start from a clear selection state
        this.selectionLayer.clear();
        // select a single cell
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 0, false, false));
        // deselect the cell again
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 0, false, true));

        // trigger selection of column 4 with ctrl
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertEquals(4, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertFalse("[2, 0] is selected", this.selectionLayer.isCellPositionSelected(2, 0));
        assertTrue("column 4 not fully selected", this.selectionLayer.isColumnPositionFullySelected(4));
    }

}
