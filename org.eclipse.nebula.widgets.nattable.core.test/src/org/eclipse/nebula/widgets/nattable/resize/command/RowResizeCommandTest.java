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
package org.eclipse.nebula.widgets.nattable.resize.command;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.junit.Before;
import org.junit.Test;

public class RowResizeCommandTest {

    private DataLayer dataLayer;

    @Before
    public void setup() {
        this.dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
    }

    @Test
    public void testHandleRowResizeCommand() {
        assertEquals(20, this.dataLayer.getRowHeightByPosition(3));

        RowResizeCommand rowResizeCommand = new RowResizeCommand(this.dataLayer, 3, 50);
        this.dataLayer.doCommand(rowResizeCommand);

        assertEquals(50, this.dataLayer.getRowHeightByPosition(3));
    }

    @Test
    public void testResizeWithoutDownscale() {
        this.dataLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.dataLayer.getRowHeightByPosition(3));

        RowResizeCommand rowResizeCommand = new RowResizeCommand(this.dataLayer, 3, 50);
        this.dataLayer.doCommand(rowResizeCommand);

        // command executed with down scaling disabled, therefore set height 50
        // is up scaled to 63
        assertEquals(63, this.dataLayer.getRowHeightByPosition(3));
    }

    @Test
    public void testResizeWithDownscale() {
        this.dataLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.dataLayer.getRowHeightByPosition(3));

        RowResizeCommand rowResizeCommand = new RowResizeCommand(this.dataLayer, 3, 50, true);
        this.dataLayer.doCommand(rowResizeCommand);

        // command executed with down scaling enabled, therefore set height 50
        // is first down scaled on setting the value and then up scaled to 50
        // again on accessing the height
        assertEquals(50, this.dataLayer.getRowHeightByPosition(3));
    }
}
