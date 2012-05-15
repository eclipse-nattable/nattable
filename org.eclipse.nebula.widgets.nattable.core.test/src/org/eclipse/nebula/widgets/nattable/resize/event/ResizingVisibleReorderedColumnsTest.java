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
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseColumnReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/*
 * This test was for selective repainting during resize i.e paint the bare minimum needed.
 * This was done for performance reasons. This approach to performance has been abandoned. 
 * This would be a useful thing to do but is on low priority now. Hence these tests will 
 * be ignored till we pick this up again. 
 */
@Ignore
public class ResizingVisibleReorderedColumnsTest {

	private LayerListenerFixture layerListener;
	
	private DataLayerFixture dataLayer;
	
	private BaseColumnReorderLayerFixture reorderLayer;
	
	@Before
	public void setUp() {
		dataLayer = new DataLayerFixture(100, 40);
		reorderLayer = new BaseColumnReorderLayerFixture(dataLayer);
		layerListener = new LayerListenerFixture();
		reorderLayer.addLayerListener(layerListener);
	}
	
	@Test
	public void changeShouldIncludeLastColumn() {
		// Reorder columns should now be 0, 2, 3, 4, 1
		reorderLayer.reorderColumnPosition(1, 4);
		
		// Resize last column
		dataLayer.setColumnWidthByPosition(1, 200);
		
		// The changed position rectangle should just have one column, and the column position should be the last column (4)
		Rectangle expectedRectangle = new Rectangle(4, 0, 1, 7);
		Collection<Rectangle> actualRectangles = ((ColumnResizeEvent) layerListener.getReceivedEvent(ColumnResizeEvent.class)).getChangedPositionRectangles();
		Assert.assertEquals(expectedRectangle, actualRectangles.iterator().next());
	}
	
	@Test
	public void changeShouldIncludeHalfOfGrid() {
		// Reorder columns should now be 0, 1, 3, 2, 4
		reorderLayer.reorderColumnPosition(3, 2);
		
		// Resize last column
		dataLayer.setColumnWidthByPosition(3, 200);
		
		// The changed position rectangle should just have one column, and the column position should be the last column (4)
		Rectangle expectedRectangle = new Rectangle(2, 0, 3, 7);
		Collection<Rectangle> actualRectangles = ((ColumnResizeEvent) layerListener.getReceivedEvent(ColumnResizeEvent.class)).getChangedPositionRectangles();
		Assert.assertEquals(expectedRectangle, actualRectangles.iterator().next());
	}
	
	@Test
	public void changeShouldIncludeAllColumns() {
		// Reorder columns again, should now be 3, 0, 1, 2, 4
		reorderLayer.reorderColumnPosition(3, 0);
		
		// Resize first column
		dataLayer.setColumnWidthByPosition(3, 200);
		
		// The changed position rectangle should now be the entire grid
		Rectangle expectedRectangle = new Rectangle(0, 0, 5, 7);
		Collection<Rectangle> actualRectangles = ((ColumnResizeEvent) layerListener.getReceivedEvent(ColumnResizeEvent.class)).getChangedPositionRectangles();
		Assert.assertEquals(expectedRectangle, actualRectangles);
	}
}
