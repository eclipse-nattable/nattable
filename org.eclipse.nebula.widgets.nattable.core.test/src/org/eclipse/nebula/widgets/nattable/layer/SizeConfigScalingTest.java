/*******************************************************************************
 * Copyright (c) 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SizeConfigScalingTest {

    private static final int DEFAULT_SIZE = 100;
    private SizeConfig sizeConfig;

    @Before
    public void setup() {
        this.sizeConfig = new SizeConfig(DEFAULT_SIZE);
        this.sizeConfig.setDpiConverter(new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                // use dpi of 144 which will result in a dpi factor of 1.5
                this.dpi = 144;
            }

        });
    }

    @Test
    public void getAggregateSize() throws Exception {
        Assert.assertEquals(1500, this.sizeConfig.getAggregateSize(10));
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

        // rounding issue with downscaling and upscaling
        Assert.assertEquals(9, this.sizeConfig.getAggregateSize(1));
        Assert.assertEquals(609, this.sizeConfig.getAggregateSize(5));
        Assert.assertEquals(1329, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void setIndexResizable() throws Exception {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setPositionResizable(2, true);
        this.sizeConfig.setSize(2, 120);

        Assert.assertEquals(420, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void ingnoreResizeForNonResizableColumns() throws Exception {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setSize(2, 120);

        Assert.assertEquals(450, this.sizeConfig.getAggregateSize(3));
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
        sc.setDpiConverter(new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                // use dpi of 144 which will result in a dpi factor of 1.5
                this.dpi = 144;
            }

        });

        sc.setSize(0, 30);
        sc.setSize(1, 30);
        // use global default for 3rd and 4th position

        Assert.assertEquals(210, sc.getAggregateSize(4));
    }

    @Test
    public void testAggregateSizeWithPositionDefaults() {
        final SizeConfig sc = new SizeConfig(50); // Global default of 50
        sc.setDpiConverter(new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                // use dpi of 144 which will result in a dpi factor of 1.5
                this.dpi = 144;
            }

        });

        sc.setSize(0, 30);
        sc.setSize(1, 30);
        sc.setDefaultSize(0, 10); // must not be considered as there is a size
        // set on 1st position
        sc.setDefaultSize(2, 10); // must be considered as there is no size
        // setting on 3rd position
        // use global default for 4th position

        Assert.assertEquals(150, sc.getAggregateSize(4));
    }

    @Test
    public void testAggregateSizeCache() {
        final SizeConfig sc = new SizeConfig(100);
        sc.setDpiConverter(new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                // use dpi of 144 which will result in a dpi factor of 1.5
                this.dpi = 144;
            }

        });

        sc.setSize(0, 75);
        Assert.assertEquals(675, sc.getAggregateSize(5));
        sc.setSize(1, 75);
        Assert.assertEquals(600, sc.getAggregateSize(5));
        sc.setSize(2, 75);
        Assert.assertEquals(525, sc.getAggregateSize(5));
        sc.setDefaultSize(75);
        Assert.assertEquals(450, sc.getAggregateSize(5));
        sc.setSize(2, 100);
        Assert.assertEquals(474, sc.getAggregateSize(5));
    }

}
