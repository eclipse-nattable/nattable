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
package org.eclipse.nebula.widgets.nattable.test.performance;


import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.junit.Test;

public class ElementalLayerPerformanceTest extends AbstractLayerPerformanceTest {

	@Test
	public void testNormalDataLayerPerformance() {
		layer = new DataLayer(new DummyBodyDataProvider(10, 50));
	}

	@Test
	public void testBigDataLayerPerformance() {
		layer = new DataLayer(new DummyBodyDataProvider(50, 100));
		setExpectedTimeInMillis(250);
	}
	
	@Test
	public void testReorderDataLayerPerformance() {
		layer = new ColumnReorderLayer(new DataLayer(new DummyBodyDataProvider(10, 50)));
	}
	
	@Test
	public void testHideShowDataLayerPerformance() {
		layer = new ColumnHideShowLayer(new DataLayer(new DummyBodyDataProvider(10, 50)));
	}
	
	@Test
	public void testSelectionDataLayerPerformance() {
		layer = new SelectionLayer(new DataLayer(new DummyBodyDataProvider(10, 50)));
	}
	
	@Test
	public void testCompositeDataLayerPerformance() {
		CompositeLayer compositeLayer = new CompositeLayer(1, 1);
		compositeLayer.setChildLayer(GridRegion.BODY, new DataLayer(new DummyBodyDataProvider(10, 50)), 0, 0);
		
		layer = compositeLayer;
	}
	
}
