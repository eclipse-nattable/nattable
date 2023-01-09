/*******************************************************************************
 * Copyright (c) 2018, 2023 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowCellInViewportCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommand;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

public class ShowInViewportCommandTest {

    DummyGridLayerStack gridLayer = new DummyGridLayerStack(10, 50);
    NatTableFixture natTable = new NatTableFixture(this.gridLayer, 220, 220, true);

    @Test
    public void shouldShowColumnInViewport() {
        assertEquals(0, this.natTable.getColumnIndexByPosition(1));

        this.natTable.doCommand(new ShowColumnInViewportCommand(9));

        assertEquals(8, this.natTable.getColumnIndexByPosition(1));

        this.natTable.doCommand(new ShowColumnInViewportCommand(0));

        assertEquals(0, this.natTable.getColumnIndexByPosition(1));
    }

    @Test
    public void shouldShowRowInViewport() {
        assertEquals(0, this.natTable.getRowIndexByPosition(1));

        this.natTable.doCommand(new ShowRowInViewportCommand(49));

        assertEquals(40, this.natTable.getRowIndexByPosition(1));

        this.natTable.doCommand(new ShowRowInViewportCommand(0));

        assertEquals(0, this.natTable.getRowIndexByPosition(1));
    }

    @Test
    public void shouldShowCellInViewport() {
        assertEquals(0, this.natTable.getColumnIndexByPosition(1));
        assertEquals(0, this.natTable.getRowIndexByPosition(1));

        this.natTable.doCommand(new ShowCellInViewportCommand(9, 49));

        assertEquals(8, this.natTable.getColumnIndexByPosition(1));
        assertEquals(40, this.natTable.getRowIndexByPosition(1));

        this.natTable.doCommand(new ShowCellInViewportCommand(0, 0));

        assertEquals(0, this.natTable.getColumnIndexByPosition(1));
        assertEquals(0, this.natTable.getRowIndexByPosition(1));
    }

    @Test
    public void shouldShowColumnCompletelyInViewport() {
        this.natTable.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(96)));

        Rectangle bounds = this.natTable.getCellByPosition(1, 1).getBounds();
        assertEquals(40, bounds.x);

        // scroll 90 pixels to the right so the first column is only half
        // visible (40 pixels row header, 50 pixels half cell width)
        ViewportLayer viewportLayer = this.gridLayer.getBodyLayer().getViewportLayer();
        viewportLayer.invalidateHorizontalStructure();
        viewportLayer.setOriginX(90);

        bounds = this.natTable.getCellByPosition(1, 1).getBounds();
        assertEquals(-50, bounds.x);

        // bring the first column completely into the viewport again
        this.natTable.doCommand(new ShowColumnInViewportCommand(this.natTable, 1));

        bounds = this.natTable.getCellByPosition(1, 1).getBounds();
        assertEquals(40, bounds.x);
    }
}
