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
        this.sizeConfigCalculationMode = new SizeConfig(DEFAULT_SIZE);
        this.sizeConfigCalculationMode.setPercentageSizing(true);
        this.sizeConfigCalculationMode.calculatePercentages(1000, 10);

        this.sizeConfigFixedMode = new SizeConfig(DEFAULT_SIZE);
        this.sizeConfigFixedMode.setPercentageSizing(true);
        this.sizeConfigFixedMode.setPercentage(0, 50);
        this.sizeConfigFixedMode.setPercentage(1, 50);
        this.sizeConfigFixedMode.calculatePercentages(255, 2);

        this.sizeConfigMixedPercentageMode = new SizeConfig(DEFAULT_SIZE);
        this.sizeConfigMixedPercentageMode.setPercentageSizing(true);
        this.sizeConfigMixedPercentageMode.setPercentage(0, 30);
        this.sizeConfigMixedPercentageMode.setPercentage(2, 30);
        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 3);

        this.sizeConfigMixedMode = new SizeConfig(DEFAULT_SIZE);
        this.sizeConfigMixedMode.setPercentageSizing(true);
        this.sizeConfigMixedMode.setPercentageSizing(0, false);
        this.sizeConfigMixedMode.setPercentageSizing(1, false);
        this.sizeConfigMixedMode.setSize(0, 100);
        this.sizeConfigMixedMode.setSize(1, 100);
        this.sizeConfigMixedMode.calculatePercentages(500, 3);
    }

    @Test
    public void getAggregateSizeCalculationMode() throws Exception {
        Assert.assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(3));
        Assert.assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(5));
        Assert.assertEquals(600, this.sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(700, this.sizeConfigCalculationMode.getAggregateSize(7));
        Assert.assertEquals(800, this.sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(900, this.sizeConfigCalculationMode.getAggregateSize(9));
        Assert.assertEquals(1000,
                this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void sizeOverrideCalculationMode() throws Exception {
        this.sizeConfigCalculationMode.setSize(5, 200);

        Assert.assertEquals(201, this.sizeConfigCalculationMode.getSize(5));
    }

    @Test
    public void percentageOverrideCalculationMode() throws Exception {
        this.sizeConfigCalculationMode.setPercentage(5, 20);

        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(0));
        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(1));
        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(2));
        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(3));
        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(4));
        Assert.assertEquals(201, this.sizeConfigCalculationMode.getSize(5));
        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(6));
        Assert.assertEquals(89, this.sizeConfigCalculationMode.getSize(7));
        Assert.assertEquals(88, this.sizeConfigCalculationMode.getSize(8));
        Assert.assertEquals(88, this.sizeConfigCalculationMode.getSize(9));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesCalculationMode()
            throws Exception {
        this.sizeConfigCalculationMode.setPercentage(5, 20);

        Assert.assertEquals(89, this.sizeConfigCalculationMode.getAggregateSize(1));
        Assert.assertEquals(178, this.sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(267, this.sizeConfigCalculationMode.getAggregateSize(3));
        Assert.assertEquals(356, this.sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(445, this.sizeConfigCalculationMode.getAggregateSize(5));
        Assert.assertEquals(646, this.sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(735, this.sizeConfigCalculationMode.getAggregateSize(7));
        Assert.assertEquals(824, this.sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(912, this.sizeConfigCalculationMode.getAggregateSize(9));
        Assert.assertEquals(1000,
                this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void getAggregateSizeCalculationModeAfterAdding() throws Exception {
        this.sizeConfigCalculationMode.calculatePercentages(1000, 20);

        Assert.assertEquals(50, this.sizeConfigCalculationMode.getAggregateSize(1));
        Assert.assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(150, this.sizeConfigCalculationMode.getAggregateSize(3));
        Assert.assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(250, this.sizeConfigCalculationMode.getAggregateSize(5));
        Assert.assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(350, this.sizeConfigCalculationMode.getAggregateSize(7));
        Assert.assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(450, this.sizeConfigCalculationMode.getAggregateSize(9));
        Assert.assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(10));
        Assert.assertEquals(550, this.sizeConfigCalculationMode.getAggregateSize(11));
        Assert.assertEquals(600, this.sizeConfigCalculationMode.getAggregateSize(12));
        Assert.assertEquals(650, this.sizeConfigCalculationMode.getAggregateSize(13));
        Assert.assertEquals(700, this.sizeConfigCalculationMode.getAggregateSize(14));
        Assert.assertEquals(750, this.sizeConfigCalculationMode.getAggregateSize(15));
        Assert.assertEquals(800, this.sizeConfigCalculationMode.getAggregateSize(16));
        Assert.assertEquals(850, this.sizeConfigCalculationMode.getAggregateSize(17));
        Assert.assertEquals(900, this.sizeConfigCalculationMode.getAggregateSize(18));
        Assert.assertEquals(950, this.sizeConfigCalculationMode.getAggregateSize(19));
        Assert.assertEquals(1000,
                this.sizeConfigCalculationMode.getAggregateSize(20));
    }

    @Test
    public void getAggregateSizeCalculationModeSpaceChangeCacheCheck()
            throws Exception {
        // Change the space and positionCount to test the cached aggregated size
        // values.
        this.sizeConfigCalculationMode.calculatePercentages(1000, 20);

        Assert.assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(10));

        this.sizeConfigCalculationMode.calculatePercentages(500, 20);

        Assert.assertEquals(50, this.sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(150, this.sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(250, this.sizeConfigCalculationMode.getAggregateSize(10));

        this.sizeConfigCalculationMode.calculatePercentages(500, 10);

        Assert.assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(2));
        Assert.assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(4));
        Assert.assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(6));
        Assert.assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(8));
        Assert.assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void getAggregateSizeCalculationModeSizeChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(5));

        this.sizeConfigCalculationMode.setPercentage(5, 20);

        Assert.assertEquals(445, this.sizeConfigCalculationMode.getAggregateSize(5));
    }

    @Test
    public void getSizeConfigFixedMode() throws Exception {
        Assert.assertEquals(128, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeConfigFixedMode() throws Exception {
        Assert.assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void sizeOverrideFixedMode() throws Exception {
        this.sizeConfigFixedMode.setSize(1, 102);

        Assert.assertEquals(102, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void percentageOverrideFixedMode() throws Exception {
        this.sizeConfigFixedMode.setPercentage(1, 40);

        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(102, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesFixedMode() throws Exception {
        this.sizeConfigFixedMode.setPercentage(1, 40);

        Assert.assertEquals(127, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(229, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void getSizeFixedModeAfterAdding() throws Exception {
        this.sizeConfigFixedMode.calculatePercentages(510, 4);
        this.sizeConfigFixedMode.setPercentage(2, 50);
        this.sizeConfigFixedMode.setPercentage(3, 50);

        // the correct double value would be 127.5 - because of the rounding, as
        // there are no double pixels, and the distribution of the missing
        // pixels the values for the first 2 positions will be 128
        Assert.assertEquals(128, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(128, this.sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(2));
        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(3));
    }

    @Test
    public void getSizeFixedModeAfterAddingTooMuch() throws Exception {
        this.sizeConfigFixedMode.calculatePercentages(255, 3);
        this.sizeConfigFixedMode.setPercentage(2, 50);

        Assert.assertEquals(85, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(85, this.sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(85, this.sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getSizeFixedModeAfterAddingWithNoSize() throws Exception {
        this.sizeConfigFixedMode.calculatePercentages(255, 3);

        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(1, this.sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeFixedModeAfterAdding() throws Exception {
        this.sizeConfigFixedMode.calculatePercentages(510, 4);
        this.sizeConfigFixedMode.setPercentage(2, 50);
        this.sizeConfigFixedMode.setPercentage(3, 50);

        Assert.assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(256, this.sizeConfigFixedMode.getAggregateSize(2));
        Assert.assertEquals(383, this.sizeConfigFixedMode.getAggregateSize(3));
        Assert.assertEquals(510, this.sizeConfigFixedMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeFixedModeAfterAddingTooMuch() throws Exception {
        this.sizeConfigFixedMode.calculatePercentages(255, 3);
        this.sizeConfigFixedMode.setPercentage(2, 50);

        Assert.assertEquals(85, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(170, this.sizeConfigFixedMode.getAggregateSize(2));
        Assert.assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(3));
    }

    @Test
    public void getAggregateSizeFixedModeSpaceChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(2));

        this.sizeConfigFixedMode.calculatePercentages(500, 2);

        Assert.assertEquals(250, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(500, this.sizeConfigFixedMode.getAggregateSize(2));

        this.sizeConfigFixedMode.calculatePercentages(255, 3);

        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(127, this.sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(1, this.sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeFixedModeSizeChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(2));

        this.sizeConfigFixedMode.setPercentage(1, 40);

        Assert.assertEquals(127, this.sizeConfigFixedMode.getAggregateSize(1));
        Assert.assertEquals(229, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void getSizeConfigMixedPercentageMode() throws Exception {
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void getAggregateSizeConfigMixedPercentageMode() throws Exception {
        Assert.assertEquals(300,
                this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(700,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void sizeOverrideMixedMode() throws Exception {
        this.sizeConfigMixedPercentageMode.setSize(2, 400);

        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void percentageOverrideMixedMode() throws Exception {
        this.sizeConfigMixedPercentageMode.setPercentage(2, 40);

        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesMixedMode() throws Exception {
        this.sizeConfigMixedPercentageMode.setPercentage(2, 40);

        Assert.assertEquals(600,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAdding() throws Exception {
        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));
        Assert.assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAddingExactly100()
            throws Exception {
        this.sizeConfigMixedPercentageMode.setPercentage(3, 40);

        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(0, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));
        Assert.assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAddingTooMuch() throws Exception {
        this.sizeConfigMixedPercentageMode.setPercentage(0, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(2, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(3, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(4, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(5, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(6, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(7, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(8, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(9, 20);
        this.sizeConfigMixedPercentageMode.setPercentage(10, 20);

        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 11);

        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(0, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(2));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(3));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(4));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(5));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(6));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(7));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(8));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(9));
        Assert.assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(10));
    }

    @Test
    public void getAggregateSizeMixedModeAfterAdding() throws Exception {
        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300,
                this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(500,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(800,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeMixedModeSpaceChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(300,
                this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(700,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        Assert.assertEquals(300,
                this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(500,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(800,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeMixedModeSizeChangeCacheCheck()
            throws Exception {
        Assert.assertEquals(300,
                this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(700,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        this.sizeConfigMixedPercentageMode.setPercentage(2, 40);

        Assert.assertEquals(300,
                this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        Assert.assertEquals(600,
                this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void getSizeConfigMixedMode() throws Exception {
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeConfigMixedMode() throws Exception {
        Assert.assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void getSizeMixedModeAfterAdding() throws Exception {
        this.sizeConfigMixedMode.calculatePercentages(500, 4);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(150, this.sizeConfigMixedMode.getSize(2));
        Assert.assertEquals(150, this.sizeConfigMixedMode.getSize(3));
    }

    @Test
    public void getSizeMixedModeAfterAddingExactly100() throws Exception {
        this.sizeConfigMixedMode.setPercentage(2, 25);
        this.sizeConfigMixedMode.setPercentage(3, 25);
        this.sizeConfigMixedMode.setPercentage(4, 25);
        this.sizeConfigMixedMode.setPercentage(5, 25);

        this.sizeConfigMixedMode.calculatePercentages(1000, 6);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(2));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(3));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(4));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(5));
    }

    @Test
    public void getSizeMixedModeAfterAddingTooMuch() throws Exception {
        this.sizeConfigMixedMode.setPercentage(2, 40);
        this.sizeConfigMixedMode.setPercentage(3, 40);
        this.sizeConfigMixedMode.setPercentage(4, 40);
        this.sizeConfigMixedMode.setPercentage(5, 40);

        this.sizeConfigMixedMode.calculatePercentages(600, 6);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        // 4 additional percentage sized positions that have the same percentage
        // size
        // as 400 pixels remain after the fixed sized positions, all positions
        // should have 100 pixels
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(2));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(3));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(4));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(5));
    }

    @Test
    public void getSizeMixedMixedModeAfterAddingTooMuch() throws Exception {
        this.sizeConfigMixedMode.setPercentage(3, 40);
        this.sizeConfigMixedMode.setPercentage(4, 40);
        this.sizeConfigMixedMode.setPercentage(5, 40);
        this.sizeConfigMixedMode.setPercentage(6, 40);

        this.sizeConfigMixedMode.calculatePercentages(600, 7);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        // this column does not have a percentage width set, so it will be 0 if
        // other columns with fixed percentage widths are added
        Assert.assertEquals(0, this.sizeConfigMixedMode.getSize(2));
        // 4 additional percentage sized positions that have the same percentage
        // size
        // as 400 pixels remain after the fixed sized positions, all positions
        // should have 100 pixels
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(3));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(4));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(5));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(6));
    }

    @Test
    public void getAggregateMixedModeSpaceChangeCacheCheck() throws Exception {
        Assert.assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        this.sizeConfigMixedMode.calculatePercentages(1000, 3);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(1000, this.sizeConfigMixedMode.getAggregateSize(3));

        this.sizeConfigMixedMode.calculatePercentages(500, 4);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(350, this.sizeConfigMixedMode.getAggregateSize(3));
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateMixedModeSizeChangeCacheCheck() throws Exception {
        Assert.assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        this.sizeConfigMixedMode.setPercentage(2, 25);
        this.sizeConfigMixedMode.setPercentage(3, 25);
        this.sizeConfigMixedMode.setPercentage(4, 25);
        this.sizeConfigMixedMode.setPercentage(5, 25);

        this.sizeConfigMixedMode.calculatePercentages(1000, 6);

        Assert.assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        Assert.assertEquals(400, this.sizeConfigMixedMode.getAggregateSize(3));
        Assert.assertEquals(600, this.sizeConfigMixedMode.getAggregateSize(4));
        Assert.assertEquals(800, this.sizeConfigMixedMode.getAggregateSize(5));
        Assert.assertEquals(1000, this.sizeConfigMixedMode.getAggregateSize(6));
    }

    @Test
    public void setSizeCalculation() {
        Assert.assertEquals(100, this.sizeConfigCalculationMode.getSize(5));
        Assert.assertEquals(1000,
                this.sizeConfigCalculationMode.getAggregateSize(10));

        // resize position 5 to 150
        this.sizeConfigCalculationMode.setSize(5, 150);

        // the position itself needs to be 150
        Assert.assertEquals(150, this.sizeConfigCalculationMode.getSize(5));
        // the other cells need to be modified where all positions are adjusted
        // as they are
        // configured to take the remaining space
        Assert.assertEquals(94, this.sizeConfigCalculationMode.getSize(4));
        Assert.assertEquals(94, this.sizeConfigCalculationMode.getSize(6));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void setSizeFixed() {
        this.sizeConfigFixedMode.calculatePercentages(400, 2);

        Assert.assertEquals(200, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(200, this.sizeConfigFixedMode.getSize(1));
        Assert.assertEquals(400, this.sizeConfigFixedMode.getAggregateSize(2));

        // resize position 0 to 25 percent
        this.sizeConfigFixedMode.setSize(0, 100);

        // the position itself needs to be 100
        Assert.assertEquals(100, this.sizeConfigFixedMode.getSize(0));
        Assert.assertEquals(300, this.sizeConfigFixedMode.getSize(1));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(400, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void setSizeMixedPercentage() {
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));

        // resize position 0 to 20 percent
        this.sizeConfigMixedPercentageMode.setSize(0, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(500, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        // resize position 2 to 20 percent
        this.sizeConfigMixedPercentageMode.setSize(2, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(600, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        // resize position 1 to 50 percent
        this.sizeConfigMixedPercentageMode.setSize(1, 500);

        // the position itself needs to be 200
        Assert.assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(0));
        Assert.assertEquals(500, this.sizeConfigMixedPercentageMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(1000,
                this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void setSizeMixed() {
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(300, this.sizeConfigMixedMode.getSize(2));

        // resize position 0 to 200
        this.sizeConfigMixedMode.setSize(0, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        // resize position 1 to 200
        this.sizeConfigMixedMode.setSize(1, 200);

        // the position itself needs to be 200
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        // resize position 2
        this.sizeConfigMixedMode.setSize(2, 500);

        // no changes as last column should take remaining space
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(0));
        Assert.assertEquals(200, this.sizeConfigMixedMode.getSize(1));
        Assert.assertEquals(100, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        Assert.assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));
    }
}
