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
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;


/**
 * This abstract layer can be used if a layer implementation bases on implementation of layer
 * dimensions.
 */
public abstract class DimBasedLayer extends AbstractLayer {
	
	
	private static int computePreferredPositionCount(final ILayerDim dim) {
		final int preferredSize = dim.getPreferredSize();
		int position = 0;
		int size = 0;
		try {
			while (size < preferredSize) {
				size += dim.getPositionSize(position++);
			}
		}
		catch (final Exception e) {}
		return position;
	}
	
	private static List<ILayer> convertDim2LayerList(final Collection<ILayerDim> dims) {
		switch (dims.size()) {
		case 0:
			return Collections.emptyList();
		case 1:
			return Collections.singletonList(dims.iterator().next().getLayer());
		default:
			final List<ILayer> layers = new ArrayList<ILayer>(dims.size());
			for (final ILayerDim underlyingDim : dims) {
				layers.add(underlyingDim.getLayer());
			}
			return layers;
		}
	}
	
	
	protected DimBasedLayer() {
	}
	
	
	@Override
	protected abstract void updateDims();
	
	
	@Override
	public final int getColumnIndexByPosition(final int columnPosition) {
		return super.getDim(HORIZONTAL).getPositionIndex(columnPosition);
	}
	
	
	@Override
	public final int getColumnCount() {
		return super.getDim(HORIZONTAL).getPositionCount();
	}
	
	@Override
	public final int getPreferredColumnCount() {
		return computePreferredPositionCount(super.getDim(HORIZONTAL));
	}
	
	@Override
	public final int localToUnderlyingColumnPosition(final int localColumnPosition) {
		return super.getDim(HORIZONTAL).localToUnderlyingPosition(localColumnPosition);
	}
	
	@Override
	public final int underlyingToLocalColumnPosition(final ILayer sourceUnderlyingLayer,
			final int underlyingColumnPosition) {
		return super.getDim(HORIZONTAL).underlyingToLocalPosition(sourceUnderlyingLayer.getDim(HORIZONTAL),
				underlyingColumnPosition );
	}
	
	@Override
	public final Collection<Range> underlyingToLocalColumnPositions(final ILayer sourceUnderlyingLayer,
			final Collection<Range> underlyingColumnPositionRanges) {
		return super.getDim(HORIZONTAL).underlyingToLocalPositions(sourceUnderlyingLayer.getDim(HORIZONTAL),
				underlyingColumnPositionRanges );
	}
	
	@Override
	public final int getWidth() {
		return super.getDim(HORIZONTAL).getSize();
	}
	
	@Override
	public final int getPreferredWidth() {
		return super.getDim(HORIZONTAL).getPreferredSize();
	}
	
	@Override
	public final int getColumnWidthByPosition(final int columnPosition) {
		return super.getDim(HORIZONTAL).getPositionSize(columnPosition);
	}
	
	@Override
	public final boolean isColumnPositionResizable(final int columnPosition) {
		return super.getDim(HORIZONTAL).isPositionResizable(columnPosition);
	}
	
	@Override
	public final int getColumnPositionByX(final int x) {
		return super.getDim(HORIZONTAL).getPositionByPixel(x);
	}
	
	@Override
	public final int getStartXOfColumnPosition(final int columnPosition) {
		return super.getDim(HORIZONTAL).getPositionStart(columnPosition);
	}
	
	@Override
	public final Collection<ILayer> getUnderlyingLayersByColumnPosition(
			final int columnPosition) {
		return convertDim2LayerList(super.getDim(HORIZONTAL).getUnderlyingDimsByPosition(columnPosition));
	}
	
	
	@Override
	public final int getRowIndexByPosition(final int rowPosition) {
		return super.getDim(VERTICAL).getPositionIndex(rowPosition);
	}
	
	
	@Override
	public final int getRowCount() {
		return super.getDim(VERTICAL).getPositionCount();
	}
	
	@Override
	public final int getPreferredRowCount() {
		return computePreferredPositionCount(super.getDim(VERTICAL));
	}
	
	@Override
	public final int localToUnderlyingRowPosition(final int localRowPosition) {
		return super.getDim(VERTICAL).localToUnderlyingPosition(localRowPosition);
	}
	
	@Override
	public final int underlyingToLocalRowPosition(final ILayer sourceUnderlyingLayer,
			final int underlyingRowPosition) {
		return super.getDim(VERTICAL).underlyingToLocalPosition(sourceUnderlyingLayer.getDim(VERTICAL),
				underlyingRowPosition );
	}
	
	@Override
	public final Collection<Range> underlyingToLocalRowPositions(final ILayer sourceUnderlyingLayer,
			final Collection<Range> underlyingRowPositionRanges) {
		return super.getDim(VERTICAL).underlyingToLocalPositions(sourceUnderlyingLayer.getDim(VERTICAL),
				underlyingRowPositionRanges );
	}
	
	@Override
	public final int getHeight() {
		return super.getDim(VERTICAL).getSize();
	}
	
	@Override
	public final int getPreferredHeight() {
		return super.getDim(VERTICAL).getPreferredSize();
	}
	
	@Override
	public final int getRowHeightByPosition(final int rowPosition) {
		return super.getDim(VERTICAL).getPositionSize(rowPosition);
	}
	
	@Override
	public final boolean isRowPositionResizable(final int rowPosition) {
		return super.getDim(VERTICAL).isPositionResizable(rowPosition);
	}
	
	@Override
	public final int getRowPositionByY(final int y) {
		return super.getDim(VERTICAL).getPositionByPixel(y);
	}
	
	@Override
	public final int getStartYOfRowPosition(final int rowPosition) {
		return super.getDim(VERTICAL).getPositionStart(rowPosition);
	}
	
	@Override
	public final Collection<ILayer> getUnderlyingLayersByRowPosition(final int rowPosition) {
		return convertDim2LayerList(super.getDim(VERTICAL).getUnderlyingDimsByPosition(rowPosition));
	}
	
}
