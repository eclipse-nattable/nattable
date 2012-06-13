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
	private SizeConfig sizeConfigMixedMode;

	@Before
	public void setup(){
		sizeConfigCalculationMode = new SizeConfig(DEFAULT_SIZE);
		sizeConfigCalculationMode.setPercentageSizing(true);
		sizeConfigCalculationMode.calculatePercentages(1000, 10);

		sizeConfigFixedMode = new SizeConfig(DEFAULT_SIZE);
		sizeConfigFixedMode.setPercentageSizing(true);
		sizeConfigFixedMode.setPercentage(0, 50);
		sizeConfigFixedMode.setPercentage(1, 50);
		sizeConfigFixedMode.calculatePercentages(255, 2);

		sizeConfigMixedMode = new SizeConfig(DEFAULT_SIZE);
		sizeConfigMixedMode.setPercentageSizing(true);
		sizeConfigMixedMode.setPercentage(0, 30);
		sizeConfigMixedMode.setPercentage(2, 30);
		sizeConfigMixedMode.calculatePercentages(1000, 3);
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
		Assert.assertEquals(1000, sizeConfigCalculationMode.getAggregateSize(10));
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
	public void getAggregateSizeWithSizeOverridesCalculationMode() throws Exception {
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
		Assert.assertEquals(1000, sizeConfigCalculationMode.getAggregateSize(10));
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
		Assert.assertEquals(1000, sizeConfigCalculationMode.getAggregateSize(20));
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
		sizeConfigFixedMode.calculatePercentages(255, 3);
		sizeConfigFixedMode.setPercentage(2, 50);

		Assert.assertEquals(127, sizeConfigFixedMode.getSize(0));
		Assert.assertEquals(127, sizeConfigFixedMode.getSize(1));
		Assert.assertEquals(127, sizeConfigFixedMode.getSize(2));
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
		sizeConfigFixedMode.calculatePercentages(255, 3);
		sizeConfigFixedMode.setPercentage(2, 50);

		Assert.assertEquals(127, sizeConfigFixedMode.getAggregateSize(1));
		Assert.assertEquals(254, sizeConfigFixedMode.getAggregateSize(2));
		Assert.assertEquals(381, sizeConfigFixedMode.getAggregateSize(3));
	}


	@Test
	public void getSizeConfigMixedMode() throws Exception {
		Assert.assertEquals(300, sizeConfigMixedMode.getSize(0));
		Assert.assertEquals(400, sizeConfigMixedMode.getSize(1));
		Assert.assertEquals(300, sizeConfigMixedMode.getSize(2));
	}

	@Test
	public void getAggregateSizeConfigMixedMode() throws Exception {
		Assert.assertEquals(300, sizeConfigMixedMode.getAggregateSize(1));
		Assert.assertEquals(700, sizeConfigMixedMode.getAggregateSize(2));
		Assert.assertEquals(1000, sizeConfigMixedMode.getAggregateSize(3));
	}

	@Test
	public void sizeOverrideMixedMode() throws Exception {
		sizeConfigMixedMode.setSize(2, 400);

		Assert.assertEquals(300, sizeConfigMixedMode.getSize(0));
		Assert.assertEquals(300, sizeConfigMixedMode.getSize(1));
		Assert.assertEquals(400, sizeConfigMixedMode.getSize(2));
	}

	@Test
	public void percentageOverrideMixedMode() throws Exception {
		sizeConfigMixedMode.setPercentage(2, 40);

		Assert.assertEquals(300, sizeConfigMixedMode.getSize(0));
		Assert.assertEquals(300, sizeConfigMixedMode.getSize(1));
		Assert.assertEquals(400, sizeConfigMixedMode.getSize(2));
	}

	@Test
	public void getAggregateSizeWithSizeOverridesMixedMode() throws Exception {
		sizeConfigMixedMode.setPercentage(2, 40);

		Assert.assertEquals(600, sizeConfigMixedMode.getAggregateSize(2));
		Assert.assertEquals(1000, sizeConfigMixedMode.getAggregateSize(3));
	}

	@Test
	public void getSizeMixedModeAfterAdding() throws Exception {
		sizeConfigMixedMode.calculatePercentages(1000, 4);

		Assert.assertEquals(300, sizeConfigMixedMode.getSize(0));
		Assert.assertEquals(200, sizeConfigMixedMode.getSize(1));
		Assert.assertEquals(300, sizeConfigMixedMode.getSize(2));
		Assert.assertEquals(200, sizeConfigMixedMode.getSize(3));
	}

	@Test
	public void getAggregateSizeMixedModeAfterAdding() throws Exception {
		sizeConfigMixedMode.calculatePercentages(1000, 4);

		Assert.assertEquals(300, sizeConfigMixedMode.getAggregateSize(1));
		Assert.assertEquals(500, sizeConfigMixedMode.getAggregateSize(2));
		Assert.assertEquals(800, sizeConfigMixedMode.getAggregateSize(3));
		Assert.assertEquals(1000, sizeConfigMixedMode.getAggregateSize(4));
	}
}
