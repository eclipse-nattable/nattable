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
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.swt.SWT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for all default selection behavior.
 */
public class SelectionIntegrationTest {

    private static boolean NO_SHIFT = false;
    private static boolean NO_CTRL = false;
    private static boolean SHIFT = true;

    private NatTableFixture natTable;
    private DefaultGridLayer layerStack;
    private SelectionLayer selectionLayer;

    @Before
    public void setup() {
        this.layerStack = new DummyGridLayerStack(10, 5);
        this.selectionLayer = this.layerStack.getBodyLayer().getSelectionLayer();
        this.natTable = new NatTableFixture(this.layerStack, 1000, 200, true);
    }

    @Test
    public void movingSelectionWithLeftArrow() throws Exception {
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 5, 2, NO_SHIFT,
                NO_CTRL));

        // Note: the co-ordinates from this point on are in selection later
        // co-ordinates

        SWTUtils.pressKey(SWT.ARROW_LEFT, this.natTable);
        assertPositionEquals(3, 1, getSelectedCells()[0]);
        assertSelectionAnchorEquals(3, 1);

        SWTUtils.pressKey(SWT.ARROW_LEFT, SWT.SHIFT, this.natTable);
        assertSelectCellsCount(2);
        assertPositionEquals(2, 1, getSelectedCells()[0]);
        assertPositionEquals(3, 1, getSelectedCells()[1]);
        assertSelectionAnchorEquals(3, 1);

        SWTUtils.pressKey(SWT.ARROW_LEFT, SWT.MOD1, this.natTable);
        assertSelectCellsCount(1);
        assertPositionEquals(0, 1, getSelectedCells()[0]);
        assertSelectionAnchorEquals(0, 1);
    }

    @Test
    public void movingSelectionWithRightArrow() throws Exception {
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 5, 2, NO_SHIFT,
                NO_CTRL));

        // Note: the co-ordinates from this point on are in selection later
        // co-ordinates

        SWTUtils.pressKey(SWT.ARROW_RIGHT, this.natTable);
        assertPositionEquals(5, 1, this.selectionLayer.getLastSelectedCellPosition());
        assertSelectionAnchorEquals(5, 1);

        SWTUtils.pressKey(SWT.ARROW_RIGHT, SWT.SHIFT, this.natTable);
        assertSelectCellsCount(2);
        assertCellSelected(5, 1);
        assertCellSelected(6, 1);
        assertSelectionAnchorEquals(5, 1);

        SWTUtils.pressKey(SWT.ARROW_RIGHT, SWT.MOD1, this.natTable);
        assertSelectCellsCount(1);
        assertPositionEquals(9, 1, this.selectionLayer.getSelectedCellPositions()[0]);
        assertPositionEquals(9, 1, this.selectionLayer.getLastSelectedCellPosition());
        assertSelectionAnchorEquals(9, 1);
    }

    @Test
    public void movingSelectionWithDownArrow() throws Exception {
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 5, 2, NO_SHIFT,
                NO_CTRL));

        // Note: the co-ordinates from this point on are in selection later
        // co-ordinates

        SWTUtils.pressKey(SWT.ARROW_DOWN, this.natTable);
        SWTUtils.pressKey(SWT.ARROW_DOWN, this.natTable);
        assertPositionEquals(4, 3, this.selectionLayer.getLastSelectedCellPosition());
        assertSelectionAnchorEquals(4, 3);

        SWTUtils.pressKey(SWT.ARROW_DOWN, SWT.SHIFT, this.natTable);
        SWTUtils.pressKey(SWT.ARROW_DOWN, SWT.SHIFT, this.natTable);
        assertSelectCellsCount(2);
        assertCellSelected(4, 3);
        assertCellSelected(4, 4);
        assertSelectionAnchorEquals(4, 3);

        SWTUtils.pressKey(SWT.ARROW_DOWN, SWT.MOD1, this.natTable);
        assertSelectCellsCount(1);
        int lastRow = this.selectionLayer.getRowCount() - 1;
        assertPositionEquals(4, lastRow,
                this.selectionLayer.getSelectedCellPositions()[0]);
        assertPositionEquals(4, lastRow,
                this.selectionLayer.getLastSelectedCellPosition());
        assertSelectionAnchorEquals(4, lastRow);
    }

    @Test
    public void movingSelectionWithUpArrow() throws Exception {
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 5, 4, NO_SHIFT,
                NO_CTRL));

        // Note: the co-ordinates from this point on are in selection later
        // co-ordinates

        SWTUtils.pressKey(SWT.ARROW_UP, this.natTable);
        assertPositionEquals(4, 2, this.selectionLayer.getSelectedCellPositions()[0]);
        assertSelectionAnchorEquals(4, 2);

        SWTUtils.pressKey(SWT.ARROW_UP, SWT.SHIFT, this.natTable);
        SWTUtils.pressKey(SWT.ARROW_UP, SWT.SHIFT, this.natTable);
        assertSelectCellsCount(3);
        assertCellSelected(4, 0);
        assertCellSelected(4, 1);
        assertCellSelected(4, 2);
        assertSelectionAnchorEquals(4, 2);

        this.natTable.doCommand(new SelectCellCommand(this.natTable, 5, 4, NO_SHIFT,
                NO_CTRL));
        assertSelectCellsCount(1);

        SWTUtils.pressKey(SWT.ARROW_UP, SWT.MOD1, this.natTable);
        assertSelectCellsCount(1);
        int lastRow = 0;
        assertPositionEquals(4, lastRow,
                this.selectionLayer.getSelectedCellPositions()[0]);
        assertPositionEquals(4, lastRow,
                this.selectionLayer.getLastSelectedCellPosition());
        assertSelectionAnchorEquals(4, lastRow);
    }

    @Test
    public void selectingRectangularAreasWithShiftKey() throws Exception {
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 2, 1, NO_SHIFT,
                NO_CTRL));
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 2, 3, SHIFT, NO_CTRL));

        assertSelectCellsCount(3);
        assertSelectionAnchorEquals(1, 0);
        assertCellSelected(1, 0);
        assertCellSelected(1, 1);
        assertCellSelected(1, 2);

        SWTUtils.pressKey(SWT.ARROW_RIGHT, SWT.SHIFT, this.natTable);

        // Previously selected rectangular area must be extended
        assertSelectCellsCount(6);
        assertSelectionAnchorEquals(1, 0);
        assertCellSelected(1, 0);
        assertCellSelected(1, 1);
        assertCellSelected(1, 2);
        assertCellSelected(2, 0);
        assertCellSelected(2, 1);
        assertCellSelected(2, 2);

        SWTUtils.pressKey(SWT.ARROW_UP, SWT.SHIFT, this.natTable);
        assertSelectCellsCount(4);
        assertSelectionAnchorEquals(1, 0);
        assertCellSelected(1, 0);
        assertCellSelected(1, 1);
        assertCellSelected(2, 0);
        assertCellSelected(2, 1);
    }

    // Convenience asserts

    private void assertCellSelected(int column, int row) {
        PositionCoordinate[] selectedCells = this.selectionLayer
                .getSelectedCellPositions();
        boolean selected = false;

        for (PositionCoordinate positionCoordinate : selectedCells) {
            if (column == positionCoordinate.getColumnPosition()
                    && row == positionCoordinate.getRowPosition()) {
                selected = true;
                break;
            }
        }
        Assert.assertTrue(selected);
    }

    private PositionCoordinate[] getSelectedCells() {
        return this.selectionLayer.getSelectedCellPositions();
    }

    private void assertSelectCellsCount(int count) {
        assertEquals(count, this.selectionLayer.getSelectedCellPositions().length);
    }

    private void assertSelectionAnchorEquals(int column, int row) {
        assertPositionEquals(column, row, this.selectionLayer.getSelectionAnchor());
    }

    private void assertPositionEquals(int column, int row,
            PositionCoordinate position) {
        assertEquals(column, position.getColumnPosition());
        assertEquals(row, position.getRowPosition());
    }

}
