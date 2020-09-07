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

public class ColumnHideShowLayerFixtureTest {

    private ILayer columnHideShowLayerFixture;

    @Before
    public void setup() {
        this.columnHideShowLayerFixture = new ColumnHideShowLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        assertEquals(4, this.columnHideShowLayerFixture.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayerFixture.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayerFixture.getColumnIndexByPosition(2));
        assertEquals(-1, this.columnHideShowLayerFixture.getColumnIndexByPosition(3));
    }

    @Test
    public void testColumnWidths() {
        assertEquals(80, this.columnHideShowLayerFixture.getColumnWidthByPosition(0));
        assertEquals(100, this.columnHideShowLayerFixture.getColumnWidthByPosition(1));
        assertEquals(35, this.columnHideShowLayerFixture.getColumnWidthByPosition(2));
    }

    @Test
    public void testRowIndexes() {
        assertEquals(0, this.columnHideShowLayerFixture.getRowIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayerFixture.getRowIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayerFixture.getRowIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayerFixture.getRowIndexByPosition(3));
        assertEquals(4, this.columnHideShowLayerFixture.getRowIndexByPosition(4));
        assertEquals(5, this.columnHideShowLayerFixture.getRowIndexByPosition(5));
        assertEquals(6, this.columnHideShowLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        assertEquals(40, this.columnHideShowLayerFixture.getRowHeightByPosition(0));
        assertEquals(70, this.columnHideShowLayerFixture.getRowHeightByPosition(1));
        assertEquals(25, this.columnHideShowLayerFixture.getRowHeightByPosition(2));
        assertEquals(40, this.columnHideShowLayerFixture.getRowHeightByPosition(3));
        assertEquals(50, this.columnHideShowLayerFixture.getRowHeightByPosition(4));
        assertEquals(40, this.columnHideShowLayerFixture.getRowHeightByPosition(5));
        assertEquals(100, this.columnHideShowLayerFixture.getRowHeightByPosition(6));
    }

}
