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

public class DataLayerFixtureTest {

    private ILayer dataLayerFixture;

    @Before
    public void setup() {
        this.dataLayerFixture = new DataLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        assertEquals(0, this.dataLayerFixture.getColumnIndexByPosition(0));
        assertEquals(1, this.dataLayerFixture.getColumnIndexByPosition(1));
        assertEquals(2, this.dataLayerFixture.getColumnIndexByPosition(2));
        assertEquals(3, this.dataLayerFixture.getColumnIndexByPosition(3));
        assertEquals(4, this.dataLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        assertEquals(150, this.dataLayerFixture.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayerFixture.getColumnWidthByPosition(1));
        assertEquals(35, this.dataLayerFixture.getColumnWidthByPosition(2));
        assertEquals(100, this.dataLayerFixture.getColumnWidthByPosition(3));
        assertEquals(80, this.dataLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        assertEquals(0, this.dataLayerFixture.getRowIndexByPosition(0));
        assertEquals(1, this.dataLayerFixture.getRowIndexByPosition(1));
        assertEquals(2, this.dataLayerFixture.getRowIndexByPosition(2));
        assertEquals(3, this.dataLayerFixture.getRowIndexByPosition(3));
        assertEquals(4, this.dataLayerFixture.getRowIndexByPosition(4));
        assertEquals(5, this.dataLayerFixture.getRowIndexByPosition(5));
        assertEquals(6, this.dataLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        assertEquals(40, this.dataLayerFixture.getRowHeightByPosition(0));
        assertEquals(70, this.dataLayerFixture.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayerFixture.getRowHeightByPosition(2));
        assertEquals(40, this.dataLayerFixture.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayerFixture.getRowHeightByPosition(4));
        assertEquals(40, this.dataLayerFixture.getRowHeightByPosition(5));
        assertEquals(100, this.dataLayerFixture.getRowHeightByPosition(6));
    }

}
