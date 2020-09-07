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

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHideShowLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ShowAllColumnsCommandTest {

    private ColumnHideShowLayer columnHideShowLayer;

    @Before
    public void setup() {
        this.columnHideShowLayer = new ColumnHideShowLayerFixture();
    }

    @Test
    public void testHideColumnCommand() {
        assertEquals(3, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(5, this.columnHideShowLayer.getColumnCount());

        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(4));
    }

}
