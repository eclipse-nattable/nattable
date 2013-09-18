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
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
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
		selectionLayer = new SelectionLayer(new DataLayerFixture(10, 50, 100, 40));
		// Selection grid origin as starting point
		selectionLayer.setSelectedCell(0, 0);
		commandHandler = new SelectColumnCommandHandler(selectionLayer);
	}

	@After
	public void cleanUp() {
		selectionLayer.clear();
	}

	@Test
	public void shouldSelectAllCellsInAColumn() {
		final int rowCount = selectionLayer.getRowCount();
		// User has clicked on second column header cell
		final int rowPosition = 25;
		commandHandler.selectColumn(2, rowPosition, false, false);

		// Selection anchor should be at row 0, col 2
		assertEquals(2, selectionLayer.getSelectionAnchor().getColumnPosition());
		assertEquals(rowPosition, selectionLayer.getSelectionAnchor().getRowPosition());

		// Last selected cell should be part of last row
		assertEquals(2, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		assertEquals(rowPosition, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Cells in between should have been selected
		assertEquals(rowCount, selectionLayer.getSelectedRowCount());
	}

	@Test
	public void shouldSelectAllCellsToTheRightWithShiftKeyPressed() throws Exception {
		selectionLayer.selectCell(2, 2, false, false);

		// User selects column using shift key mask
		commandHandler.selectColumn(4, 25, true, false);

		// Selection Anchor should not change
		assertEquals(2, selectionLayer.getSelectionAnchor().columnPosition);
		assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		assertCellsSelectedBetween(2,4);
	}

	@Test
	public void shouldSelectAllCellsToTheLeftWithShiftKeyPressed() throws Exception {
		selectionLayer.selectCell(2, 2, false, false);

		// User selects column using shift key mask
		commandHandler.selectColumn(0, 25,  true, false);

		// Selection Anchor should not change
		assertEquals(2, selectionLayer.getSelectionAnchor().columnPosition);
		assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		assertCellsSelectedBetween(0,2);
	}

	@Test
	public void shouldAppendColumnsToTheRightWithShiftKeyPressed() throws Exception {
		selectionLayer.selectCell(2, 2, false, false);

		commandHandler.selectColumn(3, 25,  true, false);
		assertCellsSelectedBetween(2,3);

		commandHandler.selectColumn(4, 25,  true, false);
		assertCellsSelectedBetween(2,4);
	}

	private void assertCellsSelectedBetween(int startColPosition, int endColPosition) {
		for (int col = startColPosition; col <= endColPosition; col++) {
			for (int row = 0; row <= 6; row++) {
				assertTrue("["+col +", "+row +"] not selected", selectionLayer.isCellPositionSelected(col, row));
			}
		}
	}

	@Test
	public void shouldExtendSelectionWithAllCellsInAColumnUsingTheCtrlKey() {
		final int rowCount = selectionLayer.getRowCount();

		// User has selected 3 non-consecutive cells
		selectionLayer.selectCell(2, 2, false, false);
		selectionLayer.selectCell(3, 2, false, true);
		selectionLayer.selectCell(2, 0, false, true);

		// User has clicked on second column header cell
		final int rowPosition = 25;
		commandHandler.selectColumn(1, rowPosition,  false, true);

		// Selection anchor should be at row 0, column 1
		assertEquals(1, selectionLayer.getSelectionAnchor().getColumnPosition());
		assertEquals(rowPosition, selectionLayer.getSelectionAnchor().getRowPosition());

		// Last selected cell should be part of last row
		assertEquals(1, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		final int lastRowPosition = selectionLayer.getLastSelectedCellPosition().getRowPosition();
		assertEquals(rowPosition, lastRowPosition);

		// Cells in column should have been selected
		assertEquals(rowCount, selectionLayer.getSelectedRowCount());
		assertTrue(selectionLayer.isCellPositionSelected(1, 4));

		// Test extending column selection to the right of previous column selection
		commandHandler.selectColumn(3, rowPosition,  false, true);

		// Selection model should contain all previously selected cells
		assertTrue(selectionLayer.isCellPositionSelected(2, 2));
		assertTrue(selectionLayer.isCellPositionSelected(3, 2));
		assertTrue(selectionLayer.isCellPositionSelected(2, 0));
		assertTrue(selectionLayer.isCellPositionSelected(1, 6));
		assertTrue(selectionLayer.isCellPositionSelected(3, 6));
	}
	
	@Test
	public void onlyOneCellSelectedAtAnyTime() {
		selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

		selectionLayer.clear();
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, true));

		Collection<PositionCoordinate> cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		assertEquals(1, cells.size());
		assertEquals(1, selectionLayer.getSelectedColumnPositions().length);
		assertEquals(1, selectionLayer.getSelectedColumnPositions()[0]);
		assertEquals(1, selectionLayer.getSelectedRowCount());

		//select another column with control mask
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		assertEquals(1, cells.size());
		assertEquals(1, selectionLayer.getSelectedColumnPositions().length);
		assertEquals(2, selectionLayer.getSelectedColumnPositions()[0]);
		assertEquals(1, selectionLayer.getSelectedRowCount());

		//select additional columns with shift mask
		//only the last column should be selected afterwards
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 5, 0, true, false));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		assertEquals(1, cells.size());
		assertEquals(1, selectionLayer.getSelectedColumnPositions().length);
		assertEquals(5, selectionLayer.getSelectedColumnPositions()[0]);
		assertEquals(1, selectionLayer.getSelectedRowCount());
	}

}
