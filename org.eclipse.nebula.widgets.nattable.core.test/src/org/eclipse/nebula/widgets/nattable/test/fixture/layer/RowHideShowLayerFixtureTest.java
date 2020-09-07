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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Before;
import org.junit.Test;

public class RowHideShowLayerFixtureTest {

    private ILayer rowHideShowLayerFixture;

    @Before
    public void setup() {
        this.rowHideShowLayerFixture = new RowHideShowLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        assertEquals(0, this.rowHideShowLayerFixture.getColumnIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayerFixture.getColumnIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayerFixture.getColumnIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayerFixture.getColumnIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        assertEquals(150, this.rowHideShowLayerFixture.getColumnWidthByPosition(0));
        assertEquals(100, this.rowHideShowLayerFixture.getColumnWidthByPosition(1));
        assertEquals(35, this.rowHideShowLayerFixture.getColumnWidthByPosition(2));
        assertEquals(100, this.rowHideShowLayerFixture.getColumnWidthByPosition(3));
        assertEquals(80, this.rowHideShowLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        assertEquals(4, this.rowHideShowLayerFixture.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayerFixture.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayerFixture.getRowIndexByPosition(2));
        assertEquals(5, this.rowHideShowLayerFixture.getRowIndexByPosition(3));
        assertEquals(6, this.rowHideShowLayerFixture.getRowIndexByPosition(4));
        assertEquals(-1, this.rowHideShowLayerFixture.getRowIndexByPosition(5));
    }

    @Test
    public void testRowHeights() {
        assertEquals(50, this.rowHideShowLayerFixture.getRowHeightByPosition(0));
        assertEquals(70, this.rowHideShowLayerFixture.getRowHeightByPosition(1));
        assertEquals(25, this.rowHideShowLayerFixture.getRowHeightByPosition(2));
        assertEquals(40, this.rowHideShowLayerFixture.getRowHeightByPosition(3));
        assertEquals(100, this.rowHideShowLayerFixture.getRowHeightByPosition(4));
    }

}
