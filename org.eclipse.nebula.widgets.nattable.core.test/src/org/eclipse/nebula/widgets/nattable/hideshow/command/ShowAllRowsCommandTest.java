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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.RowHideShowLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShowAllRowsCommandTest {

    private RowHideShowLayer rowHideShowLayer;

    @BeforeEach
    public void setup() {
        this.rowHideShowLayer = new RowHideShowLayerFixture();
    }

    @Test
    public void testHideColumnCommand() {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(7, this.rowHideShowLayer.getRowCount());

        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(6));
    }

}
