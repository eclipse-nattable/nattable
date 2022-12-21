/*******************************************************************************
 * Copyright (c) 2017, 2022 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class MultiRowResizeCommandTest {

    @Test
    public void testGetRowHeight() {
        MultiRowResizeCommand resizeCommand =
                new MultiRowResizeCommand(
                        new DataLayerFixture(),
                        new int[] { 5, 9 },
                        new int[] { 12, 20 });

        assertEquals(12, resizeCommand.getRowHeight(5));
        assertEquals(20, resizeCommand.getRowHeight(9));

        assertEquals(-1, resizeCommand.getRowHeight(10)); // Error case
        assertEquals(-1, resizeCommand.getCommonRowHeight());
    }

    @Test
    public void testGetCommonColumnWidth() {
        MultiRowResizeCommand resizeCommand =
                new MultiRowResizeCommand(new DataLayerFixture(), new int[] { 1, 2 }, 100);

        assertEquals(100, resizeCommand.getCommonRowHeight());
        assertEquals(100, resizeCommand.getRowHeight(1));
    }

    @Test
    public void testMultiResizeWithoutDownscale() {
        GridLayer gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(25, gridLayer.getRowHeightByPosition(3));
        assertEquals(25, gridLayer.getRowHeightByPosition(4));
        assertEquals(25, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));

        MultiRowResizeCommand resizeCommand =
                new MultiRowResizeCommand(gridLayer, new int[] { 3, 4, 5 }, 50);
        gridLayer.doCommand(resizeCommand);

        // command executed with down scaling disabled, therefore set height 50
        // is up scaled to 63
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(63, gridLayer.getRowHeightByPosition(3));
        assertEquals(63, gridLayer.getRowHeightByPosition(4));
        assertEquals(63, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));
    }

    @Test
    public void testMultiResizeWithDownscale() {
        GridLayer gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(25, gridLayer.getRowHeightByPosition(3));
        assertEquals(25, gridLayer.getRowHeightByPosition(4));
        assertEquals(25, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));

        MultiRowResizeCommand resizeCommand =
                new MultiRowResizeCommand(gridLayer, new int[] { 3, 4, 5 }, 50, true);
        gridLayer.doCommand(resizeCommand);

        // command executed with down scaling enabled, therefore set height 50
        // is first down scaled on setting the value and then up scaled to 50
        // again on accessing the height
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(50, gridLayer.getRowHeightByPosition(3));
        assertEquals(50, gridLayer.getRowHeightByPosition(4));
        assertEquals(50, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));
    }

    @Test
    public void testMultiResizeWithoutDownscaleOnSelection() {
        DummyGridLayerStack gridLayer = new DummyGridLayerStack();

        gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        setClientAreaProvider(gridLayer);

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(25, gridLayer.getRowHeightByPosition(3));
        assertEquals(25, gridLayer.getRowHeightByPosition(4));
        assertEquals(25, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));

        // select rows
        gridLayer.doCommand(new SelectRowsCommand(gridLayer, 1, new int[] { 3, 4, 5 }, false, true, -1));

        // resize one of the selected columns
        RowResizeCommand columnResizeCommand = new RowResizeCommand(gridLayer, 3, 50);
        gridLayer.doCommand(columnResizeCommand);

        // command executed with down scaling disabled, therefore set height 50
        // is up scaled to 63
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(63, gridLayer.getRowHeightByPosition(3));
        assertEquals(63, gridLayer.getRowHeightByPosition(4));
        assertEquals(63, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));
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

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(25, gridLayer.getRowHeightByPosition(3));
        assertEquals(25, gridLayer.getRowHeightByPosition(4));
        assertEquals(25, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));

        // select rows
        gridLayer.doCommand(new SelectRowsCommand(gridLayer, 1, new int[] { 3, 4, 5 }, false, true, -1));

        // resize one of the selected columns
        RowResizeCommand columnResizeCommand = new RowResizeCommand(gridLayer, 3, 50, true);
        gridLayer.doCommand(columnResizeCommand);

        // command executed with down scaling enabled, therefore set height 50
        // is first down scaled on setting the value and then up scaled to 50
        // again on accessing the height
        assertEquals(25, gridLayer.getRowHeightByPosition(2));
        assertEquals(50, gridLayer.getRowHeightByPosition(3));
        assertEquals(50, gridLayer.getRowHeightByPosition(4));
        assertEquals(50, gridLayer.getRowHeightByPosition(5));
        assertEquals(25, gridLayer.getRowHeightByPosition(6));
    }

    private void setClientAreaProvider(ILayer layer) {
        layer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 500);
            }
        });
        layer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

}
