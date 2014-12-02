/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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

public class RowHideShowLayerFixtureTest {

    private ILayer rowHideShowLayerFixture;

    @Before
    public void setup() {
        this.rowHideShowLayerFixture = new RowHideShowLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        Assert.assertEquals(0,
                this.rowHideShowLayerFixture.getColumnIndexByPosition(0));
        Assert.assertEquals(1,
                this.rowHideShowLayerFixture.getColumnIndexByPosition(1));
        Assert.assertEquals(2,
                this.rowHideShowLayerFixture.getColumnIndexByPosition(2));
        Assert.assertEquals(3,
                this.rowHideShowLayerFixture.getColumnIndexByPosition(3));
        Assert.assertEquals(4,
                this.rowHideShowLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        Assert.assertEquals(150,
                this.rowHideShowLayerFixture.getColumnWidthByPosition(0));
        Assert.assertEquals(100,
                this.rowHideShowLayerFixture.getColumnWidthByPosition(1));
        Assert.assertEquals(35,
                this.rowHideShowLayerFixture.getColumnWidthByPosition(2));
        Assert.assertEquals(100,
                this.rowHideShowLayerFixture.getColumnWidthByPosition(3));
        Assert.assertEquals(80,
                this.rowHideShowLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        Assert.assertEquals(4, this.rowHideShowLayerFixture.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.rowHideShowLayerFixture.getRowIndexByPosition(1));
        Assert.assertEquals(2, this.rowHideShowLayerFixture.getRowIndexByPosition(2));
        Assert.assertEquals(5, this.rowHideShowLayerFixture.getRowIndexByPosition(3));
        Assert.assertEquals(6, this.rowHideShowLayerFixture.getRowIndexByPosition(4));
        Assert.assertEquals(-1,
                this.rowHideShowLayerFixture.getRowIndexByPosition(5));
    }

    @Test
    public void testRowHeights() {
        Assert.assertEquals(50,
                this.rowHideShowLayerFixture.getRowHeightByPosition(0));
        Assert.assertEquals(70,
                this.rowHideShowLayerFixture.getRowHeightByPosition(1));
        Assert.assertEquals(25,
                this.rowHideShowLayerFixture.getRowHeightByPosition(2));
        Assert.assertEquals(40,
                this.rowHideShowLayerFixture.getRowHeightByPosition(3));
        Assert.assertEquals(100,
                this.rowHideShowLayerFixture.getRowHeightByPosition(4));
    }

}
