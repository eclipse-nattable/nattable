/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowHideShowLayerPersistenceTest {

	private RowHideShowLayer layer;
	
	@Before
	public void setup() {
		layer = new RowHideShowLayer(new DataLayer(new DummyBodyDataProvider(10, 10)) {
			
			@Override
			public void saveState(String prefix, Properties properties) {
				// Do nothing
			}
			
			@Override
			public void loadState(String prefix, Properties properties) {
				// Do nothing
			}
			
		});
	}
	
	@Test
	public void testSaveState() {
		layer.hideRowPositions(Arrays.asList(new Integer[] { 3, 5, 6 }));
		
		Properties properties = new Properties();
		layer.saveState("prefix", properties);
		
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals("3,5,6,", properties.getProperty("prefix" + RowHideShowLayer.PERSISTENCE_KEY_HIDDEN_ROW_INDEXES));
	}
	
	@Test
	public void testLoadState() {
		Properties properties = new Properties();
		properties.setProperty("prefix" + RowHideShowLayer.PERSISTENCE_KEY_HIDDEN_ROW_INDEXES, "1,3,5,");
		
		layer.loadState("prefix", properties);
		
		Assert.assertEquals(7, layer.getRowCount());
		
		Assert.assertEquals(0, layer.getRowIndexByPosition(0));
		Assert.assertEquals(2, layer.getRowIndexByPosition(1));
		Assert.assertEquals(4, layer.getRowIndexByPosition(2));
		Assert.assertEquals(6, layer.getRowIndexByPosition(3));
		Assert.assertEquals(7, layer.getRowIndexByPosition(4));
		Assert.assertEquals(8, layer.getRowIndexByPosition(5));
		Assert.assertEquals(9, layer.getRowIndexByPosition(6));
	}
}
