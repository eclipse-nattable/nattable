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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

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

		assertEquals(6, properties.size());
		assertEquals("100", properties.getProperty("prefix.defaultSize"));
		assertEquals("5:50,6:60,", properties.getProperty("prefix.defaultSizes"));
		assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
		assertFalse(Boolean.valueOf(properties.getProperty("prefix.resizableByDefault")));
		assertEquals("3:true,9:true,", properties.getProperty("prefix.resizableIndexes"));
		assertFalse(Boolean.valueOf(properties.getProperty("prefix.percentageSizing")));
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

		assertEquals(40, sizeConfig.getSize(0));
		assertEquals(100, sizeConfig.getSize(1));
		assertEquals(20, sizeConfig.getSize(2));
		assertEquals(30, sizeConfig.getSize(3));
		assertEquals(400, sizeConfig.getSize(4));
		assertEquals(500, sizeConfig.getSize(5));
		assertEquals(40, sizeConfig.getSize(6));

		assertTrue(sizeConfig.isPositionResizable(0));
		assertFalse(sizeConfig.isPositionResizable(1));
		assertTrue(sizeConfig.isPositionResizable(2));
		assertTrue(sizeConfig.isPositionResizable(3));
		assertTrue(sizeConfig.isPositionResizable(4));
		assertTrue(sizeConfig.isPositionResizable(5));
		assertFalse(sizeConfig.isPositionResizable(6));
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

		assertTrue(sizeConfig.isResizableByDefault());
		assertTrue(sizeConfig.isPercentageSizing());
	}
	
	@Test
	public void loadStateFromEmptyPropertiesObject() throws Exception {
		Properties properties = new Properties();
		sizeConfig.loadState("prefix", properties);

		assertTrue(sizeConfig.isResizableByDefault());
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(0));
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(1));
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(2));
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(3));
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(4));
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(5));
		assertEquals(DEFAULT_SIZE, sizeConfig.getSize(6));
	}

	@Test
	public void testSaveEnhancedPercentageSizingState() {
		sizeConfig.setDefaultSize(5, 50);
		sizeConfig.setDefaultSize(6, 60);

		sizeConfig.setSize(5, 25);
		sizeConfig.setSize(2, 88);
		sizeConfig.setSize(4, 57);

		sizeConfig.setResizableByDefault(false);

		sizeConfig.setPositionResizable(3, true);
		sizeConfig.setPositionResizable(9, true);

		sizeConfig.setPercentageSizing(3, false);
		sizeConfig.setPercentageSizing(9, false);
		sizeConfig.setPercentageSizing(7, true);
		sizeConfig.setPercentageSizing(8, true);
		
		Properties properties = new Properties();
		sizeConfig.saveState("prefix", properties);

		assertEquals(7, properties.size());
		assertEquals("100", properties.getProperty("prefix.defaultSize"));
		assertEquals("5:50,6:60,", properties.getProperty("prefix.defaultSizes"));
		assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
		assertFalse(Boolean.valueOf(properties.getProperty("prefix.resizableByDefault")));
		assertEquals("3:true,9:true,", properties.getProperty("prefix.resizableIndexes"));
		assertFalse(Boolean.valueOf(properties.getProperty("prefix.percentageSizing")));
		assertEquals("3:false,7:true,8:true,9:false,", properties.getProperty("prefix.percentageSizingIndexes"));
	}

	@Test
	public void testLoadEnhancedPercentageSizingState() {
		Properties properties = new Properties();
		properties.setProperty("prefix.defaultSize", "40");
		properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
		properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
		properties.setProperty("prefix.resizableByDefault", "true");
		properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");
		properties.setProperty("prefix.percentageSizing", "true");
		properties.setProperty("prefix.percentageSizingIndexes", "3:false,7:true,8:true,9:false,");

		sizeConfig.loadState("prefix", properties);

		assertTrue(sizeConfig.isResizableByDefault());
		assertTrue(sizeConfig.isPercentageSizing());
		
		assertFalse(sizeConfig.isPercentageSizing(3));
		assertTrue(sizeConfig.isPercentageSizing(7));
		assertTrue(sizeConfig.isPercentageSizing(8));
		assertFalse(sizeConfig.isPercentageSizing(9));

		assertTrue(sizeConfig.isPercentageSizing(2));
		assertTrue(sizeConfig.isPercentageSizing(6));
	}
}