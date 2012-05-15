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

import java.util.Arrays;


import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.Test;

public class ElementalViewportLayerPerformanceTest extends AbstractLayerPerformanceTest {

	@Test
	public void testViewportDataLayerPerformance() {
		layer = new ViewportLayer(new DataLayer(new DummyBodyDataProvider(1000000, 1000000)));
	}
	
	@Test
	public void testViewportReorderDataLayerPerformance() {
		ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new DataLayer(new DummyBodyDataProvider(1000000, 1000000)));
		reorderLayer.reorderColumnPosition(1, 2);
		layer = new ViewportLayer(reorderLayer);
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void testViewportHideShowDataLayerPerformance() {
		ColumnHideShowLayer hideShowLayer = new ColumnHideShowLayer(new DataLayer(new DummyBodyDataProvider(1000000, 1000000)));
		hideShowLayer.hideColumnPositions(Arrays.asList(new Integer[] { 3, 5 }));
		layer = new ViewportLayer(hideShowLayer);
	}
	
	@Test
	public void testViewportSelectionDataLayerPerformance() {
		layer = new ViewportLayer(new SelectionLayer(new DataLayer(new DummyBodyDataProvider(1000000, 1000000))));
	}
	
	@Test
	public void testCompositeViewportDataLayerPerformance() {
		CompositeLayer compositeLayer = new CompositeLayer(1, 1);
		compositeLayer.setChildLayer(GridRegion.BODY, new ViewportLayer(new DataLayer(new DummyBodyDataProvider(1000000, 1000000))), 0, 0);
		
		layer = compositeLayer;
	}
	
}
