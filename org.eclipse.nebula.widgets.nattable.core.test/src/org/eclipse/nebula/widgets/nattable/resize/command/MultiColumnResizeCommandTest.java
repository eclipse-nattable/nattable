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

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class MultiColumnResizeCommandTest {

    @Test
    public void getColumnWidth() {
        MultiColumnResizeCommand resizeCommand =
                new MultiColumnResizeCommand(
                        new DataLayerFixture(),
                        new int[] { 5, 9 },
                        new int[] { 12, 20 });

        assertEquals(12, resizeCommand.getColumnWidth(5));
        assertEquals(20, resizeCommand.getColumnWidth(9));

        assertEquals(-1, resizeCommand.getColumnWidth(10)); // Error case
    }

    @Test
    public void getCommonColumnWidth() {
        MultiColumnResizeCommand resizeCommand =
                new MultiColumnResizeCommand(new DataLayerFixture(), new int[] { 1, 2 }, 100);

        assertEquals(100, resizeCommand.getCommonColumnWidth());
        assertEquals(100, resizeCommand.getColumnWidth(1));
    }

    @Test
    public void getColumnWidthWhenTheColumnPositionsHaveBeenConverted() {
        DataLayerFixture dataLayer = new DataLayerFixture();
        // Indexes re-ordered: 4 1 0 2 3
        ColumnReorderLayer reorderLayerFixture = new ColumnReorderLayerFixture(dataLayer);

        MultiColumnResizeCommand resizeCommand =
                new MultiColumnResizeCommand(reorderLayerFixture, new int[] { 1, 2 }, new int[] { 100, 150 });
        reorderLayerFixture.doCommand(resizeCommand);

        // As the Commands goes down the stack - positions might get converted
        // to entirely different values.
        assertEquals(-1, resizeCommand.getCommonColumnWidth());
        assertEquals(-1, resizeCommand.getColumnWidth(5));
        assertEquals(-1, resizeCommand.getColumnWidth(12));

        assertEquals(100, resizeCommand.getColumnWidth(1));
        assertEquals(150, resizeCommand.getColumnWidth(0));
    }

    @Test
    public void testMultiResizeWithoutDownscale() {
        GridLayer gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // scaling enabled, therefore default width of 100 pixels is up scaled
        // to 125
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(125, gridLayer.getColumnWidthByPosition(3));
        assertEquals(125, gridLayer.getColumnWidthByPosition(4));
        assertEquals(125, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));

        MultiColumnResizeCommand resizeCommand =
                new MultiColumnResizeCommand(gridLayer, new int[] { 3, 4, 5 }, 150);
        gridLayer.doCommand(resizeCommand);

        // command executed with down scaling disabled, therefore set width 150
        // is up scaled to 188
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(188, gridLayer.getColumnWidthByPosition(3));
        assertEquals(188, gridLayer.getColumnWidthByPosition(4));
        assertEquals(188, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));
    }

    @Test
    public void testMultiResizeWithDownscale() {
        GridLayer gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // scaling enabled, therefore default width of 100 pixels is up scaled
        // to 125
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(125, gridLayer.getColumnWidthByPosition(3));
        assertEquals(125, gridLayer.getColumnWidthByPosition(4));
        assertEquals(125, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));

        MultiColumnResizeCommand resizeCommand =
                new MultiColumnResizeCommand(gridLayer, new int[] { 3, 4, 5 }, 150, true);
        gridLayer.doCommand(resizeCommand);

        // command executed with down scaling enabled, therefore set width 150
        // is first down scaled on setting the value and then up scaled to 150
        // again on accessing the width
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(150, gridLayer.getColumnWidthByPosition(3));
        assertEquals(150, gridLayer.getColumnWidthByPosition(4));
        assertEquals(150, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));
    }

    @Test
    public void testMultiResizeWithoutDownscaleOnSelection() {
        DummyGridLayerStack gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // scaling enabled, therefore default width of 100 pixels is up scaled
        // to 125
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(125, gridLayer.getColumnWidthByPosition(3));
        assertEquals(125, gridLayer.getColumnWidthByPosition(4));
        assertEquals(125, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));

        // select columns
        gridLayer.doCommand(new SelectColumnCommand(gridLayer, 3, 1, false, false));
        gridLayer.doCommand(new SelectColumnCommand(gridLayer, 4, 1, false, true));
        gridLayer.doCommand(new SelectColumnCommand(gridLayer, 5, 1, false, true));

        // resize one of the selected columns
        ColumnResizeCommand columnResizeCommand = new ColumnResizeCommand(gridLayer, 3, 150);
        gridLayer.doCommand(columnResizeCommand);

        // command executed with down scaling disabled, therefore set width 150
        // is up scaled to 188
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(188, gridLayer.getColumnWidthByPosition(3));
        assertEquals(188, gridLayer.getColumnWidthByPosition(4));
        assertEquals(188, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));
    }

    @Test
    public void testMultiResizeWithDownscaleOnSelection() {
        GridLayer gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // select columns
        gridLayer.doCommand(new SelectColumnCommand(gridLayer, 3, 1, false, false));
        gridLayer.doCommand(new SelectColumnCommand(gridLayer, 4, 1, false, true));
        gridLayer.doCommand(new SelectColumnCommand(gridLayer, 5, 1, false, true));

        // scaling enabled, therefore default width of 100 pixels is up scaled
        // to 125
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(125, gridLayer.getColumnWidthByPosition(3));
        assertEquals(125, gridLayer.getColumnWidthByPosition(4));
        assertEquals(125, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));

        // resize one of the selected columns
        ColumnResizeCommand columnResizeCommand = new ColumnResizeCommand(gridLayer, 3, 150, true);
        gridLayer.doCommand(columnResizeCommand);

        // command executed with down scaling enabled, therefore set width 150
        // is first down scaled on setting the value and then up scaled to 150
        // again on accessing the width
        assertEquals(125, gridLayer.getColumnWidthByPosition(2));
        assertEquals(150, gridLayer.getColumnWidthByPosition(3));
        assertEquals(150, gridLayer.getColumnWidthByPosition(4));
        assertEquals(150, gridLayer.getColumnWidthByPosition(5));
        assertEquals(125, gridLayer.getColumnWidthByPosition(6));
    }

    private void setClientAreaProvider(ILayer layer) {
        layer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }
        });
        layer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

}
