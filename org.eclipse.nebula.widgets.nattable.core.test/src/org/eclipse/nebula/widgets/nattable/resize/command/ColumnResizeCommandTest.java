/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
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

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.AbstractDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.junit.Before;
import org.junit.Test;

public class ColumnResizeCommandTest {

    private DataLayer dataLayer;

    @Before
    public void setup() {
        this.dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
    }

    @Test
    public void testHandleColumnResizeCommand() {
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));

        ColumnResizeCommand columnResizeCommand = new ColumnResizeCommand(this.dataLayer, 3, 150);
        this.dataLayer.doCommand(columnResizeCommand);

        assertEquals(150, this.dataLayer.getColumnWidthByPosition(3));
    }

    @Test
    public void shouldResizeAllSelectedColumns() {
        int columnPositions[] = new int[] { 3, 2, 4 };
        int newWidth = 250;
        MultiColumnResizeCommand columnResizeCommand = new MultiColumnResizeCommand(this.dataLayer, columnPositions, newWidth);

        this.dataLayer.doCommand(columnResizeCommand);

        for (int columnPosition : columnPositions) {
            assertEquals(newWidth, this.dataLayer.getColumnWidthByPosition(columnPosition));
        }
    }

    @Test
    public void testResizeWithoutDownscale() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 120;
            }

        };
        this.dataLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // scaling enabled, therefore default width of 100 pixels is up scaled
        // to 125
        assertEquals(125, this.dataLayer.getColumnWidthByPosition(3));

        ColumnResizeCommand columnResizeCommand = new ColumnResizeCommand(this.dataLayer, 3, 150);
        this.dataLayer.doCommand(columnResizeCommand);

        // command executed with down scaling disabled, therefore set width 150
        // is up scaled to 188
        assertEquals(188, this.dataLayer.getColumnWidthByPosition(3));
    }

    @Test
    public void testResizeWithDownscale() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 120;
            }

        };
        this.dataLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // scaling enabled, therefore default width of 100 pixels is up scaled
        // to 125
        assertEquals(125, this.dataLayer.getColumnWidthByPosition(3));

        ColumnResizeCommand columnResizeCommand = new ColumnResizeCommand(this.dataLayer, 3, 150, true);
        this.dataLayer.doCommand(columnResizeCommand);

        // command executed with down scaling enabled, therefore set width 150
        // is first down scaled on setting the value and then up scaled to 150
        // again on accessing the width
        assertEquals(150, this.dataLayer.getColumnWidthByPosition(3));
    }
}
