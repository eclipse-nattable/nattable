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


import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public abstract class ScrollBarHandlerTemplate implements Listener {

	public static final int DEFAULT_OFFSET = 1;
	protected final ViewportLayer viewportLayer;
	protected final IUniqueIndexLayer scrollableLayer;
	protected final ScrollBar scrollBar;
	
	/**
	 * Flag to remember if the scroll bar is moved by dragging.
	 * Needed because if the scroll bar is moved by dragging, there will be 
	 * another event that is handled for releasing the drag mode. 
	 * We only need to handle the dragging once, otherwise if the 
	 * DialogErrorHandling strategy is used, the dialog would be showed
	 * twice.
	 */
	private boolean dragging = false;

	public ScrollBarHandlerTemplate(ViewportLayer viewportLayer, ScrollBar scrollBar) {
		this.viewportLayer = viewportLayer;
		this.scrollableLayer = viewportLayer.getScrollableLayer();
		this.scrollBar = scrollBar;
		this.scrollBar.addListener(SWT.Selection, this);
	}
	
	public void dispose() {
		if (this.scrollBar != null && !this.scrollBar.isDisposed()) {
			this.scrollBar.removeListener(SWT.Selection, this);
		}
	}

	public void handleEvent(Event event) {
		boolean handle = true;
		
		if (!this.dragging) {
			if (EditUtils.commitAndCloseActiveEditor()) {
				handle = false;
			}
		}
		if (event.detail == SWT.DRAG) {
			this.dragging = true;
		}
		else {
			this.dragging = false;
		}
			
		if (handle) {
			ScrollBar scrollBar = (ScrollBar) event.widget;
			
			int position = getPositionByPixel(getViewportPixelOffset() + scrollBar.getSelection());
			
			setViewportOrigin(position);
		}
		else {
			adjustScrollBar();
		}
	}

	void adjustScrollBar() {
		
		if (scrollBar.isDisposed()) {
			return;
		}
		int scrollablePosition = getScrollablePosition();
		int startPixel = getStartPixelOfPosition(scrollablePosition) - getViewportPixelOffset();
		
		scrollBar.setSelection(startPixel);
	}

	void recalculateScrollBarSize() {
		if (scrollBar.isDisposed()) {
			return;
		}
		
		int max = getScrollableLayerSpan() - getViewportPixelOffset() + getScrollBarOverhang();
		if (! scrollBar.isDisposed()) {
			scrollBar.setMaximum(max);
		}
		
		int viewportWindowSpan = getViewportWindowSpan();
		scrollBar.setPageIncrement(viewportWindowSpan);
		
		int thumbSize;
		if (viewportWindowSpan < max && viewportWindowSpan != 0) {
			thumbSize = viewportWindowSpan;
			scrollBar.setEnabled(true);
			scrollBar.setVisible(true);
		} else {
			thumbSize = max;
			scrollBar.setEnabled(false);
			scrollBar.setVisible(false);
		}
		scrollBar.setThumb(thumbSize);
		
		adjustScrollBar();
	}

	/**
	 * Overhang - the extra white area left at the right/bottom edge
	 *    (due to the first column aligning with the left edge)
	 */
	protected int getScrollBarOverhang() {
		/*
		 * If the scrollbar is moved to its max extent and the left/topmost cell is partially visible, then the viewport
		 * would be snapped back to align with the left/top edge of the first visible cell as above, but this would then
		 * move the right/bottommost cell out of the viewport. In this case there would be no way to view the
		 * right/bottommost edge of the right/bottommost cells. In order to remedy this, an overhang size is calculated
		 * and added to the size of the underlying scrollable area when calculating the scroll handle sizes, like so
		 * (this example is for column widths; similar logic applies for row heights):
		 *
		 * a. take the width of the viewport
		 * b. take the width of the underlying scrollable area
		 * c. subtract the width of the viewport from the width of the scrollable area to get an x pixel position
		 * d. find the column at the x pixel position
		 * e. find the start x pixel position of the column found in d
		 * f. if the start x pixel position in e is not equal to the x pixel position in c, then we must calculate an
		 *    overhang
		 * g. overhang = width of column d - (x pixel position found in c - start x position of column d)
		 *
		 * The scrollbar handle width is then proportional to the viewport width compared to the scrollable area width
		 * plus this overhang. The end effect is that when the scroll handle is moved to its maximal position, instead
		 * of the left edge of the viewport being positioned in the middle of a cell, it will be positioned on the right
		 * edge of that cell (or the left edge of the next cell). The last cell will then be completely visible, along
		 * with some extra white space.
		 */

		int viewportWindowSpan = getViewportWindowSpan();
		int scrollableLayerSpan = getScrollableLayerSpan();
		int viewportPixelOffset = getViewportPixelOffset();
		if (viewportWindowSpan <= 0 || viewportWindowSpan >= scrollableLayerSpan - viewportPixelOffset) {
			return 0;
		}

		int edgePixel = scrollableLayerSpan - viewportWindowSpan;
		int positionAtEdge = getPositionByPixel(edgePixel);
		int startPixelOfPositionAtEdge = getStartPixelOfPosition(positionAtEdge);

		int overhang = 0;
		if (edgePixel != startPixelOfPositionAtEdge) {
			overhang = (getSpanByPosition(positionAtEdge)) - (edgePixel - startPixelOfPositionAtEdge);
		}
		return overhang;
	}

	/**
	 * Methods to be implemented by the Horizontal/Vertical scroll bar handlers.
	 * @return
	 */
	abstract int getViewportWindowSpan();
	
	abstract int getScrollableLayerSpan();

	abstract boolean keepScrolling();

	abstract int pageScrollDistance();

	abstract int getSpanByPosition(int scrollablePosition);

	abstract int getScrollablePosition();

	abstract int getStartPixelOfPosition(int position);

	abstract int getPositionByPixel(int pixelValue);
	
	abstract int getViewportPixelOffset();

	abstract void setViewportOrigin(int position);

	abstract MoveDirectionEnum scrollDirectionForEventDetail(int eventDetail);

}
