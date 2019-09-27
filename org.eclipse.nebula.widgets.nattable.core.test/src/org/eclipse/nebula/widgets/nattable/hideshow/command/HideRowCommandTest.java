/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HideRowCommandTest {

    private RowHideShowLayer rowHideShowLayer;

    @BeforeEach
    public void setup() {
        this.rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture());
    }

    @Test
    public void shouldHideRow() {
        ILayerCommand hideRowCommand = new MultiRowHideCommand(this.rowHideShowLayer, 2);

        assertEquals(7, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.doCommand(hideRowCommand);

        assertAll("row hidden",
                () -> assertEquals(6, this.rowHideShowLayer.getRowCount()),
                () -> assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0)),
                () -> assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1)),
                () -> assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2)),
                () -> assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3)),
                () -> assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4)),
                () -> assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(5)));
    }

    @Test
    public void shouldNotConvertOnInvalidPosition() {
        RowReorderLayer layer = new RowReorderLayer(this.rowHideShowLayer);
        ILayerCommand hideRowCommand = new MultiRowHideCommand(layer, 10);

        assertFalse(hideRowCommand.convertToTargetLayer(this.rowHideShowLayer));
    }
}
