/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.nebula.widgets.nattable.layer.ILayerDim;


/**
 * Dimension of a viewport layer.
 */
public interface IViewportDim extends ILayerDim {
	
	
	/**
	 * Returns the underlying scrollable layer dimension.
	 * 
	 * @return the underlying scrollable layer dimension
	 */
	ILayerDim getScrollable();
	
	
	/**
	 * Returns the minimum origin.
	 * 
	 * @return the pixel coordinate of the minimum origin in terms of the scrollable layer
	 */
	int getMinimumOriginPixel();
	
	/**
	 * Returns the minimum origin.
	 * 
	 * @return the position of the minimum origin in terms of the scrollable layer
	 */
	int getMinimumOriginPosition();
	
	/**
	 * Sets the minimum origin.
	 * 
	 * If required, the current origin is adapted to the next valid value; otherwise the origin is
	 * not changed.
	 * 
	 * @param scrollablePosition position of the origin in terms of the scrollable layer
	 */
	void setMinimumOriginPosition(int scrollablePosition);
	
	
	/**
	 * Returns the current origin.
	 * 
	 * @return the pixel coordinate of the current origin in terms of the scrollable layer
	 */
	int getOriginPixel();
	
	/**
	 * Returns the current origin.
	 * 
	 * @return the position of the current origin in terms of the scrollable layer
	 */
	int getOriginPosition();
	
	/**
	 * Sets the current origin.
	 * 
	 * If required, the specified origin is adjusted to the next valid value.
	 * 
	 * @param scrollablePixel pixel coordinate of the origin in terms of the scrollable layer
	 */
	void setOriginPixel(int scrollablePixel);
	
	/**
	 * Sets the current origin.
	 * 
	 * @param scrollablePixel position of the origin in terms of the scrollable layer
	 * 
	 * @throws IndexOutOfBoundsException if scrollablePosition is not a valid position
	 */
	void setOriginPosition(int scrollablePosition);
	
	/**
	 * Resets the viewport dimension.
	 * 
	 * Resets all settings, sets the minimum origin position to 0 and sets the origin to the 
	 * specified position.
	 * 
	 * @param scrollablePosition position in terms of the scrollable layer
	 * 
	 * @throws IndexOutOfBoundsException if scrollablePosition is not a valid position
	 */
	void reset(int scrollablePosition);
	
	
	/**
	 * Scrolls the viewport (if required) so that the specified position is visible.
	 * 
	 * @param scrollablePosition position in terms of the scrollable layer
	 */
	void movePositionIntoViewport(int scrollablePosition);
	
	
	/**
	 * Scrolls the viewport content backward by one step.
	 */
	void scrollBackwardByStep();
	
	/**
	 * Scrolls the viewport content forward by one step.
	 */
	void scrollForwardByStep();
	
	
	/**
	 * Scrolls the viewport content backward by one position.
	 */
	void scrollBackwardByPosition();
	
	/**
	 * Scrolls the viewport content forward by one position.
	 */
	void scrollForwardByPosition();
	
	
	/**
	 * Scrolls the viewport content backward by one viewport page.
	 */
	void scrollBackwardByPage();
	
	/**
	 * Scrolls the viewport content forward by one viewport page.
	 */
	void scrollForwardByPage();
	
	
	/**
	 * Scrolls the viewport content to the first position (minimum origin).
	 */
	void scrollBackwardToBound();
	
	/**
	 * Scrolls the viewport content to the last position.
	 */
	void scrollForwardToBound();
	
}
