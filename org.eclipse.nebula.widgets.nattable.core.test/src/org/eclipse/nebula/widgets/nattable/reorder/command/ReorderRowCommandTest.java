/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReorderRowCommandTest {

    private RowReorderLayer rowReorderLayer;

    @BeforeEach
    public void setup() {
        this.rowReorderLayer = new RowReorderLayer(new DataLayerFixture());
    }

    @Test
    public void testReorderRowCommand() {
        int fromRowPosition = 4;
        int toRowPosition = 1;
        ILayerCommand reorderRowCommand = new RowReorderCommand(
                this.rowReorderLayer, fromRowPosition, toRowPosition);

        this.rowReorderLayer.doCommand(reorderRowCommand);

        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(4, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(4));
    }

}
