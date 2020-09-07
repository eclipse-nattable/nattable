/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ViewportLayerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for Vertical scrolling of the viewport.
 *
 * The {@link DataLayerFixture} has the following config. Used by all tests. The
 * viewport is 100px wide (configured via ClientArea parameter)
 *
 * DO NOT FORMAT ! Row pos | Row Height 0 | 40 1 | 70 2 | 25 3 | 40 4 | 50 5 |
 * 40 6 | 100
 *
 */
public class VerticalScrollBarHandlerTest {

    ViewportLayerFixture viewport = new ViewportLayerFixture();
    private VerticalScrollBarHandler scrollHandler;
    private ScrollBar scrollBar;

    @Before
    public void init() {
        this.viewport = new ViewportLayerFixture();
        this.scrollBar = ViewportLayerFixture.DEFAULT_SCROLLABLE.getVerticalBar();
        this.scrollHandler = new VerticalScrollBarHandler(this.viewport, this.scrollBar);

        assertEquals(0, this.viewport.getRowIndexByPosition(0));
        assertEquals(1, this.viewport.getRowIndexByPosition(1));
    }

    private void scrollViewportByOffset(int offset) {
        this.scrollHandler.setViewportOrigin(this.viewport.getOrigin().getY() + offset);
    }

    private void scrollViewportToPixel(int y) {
        this.scrollHandler.setViewportOrigin(y);
    }

    @Test
    public void scrollViewportUpByOffset() throws Exception {
        this.viewport.setOriginY(this.viewport.getStartYOfRowPosition(2));
        assertEquals(2, this.viewport.getRowIndexByPosition(0));

        scrollViewportByOffset(-1);
        assertEquals(1, this.viewport.getRowIndexByPosition(0));
        assertEquals(2, this.viewport.getRowIndexByPosition(1));

        this.viewport.setOriginY(this.viewport.getStartYOfRowPosition(1));
        scrollViewportByOffset(-1);
        assertEquals(0, this.viewport.getRowIndexByPosition(0));
        assertEquals(1, this.viewport.getRowIndexByPosition(1));
    }

    @Test
    public void scrollViewportDownByOffset() throws Exception {
        this.viewport.moveRowPositionIntoViewport(2);
        assertEquals(0, this.viewport.getRowIndexByPosition(0));

        scrollViewportByOffset(20);
        assertEquals(1, this.viewport.getRowIndexByPosition(0));
        assertEquals(2, this.viewport.getRowIndexByPosition(1));
        assertEquals(3, this.viewport.getRowIndexByPosition(2));
    }

    @Test
    public void dragDown() throws Exception {
        scrollViewportToPixel(200);
        assertEquals(4, this.viewport.getRowIndexByPosition(0));
        assertEquals(5, this.viewport.getRowIndexByPosition(1));
    }

    @Test
    public void dragUp() throws Exception {
        this.viewport.moveRowPositionIntoViewport(4);
        assertEquals(2, this.viewport.getRowIndexByPosition(0));

        scrollViewportToPixel(50);
        assertEquals(1, this.viewport.getRowIndexByPosition(0));
    }

    @Test
    public void verticalScrollbarThumbSize() throws Exception {
        this.viewport = new ViewportLayerFixture(new Rectangle(0, 0, 250, 100));
        this.scrollHandler = new VerticalScrollBarHandler(this.viewport, this.scrollBar);

        assertEquals(250, this.viewport.getWidth());
        this.scrollHandler.recalculateScrollBarSize();

        // Fixture data - viewport height (100px), scrollable height (365px)
        // No overhang
        assertEquals(100, this.scrollHandler.scroller.getThumb());

        this.viewport.moveRowPositionIntoViewport(5);
        assertEquals(100, this.scrollHandler.scroller.getThumb());
    }

    @Test
    public void verticalScrollbarThumbSizeCalcNoScrollingNeeded()
            throws Exception {
        this.viewport = new ViewportLayerFixture(new Rectangle(0, 0, 500, 500));
        this.scrollHandler = new VerticalScrollBarHandler(this.viewport, this.scrollBar);

        assertEquals(465, this.viewport.getWidth());
        assertEquals(465, this.viewport.getWidth());

        this.scrollHandler.recalculateScrollBarSize();

        assertEquals(365, this.scrollHandler.scroller.getThumb());
        assertFalse(this.scrollBar.isEnabled());
        assertFalse(this.scrollBar.isVisible());
    }

}
