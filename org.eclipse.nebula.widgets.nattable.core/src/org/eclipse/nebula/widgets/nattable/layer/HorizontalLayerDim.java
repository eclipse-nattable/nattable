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

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;


/**
 * Implementation of horizontal layer dimension which forwards all method calls to the
 * corresponding "column" methods in the layer.
 */
public class HorizontalLayerDim extends AbstractLayerDim<ILayer> {
	
	
	public HorizontalLayerDim(/*@NonNull*/ final ILayer layer) {
		super(layer, HORIZONTAL);
	}
	
	
	@Override
	public int getPositionIndex(final int position) {
		return this.layer.getColumnIndexByPosition(position);
	}
	
	
	@Override
	public int getPositionCount() {
		return this.layer.getColumnCount();
	}
	
	@Override
	public int localToUnderlyingPosition(final int position) {
		return this.layer.localToUnderlyingColumnPosition(position);
	}
	
	@Override
	public int underlyingToLocalPosition(final ILayerDim sourceUnderlyingDim,
			final int underlyingPosition) {
		return this.layer.underlyingToLocalColumnPosition(sourceUnderlyingDim.getLayer(), underlyingPosition);
	}
	
	@Override
	public Collection<Range> underlyingToLocalPositions(final ILayerDim sourceUnderlyingDim,
			final Collection<Range> underlyingPositionRanges) {
		return this.layer.underlyingToLocalColumnPositions(sourceUnderlyingDim.getLayer(), underlyingPositionRanges);
	}
	
	@Override
	public Collection<ILayerDim> getUnderlyingDimsByPosition(final int position) {
		final Collection<ILayer> underlyingLayers = this.layer.getUnderlyingLayersByColumnPosition(position);
		final List<ILayerDim> underlyingDims = new ArrayList<ILayerDim>(underlyingLayers.size());
		for (final ILayer underlyingLayer : underlyingLayers) {
			underlyingDims.add(underlyingLayer.getDim(getOrientation()));
		}
		return underlyingDims;
	}
	
	
	@Override
	public int getSize() {
		return this.layer.getWidth();
	}
	
	@Override
	public int getPreferredSize() {
		return this.layer.getPreferredWidth();
	}
	
	@Override
	public int getPositionByPixel(final int pixel) {
		return this.layer.getColumnPositionByX(pixel);
	}
	
	@Override
	public int getPositionStart(final int position) {
		return this.layer.getStartXOfColumnPosition(position);
	}
	
	@Override
	public int getPositionSize(final int position) {
		return this.layer.getColumnWidthByPosition(position);
	}
	
	@Override
	public boolean isPositionResizable(final int position) {
		return this.layer.isColumnPositionResizable(position);
	}
	
}
