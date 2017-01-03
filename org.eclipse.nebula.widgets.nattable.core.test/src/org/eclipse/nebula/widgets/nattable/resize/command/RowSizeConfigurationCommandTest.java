/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class RowSizeConfigurationCommandTest {

    private DataLayer dataLayer;

    @Before
    public void setup() {
        this.dataLayer = new DataLayer(new DummyBodyDataProvider(4, 4));
        this.dataLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                configLabels.addLabel("ROW_" + rowPosition);
            }
        });
    }

    @Test
    public void testSetSize() {
        assertEquals(20, this.dataLayer.getRowHeightByPosition(3));

        int newHeight = 50;
        RowSizeConfigurationCommand command =
                new RowSizeConfigurationCommand("ROW_3", newHeight, false);

        this.dataLayer.doCommand(command);

        assertEquals(50, this.dataLayer.getRowHeightByPosition(3));
    }

    @Test
    public void testDefaultSize() {
        assertEquals(20, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(20, this.dataLayer.getDefaultRowHeight());
        assertEquals(80, this.dataLayer.getHeight());

        int newHeight = 50;
        RowSizeConfigurationCommand command =
                new RowSizeConfigurationCommand(null, newHeight, false);

        this.dataLayer.doCommand(command);

        assertEquals(50, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayer.getDefaultRowHeight());
        assertEquals(200, this.dataLayer.getHeight());
    }

    @Test
    public void testPercentageSizing() {
        assertEquals(20, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(3));

        RowSizeConfigurationCommand command =
                new RowSizeConfigurationCommand(null, null, true);

        this.dataLayer.doCommand(command);

        // recalculate percentages
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 500, 500));
        this.dataLayer.doCommand(resizeCommand);

        assertEquals(125, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(125, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(125, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(125, this.dataLayer.getRowHeightByPosition(3));
    }

    @Test
    public void testSetPercentage() {
        assertEquals(20, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(3));

        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_0", 100, false));
        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_1", 100, false));
        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_2", null, true));
        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_3", null, true));

        // recalculate percentages
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 500, 500));
        this.dataLayer.doCommand(resizeCommand);

        assertEquals(100, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(100, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(150, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(150, this.dataLayer.getRowHeightByPosition(3));
    }

    @Test
    public void testSetPercentageValue() {
        assertEquals(20, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(20, this.dataLayer.getRowHeightByPosition(3));

        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_0", 20, true));
        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_1", 20, true));
        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_2", 30, true));
        this.dataLayer.doCommand(new RowSizeConfigurationCommand("ROW_3", 30, true));

        // recalculate percentages
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 1000, 1000));
        this.dataLayer.doCommand(resizeCommand);

        assertEquals(200, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(200, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(300, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(300, this.dataLayer.getRowHeightByPosition(3));
    }

    @Test
    public void testSetSizeInGrid() {
        GridLayer grid = new DummyGridLayerStack();
        assertEquals(20, grid.getColumnHeaderLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(9));

        grid.doCommand(new RowSizeConfigurationCommand(null, 50, false));

        assertEquals(50, grid.getColumnHeaderLayer().getRowHeightByPosition(0));
        assertEquals(50, grid.getBodyLayer().getRowHeightByPosition(0));
        assertEquals(50, grid.getBodyLayer().getRowHeightByPosition(9));
    }

    @Test
    public void testSetSizeInBodyRegion() {
        GridLayer grid = new DummyGridLayerStack();
        assertEquals(20, grid.getColumnHeaderLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(9));

        grid.doCommand(new RowSizeConfigurationCommand(GridRegion.BODY, 50, false));

        assertEquals(20, grid.getColumnHeaderLayer().getRowHeightByPosition(0));
        assertEquals(50, grid.getBodyLayer().getRowHeightByPosition(0));
        assertEquals(50, grid.getBodyLayer().getRowHeightByPosition(9));
    }

    @Test
    public void testSetSizeInColumnHeaderRegion() {
        GridLayer grid = new DummyGridLayerStack();
        assertEquals(20, grid.getColumnHeaderLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(9));

        grid.doCommand(new RowSizeConfigurationCommand(GridRegion.COLUMN_HEADER, 50, false));

        assertEquals(50, grid.getColumnHeaderLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(0));
        assertEquals(20, grid.getBodyLayer().getRowHeightByPosition(9));
    }
}
