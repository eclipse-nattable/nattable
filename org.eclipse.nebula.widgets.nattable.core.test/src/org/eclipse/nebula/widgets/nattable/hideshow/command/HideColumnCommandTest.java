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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HideColumnCommandTest {

    private ColumnHideShowLayer columnHideShowLayer;

    @BeforeEach
    public void setup() {
        this.columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture());
    }

    @Test
    public void shouldHideColumn() {
        ILayerCommand hideColumnCommand =
                new MultiColumnHideCommand(this.columnHideShowLayer, 2);

        assertEquals(5, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.doCommand(hideColumnCommand);

        assertAll("column hidden",
                () -> assertEquals(4, this.columnHideShowLayer.getColumnCount()),
                () -> assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0)),
                () -> assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1)),
                () -> assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2)),
                () -> assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3)));
    }

    @Test
    public void shouldHideColumnByIndex() {
        ILayerCommand hideColumnCommand = new HideColumnByIndexCommand(2);

        assertEquals(5, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.doCommand(hideColumnCommand);

        assertAll("column hidden",
                () -> assertEquals(4, this.columnHideShowLayer.getColumnCount()),
                () -> assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0)),
                () -> assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1)),
                () -> assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2)),
                () -> assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3)));
    }

}
