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
		layerStack = new DummyGridLayerStack(10, 5);
		selectionLayer = layerStack.getBodyLayer().getSelectionLayer();
		natTable = new NatTableFixture(layerStack, 1000, 200, true);
	}

	@Test
	public void movingSelectionWithLeftArrow() throws Exception {
		natTable.doCommand(new SelectCellCommand(natTable, 5, 2, NO_SHIFT, NO_CTRL));

		// Note: the co-ordinates from this point on are in selection later co-ordinates

		SWTUtils.pressKey(SWT.ARROW_LEFT, natTable);
		assertPositionEquals(3, 1, getSelectedCells()[0]);
		assertSelectionAnchorEquals(3, 1);

		SWTUtils.pressKey(SWT.ARROW_LEFT, SWT.SHIFT, natTable);
		assertSelectCellsCount(2);
		assertPositionEquals(2, 1, getSelectedCells()[0]);
		assertPositionEquals(3, 1, getSelectedCells()[1]);
		assertSelectionAnchorEquals(3, 1);

		SWTUtils.pressKey(SWT.ARROW_LEFT, SWT.MOD1, natTable);
		assertSelectCellsCount(1);
		assertPositionEquals(0, 1, getSelectedCells()[0]);
		assertSelectionAnchorEquals(0, 1);
	}

	@Test
	public void movingSelectionWithRightArrow() throws Exception {
		natTable.doCommand(new SelectCellCommand(natTable, 5, 2, NO_SHIFT, NO_CTRL));

		// Note: the co-ordinates from this point on are in selection later co-ordinates

		SWTUtils.pressKey(SWT.ARROW_RIGHT, natTable);
		assertPositionEquals(5, 1, selectionLayer.getLastSelectedCellPosition());
		assertSelectionAnchorEquals(5, 1);

		SWTUtils.pressKey(SWT.ARROW_RIGHT, SWT.SHIFT, natTable);
		assertSelectCellsCount(2);
		assertCellSelected(5, 1);
		assertCellSelected(6, 1);
		assertSelectionAnchorEquals(5, 1);

		SWTUtils.pressKey(SWT.ARROW_RIGHT, SWT.MOD1, natTable);
		assertSelectCellsCount(1);
		assertPositionEquals(9, 1, selectionLayer.getSelectedCellPositions()[0]);
		assertPositionEquals(9, 1, selectionLayer.getLastSelectedCellPosition());
		assertSelectionAnchorEquals(9, 1);
	}

	@Test
	public void movingSelectionWithDownArrow() throws Exception {
		natTable.doCommand(new SelectCellCommand(natTable, 5, 2, NO_SHIFT, NO_CTRL));

		// Note: the co-ordinates from this point on are in selection later co-ordinates

		SWTUtils.pressKey(SWT.ARROW_DOWN, natTable);
		SWTUtils.pressKey(SWT.ARROW_DOWN, natTable);
		assertPositionEquals(4, 3, selectionLayer.getLastSelectedCellPosition());
		assertSelectionAnchorEquals(4, 3);

		SWTUtils.pressKey(SWT.ARROW_DOWN, SWT.SHIFT, natTable);
		SWTUtils.pressKey(SWT.ARROW_DOWN, SWT.SHIFT, natTable);
		assertSelectCellsCount(2);
		assertCellSelected(4, 3);
		assertCellSelected(4, 4);
		assertSelectionAnchorEquals(4, 3);

		SWTUtils.pressKey(SWT.ARROW_DOWN, SWT.MOD1, natTable);
		assertSelectCellsCount(1);
		int lastRow = selectionLayer.getRowCount() - 1;
		assertPositionEquals(4, lastRow, selectionLayer.getSelectedCellPositions()[0]);
		assertPositionEquals(4, lastRow, selectionLayer.getLastSelectedCellPosition());
		assertSelectionAnchorEquals(4, lastRow);
	}

	@Test
	public void movingSelectionWithUpArrow() throws Exception {
		natTable.doCommand(new SelectCellCommand(natTable, 5, 4, NO_SHIFT, NO_CTRL));

		// Note: the co-ordinates from this point on are in selection later co-ordinates

		SWTUtils.pressKey(SWT.ARROW_UP, natTable);
		assertPositionEquals(4, 2, selectionLayer.getSelectedCellPositions()[0]);
		assertSelectionAnchorEquals(4, 2);

		SWTUtils.pressKey(SWT.ARROW_UP, SWT.SHIFT, natTable);
		SWTUtils.pressKey(SWT.ARROW_UP, SWT.SHIFT, natTable);
		assertSelectCellsCount(3);
		assertCellSelected(4, 0);
		assertCellSelected(4, 1);
		assertCellSelected(4, 2);
		assertSelectionAnchorEquals(4, 2);

		natTable.doCommand(new SelectCellCommand(natTable, 5, 4, NO_SHIFT, NO_CTRL));
		assertSelectCellsCount(1);

		SWTUtils.pressKey(SWT.ARROW_UP, SWT.MOD1, natTable);
		assertSelectCellsCount(1);
		int lastRow = 0;
		assertPositionEquals(4, lastRow, selectionLayer.getSelectedCellPositions()[0]);
		assertPositionEquals(4, lastRow, selectionLayer.getLastSelectedCellPosition());
		assertSelectionAnchorEquals(4, lastRow);
	}

	@Test
	public void selectingRectangularAreasWithShiftKey() throws Exception {
		natTable.doCommand(new SelectCellCommand(natTable, 2, 1, NO_SHIFT, NO_CTRL));
		natTable.doCommand(new SelectCellCommand(natTable, 2, 3, SHIFT, NO_CTRL));

		assertSelectCellsCount(3);
		assertSelectionAnchorEquals(1, 0);
		assertCellSelected(1, 0);
		assertCellSelected(1, 1);
		assertCellSelected(1, 2);

		SWTUtils.pressKey(SWT.ARROW_RIGHT, SWT.SHIFT, natTable);

		// Previously selected rectangular area must be extended
		assertSelectCellsCount(6);
		assertSelectionAnchorEquals(1, 0);
		assertCellSelected(1, 0);
		assertCellSelected(1, 1);
		assertCellSelected(1, 2);
		assertCellSelected(2, 0);
		assertCellSelected(2, 1);
		assertCellSelected(2, 2);

		SWTUtils.pressKey(SWT.ARROW_UP, SWT.SHIFT, natTable);
		assertSelectCellsCount(4);
		assertSelectionAnchorEquals(1, 0);
		assertCellSelected(1, 0);
		assertCellSelected(1, 1);
		assertCellSelected(2, 0);
		assertCellSelected(2, 1);
	}

	// Convenience asserts

	private void assertCellSelected(int column, int row){
		PositionCoordinate[] selectedCells = selectionLayer.getSelectedCellPositions();
		boolean selected  = false;

		for (PositionCoordinate positionCoordinate : selectedCells) {
			if(column == positionCoordinate.getColumnPosition() &&
					row == positionCoordinate.getRowPosition()){
				selected = true;
				break;
			}
		}
		Assert.assertTrue(selected);
	}

	private PositionCoordinate[] getSelectedCells(){
		return selectionLayer.getSelectedCellPositions();
	}

	private void assertSelectCellsCount(int count) {
		assertEquals(count, selectionLayer.getSelectedCellPositions().length);
	}

	private void assertSelectionAnchorEquals(int column, int row) {
		assertPositionEquals(column, row, selectionLayer.getSelectionAnchor());
	}

	private void assertPositionEquals(int column, int row, PositionCoordinate position) {
		assertEquals(column, position.getColumnPosition());
		assertEquals(row, position.getRowPosition());
	}


}
