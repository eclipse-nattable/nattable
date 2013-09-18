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
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowSelectionTest {
// Tests for column selection NTBL-225

	private SelectionLayer selectionLayer;

	@Before
	public void setUp() {
		selectionLayer = new SelectionLayer(new DataLayerFixture());
		// Selection grid origin as starting point
		selectionLayer.setSelectedCell(0, 0);
	}

	@After
	public void cleanUp() {
		selectionLayer.clear();
	}

	@Test
	public void shouldSelectAllCellsInARow() {
		final int columnCount = selectionLayer.getColumnCount();
		// User has clicked on second row header cell
		selectionLayer.selectRow(1, 2, false, false);

		// Selection anchor should be at row 2, column 0
		Assert.assertEquals(1, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		// Last selected cell should be part of last column
		Assert.assertEquals(1, selectionLayer.getLastSelectedCellPosition().getColumnPosition());
		Assert.assertEquals(2, selectionLayer.getLastSelectedCellPosition().getRowPosition());

		// Cells in between should have been selected
		Assert.assertEquals(columnCount, selectionLayer.getSelectedColumnPositions().length);
	}

	@Test
	public void shouldExtendSelectionUpWithShiftKey() {
		// User selects cell
		selectionLayer.selectCell(2, 2, false, false);
		// User selects column using shift key mask
		selectionLayer.selectRow(1, 1, true, false);

		// Selection Anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().columnPosition);
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		assertCellsSelectedBetween(2, 1);
	}

	@Test
	public void shouldExtendSelectionDownWithShiftKey() {
		// User selects cell
		selectionLayer.selectCell(2, 2, false, false);
		// User selects column using shift key mask
		selectionLayer.selectRow(1, 4, true, false);

		// Selection Anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().columnPosition);
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		assertCellsSelectedBetween(2, 4);
	}

	@Test
	public void shouldAppendSelectionUpWithShiftKey() {
		// User selects cell
		selectionLayer.selectCell(2, 2, false, false);
		// User selects column using shift key mask
		selectionLayer.selectRow(1, 1, true, false);

		// Selection Anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().columnPosition);
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		assertCellsSelectedBetween(2, 1);

		selectionLayer.selectRow(1, 0, true, false);
		assertCellsSelectedBetween(2, 0);
	}

	@Test
	public void shouldAppendSelectionDownWithShiftKey() {
		// User selects cell
		selectionLayer.selectCell(2, 2, false, false);
		// User selects column using shift key mask
		selectionLayer.selectRow(1, 3, true, false);

		// Selection Anchor should not have changed
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().columnPosition);
		Assert.assertEquals(2, selectionLayer.getSelectionAnchor().getRowPosition());

		assertCellsSelectedBetween(2, 3);

		selectionLayer.selectRow(1, 5, true, false);
		assertCellsSelectedBetween(2, 5);
	}

	private void assertCellsSelectedBetween(int startRowPosition, int endRowPosition) {
		for (int row = startRowPosition; row <= endRowPosition; row++) {
			for (int col = 0; col <= 4; col++) {
				assertTrue("["+col +", "+row +"] not selected", selectionLayer.isCellPositionSelected(col, row));
			}
		}
	}

	@Test
	public void shouldExtendSelectionWithAllCellsInARowUsingTheCtrlKey() {
		final int columnCount = selectionLayer.getColumnCount();

		// User has selected 3 non-consecutive cells
		selectionLayer.selectCell(2, 2, false, false);
		selectionLayer.selectCell(3, 2, false, true);
		selectionLayer.selectCell(2, 0, false, true);

		// User has clicked on second row header cell
		selectionLayer.selectRow(1, 1, false, true);

		// Selection anchor should be at row 1, col 0
		Assert.assertEquals(1, selectionLayer.getSelectionAnchor().getColumnPosition());
		Assert.assertEquals(1, selectionLayer.getSelectionAnchor().getRowPosition());

		// Last selected cell should be part of last column
		Assert.assertEquals(1, selectionLayer.getLastSelectedCellPosition().getRowPosition());
		final int lastColumnPosition = selectionLayer.getLastSelectedCellPosition().getColumnPosition();
		Assert.assertEquals(1, lastColumnPosition);

		// Cells in row should have been selected
		final int[] selectedColumns = selectionLayer.getSelectedColumnPositions();
		Assert.assertEquals(columnCount, selectedColumns.length);
		Assert.assertTrue(selectionLayer.isCellPositionSelected(4, 1));

		// Test extending column selection to the right of previous column selection
		selectionLayer.selectRow(1, 3, false, true);

		// Selection model should contain all previously selected cells
		Assert.assertTrue(selectionLayer.isCellPositionSelected(2, 2));
		Assert.assertTrue(selectionLayer.isCellPositionSelected(3, 2));
		Assert.assertTrue(selectionLayer.isCellPositionSelected(2, 0));

		// 3rd row cells should be selected
		Assert.assertTrue(selectionLayer.isCellPositionSelected(1, 3));
		Assert.assertTrue(selectionLayer.isCellPositionSelected(3, 3));
	}
	
	
	@Test
	public void onlyOneCellSelectedAtAnyTime() {
		selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

		selectionLayer.clear();
		selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 1, 0, false, true));

		Collection<PositionCoordinate> cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		assertEquals(1, cells.size());
		assertEquals(1, selectionLayer.getSelectedRowPositions().size());
		assertEquals(1, selectionLayer.getSelectedRowCount());

		//select another row with control mask
		selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 1, 2, false, true));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		assertEquals(1, cells.size());
		assertEquals(1, selectionLayer.getSelectedRowPositions().size());
		assertEquals(1, selectionLayer.getSelectedRowCount());

		//select additional rows with shift mask
		selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 1, 5, true, false));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		assertEquals(1, cells.size());
		assertEquals(1, selectionLayer.getSelectedRowPositions().size());
		assertEquals(1, selectionLayer.getSelectedRowCount());
	}

}
