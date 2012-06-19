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

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SizeConfigPersistenceTest {

	private static final int DEFAULT_SIZE = 100;
	private SizeConfig sizeConfig;

	@Before
	public void setup() {
		sizeConfig = new SizeConfig(DEFAULT_SIZE);
	}

	@Test
	public void testSaveState() {
		sizeConfig.setDefaultSize(5, 50);
		sizeConfig.setDefaultSize(6, 60);

		sizeConfig.setSize(5, 25);
		sizeConfig.setSize(2, 88);
		sizeConfig.setSize(4, 57);

		sizeConfig.setResizableByDefault(false);

		sizeConfig.setPositionResizable(3, true);
		sizeConfig.setPositionResizable(9, true);

		Properties properties = new Properties();
		sizeConfig.saveState("prefix", properties);

		Assert.assertEquals(6, properties.size());
		Assert.assertEquals("100", properties.getProperty("prefix.defaultSize"));
		Assert.assertEquals("5:50,6:60,", properties.getProperty("prefix.defaultSizes"));
		Assert.assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
		Assert.assertFalse(Boolean.valueOf(properties.getProperty("prefix.resizableByDefault")));
		Assert.assertEquals("3:true,9:true,", properties.getProperty("prefix.resizableIndexes"));
		Assert.assertFalse(Boolean.valueOf(properties.getProperty("prefix.percentageSizing")));
	}

	@Test
	public void testLoadState() {
		Properties properties = new Properties();
		properties.setProperty("prefix.defaultSize", "40");
		properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
		properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
		properties.setProperty("prefix.resizableByDefault", "true");
		properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");

		sizeConfig.loadState("prefix", properties);

		Assert.assertEquals(40, sizeConfig.getSize(0));
		Assert.assertEquals(100, sizeConfig.getSize(1));
		Assert.assertEquals(20, sizeConfig.getSize(2));
		Assert.assertEquals(30, sizeConfig.getSize(3));
		Assert.assertEquals(400, sizeConfig.getSize(4));
		Assert.assertEquals(500, sizeConfig.getSize(5));
		Assert.assertEquals(40, sizeConfig.getSize(6));

		Assert.assertTrue(sizeConfig.isPositionResizable(0));
		Assert.assertFalse(sizeConfig.isPositionResizable(1));
		Assert.assertTrue(sizeConfig.isPositionResizable(2));
		Assert.assertTrue(sizeConfig.isPositionResizable(3));
		Assert.assertTrue(sizeConfig.isPositionResizable(4));
		Assert.assertTrue(sizeConfig.isPositionResizable(5));
		Assert.assertFalse(sizeConfig.isPositionResizable(6));
	}

	@Test
	public void testLoadStatePercentageSizing() {
		Properties properties = new Properties();
		properties.setProperty("prefix.defaultSize", "40");
		properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
		properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
		properties.setProperty("prefix.resizableByDefault", "true");
		properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");
		properties.setProperty("prefix.percentageSizing", "true");

		sizeConfig.loadState("prefix", properties);

		Assert.assertTrue(sizeConfig.isResizableByDefault());
		Assert.assertTrue(sizeConfig.isPercentageSizing());
	}
	
	@Test
	public void loadStateFromEmptyPropertiesObject() throws Exception {
		Properties properties = new Properties();
		sizeConfig.loadState("prefix", properties);

		Assert.assertTrue(sizeConfig.isResizableByDefault());
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(0));
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(1));
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(2));
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(3));
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(4));
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(5));
		Assert.assertEquals(DEFAULT_SIZE, sizeConfig.getSize(6));
	}

}