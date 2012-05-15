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
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import java.util.Arrays;


import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseColumnHideShowLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("boxing")
public class HideShowColumnEventTest {
	
	private LayerListenerFixture layerListener;
	private BaseColumnHideShowLayerFixture hideShowLayer;
	
	@Before
	public void setUp() {
		hideShowLayer = new BaseColumnHideShowLayerFixture(new DataLayerFixture(100, 40));
		layerListener = new LayerListenerFixture();
	}
	
	@Test
	public void willHideColumnShouldThrowsHideShowEvent() {
		hideShowLayer.addLayerListener(layerListener);
		hideShowLayer.hideColumnPositions(Arrays.asList(1, 4));
		
		Assert.assertTrue(hideShowLayer.isColumnIndexHidden(1));
		Assert.assertTrue(hideShowLayer.isColumnIndexHidden(4));
	}
	
	@Test
	public void willShowColumnShouldThrowsHideShowEvent() {
		hideShowLayer.hideColumnPositions(Arrays.asList(1, 4));
		hideShowLayer.addLayerListener(layerListener);
		
		Assert.assertTrue(hideShowLayer.isColumnIndexHidden(1));
		Assert.assertTrue(hideShowLayer.isColumnIndexHidden(4));
		
		hideShowLayer.showColumnIndexes(Arrays.asList(1, 4));
		Assert.assertFalse(hideShowLayer.isColumnIndexHidden(1));
		Assert.assertFalse(hideShowLayer.isColumnIndexHidden(4));
	}
	
	@Test
	public void willShowAllColumnsThrowsHideShowEvent() {
		hideShowLayer.hideColumnPositions(Arrays.asList(1, 4));
		hideShowLayer.addLayerListener(layerListener);
		
		Assert.assertTrue(hideShowLayer.isColumnIndexHidden(1));
		Assert.assertTrue(hideShowLayer.isColumnIndexHidden(4));
		
		hideShowLayer.showAllColumns();
		Assert.assertFalse(hideShowLayer.isColumnIndexHidden(1));
		Assert.assertFalse(hideShowLayer.isColumnIndexHidden(4));
	}
}
