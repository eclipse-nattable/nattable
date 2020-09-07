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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Before;
import org.junit.Test;

public class RowReorderLayerFixtureTest {

    private ILayer rowReorderLayerFixture;

    @Before
    public void setup() {
        this.rowReorderLayerFixture = new RowReorderLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        assertEquals(0, this.rowReorderLayerFixture.getColumnIndexByPosition(0));
        assertEquals(1, this.rowReorderLayerFixture.getColumnIndexByPosition(1));
        assertEquals(2, this.rowReorderLayerFixture.getColumnIndexByPosition(2));
        assertEquals(3, this.rowReorderLayerFixture.getColumnIndexByPosition(3));
        assertEquals(4, this.rowReorderLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        assertEquals(150, this.rowReorderLayerFixture.getColumnWidthByPosition(0));
        assertEquals(100, this.rowReorderLayerFixture.getColumnWidthByPosition(1));
        assertEquals(35, this.rowReorderLayerFixture.getColumnWidthByPosition(2));
        assertEquals(100, this.rowReorderLayerFixture.getColumnWidthByPosition(3));
        assertEquals(80, this.rowReorderLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        assertEquals(4, this.rowReorderLayerFixture.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayerFixture.getRowIndexByPosition(1));
        assertEquals(0, this.rowReorderLayerFixture.getRowIndexByPosition(2));
        assertEquals(2, this.rowReorderLayerFixture.getRowIndexByPosition(3));
        assertEquals(3, this.rowReorderLayerFixture.getRowIndexByPosition(4));
        assertEquals(5, this.rowReorderLayerFixture.getRowIndexByPosition(5));
        assertEquals(6, this.rowReorderLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        assertEquals(50, this.rowReorderLayerFixture.getRowHeightByPosition(0));
        assertEquals(70, this.rowReorderLayerFixture.getRowHeightByPosition(1));
        assertEquals(40, this.rowReorderLayerFixture.getRowHeightByPosition(2));
        assertEquals(25, this.rowReorderLayerFixture.getRowHeightByPosition(3));
        assertEquals(40, this.rowReorderLayerFixture.getRowHeightByPosition(4));
        assertEquals(40, this.rowReorderLayerFixture.getRowHeightByPosition(5));
        assertEquals(100, this.rowReorderLayerFixture.getRowHeightByPosition(6));
    }

}
