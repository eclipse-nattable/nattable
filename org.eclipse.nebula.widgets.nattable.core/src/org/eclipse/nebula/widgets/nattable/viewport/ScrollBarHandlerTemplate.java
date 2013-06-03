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
			if (!EditUtils.commitAndCloseActiveEditor()) {
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
			
			setViewportOrigin(getViewportMinimumOrigin() + scrollBar.getSelection());
			setScrollIncrement();
		} else {
			adjustScrollBar();
		}
	}

	void adjustScrollBar() {
		
		if (scrollBar.isDisposed()) {
			return;
		}
		int startPixel = getViewportOrigin() - getViewportMinimumOrigin();
		
		scrollBar.setSelection(startPixel);
	}

	void recalculateScrollBarSize() {
		if (scrollBar.isDisposed()) {
			return;
		}
		
		int max = getScrollableLayerSpan() - getViewportMinimumOrigin();
		if (! scrollBar.isDisposed()) {
			scrollBar.setMaximum(max);
		}
		
		int viewportWindowSpan = getViewportWindowSpan();
		
		int thumbSize;
		if (viewportWindowSpan < max && viewportWindowSpan != 0) {
			thumbSize = viewportWindowSpan;
			scrollBar.setEnabled(true);
			scrollBar.setVisible(true);
			
			setScrollIncrement();
			
			scrollBar.setPageIncrement(viewportWindowSpan);
		} else {
			thumbSize = max;
			scrollBar.setEnabled(false);
			scrollBar.setVisible(false);
		}
		scrollBar.setThumb(thumbSize);
		
		adjustScrollBar();
	}

	void setScrollIncrement() {
		int scrollIncrement = Math.min(getScrollIncrement(), getViewportWindowSpan() / 4);
		scrollBar.setIncrement(scrollIncrement);
	}
	
	/**
	 * Methods to be implemented by the Horizontal/Vertical scroll bar handlers.
	 * @return
	 */
	abstract int getViewportWindowSpan();
	
	abstract int getScrollableLayerSpan();

	abstract boolean keepScrolling();

	abstract int getViewportOrigin();

	abstract int getViewportMinimumOrigin();

	abstract void setViewportOrigin(int pixel);

	abstract MoveDirectionEnum scrollDirectionForEventDetail(int eventDetail);
	
	abstract int getScrollIncrement();

}
