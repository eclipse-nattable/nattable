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

import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.LEFT;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.RIGHT;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ScrollBar;


/**
 * Listener for the Horizontal scroll bar events on the Viewport Layer. State is
 * exposed to this class from the viewport, since it works in close conjnuction
 * with it.
 */
public class HorizontalScrollBarHandler extends ScrollBarHandlerTemplate {

	public HorizontalScrollBarHandler(ViewportLayer viewportLayer, ScrollBar scrollBar) {
		this(viewportLayer, new ScrollBarScroller(scrollBar));
	}
	
	public HorizontalScrollBarHandler(ViewportLayer viewportLayer, IScroller<?> scroller) {
		super(viewportLayer, scroller);
		
	}

	@Override
	int getViewportOrigin() {
		return viewportLayer.getOrigin().getX();
	}
	
	@Override
	int getViewportMinimumOrigin() {
		return viewportLayer.getMinimumOrigin().getX();
	}

	@Override
	void setViewportOrigin(int x) {
		viewportLayer.setOriginX(x);
	}
	
	@Override
	MoveDirectionEnum scrollDirectionForEventDetail(int eventDetail){
		return (eventDetail == SWT.PAGE_UP || eventDetail == SWT.ARROW_UP )	? LEFT : RIGHT;
	}
	
	@Override
	boolean keepScrolling() {
		return !viewportLayer.isLastColumnCompletelyDisplayed();
	}
	
	@Override
	int getViewportWindowSpan() {
		return viewportLayer.getClientAreaWidth();
	}

	@Override
	int getScrollableLayerSpan() {
		if (viewportLayer.getMaxWidth() >= 0 && viewportLayer.getMaxWidth() < scrollableLayer.getWidth()) {
			return viewportLayer.getMaxWidth();
		} else {
			return scrollableLayer.getWidth();
		}
	}
	
	@Override
	int getScrollIncrement() {
		return viewportLayer.getColumnCount() > 0 ? viewportLayer.getColumnWidthByPosition(0) : 0;
	}
}
