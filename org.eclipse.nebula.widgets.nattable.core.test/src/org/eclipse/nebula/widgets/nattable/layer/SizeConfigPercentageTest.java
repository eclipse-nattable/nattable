/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;

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
    public void getAggregateSizeCalculationMode() {
        assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(2));
        assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(3));
        assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(4));
        assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(5));
        assertEquals(600, this.sizeConfigCalculationMode.getAggregateSize(6));
        assertEquals(700, this.sizeConfigCalculationMode.getAggregateSize(7));
        assertEquals(800, this.sizeConfigCalculationMode.getAggregateSize(8));
        assertEquals(900, this.sizeConfigCalculationMode.getAggregateSize(9));
        assertEquals(1000, this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void sizeOverrideCalculationMode() {
        this.sizeConfigCalculationMode.setSize(5, 200);

        assertEquals(201, this.sizeConfigCalculationMode.getSize(5));
    }

    @Test
    public void percentageOverrideCalculationMode() {
        this.sizeConfigCalculationMode.setPercentage(5, 20);

        assertEquals(89, this.sizeConfigCalculationMode.getSize(0));
        assertEquals(89, this.sizeConfigCalculationMode.getSize(1));
        assertEquals(89, this.sizeConfigCalculationMode.getSize(2));
        assertEquals(89, this.sizeConfigCalculationMode.getSize(3));
        assertEquals(89, this.sizeConfigCalculationMode.getSize(4));
        assertEquals(201, this.sizeConfigCalculationMode.getSize(5));
        assertEquals(89, this.sizeConfigCalculationMode.getSize(6));
        assertEquals(89, this.sizeConfigCalculationMode.getSize(7));
        assertEquals(88, this.sizeConfigCalculationMode.getSize(8));
        assertEquals(88, this.sizeConfigCalculationMode.getSize(9));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesCalculationMode() {
        this.sizeConfigCalculationMode.setPercentage(5, 20);

        assertEquals(89, this.sizeConfigCalculationMode.getAggregateSize(1));
        assertEquals(178, this.sizeConfigCalculationMode.getAggregateSize(2));
        assertEquals(267, this.sizeConfigCalculationMode.getAggregateSize(3));
        assertEquals(356, this.sizeConfigCalculationMode.getAggregateSize(4));
        assertEquals(445, this.sizeConfigCalculationMode.getAggregateSize(5));
        assertEquals(646, this.sizeConfigCalculationMode.getAggregateSize(6));
        assertEquals(735, this.sizeConfigCalculationMode.getAggregateSize(7));
        assertEquals(824, this.sizeConfigCalculationMode.getAggregateSize(8));
        assertEquals(912, this.sizeConfigCalculationMode.getAggregateSize(9));
        assertEquals(1000, this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void getAggregateSizeCalculationModeAfterAdding() {
        this.sizeConfigCalculationMode.calculatePercentages(1000, 20);

        assertEquals(50, this.sizeConfigCalculationMode.getAggregateSize(1));
        assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(2));
        assertEquals(150, this.sizeConfigCalculationMode.getAggregateSize(3));
        assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(4));
        assertEquals(250, this.sizeConfigCalculationMode.getAggregateSize(5));
        assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(6));
        assertEquals(350, this.sizeConfigCalculationMode.getAggregateSize(7));
        assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(8));
        assertEquals(450, this.sizeConfigCalculationMode.getAggregateSize(9));
        assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(10));
        assertEquals(550, this.sizeConfigCalculationMode.getAggregateSize(11));
        assertEquals(600, this.sizeConfigCalculationMode.getAggregateSize(12));
        assertEquals(650, this.sizeConfigCalculationMode.getAggregateSize(13));
        assertEquals(700, this.sizeConfigCalculationMode.getAggregateSize(14));
        assertEquals(750, this.sizeConfigCalculationMode.getAggregateSize(15));
        assertEquals(800, this.sizeConfigCalculationMode.getAggregateSize(16));
        assertEquals(850, this.sizeConfigCalculationMode.getAggregateSize(17));
        assertEquals(900, this.sizeConfigCalculationMode.getAggregateSize(18));
        assertEquals(950, this.sizeConfigCalculationMode.getAggregateSize(19));
        assertEquals(1000, this.sizeConfigCalculationMode.getAggregateSize(20));
    }

    @Test
    public void getAggregateSizeCalculationModeSpaceChangeCacheCheck() {
        // Change the space and positionCount to test the cached aggregated size
        // values.
        this.sizeConfigCalculationMode.calculatePercentages(1000, 20);

        assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(2));
        assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(4));
        assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(6));
        assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(8));
        assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(10));

        this.sizeConfigCalculationMode.calculatePercentages(500, 20);

        assertEquals(50, this.sizeConfigCalculationMode.getAggregateSize(2));
        assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(4));
        assertEquals(150, this.sizeConfigCalculationMode.getAggregateSize(6));
        assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(8));
        assertEquals(250, this.sizeConfigCalculationMode.getAggregateSize(10));

        this.sizeConfigCalculationMode.calculatePercentages(500, 10);

        assertEquals(100, this.sizeConfigCalculationMode.getAggregateSize(2));
        assertEquals(200, this.sizeConfigCalculationMode.getAggregateSize(4));
        assertEquals(300, this.sizeConfigCalculationMode.getAggregateSize(6));
        assertEquals(400, this.sizeConfigCalculationMode.getAggregateSize(8));
        assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void getAggregateSizeCalculationModeSizeChangeCacheCheck() {
        assertEquals(500, this.sizeConfigCalculationMode.getAggregateSize(5));

        this.sizeConfigCalculationMode.setPercentage(5, 20);

        assertEquals(445, this.sizeConfigCalculationMode.getAggregateSize(5));
    }

    @Test
    public void getSizeConfigFixedMode() {
        assertEquals(128, this.sizeConfigFixedMode.getSize(0));
        assertEquals(127, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeConfigFixedMode() {
        assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void sizeOverrideFixedMode() {
        this.sizeConfigFixedMode.setSize(1, 102);

        assertEquals(102, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void percentageOverrideFixedMode() {
        this.sizeConfigFixedMode.setPercentage(1, 40);

        assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        assertEquals(102, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesFixedMode() {
        this.sizeConfigFixedMode.setPercentage(1, 40);

        assertEquals(127, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(229, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void getSizeFixedModeAfterAdding() {
        this.sizeConfigFixedMode.calculatePercentages(510, 4);
        this.sizeConfigFixedMode.setPercentage(2, 50);
        this.sizeConfigFixedMode.setPercentage(3, 50);

        // the correct double value would be 127.5 - because of the rounding, as
        // there are no double pixels, and the distribution of the missing
        // pixels the values for the first 2 positions will be 128
        assertEquals(128, this.sizeConfigFixedMode.getSize(0));
        assertEquals(128, this.sizeConfigFixedMode.getSize(1));
        assertEquals(127, this.sizeConfigFixedMode.getSize(2));
        assertEquals(127, this.sizeConfigFixedMode.getSize(3));
    }

    @Test
    public void getSizeFixedModeAfterAddingTooMuch() {
        this.sizeConfigFixedMode.calculatePercentages(255, 3);
        this.sizeConfigFixedMode.setPercentage(2, 50);

        assertEquals(85, this.sizeConfigFixedMode.getSize(0));
        assertEquals(85, this.sizeConfigFixedMode.getSize(1));
        assertEquals(85, this.sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getSizeFixedModeAfterAddingWithNoSize() {
        this.sizeConfigFixedMode.calculatePercentages(255, 3);

        assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        assertEquals(127, this.sizeConfigFixedMode.getSize(1));
        assertEquals(1, this.sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeFixedModeAfterAdding() {
        this.sizeConfigFixedMode.calculatePercentages(510, 4);
        this.sizeConfigFixedMode.setPercentage(2, 50);
        this.sizeConfigFixedMode.setPercentage(3, 50);

        assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(256, this.sizeConfigFixedMode.getAggregateSize(2));
        assertEquals(383, this.sizeConfigFixedMode.getAggregateSize(3));
        assertEquals(510, this.sizeConfigFixedMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeFixedModeAfterAddingTooMuch() {
        this.sizeConfigFixedMode.calculatePercentages(255, 3);
        this.sizeConfigFixedMode.setPercentage(2, 50);

        assertEquals(85, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(170, this.sizeConfigFixedMode.getAggregateSize(2));
        assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(3));
    }

    @Test
    public void getAggregateSizeFixedModeSpaceChangeCacheCheck() {
        assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(2));

        this.sizeConfigFixedMode.calculatePercentages(500, 2);

        assertEquals(250, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(500, this.sizeConfigFixedMode.getAggregateSize(2));

        this.sizeConfigFixedMode.calculatePercentages(255, 3);

        assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        assertEquals(127, this.sizeConfigFixedMode.getSize(1));
        assertEquals(1, this.sizeConfigFixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeFixedModeSizeChangeCacheCheck() {
        assertEquals(128, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(255, this.sizeConfigFixedMode.getAggregateSize(2));

        this.sizeConfigFixedMode.setPercentage(1, 40);

        assertEquals(127, this.sizeConfigFixedMode.getAggregateSize(1));
        assertEquals(229, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void getSizeConfigMixedPercentageMode() {
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void getAggregateSizeConfigMixedPercentageMode() {
        assertEquals(300, this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        assertEquals(700, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void sizeOverrideMixedMode() {
        this.sizeConfigMixedPercentageMode.setSize(2, 400);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void percentageOverrideMixedMode() {
        this.sizeConfigMixedPercentageMode.setPercentage(2, 40);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(2));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesMixedMode() {
        this.sizeConfigMixedPercentageMode.setPercentage(2, 40);

        assertEquals(600, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAdding() {
        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));
        assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAddingExactly100() {
        this.sizeConfigMixedPercentageMode.setPercentage(3, 40);

        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(0, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));
        assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(3));
    }

    @Test
    public void getSizeMixedPercentageModeAfterAddingTooMuch() {
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

        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(0, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(2));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(3));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(4));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(5));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(6));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(7));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(8));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(9));
        assertEquals(100, this.sizeConfigMixedPercentageMode.getSize(10));
    }

    @Test
    public void getAggregateSizeMixedModeAfterAdding() {
        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        assertEquals(500, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(800, this.sizeConfigMixedPercentageMode.getAggregateSize(3));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeMixedModeSpaceChangeCacheCheck() {
        assertEquals(300, this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        assertEquals(700, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        this.sizeConfigMixedPercentageMode.calculatePercentages(1000, 4);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        assertEquals(500, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(800, this.sizeConfigMixedPercentageMode.getAggregateSize(3));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateSizeMixedModeSizeChangeCacheCheck() {
        assertEquals(300, this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        assertEquals(700, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        this.sizeConfigMixedPercentageMode.setPercentage(2, 40);

        assertEquals(300, this.sizeConfigMixedPercentageMode.getAggregateSize(1));
        assertEquals(600, this.sizeConfigMixedPercentageMode.getAggregateSize(2));
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void getSizeConfigMixedMode() {
        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedMode.getSize(2));
    }

    @Test
    public void getAggregateSizeConfigMixedMode() {
        assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void getSizeMixedModeAfterAdding() {
        this.sizeConfigMixedMode.calculatePercentages(500, 4);

        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        assertEquals(150, this.sizeConfigMixedMode.getSize(2));
        assertEquals(150, this.sizeConfigMixedMode.getSize(3));
    }

    @Test
    public void getSizeMixedModeAfterAddingExactly100() {
        this.sizeConfigMixedMode.setPercentage(2, 25);
        this.sizeConfigMixedMode.setPercentage(3, 25);
        this.sizeConfigMixedMode.setPercentage(4, 25);
        this.sizeConfigMixedMode.setPercentage(5, 25);

        this.sizeConfigMixedMode.calculatePercentages(1000, 6);

        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getSize(2));
        assertEquals(200, this.sizeConfigMixedMode.getSize(3));
        assertEquals(200, this.sizeConfigMixedMode.getSize(4));
        assertEquals(200, this.sizeConfigMixedMode.getSize(5));
    }

    @Test
    public void getSizeMixedModeAfterAddingTooMuch() {
        this.sizeConfigMixedMode.setPercentage(2, 40);
        this.sizeConfigMixedMode.setPercentage(3, 40);
        this.sizeConfigMixedMode.setPercentage(4, 40);
        this.sizeConfigMixedMode.setPercentage(5, 40);

        this.sizeConfigMixedMode.calculatePercentages(600, 6);

        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        // 4 additional percentage sized positions that have the same percentage
        // size
        // as 400 pixels remain after the fixed sized positions, all positions
        // should have 100 pixels
        assertEquals(100, this.sizeConfigMixedMode.getSize(2));
        assertEquals(100, this.sizeConfigMixedMode.getSize(3));
        assertEquals(100, this.sizeConfigMixedMode.getSize(4));
        assertEquals(100, this.sizeConfigMixedMode.getSize(5));
    }

    @Test
    public void getSizeMixedMixedModeAfterAddingTooMuch() {
        this.sizeConfigMixedMode.setPercentage(3, 40);
        this.sizeConfigMixedMode.setPercentage(4, 40);
        this.sizeConfigMixedMode.setPercentage(5, 40);
        this.sizeConfigMixedMode.setPercentage(6, 40);

        this.sizeConfigMixedMode.calculatePercentages(600, 7);

        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        // this column does not have a percentage width set, so it will be 0 if
        // other columns with fixed percentage widths are added
        assertEquals(0, this.sizeConfigMixedMode.getSize(2));
        // 4 additional percentage sized positions that have the same percentage
        // size
        // as 400 pixels remain after the fixed sized positions, all positions
        // should have 100 pixels
        assertEquals(100, this.sizeConfigMixedMode.getSize(3));
        assertEquals(100, this.sizeConfigMixedMode.getSize(4));
        assertEquals(100, this.sizeConfigMixedMode.getSize(5));
        assertEquals(100, this.sizeConfigMixedMode.getSize(6));
    }

    @Test
    public void getAggregateMixedModeSpaceChangeCacheCheck() {
        assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        this.sizeConfigMixedMode.calculatePercentages(1000, 3);

        assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        assertEquals(1000, this.sizeConfigMixedMode.getAggregateSize(3));

        this.sizeConfigMixedMode.calculatePercentages(500, 4);

        assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        assertEquals(350, this.sizeConfigMixedMode.getAggregateSize(3));
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(4));
    }

    @Test
    public void getAggregateMixedModeSizeChangeCacheCheck() {
        assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        this.sizeConfigMixedMode.setPercentage(2, 25);
        this.sizeConfigMixedMode.setPercentage(3, 25);
        this.sizeConfigMixedMode.setPercentage(4, 25);
        this.sizeConfigMixedMode.setPercentage(5, 25);

        this.sizeConfigMixedMode.calculatePercentages(1000, 6);

        assertEquals(100, this.sizeConfigMixedMode.getAggregateSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getAggregateSize(2));
        assertEquals(400, this.sizeConfigMixedMode.getAggregateSize(3));
        assertEquals(600, this.sizeConfigMixedMode.getAggregateSize(4));
        assertEquals(800, this.sizeConfigMixedMode.getAggregateSize(5));
        assertEquals(1000, this.sizeConfigMixedMode.getAggregateSize(6));
    }

    @Test
    public void setSizeCalculation() {
        assertEquals(100, this.sizeConfigCalculationMode.getSize(5));
        assertEquals(1000, this.sizeConfigCalculationMode.getAggregateSize(10));

        // resize position 5 to 150
        this.sizeConfigCalculationMode.setSize(5, 150);

        // the position itself needs to be 150
        assertEquals(150, this.sizeConfigCalculationMode.getSize(5));
        // the other cells need to be modified where all positions are adjusted
        // as they are
        // configured to take the remaining space
        assertEquals(94, this.sizeConfigCalculationMode.getSize(4));
        assertEquals(94, this.sizeConfigCalculationMode.getSize(6));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(1000, this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void setSizeFixed() {
        this.sizeConfigFixedMode.calculatePercentages(400, 2);

        assertEquals(200, this.sizeConfigFixedMode.getSize(0));
        assertEquals(200, this.sizeConfigFixedMode.getSize(1));
        assertEquals(400, this.sizeConfigFixedMode.getAggregateSize(2));

        // resize position 0 to 25 percent
        this.sizeConfigFixedMode.setSize(0, 100);

        // the position itself needs to be 100
        assertEquals(100, this.sizeConfigFixedMode.getSize(0));
        assertEquals(300, this.sizeConfigFixedMode.getSize(1));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(400, this.sizeConfigFixedMode.getAggregateSize(2));
    }

    @Test
    public void setSizeMixedPercentage() {
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(400, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));

        // resize position 0 to 20 percent
        this.sizeConfigMixedPercentageMode.setSize(0, 200);

        // the position itself needs to be 200
        assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(500, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        // resize position 2 to 20 percent
        this.sizeConfigMixedPercentageMode.setSize(2, 200);

        // the position itself needs to be 200
        assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(600, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));

        // resize position 1 to 50 percent
        this.sizeConfigMixedPercentageMode.setSize(1, 500);

        // the position itself needs to be 200
        assertEquals(200, this.sizeConfigMixedPercentageMode.getSize(0));
        assertEquals(500, this.sizeConfigMixedPercentageMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedPercentageMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(1000, this.sizeConfigMixedPercentageMode.getAggregateSize(3));
    }

    @Test
    public void setSizeMixed() {
        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedMode.getSize(2));

        // resize position 0 to 200
        this.sizeConfigMixedMode.setSize(0, 200);

        // the position itself needs to be 200
        assertEquals(200, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        assertEquals(200, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        // resize position 1 to 200
        this.sizeConfigMixedMode.setSize(1, 200);

        // the position itself needs to be 200
        assertEquals(200, this.sizeConfigMixedMode.getSize(0));
        assertEquals(200, this.sizeConfigMixedMode.getSize(1));
        assertEquals(100, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));

        // resize position 2
        this.sizeConfigMixedMode.setSize(2, 500);

        // no changes as last column should take remaining space
        assertEquals(200, this.sizeConfigMixedMode.getSize(0));
        assertEquals(200, this.sizeConfigMixedMode.getSize(1));
        assertEquals(100, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void lastColumnTakesRemaining() {
        SizeConfig sizeConfigMixedLastTakesAll = new SizeConfig(DEFAULT_SIZE);
        sizeConfigMixedLastTakesAll.setPercentageSizing(3, true);
        sizeConfigMixedLastTakesAll.calculatePercentages(500, 4);

        assertEquals(100, sizeConfigMixedLastTakesAll.getSize(0));
        assertEquals(100, sizeConfigMixedLastTakesAll.getSize(1));
        assertEquals(100, sizeConfigMixedLastTakesAll.getSize(2));
        assertEquals(200, sizeConfigMixedLastTakesAll.getSize(3));
    }
}
