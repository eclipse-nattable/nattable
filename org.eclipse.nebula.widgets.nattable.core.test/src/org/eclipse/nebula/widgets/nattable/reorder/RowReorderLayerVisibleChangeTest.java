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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

public class RowReorderLayerVisibleChangeTest {
	
	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		10	11	12	13 ... 20
	 *          --------------------
	 *  Position 	0 	1	2	3 ... 20
	 */
	public void returnsCorrectPositionRectangleForMultiColumnReorderLeftCase() {
		RowReorderLayer reorderLayer = new RowReorderLayer(new BaseDataLayerFixture(20, 20));
		
		// Build expected cell positions to redraw
		final Set<Rectangle> expectedPositions = new HashSet<Rectangle>();
		expectedPositions.add(new Rectangle(0, 0, 20, 20));
		
		reorderLayer.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(ILayerEvent event) {
				RowReorderEvent multiReorder = (RowReorderEvent) event;
				assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
			}
		});
		
		// Reorder to beginning of grid
		List<Integer> fromRowPositions = Arrays.asList(new Integer[]{10, 11, 12, 13});		
		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 0);
		
		// Reorder to middle of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(0, 10, 20, 10));		
		fromRowPositions = Arrays.asList(new Integer[]{19, 18, 17, 16});
		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 10);
		
		// Reorder to end of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(0, 5, 20, 15));		
		fromRowPositions = Arrays.asList(new Integer[]{5, 6, 7, 8});		
		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 10);
	}
	
	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		2	3	0	1 ... 20
	 *          --------------------
	 *  Position 	0 	1	2	3 ... 20
	 */
	public void returnsCorrectPositionRectangleForMultiColumnReorderRightCase() {
		RowReorderLayer reorderLayer = new RowReorderLayer(new BaseDataLayerFixture(20, 20));
		
		// Build expected cell positions to redraw
		final Set<Rectangle> expectedPositions = new HashSet<Rectangle>();
		expectedPositions.add(new Rectangle(0, 0, 20, 20));
		
		
		reorderLayer.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(ILayerEvent event) {
				RowReorderEvent multiReorder = (RowReorderEvent)event;
				assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
			}
		});
		
		// Reorder from beginning of grid
		List<Integer> fromRowPositions = Arrays.asList(new Integer[]{0, 1});
		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 2);
		
		// Reorder to middle of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(0, 5, 20, 15));		
		fromRowPositions = Arrays.asList(new Integer[]{5, 6, 7, 8});		
		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 10);
		
		// Reorder to end of grid
		expectedPositions.clear();
		expectedPositions.add(new Rectangle(0, 10, 20, 10));		
		fromRowPositions = Arrays.asList(new Integer[]{10, 11, 12, 13});		
		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 19);
	}

}
