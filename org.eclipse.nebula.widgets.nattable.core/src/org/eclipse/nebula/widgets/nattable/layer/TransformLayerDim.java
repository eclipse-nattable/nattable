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

package org.eclipse.nebula.widgets.nattable.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;


/**
 * Dim implementation for {@link TransformLayer}.
 *
 * @param <T> the type of the layer
 */
public class TransformLayerDim<T extends ILayer> extends AbstractLayerDim<T> {
	
	
	protected final ILayerDim underlyingDim;
	
	
	public TransformLayerDim(/*@NonNull*/ final T layer, /*@NonNull*/ final ILayerDim underlyingDim) {
		this(layer, underlyingDim.getOrientation(), underlyingDim);
	}
	
	public TransformLayerDim(/*@NonNull*/ final T layer, /*@NonNull*/ final Orientation orientation,
			/*@NonNull*/ final ILayerDim underlyingDim) {
		super(layer, orientation);
		if (underlyingDim == null) {
			throw new NullPointerException("underlyingDim"); //$NON-NLS-1$
		}
		this.underlyingDim = underlyingDim;
	}
	
	
	@Override
	public int getPositionIndex(final int position) {
		final int underlyingPosition = localToUnderlyingPosition(position);
		return this.underlyingDim.getPositionIndex(underlyingPosition);
	}
	
	
	@Override
	public int getPositionCount() {
		return this.underlyingDim.getPositionCount();
	}
	
	@Override
	public int localToUnderlyingPosition(final int position) {
		return position;
	}
	
	@Override
	public int underlyingToLocalPosition(final ILayerDim sourceUnderlyingDim,
			final int underlyingPosition) {
		if (sourceUnderlyingDim != this.underlyingDim) {
			throw new IllegalArgumentException("underlyingLayer"); //$NON-NLS-1$
		}
		
		return underlyingPosition;
	}
	
	@Override
	public List<Range> underlyingToLocalPositions(final ILayerDim sourceUnderlyingDim,
			final Collection<Range> underlyingPositions) {
		final List<Range> localPositions = new ArrayList<Range>(underlyingPositions.size());
		for (final Range underlyingPositionRange : underlyingPositions) {
			if (underlyingPositionRange.start == underlyingPositionRange.end) {
				final int position = underlyingToLocalPosition(sourceUnderlyingDim, underlyingPositionRange.start);
				localPositions.add(new Range(position, position));
			}
			else {
				final int first = underlyingToLocalPosition(sourceUnderlyingDim, underlyingPositionRange.start);
				final int last = underlyingToLocalPosition(sourceUnderlyingDim, underlyingPositionRange.end - 1);
				if (first <= last) {
					localPositions.add(new Range(first, last + 1));
				}
			}
		}
		return localPositions;
	}
	
	@Override
	public List<ILayerDim> getUnderlyingDimsByPosition(final int position) {
		return Collections.singletonList(this.underlyingDim);
	}
	
	
	@Override
	public int getSize() {
		return this.underlyingDim.getSize();
	}
	
	@Override
	public int getPreferredSize() {
		return this.underlyingDim.getPreferredSize();
	}
	
	@Override
	public int getPositionByPixel(final int pixel) {
		return underlyingToLocalPosition(this.underlyingDim,
				this.underlyingDim.getPositionByPixel(pixel) );
	}
	
	@Override
	public int getPositionStart(final int position) {
		final int underlyingPosition = localToUnderlyingPosition(position);
		return this.underlyingDim.getPositionStart(underlyingPosition);
	}
	
	@Override
	public int getPositionSize(final int position) {
		final int underlyingPosition = localToUnderlyingPosition(position);
		return this.underlyingDim.getPositionSize(underlyingPosition);
	}
	
	@Override
	public boolean isPositionResizable(final int position) {
		final int underlyingPosition = localToUnderlyingPosition(position);
		return this.underlyingDim.isPositionResizable(underlyingPosition);
	}
	
}
