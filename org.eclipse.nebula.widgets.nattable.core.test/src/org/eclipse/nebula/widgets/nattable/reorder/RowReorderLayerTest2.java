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
package org.eclipse.nebula.widgets.nattable.reorder;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowReorderLayerTest2 {

	private RowReorderLayer reorderLayer;

	@Before
	public void setup(){
		TestLayer dataLayer =
			new TestLayer(
			              4, 4,
			              "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
			              "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
			              "A0 | B0 | C0 | D0 \n" +
			              "A1 | B1 | C1 | D1 \n" +
			              "A2 | B2 | C2 | D2 \n" +
			              "A3 | B3 | C3 | D3 \n"
			);
		
		reorderLayer = new RowReorderLayer(dataLayer);
	}
	
	@Test
	public void shouldLoadstateFromProperties() throws Exception {
		LayerListenerFixture listener = new LayerListenerFixture();
		reorderLayer.addLayerListener(listener);

		Properties testProperties = new Properties();
		testProperties.put(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER, "0,1,3,2,");
		
		reorderLayer.loadState("", testProperties);
		
		Assert.assertEquals(0, reorderLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, reorderLayer.getRowIndexByPosition(1));
		Assert.assertEquals(3, reorderLayer.getRowIndexByPosition(2));
		Assert.assertEquals(2, reorderLayer.getRowIndexByPosition(3));
		
		Assert.assertTrue(listener.containsInstanceOf(RowStructuralRefreshEvent.class));
	}
	
	@Test
	public void skipLoadingStateIfPersistedStateDoesNotMatchDataSource() throws Exception {
		Properties testProperties = new Properties();
		
		// Index 5 is valid
		testProperties.put(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER, "0,1,5,2,");
		reorderLayer.loadState("", testProperties);
		
		// Ordering unchanged
		Assert.assertEquals(0, reorderLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, reorderLayer.getRowIndexByPosition(1));
		Assert.assertEquals(2, reorderLayer.getRowIndexByPosition(2));
		Assert.assertEquals(3, reorderLayer.getRowIndexByPosition(3));

		// Number of columns is different
		testProperties.put(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER, "2,1,0,");
		reorderLayer.loadState("", testProperties);
		
		// Ordering unchanged
		Assert.assertEquals(0, reorderLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, reorderLayer.getRowIndexByPosition(1));
		Assert.assertEquals(2, reorderLayer.getRowIndexByPosition(2));
		Assert.assertEquals(3, reorderLayer.getRowIndexByPosition(3));
	}
}
