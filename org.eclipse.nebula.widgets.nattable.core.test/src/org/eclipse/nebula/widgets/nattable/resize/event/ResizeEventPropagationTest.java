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
package org.eclipse.nebula.widgets.nattable.resize.event;

import java.util.Collection;


import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResizeEventPropagationTest {
	private LayerListenerFixture layerListener;
	
	private DataLayerFixture dataLayer;
	
	@Before
	public void setUp() {
		// Total width should be 500 and total height should be 280
		dataLayer = new DataLayerFixture(100, 40);
	}
	
	@Test
	public void shouldFireResizeEventAfterColumnResizeCommand() {
		dataLayer.addLayerListener(new LayerListenerFixture());
		dataLayer.setColumnWidthByPosition(4, 100);
	}
	
	@Test
	public void shouldFireResizeEventAfterRowResizeCommand() {
		dataLayer.addLayerListener(new LayerListenerFixture());
		dataLayer.setRowHeightByPosition(2, 100);
	}
	
	@Test
	public void shouldReturnARectangleStartingFromResizedColumnToEndOfGrid() {
		// Mimics resizing the second column
		layerListener = new LayerListenerFixture();
		dataLayer.addLayerListener(layerListener);
		dataLayer.setColumnWidthByPosition(2, 200);

		// This is the propagated event
		ColumnResizeEvent columnResizeEvent =(ColumnResizeEvent)layerListener.getReceivedEvents().get(0);
		Collection<Rectangle> actualRectangles = columnResizeEvent.getChangedPositionRectangles();
		
		// The affected region should have the following size
		Rectangle expectedRectangle = new Rectangle(2, 0, 3, 7);
		Assert.assertEquals(expectedRectangle, actualRectangles.iterator().next());
	}
	
	@Test
	public void shouldReturnARectangleStartingFromResizedRowToEndOfGrid() {
		// Mimics resizing the third row
		layerListener = new LayerListenerFixture();
		dataLayer.addLayerListener(layerListener);
		dataLayer.setRowHeightByPosition(3, 100);
		
		// This is the propagated event
		RowResizeEvent rowResizeEvent = (RowResizeEvent)layerListener.getReceivedEvents().get(0);
		Collection<Rectangle> actualRectangles = rowResizeEvent.getChangedPositionRectangles();
		
		// The affected region should have the following size
		Rectangle expectedRectangle = new Rectangle(0, 3, 5, 4);
		Assert.assertEquals(expectedRectangle, actualRectangles.iterator().next());
	}
}
