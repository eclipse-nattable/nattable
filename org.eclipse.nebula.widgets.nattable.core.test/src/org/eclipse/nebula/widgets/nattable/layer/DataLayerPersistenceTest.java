/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataLayerPersistenceTest {

	private DataLayer dataLayer;
	
	@Before
	public void setup() {
		dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
	}
	
	@Test
	public void testSaveState() {
		dataLayer.setColumnWidthByPosition(5, 10);
		dataLayer.setColumnPercentageSizing(true);
		
		Properties properties = new Properties();
		dataLayer.saveState("prefix", properties);
		
		Assert.assertEquals(7, properties.size());
		Assert.assertEquals("100", properties.getProperty("prefix.columnWidth.defaultSize"));
		Assert.assertEquals("5:10,", properties.getProperty("prefix.columnWidth.sizes"));
		Assert.assertTrue(Boolean.valueOf(properties.getProperty("prefix.columnWidth.resizableByDefault")));
		Assert.assertEquals("20", properties.getProperty("prefix.rowHeight.defaultSize"));
		Assert.assertTrue(Boolean.valueOf(properties.getProperty("prefix.rowHeight.resizableByDefault")));
		Assert.assertTrue(Boolean.valueOf(properties.getProperty("prefix.columnWidth.percentageSizing")));
		Assert.assertFalse(Boolean.valueOf(properties.getProperty("prefix.rowHeight.percentageSizing")));
	}
	
	@Test
	public void testLoadState() {
		LayerListenerFixture listener = new LayerListenerFixture();
		dataLayer.addLayerListener(listener);
		
		Properties properties = new Properties();
		properties.setProperty("prefix.columnWidth.defaultSize", "80");
		properties.setProperty("prefix.columnWidth.resizableByDefault", "false");
		properties.setProperty("prefix.columnWidth.percentageSizing", "true");
		properties.setProperty("prefix.rowHeight.defaultSize", "70");
		properties.setProperty("prefix.rowHeight.resizableByDefault", "true");
		
		dataLayer.loadState("prefix", properties);
		
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(0));
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(1));
		
		Assert.assertFalse(dataLayer.isColumnPositionResizable(0));
		Assert.assertFalse(dataLayer.isColumnPositionResizable(1));
		
		Assert.assertEquals(70, dataLayer.getRowHeightByPosition(0));
		Assert.assertEquals(70, dataLayer.getRowHeightByPosition(1));
		
		Assert.assertTrue(dataLayer.isRowPositionResizable(0));
		Assert.assertTrue(dataLayer.isRowPositionResizable(1));
		
		Assert.assertTrue(dataLayer.isColumnPercentageSizing());
		Assert.assertFalse(dataLayer.isRowPercentageSizing());
	}
	
}