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
package org.eclipse.nebula.widgets.nattable.resize;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.NoScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.jupiter.api.Test;

public class ResizeColumnTest {

    /**
     * Test for bug NTBL-431
     */
    @Test
    public void reiszeColumnInATableWithNoRows() {
        NatTableFixture natTable = new NatTableFixture(new DummyGridLayerStack(5, 0), true);
        natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        assertEquals(100, natTable.getColumnWidthByPosition(2));
        natTable.doCommand(new ColumnResizeCommand(natTable, 2, 150));

        assertEquals(150, natTable.getColumnWidthByPosition(2));
    }

}
