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
package org.eclipse.nebula.widgets.nattable.group.command;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupsCommandHandlerTest {

    private ColumnGroupsCommandHandler handler;
    private ColumnGroupModel model;
    private SelectionLayer selectionLayer;
    private DefaultGridLayer gridLayer;

    @Before
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.selectionLayer = (SelectionLayer) this.gridLayer.getBodyLayer()
                .getViewportLayer().getScrollableLayer();
        this.model = new ColumnGroupModel();
        this.handler = new ColumnGroupsCommandHandler(this.model, this.selectionLayer,
                new ColumnGroupHeaderLayer(this.gridLayer.getColumnHeaderLayer(),
                        this.gridLayer.getBodyLayer().getSelectionLayer(),
                        new ColumnGroupModel()));
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display
                .getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    @Test
    public void shouldCreateColumnGroupFromSelectedColumns() {

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, false));
        Assert.assertTrue(this.selectionLayer.isColumnPositionFullySelected(0));
        Assert.assertTrue(this.model.isEmpty());

        final String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        Assert.assertEquals(columnGroupName, getColumnGroupNameForIndex(0));
        Assert.assertEquals(1, getColumnIndexesInGroup(0).size());
    }

    @Test
    public void shouldCreateColumnGroupAfterReordering() {
        // Reorder column to first position
        this.selectionLayer
                .doCommand(new ColumnReorderCommand(this.selectionLayer, 9, 0));
        // Select first column position
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, false));

        final String columnGroupName = "Test Group";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        Assert.assertEquals(columnGroupName, getColumnGroupNameForIndex(9));
        Assert.assertEquals(9, getColumnIndexesInGroup(9).get(0).intValue());
    }

    @Test
    public void shouldUngroupMiddleSelectedColumns() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0,
                false, true));

        final String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        Assert.assertEquals(3, getColumnIndexesInGroup(0).size());
        Assert.assertEquals(0, getColumnIndexesInGroup(0).get(0).intValue());
        Assert.assertEquals(1, getColumnIndexesInGroup(0).get(1).intValue());
        Assert.assertEquals(2, getColumnIndexesInGroup(0).get(2).intValue());

        // Test ungrouping column in middle
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, false));
        this.handler.handleUngroupCommand();

        Assert.assertEquals(2, getColumnIndexesInGroup(0).size());
        Assert.assertEquals(0, getColumnIndexesInGroup(0).get(0).intValue());
        Assert.assertEquals(2, getColumnIndexesInGroup(0).get(1).intValue());

        Assert.assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        Assert.assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
        Assert.assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
    }

    @Test
    public void shouldNotUngroupColumnsInUnbreakableGroups() throws Exception {
        this.model.addColumnsIndexesToGroup("Test group 1", 0, 1, 2);
        this.model.getColumnGroupByIndex(0).setUnbreakable(true);

        // Ungroup column in the middle
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, false));
        this.handler.handleUngroupCommand();

        Assert.assertEquals(3, getColumnIndexesInGroup(0).size());
        Assert.assertTrue(getColumnIndexesInGroup(0).contains(0));
        Assert.assertTrue(getColumnIndexesInGroup(0).contains(1));
        Assert.assertTrue(getColumnIndexesInGroup(0).contains(2));

        // Ungroup first column
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, false));
        this.handler.handleUngroupCommand();

        Assert.assertEquals(3, getColumnIndexesInGroup(0).size());
        Assert.assertTrue(getColumnIndexesInGroup(0).contains(0));
        Assert.assertTrue(getColumnIndexesInGroup(0).contains(1));
        Assert.assertTrue(getColumnIndexesInGroup(0).contains(2));

        // Assert the columns haven't moved
        Assert.assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        Assert.assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
        Assert.assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
    }

    @Test
    public void shouldUngroupFirstSelectedColumn() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0,
                false, true));

        final String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        // Test ungrouping first column
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, false));
        this.handler.handleUngroupCommand();

        Assert.assertEquals(2, getColumnIndexesInGroup(2).size());
        Assert.assertEquals(1, getColumnIndexesInGroup(2).get(0).intValue());
        Assert.assertEquals(2, getColumnIndexesInGroup(2).get(1).intValue());

        Assert.assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        Assert.assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
        Assert.assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
    }

    @Test
    public void shouldUngroupFirstAndLastSelectedColumn() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0,
                false, true));

        final String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        // Test ungrouping first column
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0,
                false, true));
        this.handler.handleUngroupCommand();

        Assert.assertEquals(1, getColumnIndexesInGroup(1).size());
        Assert.assertEquals(1, getColumnIndexesInGroup(1).get(0).intValue());

        Assert.assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        Assert.assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
        Assert.assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
    }

    @Test
    public void shouldRemoveAllColumnsInGroup() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0,
                false, true));

        final String columnGroupName = "Test Group 3";
        this.handler.loadSelectedColumnsIndexesWithPositions();
        this.handler.handleGroupColumnsCommand(columnGroupName);

        // Test ungrouping first column
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0,
                false, true));
        this.handler.handleUngroupCommand();

        Assert.assertFalse(this.model.isPartOfAGroup(0));
        Assert.assertFalse(this.model.isPartOfAGroup(1));
        Assert.assertFalse(this.model.isPartOfAGroup(2));

        Assert.assertEquals(0, this.selectionLayer.getColumnPositionByIndex(0));
        Assert.assertEquals(1, this.selectionLayer.getColumnPositionByIndex(1));
        Assert.assertEquals(2, this.selectionLayer.getColumnPositionByIndex(2));
    }

    private List<Integer> getColumnIndexesInGroup(int columnIndex) {
        return this.model.getColumnGroupByIndex(columnIndex).getMembers();
    }

    private String getColumnGroupNameForIndex(int columnIndex) {
        return this.model.getColumnGroupByIndex(columnIndex).getName();
    }

}
