/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SizeConfigResizeTest {

    private static final int DEFAULT_SIZE = 100;
    private SizeConfig sizeConfig;
    private SizeConfig defaultMinSizeConfig;
    private SizeConfig noDynamicPercentageSizeConfig;

    @Before
    public void setup() {
        this.sizeConfig = new SizeConfig(DEFAULT_SIZE);

        this.sizeConfig.setPercentageSizing(true);
        this.sizeConfig.setDistributeRemainingSpace(true);

        this.sizeConfig.setPercentageSizing(0, false);
        this.sizeConfig.setSize(0, 70);

        this.sizeConfig.setPercentageSizing(1, false);
        this.sizeConfig.setSize(1, 76);

        this.sizeConfig.setPercentage(3, 5d);

        this.sizeConfig.setPercentage(4, 8d);
        this.sizeConfig.setMinSize(4, 90);

        this.sizeConfig.setPercentage(6, 7d);
        this.sizeConfig.setMinSize(6, 70);

        this.sizeConfig.setPercentage(7, 5d);
        this.sizeConfig.setMinSize(7, 49);

        this.sizeConfig.setPercentageSizing(8, false);
        this.sizeConfig.setSize(8, 135);

        this.sizeConfig.setPercentage(10, 13d);
        this.sizeConfig.setMinSize(10, 200);

        this.sizeConfig.setPercentage(12, 5d);

        this.sizeConfig.calculatePercentages(1500, 13);

        // {0=70, 1=76, 2=165, 3=60, 4=97, 5=164, 6=85,
        // 7=60, 8=135, 9=164, 10=200, 11=164, 12=60}

        this.defaultMinSizeConfig = new SizeConfig(DEFAULT_SIZE);

        this.defaultMinSizeConfig.setPercentageSizing(true);
        this.defaultMinSizeConfig.setDistributeRemainingSpace(true);
        this.defaultMinSizeConfig.setDefaultMinSize(25);

        this.defaultMinSizeConfig.setPercentageSizing(0, false);
        this.defaultMinSizeConfig.setSize(0, 70);

        this.defaultMinSizeConfig.setPercentageSizing(1, false);
        this.defaultMinSizeConfig.setSize(1, 76);

        this.defaultMinSizeConfig.setPercentage(3, 5d);

        this.defaultMinSizeConfig.setPercentage(4, 8d);
        this.defaultMinSizeConfig.setMinSize(4, 90);

        this.defaultMinSizeConfig.setPercentage(6, 7d);
        this.defaultMinSizeConfig.setMinSize(6, 70);

        this.defaultMinSizeConfig.setPercentage(7, 5d);
        this.defaultMinSizeConfig.setMinSize(7, 49);

        this.defaultMinSizeConfig.setPercentageSizing(8, false);
        this.defaultMinSizeConfig.setSize(8, 135);

        this.defaultMinSizeConfig.setPercentage(10, 13d);
        this.defaultMinSizeConfig.setMinSize(10, 200);

        this.defaultMinSizeConfig.setPercentage(12, 5d);

        this.defaultMinSizeConfig.calculatePercentages(1500, 13);

        // {0=70, 1=76, 2=165, 3=60, 4=97, 5=164, 6=85,
        // 7=60, 8=135, 9=164, 10=200, 11=164, 12=60}

        this.noDynamicPercentageSizeConfig = new SizeConfig(DEFAULT_SIZE);

        this.noDynamicPercentageSizeConfig.setPercentageSizing(true);
        this.noDynamicPercentageSizeConfig.setDistributeRemainingSpace(true);
        this.noDynamicPercentageSizeConfig.setDefaultMinSize(25);

        this.noDynamicPercentageSizeConfig.setPercentageSizing(0, false);
        this.noDynamicPercentageSizeConfig.setSize(0, 70);

        this.noDynamicPercentageSizeConfig.setPercentageSizing(1, false);
        this.noDynamicPercentageSizeConfig.setSize(1, 76);

        this.noDynamicPercentageSizeConfig.setPercentage(2, 11d);

        this.noDynamicPercentageSizeConfig.setPercentage(3, 5d);

        this.noDynamicPercentageSizeConfig.setPercentage(4, 8d);
        this.noDynamicPercentageSizeConfig.setMinSize(4, 90);

        this.noDynamicPercentageSizeConfig.setPercentage(5, 10d);

        this.noDynamicPercentageSizeConfig.setPercentage(6, 7d);
        this.noDynamicPercentageSizeConfig.setMinSize(6, 70);

        this.noDynamicPercentageSizeConfig.setPercentage(7, 5d);
        this.noDynamicPercentageSizeConfig.setMinSize(7, 49);

        this.noDynamicPercentageSizeConfig.setPercentageSizing(8, false);
        this.noDynamicPercentageSizeConfig.setSize(8, 135);

        this.noDynamicPercentageSizeConfig.setPercentage(9, 10d);

        this.noDynamicPercentageSizeConfig.setPercentage(10, 13d);
        this.noDynamicPercentageSizeConfig.setMinSize(10, 200);

        this.noDynamicPercentageSizeConfig.setPercentage(11, 10d);

        this.noDynamicPercentageSizeConfig.setPercentage(12, 5d);

        this.noDynamicPercentageSizeConfig.calculatePercentages(1500, 13);

        // {0=70, 1=76, 2=158, 3=72, 4=115, 5=143, 6=101,
        // 7=72, 8=135, 9=143, 10=200, 11=143, 12=72}
    }

    // {0=70, 1=76, 2=165, 3=60, 4=97, 5=164, 6=85,
    // 7=60, 8=135, 9=164, 10=200, 11=164, 12=60}

    @Test
    public void shouldIncreaseDynamicPercentageColumnSmallAmount() {
        assertEquals(165, this.sizeConfig.getSize(2));
        assertEquals(60, this.sizeConfig.getSize(3));

        this.sizeConfig.setSize(2, 170);

        // column 2 and 3 should have changed
        assertEquals(170, this.sizeConfig.getSize(2));
        assertEquals(55, this.sizeConfig.getSize(3));

        assertEquals(1500, this.sizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseDynamicPercentageColumnSmallAmount() {
        assertEquals(165, this.sizeConfig.getSize(2));
        assertEquals(60, this.sizeConfig.getSize(3));

        this.sizeConfig.setSize(2, 160);

        // column 2 and 3 should have changed
        assertEquals(160, this.sizeConfig.getSize(2));
        assertEquals(65, this.sizeConfig.getSize(3));

        assertEquals(1500, this.sizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseDynamicPercentageColumnHalfOfAdjacent() {
        assertEquals(165, this.sizeConfig.getSize(2));
        assertEquals(60, this.sizeConfig.getSize(3));

        this.sizeConfig.setSize(2, 195);

        // column 2 and 3 should have changed
        assertEquals(195, this.sizeConfig.getSize(2));
        assertEquals(30, this.sizeConfig.getSize(3));

        assertEquals(2.5, this.sizeConfig.getConfiguredPercentageSize(3), 0.1);

        assertEquals(1500, this.sizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseFixedPercentageColumnAndHandleMinSizeOfAdjacent() {
        assertEquals(60, this.sizeConfig.getSize(3));
        assertEquals(97, this.sizeConfig.getSize(4));
        assertEquals(164, this.sizeConfig.getSize(5));

        assertEquals(13d, this.sizeConfig.getConfiguredPercentageSize(10), 0.1);

        assertEquals(70, this.sizeConfig.getSize(0));
        assertEquals(76, this.sizeConfig.getSize(1));

        this.sizeConfig.setSize(3, 90);

        assertEquals(1500, this.sizeConfig.getAggregateSize(13));

        assertEquals(90, this.sizeConfig.getSize(3));
        // 90 because of min size
        assertEquals(90, this.sizeConfig.getSize(4));
        // should be 141 because the next dynamic column and we reduce by 23
        // but there seems to be some rounding issue on the way therefore 142
        assertEquals(142, this.sizeConfig.getSize(5));
        assertEquals(85, this.sizeConfig.getSize(6));
        assertEquals(60, this.sizeConfig.getSize(7));
        assertEquals(135, this.sizeConfig.getSize(8));
        assertEquals(164, this.sizeConfig.getSize(9));
        assertEquals(200, this.sizeConfig.getSize(10));
        assertEquals(164, this.sizeConfig.getSize(11));
        assertEquals(59, this.sizeConfig.getSize(12));

        // percentage adjusted to match the current active min size
        assertEquals(16.4, this.sizeConfig.getConfiguredPercentageSize(10), 0.1);
    }

    // default min size configuration
    // {0=70, 1=76, 2=165, 3=60, 4=97, 5=164, 6=85,
    // 7=60, 8=135, 9=164, 10=200, 11=164, 12=60}

    @Test
    public void shouldDecreaseColumn4WithMinWidthSmallAmount() {
        assertEquals(60, this.defaultMinSizeConfig.getSize(3));
        assertEquals(97, this.defaultMinSizeConfig.getSize(4));

        this.defaultMinSizeConfig.setSize(3, 55);

        assertEquals(55, this.defaultMinSizeConfig.getSize(3));

        // rounding issue when calculating the pixels for fixed percentage
        // 5% of 1219 is 60.95 which resolves to 60 pixels in simple integer
        // conversion
        // but 60 of 1219 is 4.922%
        // therefore we loose a 1 pixel precision
        assertEquals(103, this.defaultMinSizeConfig.getSize(4));
        assertEquals(59, this.defaultMinSizeConfig.getSize(12));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn4WithMinWidthBigAmount() {
        assertEquals(60, this.defaultMinSizeConfig.getSize(3));
        assertEquals(97, this.defaultMinSizeConfig.getSize(4));

        this.defaultMinSizeConfig.setSize(3, 30);

        assertEquals(30, this.defaultMinSizeConfig.getSize(3));

        // some rounding issue
        assertEquals(128, this.defaultMinSizeConfig.getSize(4));
        assertEquals(59, this.defaultMinSizeConfig.getSize(12));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn4WithMinWidthSmallAmount() {
        assertEquals(60, this.defaultMinSizeConfig.getSize(3));
        assertEquals(97, this.defaultMinSizeConfig.getSize(4));

        this.defaultMinSizeConfig.setSize(3, 65);

        assertEquals(65, this.defaultMinSizeConfig.getSize(3));

        // some rounding issue
        assertEquals(93, this.defaultMinSizeConfig.getSize(4));
        assertEquals(59, this.defaultMinSizeConfig.getSize(12));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn4WithMinWidthBigAmount() {
        assertEquals(60, this.defaultMinSizeConfig.getSize(3));
        assertEquals(97, this.defaultMinSizeConfig.getSize(4));
        assertEquals(164, this.defaultMinSizeConfig.getSize(5));

        this.defaultMinSizeConfig.setSize(3, 120);

        assertEquals(120, this.defaultMinSizeConfig.getSize(3));

        assertEquals(90, this.defaultMinSizeConfig.getSize(4));

        // some rounding issue
        assertEquals(112, this.defaultMinSizeConfig.getSize(5));
        assertEquals(59, this.defaultMinSizeConfig.getSize(12));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn5WithMinWidthSmallAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(5));
        this.defaultMinSizeConfig.setSize(5, 160);

        assertEquals(160, this.defaultMinSizeConfig.getSize(5));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn5WithMinWidthBigAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(5));
        assertEquals(25, this.defaultMinSizeConfig.getMinSize(5));
        this.defaultMinSizeConfig.setSize(5, 10);

        assertEquals(10, this.defaultMinSizeConfig.getSize(5));
        assertEquals(10, this.defaultMinSizeConfig.getMinSize(5));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn5WithMinWidthSmallAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(5));
        this.defaultMinSizeConfig.setSize(5, 170);

        assertEquals(170, this.defaultMinSizeConfig.getSize(5));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn5WithMinWidthBigAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(5));
        this.defaultMinSizeConfig.setSize(5, 180);

        assertEquals(180, this.defaultMinSizeConfig.getSize(5));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn9WithMinWidthSmallAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(9));
        this.defaultMinSizeConfig.setSize(9, 160);

        assertEquals(160, this.defaultMinSizeConfig.getSize(9));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn9WithMinWidthBigAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(9));
        this.defaultMinSizeConfig.setSize(9, 100);

        assertEquals(100, this.defaultMinSizeConfig.getSize(9));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn9WithMinWidthSmallAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(9));
        this.defaultMinSizeConfig.setSize(9, 170);

        assertEquals(170, this.defaultMinSizeConfig.getSize(9));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn9WithMinWidthBigAmount() {
        assertEquals(164, this.defaultMinSizeConfig.getSize(9));
        this.defaultMinSizeConfig.setSize(9, 180);

        assertEquals(180, this.defaultMinSizeConfig.getSize(9));

        assertEquals(1500, this.defaultMinSizeConfig.getAggregateSize(13));
    }

    // no dynamic size config
    // {0=70, 1=76, 2=158, 3=72, 4=115, 5=143, 6=101,
    // 7=72, 8=135, 9=143, 10=200, 11=143, 12=72}

    @Test
    public void shouldDecreaseColumn2WithNoDynamicSmallAmount() {
        assertEquals(158, this.noDynamicPercentageSizeConfig.getSize(2));
        this.noDynamicPercentageSizeConfig.setSize(2, 140);

        // 140 + 1 for distributing missing pixels because of rounding issues
        assertEquals(141, this.noDynamicPercentageSizeConfig.getSize(2));

        // additionally some 1 pixel changes because of rounding issues on
        // double-int - int-double conversion
        assertEquals(90, this.noDynamicPercentageSizeConfig.getSize(3));
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        assertEquals(100, this.noDynamicPercentageSizeConfig.getSize(6));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(7));
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn2WithNoDynamicSmallAmount() {
        assertEquals(158, this.noDynamicPercentageSizeConfig.getSize(2));
        this.noDynamicPercentageSizeConfig.setSize(2, 170);

        // 170 + 1 for distributing missing pixels because of rounding issues
        assertEquals(171, this.noDynamicPercentageSizeConfig.getSize(2));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn2WithNoDynamicBigAmount() {
        assertEquals(158, this.noDynamicPercentageSizeConfig.getSize(2));
        this.noDynamicPercentageSizeConfig.setSize(2, 190);

        // 190 + 1 for distributing missing pixels because of rounding issues
        assertEquals(191, this.noDynamicPercentageSizeConfig.getSize(2));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn3WithNoDynamicSmallAmount() {
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(3));
        this.noDynamicPercentageSizeConfig.setSize(3, 65);

        assertEquals(65, this.noDynamicPercentageSizeConfig.getSize(3));

        // additionally some 1 pixel changes because of rounding issues on
        // double-int - int-double conversion
        assertEquals(122, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        assertEquals(100, this.noDynamicPercentageSizeConfig.getSize(6));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(7));
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn3WithNoDynamicSmallAmount() {
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(3));
        this.noDynamicPercentageSizeConfig.setSize(3, 80);

        assertEquals(80, this.noDynamicPercentageSizeConfig.getSize(3));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn3WithNoDynamicBigAmount() {
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(3));
        this.noDynamicPercentageSizeConfig.setSize(3, 120);

        assertEquals(120, this.noDynamicPercentageSizeConfig.getSize(3));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn4WithNoDynamicSmallAmount() {
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        this.noDynamicPercentageSizeConfig.setSize(4, 110);

        assertEquals(110, this.noDynamicPercentageSizeConfig.getSize(4));

        // additionally some 1 pixel changes because of rounding issues on
        // double-int - int-double conversion
        assertEquals(148, this.noDynamicPercentageSizeConfig.getSize(5));
        assertEquals(100, this.noDynamicPercentageSizeConfig.getSize(6));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(7));
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn4WithNoDynamicBigAmount1() {
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        this.noDynamicPercentageSizeConfig.setSize(4, 95);

        assertEquals(95, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(163, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn4WithNoDynamicBigAmount2() {
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        this.noDynamicPercentageSizeConfig.setSize(4, 90);

        assertEquals(90, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(168, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn4WithNoDynamicBigAmount3() {
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        this.noDynamicPercentageSizeConfig.setSize(4, 50);

        assertEquals(50, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(208, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn4WithNoDynamicSmallAmount() {
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        this.noDynamicPercentageSizeConfig.setSize(4, 120);

        assertEquals(120, this.noDynamicPercentageSizeConfig.getSize(4));

        assertEquals(138, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn4WithNoDynamicBigAmount1() {
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        this.noDynamicPercentageSizeConfig.setSize(4, 140);

        assertEquals(140, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(118, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn5WithNoDynamicSmallAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        this.noDynamicPercentageSizeConfig.setSize(5, 140);

        assertEquals(140, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(104, this.noDynamicPercentageSizeConfig.getSize(6));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn5WithNoDynamicBigAmount1() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        this.noDynamicPercentageSizeConfig.setSize(5, 123);

        assertEquals(123, this.noDynamicPercentageSizeConfig.getSize(5));
        assertEquals(120, this.noDynamicPercentageSizeConfig.getSize(6));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn5WithNoDynamicSmallAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        this.noDynamicPercentageSizeConfig.setSize(5, 135);

        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(5));

        assertEquals(108, this.noDynamicPercentageSizeConfig.getSize(6));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn5WithNoDynamicBigAmount1() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        this.noDynamicPercentageSizeConfig.setSize(5, 180);

        assertEquals(180, this.noDynamicPercentageSizeConfig.getSize(5));
        // simple calculation would say 64, but 70 is min size
        assertEquals(70, this.noDynamicPercentageSizeConfig.getSize(6));
        assertEquals(66, this.noDynamicPercentageSizeConfig.getSize(7));
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn8WithNoDynamicSmallAmount() {
        // no percentage in the middle
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        this.noDynamicPercentageSizeConfig.setSize(8, 130);

        assertEquals(130, this.noDynamicPercentageSizeConfig.getSize(8));

        // some rounding issues while due to double to int conversion while
        // updating the available percentage space
        assertEquals(70, this.noDynamicPercentageSizeConfig.getSize(0));
        assertEquals(76, this.noDynamicPercentageSizeConfig.getSize(1));
        assertEquals(159, this.noDynamicPercentageSizeConfig.getSize(2));
        assertEquals(73, this.noDynamicPercentageSizeConfig.getSize(3));
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(142, this.noDynamicPercentageSizeConfig.getSize(5));
        assertEquals(101, this.noDynamicPercentageSizeConfig.getSize(6));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(7));

        assertEquals(148, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(142, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn8WithNoDynamicBigAmount() {
        // no percentage in the middle
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        this.noDynamicPercentageSizeConfig.setSize(8, 100);

        assertEquals(100, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(178, this.noDynamicPercentageSizeConfig.getSize(9));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn8WithNoDynamicSmallAmount() {
        // no percentage in the middle
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        this.noDynamicPercentageSizeConfig.setSize(8, 140);

        assertEquals(140, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(138, this.noDynamicPercentageSizeConfig.getSize(9));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn8WithNoDynamicBigAmount() {
        // no percentage in the middle
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));
        this.noDynamicPercentageSizeConfig.setSize(8, 170);

        assertEquals(170, this.noDynamicPercentageSizeConfig.getSize(8));
        assertEquals(108, this.noDynamicPercentageSizeConfig.getSize(9));

        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn9WithNoDynamicSmallAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(1085, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        this.noDynamicPercentageSizeConfig.setSize(9, 135);

        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(9));

        // some rounding issues while due to double to int conversion while
        // updating the available percentage space
        assertEquals(70, this.noDynamicPercentageSizeConfig.getSize(0));
        assertEquals(76, this.noDynamicPercentageSizeConfig.getSize(1));
        assertEquals(159, this.noDynamicPercentageSizeConfig.getSize(2));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(3));
        assertEquals(115, this.noDynamicPercentageSizeConfig.getSize(4));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(5));
        assertEquals(100, this.noDynamicPercentageSizeConfig.getSize(6));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(7));
        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(8));

        assertEquals(208, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(72, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1077, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn9WithNoDynamicBigAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(1085, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        this.noDynamicPercentageSizeConfig.setSize(9, 90);

        assertEquals(90, this.noDynamicPercentageSizeConfig.getSize(9));

        assertEquals(253, this.noDynamicPercentageSizeConfig.getSize(10));

        assertEquals(1032, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn9WithNoDynamicSmallAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(1085, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        this.noDynamicPercentageSizeConfig.setSize(9, 145);

        assertEquals(145, this.noDynamicPercentageSizeConfig.getSize(9));

        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(141, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1087, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn9WithNoDynamicBigAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(9));
        assertEquals(1085, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        this.noDynamicPercentageSizeConfig.setSize(9, 160);

        assertEquals(160, this.noDynamicPercentageSizeConfig.getSize(9));

        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(125, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1103, this.noDynamicPercentageSizeConfig.getAggregateSize(10));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn10WithNoDynamicSmallAmount() {
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(1285, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        this.noDynamicPercentageSizeConfig.setSize(10, 190);

        assertEquals(190, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(190, this.noDynamicPercentageSizeConfig.getMinSize(10));

        assertEquals(153, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1275, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn10WithNoDynamicBigAmount() {
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(1285, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        this.noDynamicPercentageSizeConfig.setSize(10, 150);

        assertEquals(150, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(150, this.noDynamicPercentageSizeConfig.getMinSize(10));

        assertEquals(193, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1235, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn10WithNoDynamicSmallAmount() {
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(1285, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        this.noDynamicPercentageSizeConfig.setSize(10, 205);

        // rounding issue in Java
        assertEquals(204, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getMinSize(10));

        assertEquals(138, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1290, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn10WithNoDynamicBigAmount() {
        assertEquals(200, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(1285, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        this.noDynamicPercentageSizeConfig.setSize(10, 225);

        // rounding issue in Java
        assertEquals(224, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getMinSize(10));

        assertEquals(118, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1310, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));

        // increase a second time
        this.noDynamicPercentageSizeConfig.setSize(10, 250);

        // rounding issue in Java
        assertEquals(249, this.noDynamicPercentageSizeConfig.getSize(10));
        assertEquals(200, this.noDynamicPercentageSizeConfig.getMinSize(10));

        assertEquals(93, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(1335, this.noDynamicPercentageSizeConfig.getAggregateSize(11));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn11WithNoDynamicSmallAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(1428, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        this.noDynamicPercentageSizeConfig.setSize(11, 135);

        assertEquals(135, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(80, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1420, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldDecreaseColumn11WithNoDynamicBigAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(1428, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        this.noDynamicPercentageSizeConfig.setSize(11, 100);

        assertEquals(100, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(114, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1386, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn11WithNoDynamicSmallAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(1428, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        this.noDynamicPercentageSizeConfig.setSize(11, 150);

        assertEquals(150, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(65, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1435, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

    @Test
    public void shouldIncreaseColumn11WithNoDynamicBigAmount() {
        assertEquals(143, this.noDynamicPercentageSizeConfig.getSize(11));
        assertEquals(1428, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        this.noDynamicPercentageSizeConfig.setSize(11, 180);

        assertEquals(180, this.noDynamicPercentageSizeConfig.getSize(11));

        assertEquals(35, this.noDynamicPercentageSizeConfig.getSize(12));

        assertEquals(1465, this.noDynamicPercentageSizeConfig.getAggregateSize(12));
        assertEquals(1500, this.noDynamicPercentageSizeConfig.getAggregateSize(13));
    }

}
