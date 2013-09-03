/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ViewportLayerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.VerticalScrollBarHandler;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for Vertical scrolling of the viewport.
 *
 * The {@link DataLayerFixture} has the following config. Used by all tests.
 * The viewport is 100px wide (configured via ClientArea parameter)
 *
 * DO NOT FORMAT !
 * Row pos  | Row Height
 * 	0 | 40
 * 	1 | 70
 * 	2 | 25
 * 	3 | 40
 * 	4 | 50
 * 	5 | 40
 * 	6 | 100
 *
 */
public class VerticalScrollBarHandlerTest {

	ViewportLayerFixture viewport = new ViewportLayerFixture();
	private VerticalScrollBarHandler scrollHandler;
	private ScrollBar scrollBar;

	@Before
	public void init(){
		viewport = new ViewportLayerFixture();
		scrollBar = ViewportLayerFixture.DEFAULT_SCROLLABLE.getVerticalBar();
		scrollHandler = new VerticalScrollBarHandler(viewport, scrollBar);

		assertEquals(0, viewport.getRowIndexByPosition(0));
		assertEquals(1, viewport.getRowIndexByPosition(1));
	}

	private void scrollViewportByOffset(int offset) {
		scrollHandler.setViewportOrigin(viewport.getOrigin().getY() + offset);
	}

	private void scrollViewportToPixel(int y) {
		scrollHandler.setViewportOrigin(y);
	}

	@Test
	public void scrollViewportUpByOffset() throws Exception {
		viewport.setOriginY(viewport.getStartYOfRowPosition(2));
		assertEquals(2, viewport.getRowIndexByPosition(0));

		scrollViewportByOffset(-1);
		assertEquals(1, viewport.getRowIndexByPosition(0));
		assertEquals(2, viewport.getRowIndexByPosition(1));

		viewport.setOriginY(viewport.getStartYOfRowPosition(1));
		scrollViewportByOffset(-1);
		assertEquals(0, viewport.getRowIndexByPosition(0));
		assertEquals(1, viewport.getRowIndexByPosition(1));
	}

	@Test
	public void scrollViewportDownByOffset() throws Exception {
		viewport.moveRowPositionIntoViewport(2);
		assertEquals(0, viewport.getRowIndexByPosition(0));

		scrollViewportByOffset(20);
		assertEquals(1, viewport.getRowIndexByPosition(0));
		assertEquals(2, viewport.getRowIndexByPosition(1));
		assertEquals(3, viewport.getRowIndexByPosition(2));
	}

	@Test
	public void dragDown() throws Exception {
		scrollViewportToPixel(200);
		assertEquals(4, viewport.getRowIndexByPosition(0));
		assertEquals(5, viewport.getRowIndexByPosition(1));
	}

	@Test
	public void dragUp() throws Exception {
		viewport.moveRowPositionIntoViewport(4);
		assertEquals(2, viewport.getRowIndexByPosition(0));

		scrollViewportToPixel(50);
		assertEquals(1, viewport.getRowIndexByPosition(0));
	}

	@Test
	public void verticalScrollbarThumbSize() throws Exception {
		viewport = new ViewportLayerFixture(new Rectangle(0,0,250,100));
		scrollHandler = new VerticalScrollBarHandler(viewport, scrollBar);

		assertEquals(250,viewport.getWidth());
		scrollHandler.recalculateScrollBarSize();

		// Fixture data - viewport height (100px), scrollable height (365px)
		// No overhang
		assertEquals(100, scrollHandler.scroller.getThumb());

		viewport.moveRowPositionIntoViewport(5);
		assertEquals(100, scrollHandler.scroller.getThumb());
	}

	@Test
	public void verticalScrollbarThumbSizeCalcNoScrollingNeeded() throws Exception {
		viewport = new ViewportLayerFixture(new Rectangle(0, 0, 500, 500));
		scrollHandler = new VerticalScrollBarHandler(viewport, scrollBar);

		assertEquals(465,viewport.getWidth());
		assertEquals(465,viewport.getWidth());

		scrollHandler.recalculateScrollBarSize();

		assertEquals(365, scrollHandler.scroller.getThumb());
		assertFalse(scrollBar.isEnabled());
		assertFalse(scrollBar.isVisible());
	}

}
