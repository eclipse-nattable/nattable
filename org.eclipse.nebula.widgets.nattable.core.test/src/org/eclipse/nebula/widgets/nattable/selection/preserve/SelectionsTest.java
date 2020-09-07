/*******************************************************************************
 * Copyright (c) 2014, 2020 Jonas Hugo, Markus Wahl.
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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - made Selections.Row a static inner class
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.CellPosition;
import org.junit.Test;

public class SelectionsTest {

    private Selections<String[]> testee = new Selections<>();

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
        assertFalse(this.testee.isSelected(this.rowA, this.columnPosition2));
    }

    @Test
    public void Selecting_A_Cell_For_Unselected_Row() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        assertTrue(this.testee.isSelected(this.rowA, this.columnPosition2));
    }

    @Test
    public void Selecting_A_Cell_For_Already_Selected_Row() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition1);
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        assertTrue(this.testee.isSelected(this.rowA, this.columnPosition1));
        assertTrue(this.testee.isSelected(this.rowA, this.columnPosition2));
    }

    @Test
    public void Clear_Removes_All_Selections() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition1);
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        this.testee.select(this.rowB, this.rowObjectB, this.columnPosition2);
        this.testee.clear();
        assertFalse(this.testee.isSelected(this.rowA, this.columnPosition1));
        assertFalse(this.testee.isSelected(this.rowA, this.columnPosition2));
        assertFalse(this.testee.isSelected(this.rowB, this.columnPosition2));

        assertTrue(this.testee.getRows().isEmpty());
        assertTrue(this.testee.getColumnPositions().length == 0);
    }

    @Test
    public void Deselecting_Cells_Does_Only_Affect_Those_Cells() {
        // cell not to be touched
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition1);

        // Cells to be touched
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        this.testee.select(this.rowB, this.rowObjectB, this.columnPosition2);
        this.testee.deselect(this.rowA, this.columnPosition2);
        this.testee.deselect(this.rowB, this.columnPosition2);

        assertTrue(this.testee.isSelected(this.rowA, this.columnPosition1));
        assertFalse(this.testee.isSelected(this.rowA, this.columnPosition2));
        assertFalse(this.testee.isSelected(this.rowB, this.columnPosition2));
    }

    @Test
    public void Fully_Deselected_Row_Doesent_Linger() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition1);
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        this.testee.deselect(this.rowA, this.columnPosition1);
        this.testee.deselect(this.rowA, this.columnPosition2);

        assertFalse(this.testee.isRowSelected(this.rowA));
    }

    @Test
    public void None_Selected_Cells_Is_Empty() {
        assertTrue(this.testee.isEmpty());
    }

    @Test
    public void Selected_Cell_Is_Not_Empty() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition1);
        assertFalse(this.testee.isEmpty());
    }

    @Test
    public void Fully_Deselecting_All_Rows_Causes_Is_Empty() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition1);
        this.testee.deselect(this.rowA, this.columnPosition1);
        assertTrue(this.testee.isEmpty());
    }

    @Test
    public void getSelections_Retrieves_All_Cells() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        this.testee.select(this.rowB, this.rowObjectB, this.columnPosition2);

        HashSet<CellPosition<String[]>> actualCells = new HashSet<>(this.testee.getSelections());

        HashSet<CellPosition<String[]>> expectedCells = new HashSet<>();
        expectedCells.add(new CellPosition<>(this.rowObjectA, this.columnPosition2));
        expectedCells.add(new CellPosition<>(this.rowObjectB, this.columnPosition2));

        assertEquals(expectedCells, actualCells);
    }

    @Test
    public void getRows() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        this.testee.select(this.rowB, this.rowObjectB, this.columnPosition2);

        HashSet<Serializable> actualRowIds = new HashSet<>();
        for (Selections.Row<String[]> row : this.testee.getRows()) {
            actualRowIds.add(row.getId());
        }

        HashSet<Serializable> expectedRowIds = new HashSet<>();
        expectedRowIds.add(this.rowA);
        expectedRowIds.add(this.rowB);

        assertEquals(expectedRowIds, actualRowIds);
    }

    @Test
    public void getColumns() {
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition2);
        this.testee.select(this.rowA, this.rowObjectA, this.columnPosition3);
        this.testee.select(this.rowB, this.rowObjectB, this.columnPosition1);

        int[] actualColumns = this.testee.getColumnPositions();

        int[] expectedColumns = new int[] { this.columnPosition1, this.columnPosition2, this.columnPosition3 };

        assertTrue(Arrays.equals(expectedColumns, actualColumns));
    }

}
