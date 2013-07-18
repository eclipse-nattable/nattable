/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Stephan Wahlbrink - dim-based implementation prepared for long datasets
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.swt.SWTUtil;


public class ScrollBarHandler implements Listener {
	
	
	private final IViewportDim dim;
	
	private final ScrollBar scrollBar;
	
	private double factor = 1;
	
	/**
	 * Flag to remember if the scroll bar is moved by dragging.
	 * Needed because if the scroll bar is moved by dragging, there will be 
	 * another event that is handled for releasing the drag mode. 
	 * We only need to handle the dragging once, otherwise if the 
	 * DialogErrorHandling strategy is used, the dialog would be showed
	 * twice.
	 */
	private boolean dragging = false;
	
	
	public ScrollBarHandler(final IViewportDim dim, final ScrollBar scrollBar) {
		this.dim = dim;
		this.scrollBar = scrollBar;
		this.scrollBar.addListener(SWT.Selection, this);
		
		scrollBar.getParent().addListener(SWTUtil.getMouseWheelEventType(this.dim.getOrientation()), this);
	}
	
	
	public void dispose() {
		if (this.scrollBar != null && !this.scrollBar.isDisposed()) {
			this.scrollBar.removeListener(SWT.Selection, this);
			this.scrollBar.removeListener(SWTUtil.getMouseWheelEventType(this.dim.getOrientation()), this);
		}
	}
	
	
	@Override
	public void handleEvent(final Event event) {
		boolean handle = true;
		
		if (!this.dragging) {
			if (!EditUtils.commitAndCloseActiveEditor()) {
				handle = false;
			}
		}
		this.dragging = (event.detail == SWT.DRAG);
		
		if (!handle || !event.doit) {
			adjustScrollBar();
			return;
		}
		
		switch (event.type) {
		case SWT.MouseHorizontalWheel:
		case SWT.MouseVerticalWheel:
			if (event.count > 0) {
				for (; event.count > 0; event.count--) {
					dim.scrollBackwardByStep();
				}
			}
			else if (event.count < 0) {
				for (; event.count < 0; event.count++) {
					dim.scrollForwardByStep();
				}
			}
			event.doit = false;
			return;
		case SWT.Selection:
			switch (event.detail) {
			case SWT.HOME:
				dim.scrollBackwardToBound();
				return;
			case SWT.END:
				dim.scrollForwardToBound();
				return;
			case SWT.PAGE_UP:
				dim.scrollBackwardByPage();
				return;
			case SWT.PAGE_DOWN:
				dim.scrollForwardByPage();
				return;
			case SWT.ARROW_UP:
			case SWT.ARROW_LEFT:
				dim.scrollBackwardByStep();
				return;
			case SWT.ARROW_DOWN:
			case SWT.ARROW_RIGHT:
				dim.scrollForwardByStep();
				return;
			default:
				dim.setOriginPixel(dim.getMinimumOriginPixel()
						+ (int) (this.scrollBar.getSelection() / this.factor) );
				return;
			}
		}
	}
	
	ScrollBar getScrollBar() {
		return this.scrollBar;
	}
	
	void adjustScrollBar() {
		if (this.scrollBar.isDisposed()) {
			return;
		}
		final long startPixel = dim.getOriginPixel() - dim.getMinimumOriginPixel();
		
		this.scrollBar.setSelection((int) (this.factor * startPixel));
	}
	
	void recalculateScrollBarSize() {
		if (this.scrollBar.isDisposed()) {
			return;
		}
		
		final int scrollablePixel = dim.getScrollable().getSize() - dim.getMinimumOriginPixel();
		final int viewportWindowPixel = dim.getSize();
		
		final int max;
		final int viewportWindowSpan;
//		if (scrollablePixel <= 0x3fffffff) {
			this.factor = 1.0;
			viewportWindowSpan = (int) viewportWindowPixel;
			max = (int) scrollablePixel;
//		}
//		else {
//			this.factor = ((double) 0x3fffffff) / scrollablePixel;
//			final double exactSpan = (this.factor * viewportWindowPixel);
//			viewportWindowSpan = (int) Math.ceil(exactSpan);
//			max = (int) Math.min(0x3fffffff
//						// the thumb will be larger than required, add the diff to adjust this, 
//						// so the user can scroll to the end using the mouse
//						+ (long) ((viewportWindowSpan - exactSpan) / this.factor),
//					Integer.MAX_VALUE ); 
//		}
		
		if (this.scrollBar.isDisposed()) {
			return;
		}
		
		this.scrollBar.setMaximum(max);
		this.scrollBar.setPageIncrement(Math.max(viewportWindowSpan / 4, 1));
		
		if (viewportWindowSpan < max && viewportWindowPixel != 0) {
			this.scrollBar.setThumb(viewportWindowSpan);
			this.scrollBar.setEnabled(true);
			this.scrollBar.setVisible(true);
		} else {
			this.scrollBar.setThumb(max);
			this.scrollBar.setEnabled(false);
			this.scrollBar.setVisible(false);
		}
		
		adjustScrollBar();
	}
	
}
