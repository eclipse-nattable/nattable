/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SizeConfigTest {

    private static final int DEFAULT_SIZE = 100;
    private SizeConfig sizeConfig;

    @BeforeEach
    public void setup() {
        this.sizeConfig = new SizeConfig(DEFAULT_SIZE);
    }

    @Test
    public void getAggregateSize() {
        assertEquals(1000, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void sizeOverride() {
        this.sizeConfig.setSize(5, 120);

        assertEquals(120, this.sizeConfig.getSize(5));
    }

    @Test
    public void getAggregateSizeWithSizeOverrides() {
        this.sizeConfig.setSize(5, 120);
        this.sizeConfig.setSize(0, 10);

        assertEquals(10, this.sizeConfig.getAggregateSize(1));
        assertEquals(410, this.sizeConfig.getAggregateSize(5));
        assertEquals(930, this.sizeConfig.getAggregateSize(10));
    }

    @Test
    public void setIndexResizable() {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setPositionResizable(2, true);
        this.sizeConfig.setSize(2, 120);

        assertEquals(320, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void ingnoreResizeForNonResizableColumns() {
        this.sizeConfig.setResizableByDefault(false);
        this.sizeConfig.setSize(2, 120);

        assertEquals(300, this.sizeConfig.getAggregateSize(3));
    }

    @Test
    public void allIndexesSameSize() {
        assertTrue(this.sizeConfig.isAllPositionsSameSize());

        this.sizeConfig.setSize(2, 120);
        assertFalse(this.sizeConfig.isAllPositionsSameSize());
    }

    @Test
    public void testAggregateSize() {
        // Global default of 50
        final SizeConfig sc = new SizeConfig(50);
        sc.setSize(0, 20);
        sc.setSize(1, 20);
        // use global default for 3rd and 4th position

        assertEquals(140, sc.getAggregateSize(4));
    }

    @Test
    public void testAggregateSizeWithPositionDefaults() {
        // Global default of 50
        final SizeConfig sc = new SizeConfig(50);
        sc.setSize(0, 20);
        sc.setSize(1, 20);
        // must not be considered as there is a size
        // set on 1st position
        sc.setDefaultSize(0, 10);
        // must be considered as there is no size
        // setting on 3rd position
        sc.setDefaultSize(2, 10);

        // use global default for 4th position

        assertEquals(100, sc.getAggregateSize(4));
    }

    @Test
    public void testAggregateSizeCache() {
        final SizeConfig sc = new SizeConfig(100);
        sc.setSize(0, 50);
        assertEquals(450, sc.getAggregateSize(5));
        sc.setSize(1, 50);
        assertEquals(400, sc.getAggregateSize(5));
        sc.setSize(2, 50);
        assertEquals(350, sc.getAggregateSize(5));
        sc.setDefaultSize(75);
        assertEquals(300, sc.getAggregateSize(5));
        sc.setSize(2, 100);
        assertEquals(350, sc.getAggregateSize(5));
    }

    @Test
    public void testReset() {
        final SizeConfig sc = new SizeConfig(50);
        sc.setSize(0, 20);
        sc.setSize(1, 20);
        sc.setDefaultSize(2, 10);
        sc.setPositionResizable(2, false);

        assertEquals(20, sc.getSize(0));
        assertEquals(20, sc.getSize(1));
        assertEquals(10, sc.getSize(2));
        assertEquals(50, sc.getSize(3));
        assertFalse(sc.isPositionResizable(2));

        sc.reset();

        assertEquals(50, sc.getSize(0));
        assertEquals(50, sc.getSize(1));
        assertEquals(50, sc.getSize(2));
        assertEquals(50, sc.getSize(3));
        assertTrue(sc.isPositionResizable(2));
    }

    @Test
    public void testAggregateSizeWithScaling() {
        SizeConfig sc = new SizeConfig(100);

        // set some specific sizes to avoid that sc.isAllPositionsSameSize
        // does not return true and aggregate values are not determined
        sc.setSize(0, 50);
        sc.setSize(1, 50);

        int nCols = 20;

        // cache the initial aggregate sizes of last and second last column.
        // this is done when the DataLayer is initialized
        int cachedAggregateSize = sc.getAggregateSize(nCols - 1);

        // this is done when the NatTable itself is initialized
        // programatically set a dpi > 96
        sc.setDpiConverter(new FixedScalingDpiConverter((int) (96 * 1.25)));

        int aggregateSize = sc.getAggregateSize(nCols - 1);

        assertTrue(aggregateSize != cachedAggregateSize, "aggregate size of last column is same as cached aggregate size");
    }
}
