/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Assert;
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
        Assert.assertEquals(0,
                this.rowReorderLayerFixture.getColumnIndexByPosition(0));
        Assert.assertEquals(1,
                this.rowReorderLayerFixture.getColumnIndexByPosition(1));
        Assert.assertEquals(2,
                this.rowReorderLayerFixture.getColumnIndexByPosition(2));
        Assert.assertEquals(3,
                this.rowReorderLayerFixture.getColumnIndexByPosition(3));
        Assert.assertEquals(4,
                this.rowReorderLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        Assert.assertEquals(150,
                this.rowReorderLayerFixture.getColumnWidthByPosition(0));
        Assert.assertEquals(100,
                this.rowReorderLayerFixture.getColumnWidthByPosition(1));
        Assert.assertEquals(35,
                this.rowReorderLayerFixture.getColumnWidthByPosition(2));
        Assert.assertEquals(100,
                this.rowReorderLayerFixture.getColumnWidthByPosition(3));
        Assert.assertEquals(80,
                this.rowReorderLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        Assert.assertEquals(4, this.rowReorderLayerFixture.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.rowReorderLayerFixture.getRowIndexByPosition(1));
        Assert.assertEquals(0, this.rowReorderLayerFixture.getRowIndexByPosition(2));
        Assert.assertEquals(2, this.rowReorderLayerFixture.getRowIndexByPosition(3));
        Assert.assertEquals(3, this.rowReorderLayerFixture.getRowIndexByPosition(4));
        Assert.assertEquals(5, this.rowReorderLayerFixture.getRowIndexByPosition(5));
        Assert.assertEquals(6, this.rowReorderLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        Assert.assertEquals(50,
                this.rowReorderLayerFixture.getRowHeightByPosition(0));
        Assert.assertEquals(70,
                this.rowReorderLayerFixture.getRowHeightByPosition(1));
        Assert.assertEquals(40,
                this.rowReorderLayerFixture.getRowHeightByPosition(2));
        Assert.assertEquals(25,
                this.rowReorderLayerFixture.getRowHeightByPosition(3));
        Assert.assertEquals(40,
                this.rowReorderLayerFixture.getRowHeightByPosition(4));
        Assert.assertEquals(40,
                this.rowReorderLayerFixture.getRowHeightByPosition(5));
        Assert.assertEquals(100,
                this.rowReorderLayerFixture.getRowHeightByPosition(6));
    }

}
