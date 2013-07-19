/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Edwin Park - original layer-based implementation supporting smooth scrolling
 *     Stephan Wahlbrink - initial API and dim-based implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.viewport;

import static org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer.PAGE_INTERSECTION_SIZE;

import java.util.Collection;

import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerDim;
import org.eclipse.nebula.widgets.nattable.layer.TransformLayerDim;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.swt.SWTUtil;


/**
 * Implementation of {@link IViewportDim} for {@link ViewportLayer}.
 */
public class ViewportDim extends TransformLayerDim<ViewportLayer> implements IViewportDim {
	
	
	private int minimumOriginPosition;
	private int minimumOriginPixel;
	
	private int originPixel;
	
	private int cachedClientAreaSize;
	private int cachedOriginPosition;
	private int cachedOriginPositionPixelShift;
	private int cachedPositionCount;
	private int cachedSize;
	
	private ScrollBarHandler scrollBarHandler;
	
	
	public ViewportDim(/*@NonNull*/ final ViewportLayer layer, /*@NonNull*/ final ILayerDim underlyingDim) {
		super(layer, underlyingDim);
		
		invalidateStructure();
	}
	
	
	protected void dispose() {
		if (this.scrollBarHandler != null) {
			this.scrollBarHandler.dispose();
			this.scrollBarHandler = null;
		}
	}
	
	/**
	 * @return The size of the visible client area. Will recalculate horizontal dimension
	 *     information if the size has changed.
	 */
	protected int getClientAreaSize() {
		final int clientAreaWidth = SWTUtil.getRange(this.layer.getClientAreaProvider().getClientArea(),
				this.orientation).size();
		if (clientAreaWidth != this.cachedClientAreaSize) {
			invalidateStructure();
			this.cachedClientAreaSize = clientAreaWidth;
		}
		return this.cachedClientAreaSize;
	}
	
	/**
	 * Clear horizontal caches
	 */
	protected void invalidateStructure() {
		this.cachedClientAreaSize = 0;
		this.cachedOriginPosition = -1;
		this.cachedOriginPositionPixelShift = 0;
		this.cachedPositionCount = -1;
		this.cachedSize = -1;
	}
	
	
	@Override
	public int getPositionCount() {
		if (this.layer.isViewportOff()) {
			return Math.max(this.underlyingDim.getPositionCount() - getMinimumOriginPosition(), 0);
		} else {
			if (this.cachedPositionCount < 0) {
				recalculateAvailableSizeAndPositionCount();
			}
			
			return this.cachedPositionCount;
		}
	}
	
	
	@Override
	public int localToUnderlyingPosition(final int position) {
		return getOriginPosition() + position;
	}
	
	@Override
	public int underlyingToLocalPosition(final ILayer sourceUnderlyingLayer,
			final int underlyingPosition) {
		if (sourceUnderlyingLayer != this.layer.getScrollableLayer()) {
			throw new IllegalArgumentException("underlyingLayer"); //$NON-NLS-1$
		}
		
		return underlyingPosition - getOriginPosition();
	}
	
	
	/**
	 * Recalculate dimension properties.
	 */
	protected void recalculateAvailableSizeAndPositionCount() {
		int availableSize = getClientAreaSize();
		
		this.cachedSize = Math.min(getUnderlyingSize(), availableSize);
		
		this.originPixel = boundsCheckOrigin(this.originPixel);
		
		this.cachedOriginPosition = this.underlyingDim.getPositionByPixel(getOriginPixel());
		this.cachedPositionCount = 0;
		final int positionBound = this.underlyingDim.getPositionCount();
		int position = this.cachedOriginPosition;
		if (position >= 0 && position < positionBound) {
			this.cachedOriginPositionPixelShift = getOriginPixel() - this.underlyingDim.getPositionStart(position);
			availableSize += this.cachedOriginPositionPixelShift;
		}
		while (position >= 0 && position < positionBound && availableSize > 0) {
			final int positionSize = this.underlyingDim.getPositionSize(position);
			availableSize -= positionSize;
			position++;
			this.cachedPositionCount++;
		}
	}
	
	private int getUnderlyingSize() {
		final int offsetPosition = getMinimumOriginPosition();
		return this.underlyingDim.getSize() - this.underlyingDim.getPositionStart(offsetPosition);
	}
	
	@Override
	public int getSize() {
		if (this.layer.isViewportOff()) {
			return getUnderlyingSize();
		}
		if (this.cachedSize < 0) {
			recalculateAvailableSizeAndPositionCount();
		}
		return this.cachedSize;
	}
	
	@Override
	public int getPositionByPixel(final int pixel) {
		return super.getPositionByPixel(getOriginPixel() + pixel);
	}
	
	@Override
	public int getPositionStart(final int position) {
		return super.getPositionStart(position) - getOriginPixel();
	}
	
	
	@Override
	public final ILayerDim getScrollable() {
		return this.underlyingDim;
	}
	
	
	@Override
	public int getMinimumOriginPixel() {
		return this.minimumOriginPixel;
	}
	
	@Override
	public int getMinimumOriginPosition() {
		return this.minimumOriginPosition;
	}
	
	@Override
	public void setMinimumOriginPosition(final int scrollablePosition) {
		final int pixel = (scrollablePosition < this.underlyingDim.getPositionCount()) ?
				this.underlyingDim.getPositionStart(scrollablePosition) :
				this.underlyingDim.getSize();
		if (pixel < 0) {
			return;
		}
		
		this.minimumOriginPosition = scrollablePosition;
		this.minimumOriginPixel = pixel;
		
		setOriginPixel(getOriginPixel());
		
		recalculateScrollBar();
	}
	
	
	@Override
	public int getOriginPixel() {
		if (this.layer.isViewportOff()) {
			return this.minimumOriginPixel;
		}
		return this.originPixel;
	}
	
	@Override
	public int getOriginPosition() {
		if (this.cachedOriginPosition < 0) {
			recalculateAvailableSizeAndPositionCount();
		}
		return this.cachedOriginPosition;
	}
	
	/**
	 * If the client area size is greater than the content size, move origin to fill as much content
	 * as possible.
	 */
	protected int adjustOrigin(final int pixel) {
		if (getPositionCount() == 0) {
			return 0;
		}
		
		final int availableWidth = getClientAreaSize() - (this.underlyingDim.getSize() - pixel);
		if (availableWidth <= 0) {
			return pixel;
		}
		return pixel - availableWidth;
	}
	
	/**
	 * Range checking for origin pixel position.
	 * @param pixel
	 * @return A valid x value within bounds: minimum origin x < x < max x (= column 0 x + width)
	 */
	private int boundsCheckOrigin(final int pixel) {
		final int min = getMinimumOriginPixel();
		if (pixel <= min) {
			return min;
		}
		final int max = Math.max(this.underlyingDim.getPositionStart(0) + this.underlyingDim.getSize(), min);
		if (pixel > max) {
			return max;
		}
		return pixel;
	}
	
	@Override
	public void setOriginPixel(final int scrollablePixel) {
		if (doSetOriginPixel(scrollablePixel)) {
			this.layer.fireScrollEvent();
		}
	}
	
	protected boolean doSetOriginPixel(int scrollablePixel) {
		scrollablePixel = boundsCheckOrigin(scrollablePixel);
		scrollablePixel = boundsCheckOrigin(adjustOrigin(scrollablePixel));
		
		if (this.originPixel != scrollablePixel) {
			invalidateStructure();
			this.originPixel = scrollablePixel;
			return true;
		}
		return false;
	}
	
	@Override
	public void setOriginPosition(final int scrollablePosition) {
		final int pixel = this.underlyingDim.getPositionStart(scrollablePosition);
		if (pixel >= 0) {
			setOriginPixel(pixel);
		}
	}
	
	
	@Override
	public void reset(final int scrollablePosition) {
		this.minimumOriginPosition = 0;
		this.minimumOriginPixel = 0;
		
		this.originPixel = 0;
		setOriginPosition(scrollablePosition);
	}
	
	/**
	 * Scrolls the viewport (if required) so that the given position is visible.
	 * 
	 * @param scrollablePosition position in the scrollable layer
	 */
	@Override
	public void movePositionIntoViewport(final int scrollablePosition) {
		if (this.underlyingDim.getPositionIndex(scrollablePosition) >= 0
				&& scrollablePosition >= getMinimumOriginPosition() ) {
			if (scrollablePosition <= getOriginPosition()) {
				// Move backward
				setOriginPosition(scrollablePosition);
			} else {
				// Move forward
				final int scrollableStart = this.underlyingDim.getPositionStart(scrollablePosition);
				final int scrollableEnd = scrollableStart + this.underlyingDim.getPositionSize(scrollablePosition);
				final int clientAreaHeight = getClientAreaSize();
				final int originPosition = getOriginPosition();
				final int viewportEnd = this.underlyingDim.getPositionStart(originPosition) + clientAreaHeight;
				
				if (viewportEnd < scrollableEnd) {
					setOriginPixel(Math.min(scrollableEnd - clientAreaHeight, scrollableStart));
				}
			}
			
			// TEE: at least adjust scrollbar to reflect new position
			adjustScrollBar();
		}
	}
	
	@Override
	public void scrollBackwardByStep() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		final int origin = getOriginPixel();
		final int maxSize = origin - getMinimumOriginPixel();
		final int stepSize = Math.min(getWindowStepSize(), maxSize);
		int position = getOriginPosition();
		int sizeForPosition = origin - this.underlyingDim.getPositionStart(position);
		if (sizeForPosition <= 0 && position > getMinimumOriginPosition()) {
			sizeForPosition += this.underlyingDim.getPositionSize(position - 1);
			position--;
		}
		if (sizeForPosition > 0 && sizeForPosition <= stepSize) {
			setOriginPixel(origin - sizeForPosition);
			return;
		}
//		if (stepSize == maxSize) {
//			setOriginPixel(origin - stepSize);
//			return;
//		}
		setOriginPixel(origin - stepSize / 2);
	}
	
	@Override
	public void scrollForwardByStep() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		final int origin = getOriginPixel();
		final int maxSize = this.underlyingDim.getSize() - origin;
		final int stepSize = Math.min(getWindowStepSize(), maxSize);
		final int position = getOriginPosition();
		if (position + 1 < this.underlyingDim.getPositionCount()) {
			final int sizeForPosition = this.underlyingDim.getPositionStart(position + 1) - origin;
			if (sizeForPosition > 0 && sizeForPosition <= stepSize) {
				setOriginPixel(origin + sizeForPosition);
				return;
			}
		}
//		if (stepSize == maxSize) {
//			setOriginPixel(origin + stepSize);
//			return;
//		}
		setOriginPixel(origin + stepSize / 2);
		return;
	}
	
	private int getWindowStepSize() {
		final int size = getSize();
		final int hint = size / 4 - PAGE_INTERSECTION_SIZE;
		if (hint < 3 * PAGE_INTERSECTION_SIZE) {
			return size;
		}
		return hint;
	}
	
	@Override
	public void scrollBackwardByPosition() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		final int position = getOriginPosition();
		final int positionPixel = getPositionStart(position);
		if (positionPixel < getOriginPixel()) { // first show start
			setOriginPixel(positionPixel);
			return;
		}
		setOriginPosition(position - 1);
	}
	
	@Override
	public void scrollForwardByPosition() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		setOriginPosition(getOriginPosition() + 1);
	}
	
	@Override
	public void scrollBackwardByPage() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		final int origin = getOriginPixel();
		final int maxSize = origin - getMinimumOriginPixel();
		final int pageSize = Math.min(getWindowPageSize(), maxSize);
		int position = getOriginPosition();
		int sizeForPosition = origin - this.underlyingDim.getPositionStart(position);
		for (final int bound = getMinimumOriginPosition(); position > bound; ) {
			final int size = this.underlyingDim.getPositionSize(position - 1);
			if (sizeForPosition + size <= pageSize) {
				sizeForPosition += size;
				position--;
				continue;
			}
			else {
				break;
			}
		}
		if (sizeForPosition > PAGE_INTERSECTION_SIZE && sizeForPosition <= pageSize) {
			setOriginPixel(origin - sizeForPosition);
			return;
		}
		if (pageSize == maxSize) {
			setOriginPixel(origin - pageSize);
			return;
		}
		setOriginPixel(origin - (pageSize - PAGE_INTERSECTION_SIZE));
	}
	
	@Override
	public void scrollForwardByPage() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		final int origin = getOriginPixel();
		final int maxSize = this.underlyingDim.getSize() - origin;
		final int pageSize = Math.min(getWindowPageSize(), maxSize);
		int position = getOriginPosition();
		int sizeForPosition = this.underlyingDim.getPositionStart(position) - origin;
		for (final int bound = this.underlyingDim.getPositionCount(); position + 1 < bound; ) {
			final int size = this.underlyingDim.getPositionSize(position);
			if (sizeForPosition + size <= pageSize) {
				sizeForPosition += size;
				position++;
				continue;
			}
			else {
				break;
			}
		}
		if (sizeForPosition >= PAGE_INTERSECTION_SIZE && sizeForPosition <= pageSize) {
			setOriginPixel(origin + sizeForPosition);
			return;
		}
		if (pageSize == maxSize) {
			setOriginPixel(origin + pageSize);
			return;
		}
		setOriginPixel(origin + (pageSize - PAGE_INTERSECTION_SIZE));
	}
	
	private int getWindowPageSize() {
		final int size = getSize();
		final int hint = size - PAGE_INTERSECTION_SIZE/2;
		if (hint < 4 * PAGE_INTERSECTION_SIZE) {
			return size;
		}
		return hint;
	}
	
	@Override
	public void scrollBackwardToBound() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		setOriginPosition(getMinimumOriginPosition());
	}
	
	@Override
	public void scrollForwardToBound() {
		if (getPositionCount() == 0 || this.layer.isViewportOff()) {
			return;
		}
		
		setOriginPosition(getScrollable().getPositionCount() - 1);
	}
	
	
	protected void checkScrollBar(final Scrollable control) {
		if (this.scrollBarHandler != null) {
			return;
		}
		final ScrollBar scrollBar = SWTUtil.getScrollBar(control, getOrientation());
		if (scrollBar != null) {
			this.scrollBarHandler = new ScrollBarHandler(this, scrollBar);
		}
	}
	
	/**
	 * Adjusts scrollbar to sync with current state of viewport.
	 */
	private void adjustScrollBar() {
		if (this.scrollBarHandler != null) {
			this.scrollBarHandler.adjustScrollBar();
		}
	}
	
	/**
	 * Recalculate scrollbar characteristics;
	 */
	private void recalculateScrollBar() {
		if (this.scrollBarHandler != null) {
			this.scrollBarHandler.recalculateScrollBarSize();
			
			if (!this.scrollBarHandler.getScrollBar().getEnabled()) {
				setOriginPosition(getMinimumOriginPosition());
			}
		}
	}
	
	protected void handleResize() {
		recalculateAvailableSizeAndPositionCount();
		setOriginPixel(getOriginPixel());
		recalculateScrollBar();
	}
	
	protected void handleStructuralChange(final Collection<StructuralDiff> diffs) {
		final int minimumOriginPosition = getMinimumOriginPosition();
		final int minimumOriginPixel = getMinimumOriginPixel();
		final int selectedOriginPosition = this.cachedOriginPosition;
		final int selectedOriginPositionShift = this.cachedOriginPositionPixelShift;
		final int selectedOriginPixel = this.originPixel;
		
		invalidateStructure();
		
		if (diffs != null) {
			// The handling of diffs have to be in sync with FreezeEventHandler
			int minimumPositionChange = 0;
			int selectedPositionChange = 0;
			int freezeMove = 0; // 0 = unset, 1 == true, -1 == false
			
			for (final StructuralDiff diff : diffs) {
				final int start = diff.getBeforePositionRange().start;
				switch (diff.getDiffType()) {
				case ADD:
					if (start < minimumOriginPosition
							|| (freezeMove == 1 && start == minimumOriginPosition) ) {
						minimumPositionChange += diff.getAfterPositionRange().size();
					}
					if (start < selectedOriginPosition) {
						selectedPositionChange += diff.getAfterPositionRange().size();
					}
					continue;
				case DELETE:
					if (start < minimumOriginPosition) {
						minimumPositionChange -= Math.min(diff.getBeforePositionRange().end, minimumOriginPosition + 1) - start;
						if (freezeMove == 0) {
							freezeMove = 1;
						}
					}
					else {
						freezeMove = -1;
					}
					if (start < selectedOriginPosition) {
						selectedPositionChange -= Math.min(diff.getBeforePositionRange().end, selectedOriginPosition + 1) - start;
					}
					continue;
				default:
					continue;
				}
			}
			
			setMinimumOriginPosition(minimumOriginPosition + minimumPositionChange);
			if (selectedOriginPosition >= 0) {
				final int pixel = this.underlyingDim.getPositionStart(selectedOriginPosition + selectedPositionChange);
				if (pixel >= 0) {
					setOriginPixel(pixel + selectedOriginPositionShift);
				}
			}
			else {
				setOriginPixel(selectedOriginPixel + (getMinimumOriginPixel() - minimumOriginPixel));
			}
		}
	}
	
}
