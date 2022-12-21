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

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RowSelectionTest {
    // Tests for column selection NTBL-225

    private SelectionLayer selectionLayer;

    @BeforeEach
    public void setUp() {
        this.selectionLayer = new SelectionLayer(new DataLayerFixture());
        // Selection grid origin as starting point
        this.selectionLayer.setSelectedCell(0, 0);
    }

    @AfterEach
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    @Test
    public void shouldSelectAllCellsInARow() {
        final int columnCount = this.selectionLayer.getColumnCount();
        // User has clicked on second row header cell
        this.selectionLayer.selectRow(1, 2, false, false);

        // Selection anchor should be at row 2, column 0
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // Last selected cell should be part of last column
        assertEquals(columnCount - 1l, this.selectionLayer.getLastSelectedCellPosition().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCellPosition().getRowPosition());

        // Cells in between should have been selected
        assertEquals(columnCount, this.selectionLayer.getSelectedColumnPositions().length);
    }

    @Test
    public void shouldExtendSelectionUpWithShiftKey() {
        // User selects cell
        this.selectionLayer.selectCell(2, 2, false, false);
        // User selects column using shift key mask
        this.selectionLayer.selectRow(1, 1, true, false);

        // Selection Anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertCellsSelectedBetween(2, 1);
    }

    @Test
    public void shouldExtendSelectionDownWithShiftKey() {
        // User selects cell
        this.selectionLayer.selectCell(2, 2, false, false);
        // User selects column using shift key mask
        this.selectionLayer.selectRow(1, 4, true, false);

        // Selection Anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertCellsSelectedBetween(2, 4);
    }

    @Test
    public void shouldAppendSelectionUpWithShiftKey() {
        // User selects cell
        this.selectionLayer.selectCell(2, 2, false, false);
        // User selects column using shift key mask
        this.selectionLayer.selectRow(1, 1, true, false);

        // Selection Anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertCellsSelectedBetween(2, 1);

        this.selectionLayer.selectRow(1, 0, true, false);
        assertCellsSelectedBetween(2, 0);
    }

    @Test
    public void shouldAppendSelectionDownWithShiftKey() {
        // User selects cell
        this.selectionLayer.selectCell(2, 2, false, false);
        // User selects column using shift key mask
        this.selectionLayer.selectRow(1, 3, true, false);

        // Selection Anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertCellsSelectedBetween(2, 3);

        this.selectionLayer.selectRow(1, 5, true, false);
        assertCellsSelectedBetween(2, 5);
    }

    @Test
    public void shouldSetAnchorWithInitialShiftKeyPressed() {
        // start from a clear selection state
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 4, true, false));

        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertFalse(this.selectionLayer.isRowPositionFullySelected(3), "row 3 fully selected");
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4), "row 4 not fully selected");
        assertFalse(this.selectionLayer.isRowPositionFullySelected(5), "row 5 fully selected");
    }

    @Test
    public void shouldExtendFromAnchorWithShiftKeyPressed() {
        // start from a clear selection state
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 4, true, false));
        // select rows to the top
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 2, true, false));

        for (int row = 2; row <= 4; row++) {
            assertTrue(this.selectionLayer.isRowPositionFullySelected(row), "row " + row + " not fully selected");
        }
        assertFalse(this.selectionLayer.isRowPositionFullySelected(5), "row 5 fully selected");

        // select rows to the bottom
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 6, true, false));

        assertFalse(this.selectionLayer.isRowPositionFullySelected(2), "row 2 fully selected");
        assertFalse(this.selectionLayer.isRowPositionFullySelected(3), "row 3 fully selected");
        for (int row = 4; row <= 6; row++) {
            assertTrue(this.selectionLayer.isRowPositionFullySelected(row), "row " + row + " not fully selected");
        }
    }

    private void assertCellsSelectedBetween(int startRowPosition, int endRowPosition) {
        for (int row = startRowPosition; row <= endRowPosition; row++) {
            for (int col = 0; col <= 4; col++) {
                assertTrue(this.selectionLayer.isCellPositionSelected(col, row), "[" + col + ", " + row + "] not selected");
            }
        }
    }

    @Test
    public void shouldExtendSelectionWithAllCellsInARowUsingTheCtrlKey() {
        final int columnCount = this.selectionLayer.getColumnCount();

        // User has selected 3 non-consecutive cells
        this.selectionLayer.selectCell(2, 2, false, false);
        this.selectionLayer.selectCell(3, 2, false, true);
        this.selectionLayer.selectCell(2, 0, false, true);

        // User has clicked on second row header cell
        this.selectionLayer.selectRow(1, 1, false, true);

        // Selection anchor should be at row 1, col 0
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // Last selected cell should be part of last column
        assertEquals(1, this.selectionLayer.getLastSelectedCellPosition().getRowPosition());
        int lastColumnPosition = this.selectionLayer.getLastSelectedCellPosition().getColumnPosition();
        assertEquals(columnCount - 1l, lastColumnPosition);

        // Cells in row should have been selected
        int[] selectedColumns = this.selectionLayer.getSelectedColumnPositions();
        assertEquals(columnCount, selectedColumns.length);
        assertTrue(this.selectionLayer.isCellPositionSelected(4, 1));

        // Test extending column selection to the right of previous column
        // selection
        this.selectionLayer.selectRow(1, 3, false, true);

        // Selection model should contain all previously selected cells
        assertTrue(this.selectionLayer.isCellPositionSelected(2, 2));
        assertTrue(this.selectionLayer.isCellPositionSelected(3, 2));
        assertTrue(this.selectionLayer.isCellPositionSelected(2, 0));

        // 3rd row cells should be selected
        assertTrue(this.selectionLayer.isCellPositionSelected(1, 3));
        assertTrue(this.selectionLayer.isCellPositionSelected(3, 3));
    }

    @Test
    public void onlyOneCellSelectedAtAnyTime() {
        this.selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 0, false, true));

        Collection<PositionCoordinate> cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select another row with control mask
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 2, false, true));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select additional rows with shift mask
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 5, true, false));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void testMultiSelectionRestore() {
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 0, false, false));

        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 2, true, false));

        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(3, this.selectionLayer.getSelectedRowCount());

        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 2, false, true));

        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(2, this.selectionLayer.getSelectedRowCount());

        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 1, false, true));

        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 0, false, true));

        assertEquals(0, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 0, false, true));

        assertEquals(1, this.selectionLayer.getSelectedRowPositions().size());
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldUpdateAnchorIfDeselected() {
        // start from a clear selection state
        this.selectionLayer.clear();

        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 2, true, false));
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 4, true, false));

        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());

        for (int row = 2; row <= 4; row++) {
            assertTrue(this.selectionLayer.isRowPositionFullySelected(row), "row " + row + " not fully selected");
        }

        // deselect row 2
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 2, false, true));

        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertFalse(this.selectionLayer.isRowPositionFullySelected(2), "row 2 fully selected");
        for (int row = 3; row <= 4; row++) {
            assertTrue(this.selectionLayer.isRowPositionFullySelected(row), "row " + row + " not fully selected");
        }
    }

    @Test
    public void shouldNotIncludeDeselectedCellsWithCtrlModifier() {
        // test a previous bug where single deselected cells where added to the
        // selection with following modifier selections

        // start from a clear selection state
        this.selectionLayer.clear();
        // select a single cell
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 0, 2, false, false));
        // deselect the cell again
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 0, 2, false, true));

        // trigger selection of row 4 with ctrl
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 4, false, true));

        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        assertFalse(this.selectionLayer.isCellPositionSelected(0, 2), "[0, 2] is selected");
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4), "row 4 not fully selected");
    }

}
