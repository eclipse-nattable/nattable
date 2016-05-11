/*******************************************************************************
 * Copyright (c) 2014, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
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
        assertEquals(1500, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void sizeOverride() throws Exception {
        this.sizeConfig.setSize(5, 120);

        assertEquals(120, this.sizeConfig.getSize(5));
    }

    @Test
    public void getAggregateSizeWithSizeOverrides() throws Exception {
        this.sizeConfig.setSize(5, 120);
        this.sizeConfig.setSize(0, 10);

        // rounding issue with downscaling and upscaling
        assertEquals(11, this.sizeConfig.getAggregateSize(1));
        assertEquals(611, this.sizeConfig.getAggregateSize(5));
        assertEquals(1331, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void setIndexResizable() throws Exception {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setPositionResizable(2, true);
        this.sizeConfig.setSize(2, 120);

        assertEquals(420, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void ingnoreResizeForNonResizableColumns() throws Exception {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setSize(2, 120);

        assertEquals(450, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void allIndexesSameSize() throws Exception {
        assertTrue(this.sizeConfig.isAllPositionsSameSize());

        this.sizeConfig.setSize(2, 120);
        assertFalse(this.sizeConfig.isAllPositionsSameSize());
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

        assertEquals(210, sc.getAggregateSize(4));
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

        assertEquals(150, sc.getAggregateSize(4));
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
        assertEquals(675, sc.getAggregateSize(5));
        sc.setSize(1, 75);
        assertEquals(600, sc.getAggregateSize(5));
        sc.setSize(2, 75);
        assertEquals(525, sc.getAggregateSize(5));
        sc.setDefaultSize(75);
        assertEquals(450, sc.getAggregateSize(5));
        sc.setSize(2, 100);
        assertEquals(476, sc.getAggregateSize(5));
    }

    @Test
    public void testRounding() {
        final SizeConfig sc = new SizeConfig(100);
        sc.setDpiConverter(new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                // use dpi of 120 which will result in a dpi factor of 1.25
                this.dpi = 120;
            }

        });

        assertEquals(178, sc.downScale(222));
        assertEquals(223, sc.upScale(178));
    }

    @Test
    public void testScalingFactor() {
        assertEquals(1.0f, GUIHelper.getDpiFactor(96), 0);
        assertEquals(1.25f, GUIHelper.getDpiFactor(120), 0);
        assertEquals(1.33f, GUIHelper.getDpiFactor(128), 0);
        assertEquals(1.5f, GUIHelper.getDpiFactor(144), 0);
        assertEquals(1.75f, GUIHelper.getDpiFactor(168), 0);
        assertEquals(2.0f, GUIHelper.getDpiFactor(192), 0);
        assertEquals(2.5f, GUIHelper.getDpiFactor(240), 0);
        assertEquals(3.0f, GUIHelper.getDpiFactor(288), 0);
        assertEquals(1.0f, GUIHelper.getDpiFactor(78), 0);
    }

}
