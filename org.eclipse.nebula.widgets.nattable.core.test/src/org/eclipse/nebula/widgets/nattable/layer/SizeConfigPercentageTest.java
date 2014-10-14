/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SizeConfigPercentageTest {

    private static final int DEFAULT_SIZE = 100;
    private SizeConfig sizeConfigCalculationMode;
    private SizeConfig sizeConfigFixedMode;
    private SizeConfig sizeConfigMixedPercentageMode;
    private SizeConfig sizeConfigMixedMode;

    @Before
    public void setup() {
        sizeConfigCalculationMode = new SizeConfig(DEFAULT_SIZE);
        sizeConfigCalculationMode.setPercentageSizing(true);
        sizeConfigCalculationMode.calculatePercentages(1000, 10);

        sizeConfigFixedMode = new SizeConfig(DEFAULT_SIZE);
        sizeConfigFixedMode.setPercentageSizing(true);
        sizeConfigFixedMode.setPercentage(0, 50);
        sizeConfigFixedMode.setPercentage(1, 50);
        sizeConfigFixedMode.calculatePercentages(255, 2);

        sizeConfigMixedPercentageMode = new SizeConfig(DEFAULT_SIZE);
        sizeConfigMixedPercentageMode.setPercentageSizing(true);
        sizeConfigMixedPercentageMode.setPercentage(0, 30);
        sizeConfigMixedPercentageMode.setPercentage(2, 30);
        sizeConfigMixedPercentageMode.calculatePercentages(1000, 3);

        sizeConfigMixedMode = new SizeConfig(DEFAULT_SIZE);
        sizeConfigMixedMode.setPercentageSizing(true);
        sizeConfigMixedMode.setPercentageSizing(0, false);
        sizeConfigMixedMode.setPercentageSizing(1, false);
        sizeConfigMixedMode.setSize(0, 100);
        sizeConfigMixedMode.setSize(1, 100);
        sizeConfigMixedMode.calculatePercentages(500, 3);
    }

    @Test
    public void getAggregateSizeCalculationMode() throws Exception {
        Assert.assertEquals(100, sizeConfigCalculationMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(300, sizeConfigCalculationMode.getAggregateSize(3));
        Assert.assertEquals(400, sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(500, sizeConfigCalculationMode.getAggregateSize(5));
        Assert.assertEquals(600, sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(700, sizeConfigCalculationMode.getAggregateSize(7));
        Assert.assertEquals(800, sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(900, sizeConfigCalculationMode.getAggregateSize(9));
        Assert.assertEquals(1000,
                sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void sizeOverrideCalculationMode() throws Exception {
        sizeConfigCalculationMode.setSize(5, 200);

        Assert.assertEquals(200, sizeConfigCalculationMode.getSize(5));
    }

    @Test
    public void percentageOverrideCalculationMode() throws Exception {
        sizeConfigCalculationMode.setPercentage(5, 20);

        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(1));
        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(2));
        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(3));
        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(4));
        Assert.assertEquals(200, sizeConfigCalculationMode.getSize(5));
        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(6));
        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(7));
        Assert.assertEquals(88, sizeConfigCalculationMode.getSize(8));
        Assert.assertEquals(96, sizeConfigCalculationMode.getSize(9));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesCalculationMode()
            throws Exception {
        sizeConfigCalculationMode.setPercentage(5, 20);

        Assert.assertEquals(88, sizeConfigCalculationMode.getAggregateSize(1));
        Assert.assertEquals(176, sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(264, sizeConfigCalculationMode.getAggregateSize(3));
        Assert.assertEquals(352, sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(440, sizeConfigCalculationMode.getAggregateSize(5));
        Assert.assertEquals(640, sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(728, sizeConfigCalculationMode.getAggregateSize(7));
        Assert.assertEquals(816, sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(904, sizeConfigCalculationMode.getAggregateSize(9));
        Assert.assertEquals(1000,
                sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void getAggregateSizeCalculationModeAfterAdding() throws Exception {
        sizeConfigCalculationMode.calculatePercentages(1000, 20);

        Assert.assertEquals(50, sizeConfigCalculationMode.getAggregateSize(1));
        Assert.assertEquals(100, sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(150, sizeConfigCalculationMode.getAggregateSize(3));
        Assert.assertEquals(200, sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(250, sizeConfigCalculationMode.getAggregateSize(5));
        Assert.assertEquals(300, sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(350, sizeConfigCalculationMode.getAggregateSize(7));
        Assert.assertEquals(400, sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(450, sizeConfigCalculationMode.getAggregateSize(9));
        Assert.assertEquals(500, sizeConfigCalculationMode.getAggregateSize(10));
        Assert.assertEquals(550, sizeConfigCalculationMode.getAggregateSize(11));
        Assert.assertEquals(600, sizeConfigCalculationMode.getAggregateSize(12));
        Assert.assertEquals(650, sizeConfigCalculationMode.getAggregateSize(13));
        Assert.assertEquals(700, sizeConfigCalculationMode.getAggregateSize(14));
        Assert.assertEquals(750, sizeConfigCalculationMode.getAggregateSize(15));
        Assert.assertEquals(800, sizeConfigCalculationMode.getAggregateSize(16));
        Assert.assertEquals(850, sizeConfigCalculationMode.getAggregateSize(17));
        Assert.assertEquals(900, sizeConfigCalculationMode.getAggregateSize(18));
        Assert.assertEquals(950, sizeConfigCalculationMode.getAggregateSize(19));
        Assert.assertEquals(1000,
                sizeConfigCalculationMode.getAggregateSize(20));
    }

    @Test
    public void getAggregateSizeCalculationModeSpaceChangeCacheCheck()
            throws Exception {
        // Change the space and positionCount to test the cached aggregated size
        // values.
        sizeConfigCalculationMode.calculatePercentages(1000, 20);

        Assert.assertEquals(100, sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(200, sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(300, sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(400, sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(500, sizeConfigCalculationMode.getAggregateSize(10));

        sizeConfigCalculationMode.calculatePercentages(500, 20);

        Assert.assertEquals(50, sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(100, sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(150, sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(200, sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(250, sizeConfigCalculationMode.getAggregateSize(10));

        sizeConfigCalculationMode.calculatePercentages(500, 10);

        Assert.assertEquals(100, sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(200, sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(300, sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(400, sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(500, sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void getAggregateSizeCalculationModeSizeChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(500, sizeConfigCalculationMode.getAggregateSize(5));

        sizeConfigCalculationMode.setPercentage(5, 20);

        Assert.assertEquals(440, sizeConfigCalculationMode.getAggregateSize(5));
    }

    @Test
    public void getSizeConfigFixedMode() throws Exception {
        Assert.assertEquals(127, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(128, sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeConfigFixedMode() throws Exception {
        Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(255, sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void sizeOverrideFixedMode() throws Exception {
        sizeConfigFixedMode.setSize(1, 102);

        Assert.assertEquals(102, sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void percentageOverrideFixedMode() throws Exception {
        sizeConfigFixedMode.setPercentage(1, 40);

        Assert.assertEquals(127, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(102, sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesFixedMode() throws Exception {
        sizeConfigFixedMode.setPercentage(1, 40);

        Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(229, sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void getSizeFixedModeAfterAdding() throws Exception {
        sizeConfigFixedMode.calculatePercentages(510, 4);
        sizeConfigFixedMode.setPercentage(2, 50);
        sizeConfigFixedMode.setPercentage(3, 50);

        // the correct double value would be 127.5 - because of the rounding as
        // there are no
        // double pixels, the values for the first 3 positions will be 127
        Assert.assertEquals(127, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(127, sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(127, sizeConfigFixedMode.getSize(2));
        // to correct the rounding issues in rendering, the last position will
        // have the increased
        // size to fill the available space 100%
        Assert.assertEquals(129, sizeConfigFixedMode.getSize(3));
    }

    @Test
    public void getSizeFixedModeAfterAddingTooMuch() throws Exception {
        sizeConfigFixedMode.calculatePercentages(255, 3);
        sizeConfigFixedMode.setPercentage(2, 50);

        Assert.assertEquals(84, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(84, sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(87, sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getSizeFixedModeAfterAddingWithNoSize() throws Exception {
        sizeConfigFixedMode.calculatePercentages(255, 3);

        Assert.assertEquals(127, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(127, sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(1, sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeFixedModeAfterAdding() throws Exception {
        sizeConfigFixedMode.calculatePercentages(510, 4);
        sizeConfigFixedMode.setPercentage(2, 50);
        sizeConfigFixedMode.setPercentage(3, 50);

        Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(254, sizeConfigFixedMode.getAggregateSize(2));
        Assert.assertEquals(381, sizeConfigFixedMode.getAggregateSize(3));
        Assert.assertEquals(510, sizeConfigFixedMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeFixedModeAfterAddingTooMuch() throws Exception {
        sizeConfigFixedMode.calculatePercentages(255, 3);
        sizeConfigFixedMode.setPercentage(2, 50);

        Assert.assertEquals(84, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(168, sizeConfigFixedMode.getAggregateSize(2));
        Assert.assertEquals(255, sizeConfigFixedMode.getAggregateSize(3));
    }

    @Test
    public void getAggregateSizeFixedModeSpaceChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(255, sizeConfigFixedMode.getAggregateSize(2));

        sizeConfigFixedMode.calculatePercentages(500, 2);

        Assert.assertEquals(250, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(500, sizeConfigFixedMode.getAggregateSize(2));

        sizeConfigFixedMode.calculatePercentages(255, 3);

        Assert.assertEquals(127, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(127, sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(1, sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeFixedModeSizeChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(255, sizeConfigFixedMode.getAggregateSize(2));

        sizeConfigFixedMode.setPercentage(1, 40);

        Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(229, sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void getSizeConfigMixedPercentageMode() throws Exception {
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(400, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void getAggregateSizeConfigMixedPercentageMode() throws Exception {
        Assert.assertEquals(300,
                sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(700,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void sizeOverrideMixedMode() throws Exception {
        sizeConfigMixedPercentageMode.setSize(2, 400);

        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(400, sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void percentageOverrideMixedMode() throws Exception {
        sizeConfigMixedPercentageMode.setPercentage(2, 40);

        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(400, sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesMixedMode() throws Exception {
        sizeConfigMixedPercentageMode.setPercentage(2, 40);

        Assert.assertEquals(600,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAdding() throws Exception {
        sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(200, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(2));
        Assert.assertEquals(200, sizeConfigMixedPercentageMode.getSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAddingExactly100()
            throws Exception {
        sizeConfigMixedPercentageMode.setPercentage(3, 40);

        sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(0, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(2));
        Assert.assertEquals(400, sizeConfigMixedPercentageMode.getSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAddingTooMuch() throws Exception {
        sizeConfigMixedPercentageMode.setPercentage(0, 20);
        sizeConfigMixedPercentageMode.setPercentage(2, 20);
        sizeConfigMixedPercentageMode.setPercentage(3, 20);
        sizeConfigMixedPercentageMode.setPercentage(4, 20);
        sizeConfigMixedPercentageMode.setPercentage(5, 20);
        sizeConfigMixedPercentageMode.setPercentage(6, 20);
        sizeConfigMixedPercentageMode.setPercentage(7, 20);
        sizeConfigMixedPercentageMode.setPercentage(8, 20);
        sizeConfigMixedPercentageMode.setPercentage(9, 20);
        sizeConfigMixedPercentageMode.setPercentage(10, 20);

        sizeConfigMixedPercentageMode.calculatePercentages(1000, 11);

        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(0, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(2));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(3));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(4));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(5));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(6));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(7));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(8));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(9));
        Assert.assertEquals(100, sizeConfigMixedPercentageMode.getSize(10));
    }

    @Test
    public void getAggregateSizeMixedModeAfterAdding() throws Exception {
        sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300,
                sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(500,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(800,
                sizeConfigMixedPercentageMode.getAggregateSize(3));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeMixedModeSpaceChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(300,
                sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(700,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));

        sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300,
                sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(500,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(800,
                sizeConfigMixedPercentageMode.getAggregateSize(3));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeMixedModeSizeChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(300,
                sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(700,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));

        sizeConfigMixedPercentageMode.setPercentage(2, 40);

        Assert.assertEquals(300,
                sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(600,
                sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void getSizeConfigMixedMode() throws Exception {
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeConfigMixedMode() throws Exception {
        Assert.assertEquals(100, sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void getSizeMixedModeAfterAdding() throws Exception {
        sizeConfigMixedMode.calculatePercentages(500, 4);

        Assert.assertEquals(100, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(150, sizeConfigMixedMode.getSize(2));
        Assert.assertEquals(150, sizeConfigMixedMode.getSize(3));
    }

    @Test
    public void getSizeMixedModeAfterAddingExactly100() throws Exception {
        sizeConfigMixedMode.setPercentage(2, 25);
        sizeConfigMixedMode.setPercentage(3, 25);
        sizeConfigMixedMode.setPercentage(4, 25);
        sizeConfigMixedMode.setPercentage(5, 25);

        sizeConfigMixedMode.calculatePercentages(1000, 6);

        Assert.assertEquals(100, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(2));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(3));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(4));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(5));
    }

    @Test
    public void getSizeMixedModeAfterAddingTooMuch() throws Exception {
        sizeConfigMixedMode.setPercentage(2, 40);
        sizeConfigMixedMode.setPercentage(3, 40);
        sizeConfigMixedMode.setPercentage(4, 40);
        sizeConfigMixedMode.setPercentage(5, 40);

        sizeConfigMixedMode.calculatePercentages(600, 6);

        Assert.assertEquals(100, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        // 4 additional percentage sized positions that have the same percentage
        // size
        // as 400 pixels remain after the fixed sized positions, all positions
        // should have 100 pixels
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(2));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(3));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(4));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(5));
    }

    @Test
    public void getSizeMixedMixedModeAfterAddingTooMuch() throws Exception {
        sizeConfigMixedMode.setPercentage(3, 40);
        sizeConfigMixedMode.setPercentage(4, 40);
        sizeConfigMixedMode.setPercentage(5, 40);
        sizeConfigMixedMode.setPercentage(6, 40);

        sizeConfigMixedMode.calculatePercentages(600, 7);

        Assert.assertEquals(100, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        // this column does not have a percentage width set, so it will be 0 if
        // other columns with fixed percentage widths are added
        Assert.assertEquals(0, sizeConfigMixedMode.getSize(2));
        // 4 additional percentage sized positions that have the same percentage
        // size
        // as 400 pixels remain after the fixed sized positions, all positions
        // should have 100 pixels
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(3));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(4));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(5));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(6));
    }

    @Test
    public void getAggregateMixedModeSpaceChangeCacheCheck() throws Exception {
        Assert.assertEquals(100, sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(3));

        sizeConfigMixedMode.calculatePercentages(1000, 3);

        Assert.assertEquals(100, sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(1000, sizeConfigMixedMode.getAggregateSize(3));

        sizeConfigMixedMode.calculatePercentages(500, 4);

        Assert.assertEquals(100, sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(350, sizeConfigMixedMode.getAggregateSize(3));
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateMixedModeSizeChangeCacheCheck() throws Exception {
        Assert.assertEquals(100, sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(3));

        sizeConfigMixedMode.setPercentage(2, 25);
        sizeConfigMixedMode.setPercentage(3, 25);
        sizeConfigMixedMode.setPercentage(4, 25);
        sizeConfigMixedMode.setPercentage(5, 25);

        sizeConfigMixedMode.calculatePercentages(1000, 6);

        Assert.assertEquals(100, sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(400, sizeConfigMixedMode.getAggregateSize(3));
        Assert.assertEquals(600, sizeConfigMixedMode.getAggregateSize(4));
        Assert.assertEquals(800, sizeConfigMixedMode.getAggregateSize(5));
        Assert.assertEquals(1000, sizeConfigMixedMode.getAggregateSize(6));
    }

    @Test
    public void setSizeCalculation() {
        Assert.assertEquals(100, sizeConfigCalculationMode.getSize(5));
        Assert.assertEquals(1000,
                sizeConfigCalculationMode.getAggregateSize(10));

        // resize position 5 to 150
        sizeConfigCalculationMode.setSize(5, 150);

        // the position itself needs to be 150
        Assert.assertEquals(150, sizeConfigCalculationMode.getSize(5));
        // the other cells need to be modified where all positions are adjusted
        // as they are
        // configured to take the remaining space
        Assert.assertEquals(94, sizeConfigCalculationMode.getSize(4));
        Assert.assertEquals(94, sizeConfigCalculationMode.getSize(6));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void setSizeFixed() {
        sizeConfigFixedMode.calculatePercentages(400, 2);

        Assert.assertEquals(200, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(200, sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(400, sizeConfigFixedMode.getAggregateSize(2));

        // resize position 0 to 25 percent
        sizeConfigFixedMode.setSize(0, 100);

        // the position itself needs to be 100
        Assert.assertEquals(100, sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(300, sizeConfigFixedMode.getSize(1));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(400, sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void setSizeMixedPercentage() {
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(400, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(2));

        // resize position 0 to 20 percent
        sizeConfigMixedPercentageMode.setSize(0, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(500, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));

        // resize position 2 to 20 percent
        sizeConfigMixedPercentageMode.setSize(2, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(600, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(200, sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));

        // resize position 1 to 50 percent
        sizeConfigMixedPercentageMode.setSize(1, 500);

        // the position itself needs to be 200
        Assert.assertEquals(200, sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(500, sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void setSizeMixed() {
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(300, sizeConfigMixedMode.getSize(2));

        // resize position 0 to 200
        sizeConfigMixedMode.setSize(0, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(3));

        // resize position 1 to 200
        sizeConfigMixedMode.setSize(1, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(3));

        // resize position 2
        sizeConfigMixedMode.setSize(2, 500);

        // no changes as last column should take remaining space
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(200, sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(100, sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(3));
    }
}
