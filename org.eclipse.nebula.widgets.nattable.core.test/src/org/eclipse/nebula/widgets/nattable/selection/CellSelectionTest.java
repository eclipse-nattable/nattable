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

import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.RIGHT;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CellSelectionTest {

	private SelectionLayer selectionLayer;
	private MoveCellSelectionCommandHandler moveCommandHandler;

	@Before
	public void setUp() {
		selectionLayer = new SelectionLayer(new DataLayerFixture(10, 10, 100, 40));
		// Selection grid origin as starting point
		selectionLayer.setSelectedCell(0, 0);
		moveCommandHandler = new MoveCellSelectionCommandHandler(selectionLayer);
	}

	@After
	public void cleanUp() {
		selectionLayer.clear();
	}

	@Test
	public void shouldHaveOriginSelected() {
		Assert.assertTrue(isLastCellInOrigin());
	}

	private boolean isLastCellInOrigin() {
		return (0 == selectionLayer.getLastSelectedCellPosition().columnPosition && 0 == selectionLayer.getLastSelectedCellPosition().rowPosition);
	}

	private boolean isSelectonAnchorInOrigin() {
		return (0 == selectionLayer.getSelectionAnchor().columnPosition && 0 == selectionLayer.getSelectionAnchor().rowPosition);
	}

	private boolean wasPreviousSelectionCleared() {
		// Make sure previous selection was cleared
		return (selectionLayer.getSelectedColumnPositions().length == 1 && selectionLayer.getSelectedRowCount() == 1);
	}

	private boolean wasPreviousColumnSelectionAppended() {
		// Make sure previous column selection was not cleared
		return selectionLayer.getSelectedColumnPositions().length > 1;
	}

	private boolean wasPreviousRowSelectionAppended() {
		// Make sure previous column selection was not cleared
		return selectionLayer.getSelectedRowCount() > 1;
	}

	// Tests for cell selection NTBL-224

	@Test
	public void shouldMoveTheSelectionAnchorLeftUsingLeftArrowKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, 1, false, false);
		// Should not have moved
		Assert.assertTrue(isLastCellInOrigin());

		selectionLayer.setSelectedCell(1, 0);

		// Should move back to origin
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, 1, false, false);
		Assert.assertTrue(isLastCellInOrigin());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendTheSelectionToTheLeftUsingLeftArrowAndShiftKeys() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, 1, true, false);
		// Should not have moved
		Assert.assertTrue(isLastCellInOrigin());

		selectionLayer.setSelectedCell(2, 0);

		// Should move back to origin
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, 1 , true, false);

		// Last selected cell should now be the origin
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, 1 , true, false);
		Assert.assertTrue(isLastCellInOrigin());

		// Selection anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getSelectionAnchor().getRowPosition());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousColumnSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelectionAnchorRightUsingRightArrowKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.RIGHT, 1, false, false);

		// Previous selection was cleared and origin should no longer be selected
		Assert.assertFalse(isLastCellInOrigin());

		// The selection anchor moved right
		Assert.assertEquals(1, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getSelectionAnchor().getRowPosition());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendTheSelectionRightUsingRightArrowAndShiftKeys() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.RIGHT, 1, true, false);
		moveCommandHandler.moveSelection(MoveDirectionEnum.RIGHT, 1, true, false);

		// Since selection started at origin, then origin should be part of the selected range
		Assert.assertTrue(isSelectonAnchorInOrigin());

		// Selection should now end on the cell to the right of the selection anchor
		Assert.assertEquals(2, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getSelectionAnchor().getColumnPosition());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousColumnSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelectionAnchorUpUsingUpArrowKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 1, false, false);

		// Should not have moved
		Assert.assertTrue(isLastCellInOrigin());

		selectionLayer.setSelectedCell(0, 2);

		// Should move back to origin
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 1, false, false);
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 1, false, false);
		Assert.assertTrue(isLastCellInOrigin());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendTheSelectionUpUsingUpArrowAndShiftKeys() {
		selectionLayer.setSelectedCell(0, 1);
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 1, true, false);

		// Anchor should not have changed
		Assert.assertEquals(1, selectionLayer.getSelectionAnchor().getRowPosition());
		Assert.assertEquals(0, selectionLayer.getSelectionAnchor().getColumnPosition());

		// Last selected cell should be the origin
		Assert.assertTrue(isLastCellInOrigin());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousRowSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelectionAnchorDownUsingDownArrowKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.DOWN, 1, false, false);

		// Previous selection was cleared and origin should no longer be selected
		Assert.assertFalse(isLastCellInOrigin());

		// Las selected cell is one step below origin
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(1, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendTheSelectionDownUsingDownArrowAndShiftKeys() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.DOWN, 1, true, false);

		// Selection anchor remains at origing
		Assert.assertTrue(isSelectonAnchorInOrigin());

		// Las selected cell is one step below origin
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(1, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousRowSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelecitonAnchorToStartOfRowUsingHomeKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, SelectionLayer.MOVE_ALL, false, false);

		// Should not have moved
		Assert.assertTrue(isLastCellInOrigin());

		// Move to middle of grid
		selectionLayer.setSelectedCell(2, 0);

		// Should move back to origin
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, SelectionLayer.MOVE_ALL, false, false);
		Assert.assertTrue(isLastCellInOrigin());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendTheSelectionToStartOfRowUsingHomeAndShiftKeys() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, SelectionLayer.MOVE_ALL, true, false);

		// Should not have moved
		Assert.assertTrue(isLastCellInOrigin());

		// Move to middle of grid
		selectionLayer.setSelectedCell(2, 0);

		// Should move back to origin
		moveCommandHandler.moveSelection(MoveDirectionEnum.LEFT, SelectionLayer.MOVE_ALL, true, false);
		Assert.assertTrue(isLastCellInOrigin());

		// Selection anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getSelectionAnchor().getRowPosition());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousColumnSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelectionAnchorToEndOfRowUsingEndKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.RIGHT, SelectionLayer.MOVE_ALL, false, false);

		// Selection anchor moved to end of grid
		Assert.assertEquals(9, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getSelectionAnchor().getRowPosition());

		// Previous selection was cleared and origin should no longer be selected
		Assert.assertFalse(isLastCellInOrigin());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendTheSelectionToEndOfRowUsingEndAndShiftKeys() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.RIGHT, SelectionLayer.MOVE_ALL, true, false);

		// Selection anchor should stay at the origin
		Assert.assertTrue(isSelectonAnchorInOrigin());

		// Last selected cell is at end of grid
		Assert.assertEquals(9, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousColumnSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelectionAnchorOnePageUpUsingPageUpKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 6, false, false);

		// Should not have moved
		Assert.assertTrue(isLastCellInOrigin());

		// Move to middle of grid
		final int columnPosition = 2;
		final int rowPosition = 4;
		selectionLayer.setSelectedCell(columnPosition, rowPosition);

		// Should not have moved
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 10, false, false);

		// Previous selection was cleared and origin should no longer be selected
		Assert.assertFalse(isLastCellInOrigin());

		// Should move back to first row event if step size is greater than available number of rows
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 60, false, false);
		Assert.assertEquals(2, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendSelectionOnePageUpUsingThePageUpAndShiftKeys() {
		// Move to middle of grid
		final int columnPosition = 2;
		final int rowPosition = 4;
		selectionLayer.setSelectedCell(columnPosition, rowPosition);

		// Should move back to first row event if step size is greater than available number of rows
		moveCommandHandler.moveSelection(MoveDirectionEnum.UP, 10, true, false);
		Assert.assertEquals(2, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Selection anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(4, selectionLayer.getSelectionAnchor().getRowPosition());

		// Cells in between should have been appended
		Assert.assertTrue(wasPreviousRowSelectionAppended());
	}

	@Test
	public void shouldMoveTheSelectionAnchorOnePageDownUsingPageDownKey() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.DOWN, 6, false, false);

		// Should move to last row even if step size is greater than available number of rows
		moveCommandHandler.moveSelection(MoveDirectionEnum.DOWN, 60, false, false);
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(9, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousSelectionCleared());
	}

	@Test
	public void shouldExtendSelectionOnePageDownUsingPageDownAndShiftKeys() {
		moveCommandHandler.moveSelection(MoveDirectionEnum.DOWN, 6, true, false);

		// Selection anchor should not have changed
		Assert.assertTrue(isSelectonAnchorInOrigin());

		// Last selected cell should be in the last row
		Assert.assertEquals(0, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(6, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Previous selection was cleared
		Assert.assertTrue(wasPreviousRowSelectionAppended());
	}

	/**
	 * If a range of cells is selected - clear selection and move anchor
	 *    to the next cell in the direction moved
	 */
	@Test
	public void moveCellWhenARangeOfCellsIsSelected() throws Exception {

		new SelectColumnCommandHandler(selectionLayer).selectColumn(2, 0, false, false);
		moveCommandHandler.moveSelection(RIGHT, 1, false, false);

		assertEquals(3,selectionLayer.getSelectionAnchor().columnPosition);
		assertEquals(0,selectionLayer.getSelectionAnchor().rowPosition);
	}

	/**
	 * Selected cells are (col,row): (2,3),(4,1),(1,0),(9,9)
	 */
	@Test
	public void shouldReturnTheCorrectCountOfSelectedCells() {
		selectionLayer.clear();
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 3, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 4, 1, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 9, 9, false, true));

		PositionCoordinate[] cells = selectionLayer.getSelectedCellPositions();
		Assert.assertEquals(4, cells.length);
		// (1, 0)
		Assert.assertEquals(1, cells[0].columnPosition);
		Assert.assertEquals(0, cells[0].rowPosition);
		// (2, 3)
		Assert.assertEquals(2, cells[1].columnPosition);
		Assert.assertEquals(3, cells[1].rowPosition);
		// (4, 1)
		Assert.assertEquals(4, cells[2].columnPosition);
		Assert.assertEquals(1, cells[2].rowPosition);
		// (9, 9)
		Assert.assertEquals(9, cells[3].columnPosition);
		Assert.assertEquals(9, cells[3].rowPosition);
	}

	@Test
	public void shouldReturnSixCells() {
		selectionLayer.clear();
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 0, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 1, 1, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 1, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 1, 2, false, true));
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 2, false, true));

		Collection<PositionCoordinate> cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());

		Assert.assertEquals(6, cells.size());
		// (1, 0)
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 1, 0)));
		// (1, 1)
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 1, 1)));
		// (1, 2)
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 1, 2)));
		// (2, 0)
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 2, 0)));
		// (2, 1)
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 2, 1)));
		// (2, 2)
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 2, 2)));
	}
	
	@Test
	public void onlyOneCellSelectedAtAnyTime() {
		selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

		selectionLayer.clear();
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 1, 0, false, true));

		Collection<PositionCoordinate> cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(1, cells.size());
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 1, 0)));

		//select another cell with control mask
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 0, false, true));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(1, cells.size());
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 2, 0)));

		//select additional cells with shift mask
		//only the first cell should be selected afterwards
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 10, true, false));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(1, cells.size());
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 2, 0)));

		//select additional cells with shift mask
		//only the first cell should be selected afterwards
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 10, 0, true, false));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(1, cells.size());
		Assert.assertTrue(cells.contains(new PositionCoordinate(selectionLayer, 2, 0)));
	}
}
