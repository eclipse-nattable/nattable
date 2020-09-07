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

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnReorderLayerFixtureTest {

    private ILayer columnReorderLayerFixture;

    @Before
    public void setup() {
        this.columnReorderLayerFixture = new ColumnReorderLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        Assert.assertEquals(4,
                this.columnReorderLayerFixture.getColumnIndexByPosition(0));
        Assert.assertEquals(1,
                this.columnReorderLayerFixture.getColumnIndexByPosition(1));
        Assert.assertEquals(0,
                this.columnReorderLayerFixture.getColumnIndexByPosition(2));
        Assert.assertEquals(2,
                this.columnReorderLayerFixture.getColumnIndexByPosition(3));
        Assert.assertEquals(3,
                this.columnReorderLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        Assert.assertEquals(80,
                this.columnReorderLayerFixture.getColumnWidthByPosition(0));
        Assert.assertEquals(100,
                this.columnReorderLayerFixture.getColumnWidthByPosition(1));
        Assert.assertEquals(150,
                this.columnReorderLayerFixture.getColumnWidthByPosition(2));
        Assert.assertEquals(35,
                this.columnReorderLayerFixture.getColumnWidthByPosition(3));
        Assert.assertEquals(100,
                this.columnReorderLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        Assert.assertEquals(0,
                this.columnReorderLayerFixture.getRowIndexByPosition(0));
        Assert.assertEquals(1,
                this.columnReorderLayerFixture.getRowIndexByPosition(1));
        Assert.assertEquals(2,
                this.columnReorderLayerFixture.getRowIndexByPosition(2));
        Assert.assertEquals(3,
                this.columnReorderLayerFixture.getRowIndexByPosition(3));
        Assert.assertEquals(4,
                this.columnReorderLayerFixture.getRowIndexByPosition(4));
        Assert.assertEquals(5,
                this.columnReorderLayerFixture.getRowIndexByPosition(5));
        Assert.assertEquals(6,
                this.columnReorderLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        Assert.assertEquals(40,
                this.columnReorderLayerFixture.getRowHeightByPosition(0));
        Assert.assertEquals(70,
                this.columnReorderLayerFixture.getRowHeightByPosition(1));
        Assert.assertEquals(25,
                this.columnReorderLayerFixture.getRowHeightByPosition(2));
        Assert.assertEquals(40,
                this.columnReorderLayerFixture.getRowHeightByPosition(3));
        Assert.assertEquals(50,
                this.columnReorderLayerFixture.getRowHeightByPosition(4));
        Assert.assertEquals(40,
                this.columnReorderLayerFixture.getRowHeightByPosition(5));
        Assert.assertEquals(100,
                this.columnReorderLayerFixture.getRowHeightByPosition(6));
    }

}
