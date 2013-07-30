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

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;


/**
 * Implementation of vertical layer dimension which forwards all method calls to the
 * corresponding "row" methods in the layer.
 */
public class VerticalLayerDim extends AbstractLayerDim<ILayer> {
	
	
	public VerticalLayerDim(/*@NonNull*/ final ILayer layer) {
		super(layer, VERTICAL);
	}
	
	
	@Override
	public int getPositionIndex(final int position) {
		return this.layer.getRowIndexByPosition(position);
	}
	
	
	@Override
	public int getPositionCount() {
		return this.layer.getRowCount();
	}
	
	@Override
	public int localToUnderlyingPosition(final int position) {
		return this.layer.localToUnderlyingRowPosition(position);
	}
	
	@Override
	public int underlyingToLocalPosition(final ILayerDim sourceUnderlyingDim,
			final int underlyingPosition) {
		return this.layer.underlyingToLocalRowPosition(sourceUnderlyingDim.getLayer(), underlyingPosition);
	}
	
	@Override
	public Collection<Range> underlyingToLocalPositions(final ILayerDim sourceUnderlyingDim,
			final Collection<Range> underlyingPositionRanges) {
		return this.layer.underlyingToLocalRowPositions(sourceUnderlyingDim.getLayer(), underlyingPositionRanges);
	}
	
	@Override
	public Collection<ILayerDim> getUnderlyingDimsByPosition(final int position) {
		final Collection<ILayer> underlyingLayers = this.layer.getUnderlyingLayersByRowPosition(position);
		final List<ILayerDim> underlyingDims = new ArrayList<ILayerDim>(underlyingLayers.size());
		for (final ILayer underlyingLayer : underlyingLayers) {
			underlyingDims.add(underlyingLayer.getDim(getOrientation()));
		}
		return underlyingDims;
	}
	
	
	@Override
	public int getSize() {
		return this.layer.getHeight();
	}
	
	@Override
	public int getPreferredSize() {
		return this.layer.getPreferredHeight();
	}
	
	@Override
	public int getPositionByPixel(final int pixel) {
		return this.layer.getRowPositionByY(pixel);
	}
	
	@Override
	public int getPositionStart(final int position) {
		return this.layer.getStartYOfRowPosition(position);
	}
	
	@Override
	public int getPositionSize(final int position) {
		return this.layer.getRowHeightByPosition(position);
	}
	
	@Override
	public boolean isPositionResizable(final int position) {
		return this.layer.isRowPositionResizable(position);
	}
	
}
