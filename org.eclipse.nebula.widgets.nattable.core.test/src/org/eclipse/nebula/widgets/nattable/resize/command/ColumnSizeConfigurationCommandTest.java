/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.resize.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnSizeConfigurationCommandTest {

    private DataLayer dataLayer;

    @BeforeEach
    public void setup() {
        this.dataLayer = new DataLayer(new DummyBodyDataProvider(4, 4));
        this.dataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
    }

    @Test
    public void testSetSize() {
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));

        int newWidth = 150;
        ColumnSizeConfigurationCommand command =
                new ColumnSizeConfigurationCommand("COLUMN_3", newWidth, false);

        this.dataLayer.doCommand(command);

        assertEquals(150, this.dataLayer.getColumnWidthByPosition(3));
    }

    @Test
    public void testDefaultSize() {
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(100, this.dataLayer.getDefaultColumnWidth());
        assertEquals(400, this.dataLayer.getWidth());

        int newWidth = 150;
        ColumnSizeConfigurationCommand command =
                new ColumnSizeConfigurationCommand(null, newWidth, false);

        this.dataLayer.doCommand(command);

        assertEquals(150, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.dataLayer.getDefaultColumnWidth());
        assertEquals(600, this.dataLayer.getWidth());
    }

    @Test
    public void testPercentageSizing() {
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));

        ColumnSizeConfigurationCommand command =
                new ColumnSizeConfigurationCommand(null, null, true);

        this.dataLayer.doCommand(command);

        // recalculate percentages
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 500, 500));
        this.dataLayer.doCommand(resizeCommand);

        assertEquals(125, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(125, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(125, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(125, this.dataLayer.getColumnWidthByPosition(3));
    }

    @Test
    public void testSetPercentage() {
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));

        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_0", 100, false));
        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_1", 100, false));
        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_2", null, true));
        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_3", null, true));

        // recalculate percentages
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 500, 500));
        this.dataLayer.doCommand(resizeCommand);

        assertEquals(100, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(150, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.dataLayer.getColumnWidthByPosition(3));
    }

    @Test
    public void testSetPercentageValue() {
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));

        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_0", 20, true));
        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_1", 20, true));
        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_2", 30, true));
        this.dataLayer.doCommand(new ColumnSizeConfigurationCommand("COLUMN_3", 30, true));

        // recalculate percentages
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 1000, 1000));
        this.dataLayer.doCommand(resizeCommand);

        assertEquals(200, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(200, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(300, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(300, this.dataLayer.getColumnWidthByPosition(3));
    }

    @Test
    public void testSetSizeInGrid() {
        GridLayer grid = new DummyGridLayerStack();
        assertEquals(40, grid.getRowHeaderLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(9));

        grid.doCommand(new ColumnSizeConfigurationCommand(null, 150, false));

        assertEquals(150, grid.getRowHeaderLayer().getColumnWidthByPosition(0));
        assertEquals(150, grid.getBodyLayer().getColumnWidthByPosition(0));
        assertEquals(150, grid.getBodyLayer().getColumnWidthByPosition(9));
    }

    @Test
    public void testSetSizeInBodyRegion() {
        GridLayer grid = new DummyGridLayerStack();
        assertEquals(40, grid.getRowHeaderLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(9));

        grid.doCommand(new ColumnSizeConfigurationCommand(GridRegion.BODY, 150, false));

        assertEquals(40, grid.getRowHeaderLayer().getColumnWidthByPosition(0));
        assertEquals(150, grid.getBodyLayer().getColumnWidthByPosition(0));
        assertEquals(150, grid.getBodyLayer().getColumnWidthByPosition(9));
    }

    @Test
    public void testSetSizeInColumnHeaderRegion() {
        GridLayer grid = new DummyGridLayerStack();
        assertEquals(40, grid.getRowHeaderLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(9));

        grid.doCommand(new ColumnSizeConfigurationCommand(GridRegion.ROW_HEADER, 150, false));

        assertEquals(150, grid.getRowHeaderLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(0));
        assertEquals(100, grid.getBodyLayer().getColumnWidthByPosition(9));
    }
}
