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
package org.eclipse.nebula.widgets.nattable.viewport;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.LEFT;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.RIGHT;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;


/**
 * Listener for the Horizontal scroll bar events on the Viewport Layer. State is
 * exposed to this class from the viewport, since it works in close conjnuction
 * with it.
 */
public class HorizontalScrollBarHandler extends ScrollBarHandlerTemplate {

	public HorizontalScrollBarHandler(ViewportLayer viewportLayer, ScrollBar scrollBar) {
		super(viewportLayer, scrollBar);
		
	}

	/**
	 * In a normal scenario scroll by the width of the viewport. 
	 * If the col being scrolled is wider than above, use the col width
	 */
	@Override
	int pageScrollDistance() {
		int widthOfColBeingScrolled = scrollableLayer.getColumnWidthByPosition(getScrollablePosition());
		int viewportWidth = viewportLayer.getClientAreaWidth(); 
		int scrollWidth = (widthOfColBeingScrolled > viewportWidth) ? widthOfColBeingScrolled : viewportWidth;
		return scrollWidth;
	}
	
	@Override
	int getSpanByPosition(int scrollablePosition) {
		return scrollableLayer.getColumnWidthByPosition(scrollablePosition);
	}
	
	@Override
	int getScrollablePosition() {
		return LayerUtil.convertColumnPosition(viewportLayer, 0, scrollableLayer);
	}
	
	@Override
	int getStartPixelOfPosition(int position){
		return scrollableLayer.getStartXOfColumnPosition(position);
	}
	
	@Override
	int getPositionByPixel(int pixelValue) {
		return scrollableLayer.getColumnPositionByX(pixelValue);
	}

	@Override
	int getViewportPixelOffset() {
		int column = viewportLayer.getMinimumOriginColumnPosition();
		return (column < scrollableLayer.getColumnCount()) ? scrollableLayer.getStartXOfColumnPosition(column) : scrollableLayer.getWidth();
	}

	@Override
	void setViewportOrigin(int position) {
		viewportLayer.invalidateHorizontalStructure();
		viewportLayer.setOriginColumnPosition(position);
		scrollBar.setIncrement(viewportLayer.getColumnWidthByPosition(0));
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
		return scrollableLayer.getWidth();
	}
	
}
