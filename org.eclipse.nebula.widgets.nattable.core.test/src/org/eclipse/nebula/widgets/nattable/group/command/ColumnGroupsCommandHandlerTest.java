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
package org.eclipse.nebula.widgets.nattable.group.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnGroupsCommandHandlerTest {

    private ColumnGroupsCommandHandler handler;
    private ColumnGroupModel model;
    private SelectionLayer selectionLayer;
    private DefaultGridLayer gridLayer;

    @BeforeEach
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.selectionLayer = this.gridLayer.getBodyLayer().getSelectionLayer();
        this.model = new ColumnGroupModel();
        this.handler = new ColumnGroupsCommandHandler(
                this.model,
                this.selectionLayer,
                new ColumnGroupHeaderLayer(
                        this.gridLayer.getColumnHeaderLayer(),
                        this.selectionLayer,
                        new ColumnGroupModel()));
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    @Test
    public void shouldCreateColumnGroupFromSelectedColumns() {
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(0));
        assertTrue(this.model.isEmpty());

        String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(columnGroupName, getColumnGroupNameForIndex(0));
        assertEquals(1, getColumnIndexesInGroup(0).size());
    }

    @Test
    public void shouldCreateColumnGroupAfterReordering() {
        // Reorder column to first position
        this.selectionLayer.doCommand(
                new ColumnReorderCommand(this.selectionLayer, 9, 0));
        // Select first column position
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));

        String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(columnGroupName, getColumnGroupNameForIndex(9));
        assertEquals(9, getColumnIndexesInGroup(9).get(0).intValue());
    }

    @Test
    public void shouldUngroupMiddleSelectedColumns() {
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(3, getColumnIndexesInGroup(0).size());
        assertEquals(0, getColumnIndexesInGroup(0).get(0).intValue());
        assertEquals(1, getColumnIndexesInGroup(0).get(1).intValue());
        assertEquals(2, getColumnIndexesInGroup(0).get(2).intValue());

        // Test ungrouping column in middle
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        this.handler.handleUngroupCommand();

        assertEquals(2, getColumnIndexesInGroup(0).size());
        assertEquals(0, getColumnIndexesInGroup(0).get(0).intValue());
        assertEquals(2, getColumnIndexesInGroup(0).get(1).intValue());

        assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
        assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
    }

    @Test
    public void shouldNotUngroupColumnsInUnbreakableGroups() throws Exception {
        this.model.addColumnsIndexesToGroup("Test group 1", 0, 1, 2);
        this.model.getColumnGroupByIndex(0).setUnbreakable(true);

        // Ungroup column in the middle
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        this.handler.handleUngroupCommand();

        assertEquals(3, getColumnIndexesInGroup(0).size());
        assertTrue(getColumnIndexesInGroup(0).contains(0));
        assertTrue(getColumnIndexesInGroup(0).contains(1));
        assertTrue(getColumnIndexesInGroup(0).contains(2));

        // Ungroup first column
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.handler.handleUngroupCommand();

        assertEquals(3, getColumnIndexesInGroup(0).size());
        assertTrue(getColumnIndexesInGroup(0).contains(0));
        assertTrue(getColumnIndexesInGroup(0).contains(1));
        assertTrue(getColumnIndexesInGroup(0).contains(2));

        // Assert the columns haven't moved
        assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
        assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
    }

    @Test
    public void shouldUngroupFirstSelectedColumn() {
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        // Test ungrouping first column
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.handler.handleUngroupCommand();

        assertEquals(2, getColumnIndexesInGroup(2).size());
        assertEquals(1, getColumnIndexesInGroup(2).get(0).intValue());
        assertEquals(2, getColumnIndexesInGroup(2).get(1).intValue());

        assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
        assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
    }

    @Test
    public void shouldUngroupFirstAndLastSelectedColumn() {
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        // Test ungrouping first and last column
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));
        this.handler.handleUngroupCommand();

        assertEquals(1, getColumnIndexesInGroup(1).size());
        assertEquals(1, getColumnIndexesInGroup(1).get(0).intValue());

        assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
        assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
    }

    @Test
    public void shouldRemoveAllColumnsInGroup() {
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        // Test ungrouping all columns
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));
        this.handler.handleUngroupCommand();

        assertFalse(this.model.isPartOfAGroup(0));
        assertFalse(this.model.isPartOfAGroup(1));
        assertFalse(this.model.isPartOfAGroup(2));

        assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
        assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
    }

    @Test
    public void shouldReorderToConsecutive() {
        assertEquals(0, this.selectionLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.selectionLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(2));

        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertTrue(this.model.isPartOfAGroup(0));
        assertFalse(this.model.isPartOfAGroup(1));
        assertTrue(this.model.isPartOfAGroup(2));
        assertFalse(this.model.isPartOfAGroup(3));
        assertTrue(this.model.isPartOfAGroup(4));

        assertEquals(0, this.selectionLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(1));
        assertEquals(4, this.selectionLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void shouldAddRightColumnToExistingGroup() {
        assertEquals(0, this.selectionLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.selectionLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.selectionLayer.getColumnIndexByPosition(3));

        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));

        // create the group initially with the first two columns
        String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(2, this.model.getColumnGroupByIndex(0).getSize());
        assertTrue(this.model.isPartOfAGroup(0));
        assertTrue(this.model.isPartOfAGroup(1));
        assertFalse(this.model.isPartOfAGroup(2));
        assertFalse(this.model.isPartOfAGroup(3));

        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 3, 0, false, false));

        // add the column on the right to the existing group
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(3, this.model.getColumnGroupByIndex(0).getSize());
        assertTrue(this.model.isPartOfAGroup(0));
        assertTrue(this.model.isPartOfAGroup(1));
        assertFalse(this.model.isPartOfAGroup(2));
        assertTrue(this.model.isPartOfAGroup(3));

        assertEquals(0, this.selectionLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.selectionLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.selectionLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void shouldAddLeftColumnToExistingGroup() {
        assertEquals(0, this.selectionLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.selectionLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.selectionLayer.getColumnIndexByPosition(3));

        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));

        // create the group initially with the last two columns
        String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(2, this.model.getColumnGroupByIndex(2).getSize());
        assertFalse(this.model.isPartOfAGroup(0));
        assertFalse(this.model.isPartOfAGroup(1));
        assertTrue(this.model.isPartOfAGroup(2));
        assertTrue(this.model.isPartOfAGroup(3));

        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));

        // add the column on the left to the existing group
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        assertEquals(3, this.model.getColumnGroupByIndex(0).getSize());
        assertTrue(this.model.isPartOfAGroup(0));
        assertFalse(this.model.isPartOfAGroup(1));
        assertTrue(this.model.isPartOfAGroup(2));
        assertTrue(this.model.isPartOfAGroup(3));

        assertEquals(0, this.selectionLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.selectionLayer.getColumnIndexByPosition(2));
        assertEquals(1, this.selectionLayer.getColumnIndexByPosition(3));
    }

    private List<Integer> getColumnIndexesInGroup(int columnIndex) {
        return this.model.getColumnGroupByIndex(columnIndex).getMembers();
    }

    private String getColumnGroupNameForIndex(int columnIndex) {
        return this.model.getColumnGroupByIndex(columnIndex).getName();
    }

}
