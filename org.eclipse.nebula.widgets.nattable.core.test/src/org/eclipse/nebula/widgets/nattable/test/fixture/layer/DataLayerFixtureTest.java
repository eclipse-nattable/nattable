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

public class DataLayerFixtureTest {

    private ILayer dataLayerFixture;

    @Before
    public void setup() {
        this.dataLayerFixture = new DataLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        Assert.assertEquals(0, this.dataLayerFixture.getColumnIndexByPosition(0));
        Assert.assertEquals(1, this.dataLayerFixture.getColumnIndexByPosition(1));
        Assert.assertEquals(2, this.dataLayerFixture.getColumnIndexByPosition(2));
        Assert.assertEquals(3, this.dataLayerFixture.getColumnIndexByPosition(3));
        Assert.assertEquals(4, this.dataLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        Assert.assertEquals(150, this.dataLayerFixture.getColumnWidthByPosition(0));
        Assert.assertEquals(100, this.dataLayerFixture.getColumnWidthByPosition(1));
        Assert.assertEquals(35, this.dataLayerFixture.getColumnWidthByPosition(2));
        Assert.assertEquals(100, this.dataLayerFixture.getColumnWidthByPosition(3));
        Assert.assertEquals(80, this.dataLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        Assert.assertEquals(0, this.dataLayerFixture.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.dataLayerFixture.getRowIndexByPosition(1));
        Assert.assertEquals(2, this.dataLayerFixture.getRowIndexByPosition(2));
        Assert.assertEquals(3, this.dataLayerFixture.getRowIndexByPosition(3));
        Assert.assertEquals(4, this.dataLayerFixture.getRowIndexByPosition(4));
        Assert.assertEquals(5, this.dataLayerFixture.getRowIndexByPosition(5));
        Assert.assertEquals(6, this.dataLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        Assert.assertEquals(40, this.dataLayerFixture.getRowHeightByPosition(0));
        Assert.assertEquals(70, this.dataLayerFixture.getRowHeightByPosition(1));
        Assert.assertEquals(25, this.dataLayerFixture.getRowHeightByPosition(2));
        Assert.assertEquals(40, this.dataLayerFixture.getRowHeightByPosition(3));
        Assert.assertEquals(50, this.dataLayerFixture.getRowHeightByPosition(4));
        Assert.assertEquals(40, this.dataLayerFixture.getRowHeightByPosition(5));
        Assert.assertEquals(100, this.dataLayerFixture.getRowHeightByPosition(6));
    }

}
