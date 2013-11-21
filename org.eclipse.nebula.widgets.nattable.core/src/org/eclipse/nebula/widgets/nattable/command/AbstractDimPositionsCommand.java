/*******************************************************************************
 * Copyright (c) 2012, 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.command;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerDim;


public abstract class AbstractDimPositionsCommand implements ILayerCommand {
	
	
	private final Orientation orientation;
	
	private ILayerDim layerDim;
	
	private Collection<Range> positions;
	
	
	protected AbstractDimPositionsCommand(
			final ILayerDim layerDim, final Collection<Range> positions) {
		this.orientation = layerDim.getOrientation();
		this.layerDim = layerDim;
		this.positions = positions;
	}
	
	protected AbstractDimPositionsCommand(final AbstractDimPositionsCommand command) {
		this.orientation = command.orientation;
		this.layerDim = command.layerDim;
		this.positions = command.positions;
	}
	
	
	public final Orientation getOrientation() {
		return this.orientation;
	}
	
	protected final ILayerDim getDim() {
		return this.layerDim;
	}
	
	public Collection<Range> getPositions() {
		return this.positions;
	}
	
	
	@Override
	public boolean convertToTargetLayer(final ILayer targetLayer) {
		final ILayerDim targetDim = targetLayer.getDim(this.orientation);
		if (this.layerDim == targetDim) {
			return true;
		}
		
		return convertToTargetLayer(this.layerDim, targetDim);
	}
	
	protected boolean convertToTargetLayer(final ILayerDim dim, final ILayerDim targetDim) {
		final RangeList targetPositions = new RangeList();
		
		for (final Range range : this.positions) {
			for (int position = range.start; position < range.end; position++) {
				final int targetPosition = LayerCommandUtil.convertPositionToTargetContext(dim,
						position, targetDim );
				if (targetPosition != Integer.MIN_VALUE) {
					targetPositions.values().add(targetPosition);
				}
			}
		}
		
		if (!targetPositions.isEmpty()) {
			this.layerDim = targetDim;
			this.positions = targetPositions;
			return true;
		}
		return false;
	}
	
}
