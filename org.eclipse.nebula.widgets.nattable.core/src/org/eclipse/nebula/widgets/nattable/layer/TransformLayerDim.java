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

import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;


/**
 * Dim implementation for {@link TransformLayer}.
 *
 * @param <T>
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
	public int underlyingToLocalPosition(final ILayer sourceUnderlyingLayer,
			final int underlyingPosition) {
		if (sourceUnderlyingLayer != this.underlyingDim.getLayer()) {
			throw new IllegalArgumentException("underlyingLayer"); //$NON-NLS-1$
		}
		
		return underlyingPosition;
	}
	
	@Override
	public Collection<Range> underlyingToLocalPositions(final ILayer sourceUnderlyingLayer,
			final Collection<Range> underlyingPositionRanges) {
		final Collection<Range> localPositionRanges = new ArrayList<Range>(underlyingPositionRanges.size());
		
		for (final Range underlyingPositionRange : underlyingPositionRanges) {
			localPositionRanges.add(new Range(
					underlyingToLocalPosition(sourceUnderlyingLayer, underlyingPositionRange.start),
					underlyingToLocalPosition(sourceUnderlyingLayer, underlyingPositionRange.end) ));
		}
		
		return localPositionRanges;
	}
	
	@Override
	public Collection<ILayer> getUnderlyingLayersByPosition(final int position) {
		return Collections.singletonList(this.underlyingDim.getLayer());
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
		return underlyingToLocalPosition(this.underlyingDim.getLayer(),
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
