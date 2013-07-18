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

package org.eclipse.nebula.widgets.nattable.layer;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TranslatedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;


/**
 * Abstract base class for layers that expose transformed views of an underlying layer.
 * 
 * By default the layer behaves as an identity transform of its underlying layer; that is, it
 * exposes its underlying layer as is without any changes.  Subclasses are expected to override
 * methods in this class to implement specific kinds of layer transformations.
 * 
 * The layer is similar to {@link AbstractLayerTransform}, but is {@link DimBasedLayer dim-based}.
 */
public abstract class TransformLayer extends DimBasedLayer {
	
	
	private ILayer underlyingLayer;
	
	
	public TransformLayer(/*@NonNull*/ final ILayer underlyingLayer) {
		setUnderlyingLayer(underlyingLayer);
	}
	
	protected TransformLayer() {
	}
	
	
	@Override
	protected void updateDims() {
		final ILayer underlying = getUnderlyingLayer();
		if (underlying == null) {
			return;
		}
		setDim(new TransformLayerDim<ILayer>(this, underlying.getDim(HORIZONTAL)));
		setDim(new TransformLayerDim<ILayer>(this, underlying.getDim(VERTICAL)));
	}
	
	protected void setUnderlyingLayer(/*@NonNull*/ final ILayer underlyingLayer) {
		if (underlyingLayer == null) {
			throw new NullPointerException("underlyingLayer"); //$NON-NLS-1$
		}
		if (this.underlyingLayer != null) {
			this.underlyingLayer.removeLayerListener(this);
		}
		this.underlyingLayer = underlyingLayer;
		this.underlyingLayer.setClientAreaProvider(getClientAreaProvider());
		this.underlyingLayer.addLayerListener(this);
		
		updateDims();
	}
	
	protected final ILayer getUnderlyingLayer() {
		return this.underlyingLayer;
	}
	
	// Dispose
	
	@Override
	public void dispose() {
		this.underlyingLayer.dispose();
	}
	
	// Persistence
	
	@Override
	public void saveState(final String prefix, final Properties properties) {
		this.underlyingLayer.saveState(prefix, properties);
		super.saveState(prefix, properties);
	}
	
	/**
	 * Underlying layers <i>must</i> load state first.
	 * If this is not done, {@link IStructuralChangeEvent} from underlying
	 * layers will reset caches after state has been loaded
	 */
	@Override
	public void loadState(final String prefix, final Properties properties) {
		this.underlyingLayer.loadState(prefix, properties);
		super.loadState(prefix, properties);
	}
	
	// Configuration
	
	@Override
	public void configure(final ConfigRegistry configRegistry, final UiBindingRegistry uiBindingRegistry) {
		this.underlyingLayer.configure(configRegistry, uiBindingRegistry);
		super.configure(configRegistry, uiBindingRegistry);
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return (this.layerPainter != null) ? this.layerPainter : this.underlyingLayer.getLayerPainter();
	}
	
	// Command
	
	@Override
	public boolean doCommand(final ILayerCommand command) {
		if (super.doCommand(command)) {
			return true;
		}
		
		if (this.underlyingLayer != null) {
			return this.underlyingLayer.doCommand(command);
		}
		
		return false;
	}
	
	// Client area
	
	@Override
	public void setClientAreaProvider(final IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
		if (getUnderlyingLayer() != null) {
			getUnderlyingLayer().setClientAreaProvider(clientAreaProvider);
		}
	}
	
	
	// Cell features
	
	@Override
	public ILayerCell getCellByPosition(final int columnPosition, final int rowPosition) {
		ILayerCell cell = this.underlyingLayer.getCellByPosition(
				getDim(HORIZONTAL).localToUnderlyingPosition(columnPosition),
				getDim(VERTICAL).localToUnderlyingPosition(rowPosition) );
		
		if (cell != null) {
			cell = new TranslatedLayerCell(cell, this,
					underlyingToLocalColumnPosition(this.underlyingLayer, cell.getOriginColumnPosition()),
					underlyingToLocalRowPosition(this.underlyingLayer, cell.getOriginRowPosition()),
					underlyingToLocalColumnPosition(this.underlyingLayer, cell.getColumnPosition()),
					underlyingToLocalRowPosition(this.underlyingLayer, cell.getRowPosition())
			);
		}
		
		return cell;
	}
	
	@Override
	public String getDisplayModeByPosition(final int columnPosition, final int rowPosition) {
		return this.underlyingLayer.getDisplayModeByPosition(
				getDim(HORIZONTAL).localToUnderlyingPosition(columnPosition),
				getDim(VERTICAL).localToUnderlyingPosition(rowPosition) );
	}
	
	@Override
	public LabelStack getConfigLabelsByPosition(final int columnPosition, final int rowPosition) {
		final LabelStack configLabels = this.underlyingLayer.getConfigLabelsByPosition(
				getDim(HORIZONTAL).localToUnderlyingPosition(columnPosition),
				getDim(VERTICAL).localToUnderlyingPosition(rowPosition) );
		final IConfigLabelAccumulator configLabelAccumulator = getConfigLabelAccumulator();
		if (configLabelAccumulator != null) {
			configLabelAccumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
		}
		final String regionName = getRegionName();
		if (regionName != null) {
			configLabels.addLabel(regionName);
		}
		return configLabels;
	}
	
	@Override
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
		return this.underlyingLayer.getDataValueByPosition(
				getDim(HORIZONTAL).localToUnderlyingPosition(columnPosition),
				getDim(VERTICAL).localToUnderlyingPosition(rowPosition) );
	}
	
	@Override
	public ICellPainter getCellPainter(final int columnPosition, final int rowPosition, final ILayerCell cell,
			final IConfigRegistry configRegistry) {
		return this.underlyingLayer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
	}
	
	// IRegionResolver
	
	@Override
	public LabelStack getRegionLabelsByXY(final int x, final int y) {
		final LabelStack regionLabels = this.underlyingLayer.getRegionLabelsByXY(x, y);
		final String regionName = getRegionName();
		if (regionName != null) {
			regionLabels.addLabel(regionName);
		}
		return regionLabels;
	}
	
	@Override
	public ILayer getUnderlyingLayerByPosition(final int columnPosition, final int rowPosition) {
		return this.underlyingLayer;
	}
	
}
