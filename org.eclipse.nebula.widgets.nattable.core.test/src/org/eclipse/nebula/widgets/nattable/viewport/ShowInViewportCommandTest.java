/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
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

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowCellInViewportCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommand;
import org.junit.Test;

public class ShowInViewportCommandTest {

    NatTableFixture natTable = new NatTableFixture(new DummyGridLayerStack(10, 50), 220, 220, true);

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
}
