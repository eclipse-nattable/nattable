/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

        // - we increase position 5 to 200, which means 20%
        // - this is an increase by 10%
        // - the adjacent position 6 then needs to decrease by 10%
        // as fixDynamicPercentageValues is enabled by default
        // - as a reduction to 0 is not allowed, position 6 is set to 1%
        // - the value of 1% (10 pixels) is reduced in the other next
        // position
        assertEquals(200, this.sizeConfigCalculationMode.getSize(5));
        assertEquals(10, this.sizeConfigCalculationMode.getSize(6));
        assertEquals(90, this.sizeConfigCalculationMode.getSize(7));
    }

    @Test
    public void sizeOverrideCalculationModeLeft() {
        this.sizeConfigCalculationMode.setSize(9, 200);

        // - we increase position 9 to 200, which means 20%
        // - this is an increase by 10%
        // - the adjacent position 8 then needs to decrease by 10%
        // as fixDynamicPercentageValues is enabled by default
        // - as a reduction to 0 is not allowed, position 8 is set to 1%
        // - the value of 1% (10 pixels) is reduced in the previous
        // position
        assertEquals(200, this.sizeConfigCalculationMode.getSize(9));
        assertEquals(10, this.sizeConfigCalculationMode.getSize(8));
        assertEquals(90, this.sizeConfigCalculationMode.getSize(7));
    }

    @Test
    public void sizeOverrideCalculationModeWithoutFixDynamicPercentages() {
        this.sizeConfigCalculationMode.setFixPercentageValuesOnResize(false);
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
        this.sizeConfigFixedMode.setDistributeRemainingSpace(false);

        this.sizeConfigFixedMode.setPercentage(1, 40);

        assertEquals(127, this.sizeConfigFixedMode.getSize(0));
        assertEquals(102, this.sizeConfigFixedMode.getSize(1));
    }

    @Test
    public void getAggregateSizeWithSizeOverridesFixedMode() {
        this.sizeConfigFixedMode.setDistributeRemainingSpace(false);

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
        this.sizeConfigFixedMode.setDistributeRemainingSpace(false);

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
        // the other cells should not be modified as they are fixed by default
        assertEquals(100, this.sizeConfigCalculationMode.getSize(4));
        // despite the adjacent position to the right, which should be reduced
        assertEquals(50, this.sizeConfigCalculationMode.getSize(6));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(1000, this.sizeConfigCalculationMode.getAggregateSize(10));
    }

    @Test
    public void setSizeCalculationWithoutFixPercentageValues() {
        this.sizeConfigCalculationMode.setFixPercentageValuesOnResize(false);

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

    @Test
    public void remainingSpaceShouldBeDistributedOnFixedPercentageColumns() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentage(0, 25);
        sizeConfig.setPercentage(1, 25);
        sizeConfig.setPercentage(2, 50);

        sizeConfig.calculatePercentages(500, 3);

        assertEquals(500, sizeConfig.getAggregateSize(3));
        assertEquals(125, sizeConfig.getSize(0));
        assertEquals(125, sizeConfig.getSize(1));
        assertEquals(250, sizeConfig.getSize(2));

        sizeConfig.setPercentage(2, 0);
        sizeConfig.calculatePercentages(500, 3);

        assertEquals(500, sizeConfig.getAggregateSize(3));
        assertEquals(250, sizeConfig.getSize(0));
        assertEquals(250, sizeConfig.getSize(1));
        assertEquals(0, sizeConfig.getSize(2));
    }

    @Test
    public void remainingSpaceShouldBeDistributedOnFixedPercentageColumns2() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentage(0, 25);
        sizeConfig.setPercentage(1, 25);
        sizeConfig.setPercentage(2, 50);

        sizeConfig.calculatePercentages(201, 3);

        assertEquals(201, sizeConfig.getAggregateSize(3));
        assertEquals(51, sizeConfig.getSize(0));
        assertEquals(50, sizeConfig.getSize(1));
        assertEquals(100, sizeConfig.getSize(2));

        sizeConfig.setPercentage(2, 0);
        sizeConfig.calculatePercentages(201, 3);

        assertEquals(201, sizeConfig.getAggregateSize(3));
        assertEquals(101, sizeConfig.getSize(0));
        assertEquals(100, sizeConfig.getSize(1));
        assertEquals(0, sizeConfig.getSize(2));
    }

    @Test
    public void mixedConfigurationShouldBeCorrectlyCalculatedWithGeneralPercentage() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);

        sizeConfig.setPercentageSizing(true);
        sizeConfig.setPercentage(0, 5);
        sizeConfig.setPercentageSizing(1, false);
        sizeConfig.setSize(1, 130);
        sizeConfig.setPercentageSizing(4, false);
        sizeConfig.setSize(4, 200);
        sizeConfig.setPercentageSizing(5, false);
        sizeConfig.setSize(5, 200);

        sizeConfig.calculatePercentages(961, 7);

        assertEquals(961, sizeConfig.getAggregateSize(7));
        assertEquals(22, sizeConfig.getSize(0));
        assertEquals(130, sizeConfig.getSize(1));
        assertEquals(137, sizeConfig.getSize(2));
        assertEquals(136, sizeConfig.getSize(3));
        assertEquals(200, sizeConfig.getSize(4));
        assertEquals(200, sizeConfig.getSize(5));
        assertEquals(136, sizeConfig.getSize(6));
    }

    @Test
    public void mixedConfigurationShouldBeCorrectlyCalculatedWithoutGeneralPercentage() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);

        sizeConfig.setPercentage(0, 5);
        sizeConfig.setSize(1, 130);
        sizeConfig.setPercentageSizing(2, true);
        sizeConfig.setPercentageSizing(3, true);
        sizeConfig.setSize(4, 200);
        sizeConfig.setSize(5, 200);
        sizeConfig.setPercentageSizing(6, true);

        sizeConfig.calculatePercentages(961, 7);

        assertEquals(961, sizeConfig.getAggregateSize(7));
        assertEquals(49, sizeConfig.getSize(0));
        assertEquals(130, sizeConfig.getSize(1));
        assertEquals(128, sizeConfig.getSize(2));
        assertEquals(127, sizeConfig.getSize(3));
        assertEquals(200, sizeConfig.getSize(4));
        assertEquals(200, sizeConfig.getSize(5));
        assertEquals(127, sizeConfig.getSize(6));
    }

    @Test
    public void defaultMinSizeRespected() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.calculatePercentages(1000, 5);

        assertEquals(200, sizeConfig.getAggregateSize(1));
        assertEquals(400, sizeConfig.getAggregateSize(2));
        assertEquals(600, sizeConfig.getAggregateSize(3));
        assertEquals(800, sizeConfig.getAggregateSize(4));
        assertEquals(1000, sizeConfig.getAggregateSize(5));

        sizeConfig.calculatePercentages(250, 5);

        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(100, sizeConfig.getAggregateSize(2));
        assertEquals(150, sizeConfig.getAggregateSize(3));
        assertEquals(200, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));

        sizeConfig.setDefaultMinSize(100);
        sizeConfig.calculatePercentages(250, 5);

        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(200, sizeConfig.getAggregateSize(2));
        assertEquals(300, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(500, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void singleMinSizeRespected() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);

        sizeConfig.calculatePercentages(250, 5);

        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(100, sizeConfig.getAggregateSize(2));
        assertEquals(150, sizeConfig.getAggregateSize(3));
        assertEquals(200, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 100);
        sizeConfig.calculatePercentages(250, 5);

        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(138, sizeConfig.getAggregateSize(2));
        assertEquals(176, sizeConfig.getAggregateSize(3));
        assertEquals(213, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 0);
        sizeConfig.setMinSize(4, 100);
        sizeConfig.calculatePercentages(250, 5);

        assertEquals(38, sizeConfig.getAggregateSize(1));
        assertEquals(76, sizeConfig.getAggregateSize(2));
        assertEquals(113, sizeConfig.getAggregateSize(3));
        assertEquals(150, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void defaultMinSizeRespectedWithFixedPercentageSize() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentage(0, 20);
        sizeConfig.setPercentage(1, 20);
        sizeConfig.setPercentage(2, 20);
        sizeConfig.setPercentage(3, 20);
        sizeConfig.setPercentage(4, 20);
        sizeConfig.calculatePercentages(1000, 5);

        assertEquals(200, sizeConfig.getAggregateSize(1));
        assertEquals(400, sizeConfig.getAggregateSize(2));
        assertEquals(600, sizeConfig.getAggregateSize(3));
        assertEquals(800, sizeConfig.getAggregateSize(4));
        assertEquals(1000, sizeConfig.getAggregateSize(5));

        sizeConfig.calculatePercentages(250, 5);

        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(100, sizeConfig.getAggregateSize(2));
        assertEquals(150, sizeConfig.getAggregateSize(3));
        assertEquals(200, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));

        sizeConfig.setDefaultMinSize(100);
        sizeConfig.calculatePercentages(250, 5);

        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(200, sizeConfig.getAggregateSize(2));
        assertEquals(300, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(500, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void singleMinSizeRespectedWithFixedPercentageSize() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentage(0, 20);
        sizeConfig.setPercentage(1, 20);
        sizeConfig.setPercentage(2, 20);
        sizeConfig.setPercentage(3, 20);
        sizeConfig.setPercentage(4, 20);

        sizeConfig.calculatePercentages(250, 5);

        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(100, sizeConfig.getAggregateSize(2));
        assertEquals(150, sizeConfig.getAggregateSize(3));
        assertEquals(200, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 100);
        sizeConfig.calculatePercentages(250, 5);

        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(138, sizeConfig.getAggregateSize(2));
        assertEquals(176, sizeConfig.getAggregateSize(3));
        assertEquals(213, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 0);
        sizeConfig.setMinSize(4, 100);
        sizeConfig.calculatePercentages(250, 5);

        assertEquals(38, sizeConfig.getAggregateSize(1));
        assertEquals(76, sizeConfig.getAggregateSize(2));
        assertEquals(113, sizeConfig.getAggregateSize(3));
        assertEquals(150, sizeConfig.getAggregateSize(4));
        assertEquals(250, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void singleMinSizeRespectedWithFixedPercentageSizeDifferentRatio() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentage(0, 50);
        sizeConfig.setPercentage(1, 50);
        sizeConfig.setPercentage(2, 50);
        sizeConfig.setPercentage(3, 100);

        sizeConfig.calculatePercentages(250, 4);

        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(100, sizeConfig.getAggregateSize(2));
        assertEquals(150, sizeConfig.getAggregateSize(3));
        assertEquals(250, sizeConfig.getAggregateSize(4));

        sizeConfig.setMinSize(0, 100);
        sizeConfig.calculatePercentages(250, 4);

        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(138, sizeConfig.getAggregateSize(2));
        assertEquals(175, sizeConfig.getAggregateSize(3));
        assertEquals(250, sizeConfig.getAggregateSize(4));

        sizeConfig.setMinSize(0, 0);
        sizeConfig.setMinSize(2, 100);
        sizeConfig.calculatePercentages(250, 4);

        assertEquals(38, sizeConfig.getAggregateSize(1));
        assertEquals(75, sizeConfig.getAggregateSize(2));
        assertEquals(175, sizeConfig.getAggregateSize(3));
        assertEquals(250, sizeConfig.getAggregateSize(4));
    }

    @Test
    public void mixedPercentageSizeWithMinSize_oneFixedPercentageWithMinSize() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.setPercentageSizing(1, false);
        sizeConfig.setPercentageSizing(3, false);

        sizeConfig.setPercentage(0, 50);
        sizeConfig.setSize(1, 150);
        sizeConfig.setSize(3, 150);

        // check for 4 columns - one fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(200, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - one fixed percentage, two dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(200, sizeConfig.getAggregateSize(2));
        assertEquals(225, sizeConfig.getAggregateSize(3));
        assertEquals(375, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 100);

        // check for 4 columns - one fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - one fixed percentage, two dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // check for 4 columns - one fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(450, 4);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(300, sizeConfig.getAggregateSize(3));
        assertEquals(450, sizeConfig.getAggregateSize(4));

        // check for 5 columns - one fixed percentage, two dynamic percentage
        sizeConfig.calculatePercentages(450, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(275, sizeConfig.getAggregateSize(3));
        assertEquals(425, sizeConfig.getAggregateSize(4));
        assertEquals(450, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void mixedPercentageSizeWithMinSize_twoFixedPercentage() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.setPercentageSizing(1, false);
        sizeConfig.setPercentageSizing(3, false);

        sizeConfig.setPercentage(0, 50);
        sizeConfig.setSize(1, 150);
        sizeConfig.setPercentage(2, 50);
        sizeConfig.setSize(3, 150);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(200, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - two fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(50, sizeConfig.getAggregateSize(1));
        assertEquals(200, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 100);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - two fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // reduce min size to have remaining space that can be taken by other
        // columns
        sizeConfig.setMinSize(0, 75);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(75, sizeConfig.getAggregateSize(1));
        assertEquals(225, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(75, sizeConfig.getAggregateSize(1));
        assertEquals(225, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - two fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(75, sizeConfig.getAggregateSize(1));
        assertEquals(225, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void mixedPercentageSizeWithMinSize_twoFixedPercentage_not100percent() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.setPercentageSizing(1, false);
        sizeConfig.setPercentageSizing(3, false);

        sizeConfig.setPercentage(0, 40);
        sizeConfig.setSize(1, 150);
        sizeConfig.setPercentage(2, 40);
        sizeConfig.setSize(3, 150);

        sizeConfig.setDistributeRemainingSpace(false);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(40, sizeConfig.getAggregateSize(1));
        assertEquals(190, sizeConfig.getAggregateSize(2));
        assertEquals(230, sizeConfig.getAggregateSize(3));
        assertEquals(380, sizeConfig.getAggregateSize(4));

        // check for 5 columns - two fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(40, sizeConfig.getAggregateSize(1));
        assertEquals(190, sizeConfig.getAggregateSize(2));
        assertEquals(230, sizeConfig.getAggregateSize(3));
        assertEquals(380, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        sizeConfig.setMinSize(0, 100);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - two fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // reduce min size to have remaining space that can be taken by other
        // columns
        sizeConfig.setMinSize(0, 75);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(75, sizeConfig.getAggregateSize(1));
        assertEquals(225, sizeConfig.getAggregateSize(2));
        assertEquals(230, sizeConfig.getAggregateSize(3));
        assertEquals(380, sizeConfig.getAggregateSize(4));

        // enable distribute remaining space
        sizeConfig.setDistributeRemainingSpace(true);

        // check for 4 columns - two fixed percentage
        sizeConfig.calculatePercentages(400, 4);
        assertEquals(75, sizeConfig.getAggregateSize(1));
        assertEquals(225, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(400, sizeConfig.getAggregateSize(4));

        // check for 5 columns - two fixed percentage, one dynamic percentage
        sizeConfig.calculatePercentages(400, 5);
        assertEquals(75, sizeConfig.getAggregateSize(1));
        assertEquals(225, sizeConfig.getAggregateSize(2));
        assertEquals(230, sizeConfig.getAggregateSize(3));
        assertEquals(380, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void mixedPercentageSizeWithMinSize_multiFixedPercentage() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.setPercentageSizing(1, false);
        sizeConfig.setPercentageSizing(2, false);
        sizeConfig.setDistributeRemainingSpace(false);

        sizeConfig.setPercentage(0, 20);
        sizeConfig.setSize(1, 100);
        sizeConfig.setSize(2, 100);
        sizeConfig.setPercentage(3, 25);
        sizeConfig.setPercentage(4, 25);
        sizeConfig.setPercentage(5, 10);

        sizeConfig.calculatePercentages(600, 6);
        assertEquals(80, sizeConfig.getAggregateSize(1));
        assertEquals(180, sizeConfig.getAggregateSize(2));
        assertEquals(280, sizeConfig.getAggregateSize(3));
        assertEquals(380, sizeConfig.getAggregateSize(4));
        assertEquals(480, sizeConfig.getAggregateSize(5));
        assertEquals(520, sizeConfig.getAggregateSize(6));

        sizeConfig.setMinSize(0, 150);

        sizeConfig.calculatePercentages(600, 6);
        assertEquals(150, sizeConfig.getAggregateSize(1));
        assertEquals(250, sizeConfig.getAggregateSize(2));
        assertEquals(350, sizeConfig.getAggregateSize(3));
        assertEquals(450, sizeConfig.getAggregateSize(4));
        assertEquals(550, sizeConfig.getAggregateSize(5));
        assertEquals(590, sizeConfig.getAggregateSize(6));
    }

    @Test
    public void shouldUpdateMinSizeOnResize() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentage(0, 20);
        sizeConfig.setPercentage(1, 20);
        sizeConfig.setPercentage(2, 20);
        sizeConfig.setPercentage(3, 20);
        sizeConfig.setPercentage(4, 20);

        sizeConfig.setMinSize(0, 100);

        sizeConfig.calculatePercentages(600, 5);
        assertEquals(120, sizeConfig.getAggregateSize(1));
        assertEquals(240, sizeConfig.getAggregateSize(2));
        assertEquals(360, sizeConfig.getAggregateSize(3));
        assertEquals(480, sizeConfig.getAggregateSize(4));
        assertEquals(600, sizeConfig.getAggregateSize(5));

        sizeConfig.calculatePercentages(400, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(175, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(325, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // simulate setting a size of a position with minimum to a lower value
        // than the minimum, e.g. make a column smaller than the min
        sizeConfig.setSize(0, 60);

        sizeConfig.calculatePercentages(400, 5);
        assertEquals(60, sizeConfig.getAggregateSize(1));
        assertEquals(175, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(325, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));
    }

    @Test
    public void shouldUpdateMinSizeOnResizeWithNoFixedPercentage() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);

        sizeConfig.setMinSize(0, 100);

        sizeConfig.calculatePercentages(600, 5);
        assertEquals(120, sizeConfig.getAggregateSize(1));
        assertEquals(240, sizeConfig.getAggregateSize(2));
        assertEquals(360, sizeConfig.getAggregateSize(3));
        assertEquals(480, sizeConfig.getAggregateSize(4));
        assertEquals(600, sizeConfig.getAggregateSize(5));

        sizeConfig.calculatePercentages(400, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(175, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(325, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // simulate setting a size of a position with minimum to a lower value
        // than the minimum, e.g. make a column smaller than the min
        sizeConfig.setSize(0, 60);

        sizeConfig.calculatePercentages(400, 5);
        assertEquals(60, sizeConfig.getAggregateSize(1));
        // as we reduce the width of the first position, only the second
        // position should increase. therefore the aggregate sizes should stay
        // the same as before
        assertEquals(175, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(325, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // min size was adjusted because of the resize
        assertEquals(60, sizeConfig.getMinSize(0));
    }

    @Test
    public void shouldUpdateMinSizeOnResizeWithNoFixedPercentageNoFixPercentageValues() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);

        // disable fixing of dynamic percentage values so all positions get
        // resized
        sizeConfig.setFixPercentageValuesOnResize(false);

        sizeConfig.setMinSize(0, 100);

        sizeConfig.calculatePercentages(600, 5);
        assertEquals(120, sizeConfig.getAggregateSize(1));
        assertEquals(240, sizeConfig.getAggregateSize(2));
        assertEquals(360, sizeConfig.getAggregateSize(3));
        assertEquals(480, sizeConfig.getAggregateSize(4));
        assertEquals(600, sizeConfig.getAggregateSize(5));

        sizeConfig.calculatePercentages(400, 5);
        assertEquals(100, sizeConfig.getAggregateSize(1));
        assertEquals(175, sizeConfig.getAggregateSize(2));
        assertEquals(250, sizeConfig.getAggregateSize(3));
        assertEquals(325, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // simulate setting a size of a position with minimum to a lower value
        // than the minimum, e.g. make a column smaller than the min
        sizeConfig.setSize(0, 60);

        sizeConfig.calculatePercentages(400, 5);
        assertEquals(60, sizeConfig.getAggregateSize(1));
        assertEquals(145, sizeConfig.getAggregateSize(2));
        assertEquals(230, sizeConfig.getAggregateSize(3));
        assertEquals(315, sizeConfig.getAggregateSize(4));
        assertEquals(400, sizeConfig.getAggregateSize(5));

        // min size was adjusted because of the resize
        assertEquals(60, sizeConfig.getMinSize(0));
    }

    @Test
    public void shouldNotExceedSpaceOnScaling() {
        SizeConfig scaledSizeConfig = new SizeConfig(DEFAULT_SIZE);
        scaledSizeConfig.setPercentageSizing(true);

        scaledSizeConfig.calculatePercentages(134, 4);

        assertEquals(34, scaledSizeConfig.getSize(0));
        assertEquals(34, scaledSizeConfig.getSize(1));
        assertEquals(33, scaledSizeConfig.getSize(2));
        assertEquals(33, scaledSizeConfig.getSize(3));
        assertEquals(134, scaledSizeConfig.getAggregateSize(4));

        // use dpi of 144 which will result in a dpi factor of 1.5
        scaledSizeConfig.setDpiConverter(new FixedScalingDpiConverter(144));

        scaledSizeConfig.calculatePercentages(201, 4);

        assertEquals(51, scaledSizeConfig.getSize(0));
        assertEquals(50, scaledSizeConfig.getSize(1));
        assertEquals(50, scaledSizeConfig.getSize(2));
        assertEquals(50, scaledSizeConfig.getSize(3));
        assertEquals(201, scaledSizeConfig.getAggregateSize(4));
    }

    @Test
    public void shouldNotExceedSpaceOnScaling2() {
        SizeConfig scaledSizeConfig = new SizeConfig(DEFAULT_SIZE);
        scaledSizeConfig.setPercentageSizing(true);

        scaledSizeConfig.calculatePercentages(375, 3);

        assertEquals(125, scaledSizeConfig.getSize(0));
        assertEquals(125, scaledSizeConfig.getSize(1));
        assertEquals(125, scaledSizeConfig.getSize(2));
        assertEquals(375, scaledSizeConfig.getAggregateSize(3));

        // use dpi of 144 which will result in a dpi factor of 1.5
        scaledSizeConfig.setDpiConverter(new FixedScalingDpiConverter(144));

        scaledSizeConfig.calculatePercentages(563, 3);

        assertEquals(188, scaledSizeConfig.getSize(0));
        assertEquals(188, scaledSizeConfig.getSize(1));
        assertEquals(187, scaledSizeConfig.getSize(2));
        assertEquals(563, scaledSizeConfig.getAggregateSize(3));
    }

    @Test
    public void setSizeMixedScaled() {
        // use dpi of 144 which will result in a dpi factor of 1.5
        this.sizeConfigMixedMode.setDpiConverter(new FixedScalingDpiConverter(144));
        this.sizeConfigMixedMode.calculatePercentages(750, 3);

        assertEquals(150, this.sizeConfigMixedMode.getSize(0));
        assertEquals(150, this.sizeConfigMixedMode.getSize(1));
        assertEquals(450, this.sizeConfigMixedMode.getSize(2));

        // resize position 0 to 200
        this.sizeConfigMixedMode.setSize(0, 200);

        // the position itself needs to be 200
        assertEquals(300, this.sizeConfigMixedMode.getSize(0));
        assertEquals(150, this.sizeConfigMixedMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(750, this.sizeConfigMixedMode.getAggregateSize(3));

        // resize position 1 to 200
        this.sizeConfigMixedMode.setSize(1, 200);

        // the position itself needs to be 200
        assertEquals(300, this.sizeConfigMixedMode.getSize(0));
        assertEquals(300, this.sizeConfigMixedMode.getSize(1));
        assertEquals(150, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(750, this.sizeConfigMixedMode.getAggregateSize(3));

        // resize position 2
        this.sizeConfigMixedMode.setSize(2, 500);

        // no changes as last column should take remaining space
        assertEquals(300, this.sizeConfigMixedMode.getSize(0));
        assertEquals(300, this.sizeConfigMixedMode.getSize(1));
        assertEquals(150, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(750, this.sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void setSizeMixedScaled2() {
        // use dpi of 144 which will result in a dpi factor of 1.5
        this.sizeConfigMixedMode.setDpiConverter(new FixedScalingDpiConverter(144));
        this.sizeConfigMixedMode.calculatePercentages(750, 3);

        assertEquals(150, this.sizeConfigMixedMode.getSize(0));
        assertEquals(150, this.sizeConfigMixedMode.getSize(1));
        assertEquals(450, this.sizeConfigMixedMode.getSize(2));

        this.sizeConfigMixedMode.setSize(1, 350);

        assertEquals(150, this.sizeConfigMixedMode.getSize(0));
        assertEquals(525, this.sizeConfigMixedMode.getSize(1));
        assertEquals(75, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(750, this.sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void setSizeMixed2() {

        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(100, this.sizeConfigMixedMode.getSize(1));
        assertEquals(300, this.sizeConfigMixedMode.getSize(2));

        this.sizeConfigMixedMode.setSize(1, 350);

        assertEquals(100, this.sizeConfigMixedMode.getSize(0));
        assertEquals(350, this.sizeConfigMixedMode.getSize(1));
        assertEquals(50, this.sizeConfigMixedMode.getSize(2));

        // as we're in percentage mode, the aggregate size shouldn't have
        // changed
        assertEquals(500, this.sizeConfigMixedMode.getAggregateSize(3));
    }

    @Test
    public void shouldInitiallySetSizeAsPercentage() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.setSize(0, 25);
        sizeConfig.setSize(1, 25);
        sizeConfig.setSize(2, 50);
        sizeConfig.calculatePercentages(1000, 3);

        assertEquals(250, sizeConfig.getSize(0));
        assertEquals(250, sizeConfig.getSize(1));
        assertEquals(500, sizeConfig.getSize(2));
    }

    @Test
    public void shouldInitiallySetSizeAsPercentageAfter() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setSize(0, 25);
        sizeConfig.setSize(1, 25);
        sizeConfig.setSize(2, 50);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.calculatePercentages(1000, 3);

        assertEquals(250, sizeConfig.getSize(0));
        assertEquals(250, sizeConfig.getSize(1));
        assertEquals(500, sizeConfig.getSize(2));
    }

    @Test
    public void shouldKeepConsistentPercentagesOnExceedingSpace() {
        SizeConfig sizeConfig = new SizeConfig(DEFAULT_SIZE);
        sizeConfig.setPercentageSizing(true);
        sizeConfig.calculatePercentages(900, 3);

        assertEquals(300, sizeConfig.getSize(0));
        assertEquals(300, sizeConfig.getSize(1));
        assertEquals(300, sizeConfig.getSize(2));

        // resize column 0 to be larger than available
        sizeConfig.setSize(0, 1000);

        assertEquals(882, sizeConfig.getSize(0));
        assertEquals(9, sizeConfig.getSize(1));
        assertEquals(9, sizeConfig.getSize(2));

        // resize column 0 to 300
        sizeConfig.setSize(0, 300);

        assertEquals(300, sizeConfig.getSize(0));
        assertEquals(591, sizeConfig.getSize(1));
        assertEquals(9, sizeConfig.getSize(2));

        // resize column 1 to 300
        sizeConfig.setSize(1, 300);

        assertEquals(301, sizeConfig.getSize(0));
        assertEquals(300, sizeConfig.getSize(1));
        assertEquals(299, sizeConfig.getSize(2));

        // increase column 1 by 9 pixels (1%)
        sizeConfig.setSize(1, 309);

        assertEquals(301, sizeConfig.getSize(0));
        assertEquals(309, sizeConfig.getSize(1));
        assertEquals(290, sizeConfig.getSize(2));
    }
}
