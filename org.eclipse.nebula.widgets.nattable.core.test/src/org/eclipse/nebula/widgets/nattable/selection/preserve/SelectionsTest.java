/*******************************************************************************
 * Copyright (c) 2014 Jonas Hugo, Markus Wahl.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - initial test
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections;
import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.CellPosition;
import org.junit.Test;

public class SelectionsTest {

	private Selections<String[]> testee = new Selections<String[]>();

	private Serializable rowA = "rowA";

	private Serializable rowB = "rowB";

	/**
	 * Each row consist of an array of String, one String for each column, i e
	 * each cell is a String.
	 */
	private String[] rowObjectA = new String[] { "good", "day" };

	private String[] rowObjectB = new String[] { "bad", "night" };

	private int columnPosition1 = 1;

	private int columnPosition2 = 2;

	private int columnPosition3 = 3;

	@Test
	public void Never_Selected_Cell_Is_Not_Selected() {
		assertFalse(testee.isSelected(rowA, columnPosition2));
	}

	@Test
	public void Selecting_A_Cell_For_Unselected_Row() {
		testee.select(rowA, rowObjectA, columnPosition2);
		assertTrue(testee.isSelected(rowA, columnPosition2));
	}

	@Test
	public void Selecting_A_Cell_For_Already_Selected_Row() {
		testee.select(rowA, rowObjectA, columnPosition1);
		testee.select(rowA, rowObjectA, columnPosition2);
		assertTrue(testee.isSelected(rowA, columnPosition1));
		assertTrue(testee.isSelected(rowA, columnPosition2));
	}

	@Test
	public void Clear_Removes_All_Selections() {
		testee.select(rowA, rowObjectA, columnPosition1);
		testee.select(rowA, rowObjectA, columnPosition2);
		testee.select(rowB, rowObjectB, columnPosition2);
		testee.clear();
		assertFalse(testee.isSelected(rowA, columnPosition1));
		assertFalse(testee.isSelected(rowA, columnPosition2));
		assertFalse(testee.isSelected(rowB, columnPosition2));

		assertTrue(testee.getRows().isEmpty());
		assertTrue(testee.getColumnPositions().isEmpty());
	}

	@Test
	public void Deselecting_Cells_Does_Only_Affect_Those_Cells() {
		// cell not to be touched
		testee.select(rowA, rowObjectA, columnPosition1);

		// Cells to be touched
		testee.select(rowA, rowObjectA, columnPosition2);
		testee.select(rowB, rowObjectB, columnPosition2);
		testee.deselect(rowA, columnPosition2);
		testee.deselect(rowB, columnPosition2);

		assertTrue(testee.isSelected(rowA, columnPosition1));
		assertFalse(testee.isSelected(rowA, columnPosition2));
		assertFalse(testee.isSelected(rowB, columnPosition2));
	}

	@Test
	public void Fully_Deselected_Row_Doesent_Linger() {
		testee.select(rowA, rowObjectA, columnPosition1);
		testee.select(rowA, rowObjectA, columnPosition2);
		testee.deselect(rowA, columnPosition1);
		testee.deselect(rowA, columnPosition2);

		assertFalse(testee.isRowSelected(rowA));
	}

	@Test
	public void None_Selected_Cells_Is_Empty() {
		assertTrue(testee.isEmpty());
	}

	@Test
	public void Selected_Cell_Is_Not_Empty() {
		testee.select(rowA, rowObjectA, columnPosition1);
		assertFalse(testee.isEmpty());
	}

	@Test
	public void Fully_Deselecting_All_Rows_Causes_Is_Empty() {
		testee.select(rowA, rowObjectA, columnPosition1);
		testee.deselect(rowA, columnPosition1);
		assertTrue(testee.isEmpty());
	}

	@Test
	public void getSelections_Retrieves_All_Cells() {
		testee.select(rowA, rowObjectA, columnPosition2);
		testee.select(rowB, rowObjectB, columnPosition2);

		HashSet<CellPosition<String[]>> actualCells = new HashSet<CellPosition<String[]>>(testee.getSelections());

		HashSet<CellPosition<String[]>> expectedCells = new HashSet<CellPosition<String[]>>();
		expectedCells.add(new CellPosition<String[]>(rowObjectA, columnPosition2));
		expectedCells.add(new CellPosition<String[]>(rowObjectB, columnPosition2));

		assertEquals(expectedCells, actualCells);
	}

	@Test
	public void getRows() {
		testee.select(rowA, rowObjectA, columnPosition2);
		testee.select(rowB, rowObjectB, columnPosition2);

		HashSet<Serializable> actualRowIds = new HashSet<Serializable>();
		for (Selections<String[]>.Row row : testee.getRows()) {
			actualRowIds.add(row.getId());
		}

		HashSet<Serializable> expectedRowIds = new HashSet<Serializable>();
		expectedRowIds.add(rowA);
		expectedRowIds.add(rowB);

		assertEquals(expectedRowIds, actualRowIds);
	}

	@Test
	public void getColumns() {
		testee.select(rowA, rowObjectA, columnPosition2);
		testee.select(rowA, rowObjectA, columnPosition3);
		testee.select(rowB, rowObjectB, columnPosition1);

		HashSet<Integer> actualColumns = new HashSet<Integer>(testee.getColumnPositions());

		HashSet<Integer> expectedColumns = new HashSet<Integer>();
		expectedColumns.add(columnPosition2);
		expectedColumns.add(columnPosition1);
		expectedColumns.add(columnPosition3);

		assertEquals(expectedColumns, actualColumns);
	}

}
