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
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseRowHideShowLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("boxing")
public class HideShowRowEventTest {
	
	private LayerListenerFixture layerListener;
	private BaseRowHideShowLayerFixture hideShowLayer;
	
	@Before
	public void setUp() {
		hideShowLayer = new BaseRowHideShowLayerFixture(new DataLayerFixture(100, 40));
		layerListener = new LayerListenerFixture();
	}
	
	@Test
	public void willHideColumnShouldThrowsHideShowEvent() {
		hideShowLayer.addLayerListener(layerListener);
		hideShowLayer.hideRowPositions(Arrays.asList(1, 4));
		
		Assert.assertTrue(hideShowLayer.isRowIndexHidden(1));
		Assert.assertTrue(hideShowLayer.isRowIndexHidden(4));
	}
	
	@Test
	public void willShowColumnShouldThrowsHideShowEvent() {
		hideShowLayer.hideRowPositions(Arrays.asList(1, 4));
		hideShowLayer.addLayerListener(layerListener);
		
		Assert.assertTrue(hideShowLayer.isRowIndexHidden(1));
		Assert.assertTrue(hideShowLayer.isRowIndexHidden(4));
		
		hideShowLayer.showRowIndexes(Arrays.asList(1, 4));
		Assert.assertFalse(hideShowLayer.isRowIndexHidden(1));
		Assert.assertFalse(hideShowLayer.isRowIndexHidden(4));
	}
	
	@Test
	public void willShowAllColumnsThrowsHideShowEvent() {
		hideShowLayer.hideRowPositions(Arrays.asList(1, 4));
		hideShowLayer.addLayerListener(layerListener);
		
		Assert.assertTrue(hideShowLayer.isRowIndexHidden(1));
		Assert.assertTrue(hideShowLayer.isRowIndexHidden(4));
		
		hideShowLayer.showAllRows();
		Assert.assertFalse(hideShowLayer.isRowIndexHidden(1));
		Assert.assertFalse(hideShowLayer.isRowIndexHidden(4));
	}
}
