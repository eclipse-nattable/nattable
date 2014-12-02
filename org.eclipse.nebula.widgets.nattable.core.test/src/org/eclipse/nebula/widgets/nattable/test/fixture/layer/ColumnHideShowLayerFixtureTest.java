/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Assert;
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
        Assert.assertEquals(4,
                this.columnHideShowLayerFixture.getColumnIndexByPosition(0));
        Assert.assertEquals(1,
                this.columnHideShowLayerFixture.getColumnIndexByPosition(1));
        Assert.assertEquals(2,
                this.columnHideShowLayerFixture.getColumnIndexByPosition(2));
        Assert.assertEquals(-1,
                this.columnHideShowLayerFixture.getColumnIndexByPosition(3));
    }

    @Test
    public void testColumnWidths() {
        Assert.assertEquals(80,
                this.columnHideShowLayerFixture.getColumnWidthByPosition(0));
        Assert.assertEquals(100,
                this.columnHideShowLayerFixture.getColumnWidthByPosition(1));
        Assert.assertEquals(35,
                this.columnHideShowLayerFixture.getColumnWidthByPosition(2));
    }

    @Test
    public void testRowIndexes() {
        Assert.assertEquals(0,
                this.columnHideShowLayerFixture.getRowIndexByPosition(0));
        Assert.assertEquals(1,
                this.columnHideShowLayerFixture.getRowIndexByPosition(1));
        Assert.assertEquals(2,
                this.columnHideShowLayerFixture.getRowIndexByPosition(2));
        Assert.assertEquals(3,
                this.columnHideShowLayerFixture.getRowIndexByPosition(3));
        Assert.assertEquals(4,
                this.columnHideShowLayerFixture.getRowIndexByPosition(4));
        Assert.assertEquals(5,
                this.columnHideShowLayerFixture.getRowIndexByPosition(5));
        Assert.assertEquals(6,
                this.columnHideShowLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        Assert.assertEquals(40,
                this.columnHideShowLayerFixture.getRowHeightByPosition(0));
        Assert.assertEquals(70,
                this.columnHideShowLayerFixture.getRowHeightByPosition(1));
        Assert.assertEquals(25,
                this.columnHideShowLayerFixture.getRowHeightByPosition(2));
        Assert.assertEquals(40,
                this.columnHideShowLayerFixture.getRowHeightByPosition(3));
        Assert.assertEquals(50,
                this.columnHideShowLayerFixture.getRowHeightByPosition(4));
        Assert.assertEquals(40,
                this.columnHideShowLayerFixture.getRowHeightByPosition(5));
        Assert.assertEquals(100,
                this.columnHideShowLayerFixture.getRowHeightByPosition(6));
    }

}
