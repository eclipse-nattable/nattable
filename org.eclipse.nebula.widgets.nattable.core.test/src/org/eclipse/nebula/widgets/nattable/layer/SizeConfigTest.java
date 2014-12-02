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
package org.eclipse.nebula.widgets.nattable.layer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SizeConfigTest {

    private static final int DEFAULT_SIZE = 100;
    private SizeConfig sizeConfig;

    @Before
    public void setup() {
        this.sizeConfig = new SizeConfig(DEFAULT_SIZE);
    }

    @Test
    public void getAggregateSize() throws Exception {
        Assert.assertEquals(1000, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void sizeOverride() throws Exception {
        this.sizeConfig.setSize(5, 120);

        Assert.assertEquals(120, this.sizeConfig.getSize(5));
    }

    @Test
    public void getAggregateSizeWithSizeOverrides() throws Exception {
        this.sizeConfig.setSize(5, 120);
        this.sizeConfig.setSize(0, 10);

        Assert.assertEquals(10, this.sizeConfig.getAggregateSize(1));
        Assert.assertEquals(410, this.sizeConfig.getAggregateSize(5));
        Assert.assertEquals(930, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void setIndexResizable() throws Exception {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setPositionResizable(2, true);
        this.sizeConfig.setSize(2, 120);

        Assert.assertEquals(320, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void ingnoreResizeForNonResizableColumns() throws Exception {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setSize(2, 120);

        Assert.assertEquals(300, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void allIndexesSameSize() throws Exception {
        Assert.assertTrue(this.sizeConfig.isAllPositionsSameSize());

        this.sizeConfig.setSize(2, 120);
        Assert.assertFalse(this.sizeConfig.isAllPositionsSameSize());
    }

    @Test
    public void testAggregateSize() {
        final SizeConfig sc = new SizeConfig(50); // Global default of 50
        sc.setSize(0, 20);
        sc.setSize(1, 20);
        // use global default for 3rd and 4th position

        Assert.assertEquals(140, sc.getAggregateSize(4));
    }

    @Test
    public void testAggregateSizeWithPositionDefaults() {
        final SizeConfig sc = new SizeConfig(50); // Global default of 50
        sc.setSize(0, 20);
        sc.setSize(1, 20);
        sc.setDefaultSize(0, 10); // must not be considered as there is a size
                                  // set on 1st position
        sc.setDefaultSize(2, 10); // must be considered as there is no size
                                  // setting on 3rd position
        // use global default for 4th position

        Assert.assertEquals(100, sc.getAggregateSize(4));
    }

    @Test
    public void testAggregateSizeCache() {
        final SizeConfig sc = new SizeConfig(100);
        sc.setSize(0, 50);
        Assert.assertEquals(450, sc.getAggregateSize(5));
        sc.setSize(1, 50);
        Assert.assertEquals(400, sc.getAggregateSize(5));
        sc.setSize(2, 50);
        Assert.assertEquals(350, sc.getAggregateSize(5));
        sc.setDefaultSize(75);
        Assert.assertEquals(300, sc.getAggregateSize(5));
        sc.setSize(2, 100);
        Assert.assertEquals(350, sc.getAggregateSize(5));
    }

}
