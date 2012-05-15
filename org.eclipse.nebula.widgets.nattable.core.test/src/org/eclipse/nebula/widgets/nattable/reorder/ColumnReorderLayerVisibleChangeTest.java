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
package org.eclipse.nebula.widgets.nattable.reorder;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

public class ColumnReorderLayerVisibleChangeTest {
	
	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		10	11	12	13 ... 20
	 *          --------------------
	 *  Position 	0 	1	2	3 ... 20
	 */
	public void returnsCorrectPositionRectangleForMultiColumnReorderLeftCase() {
		ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(20, 20));
		
		// Build expected cell positions to redraw
		final Set<Rectangle> expectedPositions = new HashSet<Rectangle>();
		expectedPositions.add(new Rectangle(0, 0, 20, 20));
		
		reorderLayer.addLayerListener(new ILayerListener() {
			public void handleLayerEvent(ILayerEvent event) {
				ColumnReorderEvent multiReorder = (ColumnReorderEvent) event;
				assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
			}
		});
		
		// Reorder to beginning of grid
		List<Integer> fromColumnPositions = Arrays.asList(new Integer[]{10, 11, 12, 13});		
		reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 0);
		
		// Reorder to middle of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(10, 0, 10, 20));		
		fromColumnPositions = Arrays.asList(new Integer[]{19, 18, 17, 16});
		reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 10);
		
		// Reorder to end of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(5, 0, 15, 20));		
		fromColumnPositions = Arrays.asList(new Integer[]{5, 6, 7, 8});		
		reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 10);
	}
	
	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		2	3	0	1 ... 20
	 *          --------------------
	 *  Position 	0 	1	2	3 ... 20
	 */
	public void returnsCorrectPositionRectangleForMultiColumnReorderRightCase() {
		ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(20, 20));
		
		// Build expected cell positions to redraw
		final Set<Rectangle> expectedPositions = new HashSet<Rectangle>();
		expectedPositions.add(new Rectangle(0, 0, 20, 20));
		
		
		reorderLayer.addLayerListener(new ILayerListener() {
			public void handleLayerEvent(ILayerEvent event) {
				ColumnReorderEvent multiReorder = (ColumnReorderEvent)event;
				assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
			}
		});
		
		// Reorder from beginning of grid
		List<Integer> fromColumnPositions = Arrays.asList(new Integer[]{0, 1});
		reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 2);
		
		// Reorder to middle of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(5, 0, 15, 20));		
		fromColumnPositions = Arrays.asList(new Integer[]{5, 6, 7, 8});		
		reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 10);
		
		// Reorder to end of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(10, 0, 10, 20));		
		fromColumnPositions = Arrays.asList(new Integer[]{10, 11, 12, 13});		
		reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 19);
	}

}
