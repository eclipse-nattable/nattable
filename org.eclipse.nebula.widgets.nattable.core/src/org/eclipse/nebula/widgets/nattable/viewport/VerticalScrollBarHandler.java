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

import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.DOWN;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.UP;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Listener for the Vertical scroll bar events.
 */
public class VerticalScrollBarHandler extends ScrollBarHandlerTemplate
        implements Listener {

    public VerticalScrollBarHandler(ViewportLayer viewportLayer,
            ScrollBar scrollBar) {
        this(viewportLayer, new ScrollBarScroller(scrollBar));
    }

    public VerticalScrollBarHandler(ViewportLayer viewportLayer,
            IScroller<?> scroller) {
        super(viewportLayer, scroller);
    }

    /**
     * Convert Viewport 0 pos -> Scrollable 0 pos
     *
     * @return
     */
    @Override
    int getViewportOrigin() {
        return this.viewportLayer.getOrigin().getY();
    }

    @Override
    int getViewportMinimumOrigin() {
        return this.viewportLayer.getMinimumOrigin().getY();
    }

    @Override
    void setViewportOrigin(int y) {
        this.viewportLayer.setOriginY(y);
    }

    @Override
    MoveDirectionEnum scrollDirectionForEventDetail(int eventDetail) {
        return (eventDetail == SWT.PAGE_UP || eventDetail == SWT.ARROW_UP) ? UP
                : DOWN;
    }

    @Override
    boolean keepScrolling() {
        return !this.viewportLayer.isLastRowCompletelyDisplayed();
    }

    @Override
    int getViewportWindowSpan() {
        return this.viewportLayer.getClientAreaHeight();
    }

    @Override
    int getScrollableLayerSpan() {
        if (this.viewportLayer.getMaxHeight() >= 0
                && this.viewportLayer.getMaxHeight() < this.scrollableLayer.getHeight()) {
            return this.viewportLayer.getMaxHeight();
        } else {
            return this.scrollableLayer.getHeight();
        }
    }

    @Override
    int getScrollIncrement() {
        return this.viewportLayer.getRowCount() > 0 ? this.viewportLayer
                .getRowHeightByPosition(0) : 0;
    }
}
