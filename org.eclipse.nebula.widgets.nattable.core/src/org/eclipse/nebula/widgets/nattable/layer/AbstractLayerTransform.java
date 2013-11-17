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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TranslatedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;


/**
 * Abstract base class for layers that expose transformed views of an underlying layer. By default the
 * AbstractLayerTransform behaves as an identity transform of its underlying layer; that is, it exposes
 * its underlying layer as is without any changes. Subclasses are expected to override methods in this
 * class to implement specific kinds of layer transformations.
 */
public abstract class AbstractLayerTransform extends AbstractLayer {

	protected ILayer underlyingLayer;

	public AbstractLayerTransform() {
	}

	public AbstractLayerTransform(ILayer underlyingLayer) {
		setUnderlyingLayer(underlyingLayer);
	}

	protected void setUnderlyingLayer(ILayer underlyingLayer) {
		this.underlyingLayer = underlyingLayer;
		this.underlyingLayer.setClientAreaProvider(getClientAreaProvider());
		this.underlyingLayer.addLayerListener(this);
	}

	protected ILayer getUnderlyingLayer() {
		return underlyingLayer;
	}

	// Dispose
	
	@Override
	public void dispose() {
		underlyingLayer.dispose();
	}
	
	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		underlyingLayer.saveState(prefix, properties);
	}

	/**
	 * Underlying layers <i>must</i> load state first.
	 * If this is not done, {@link IStructuralChangeEvent} from underlying
	 * layers will reset caches after state has been loaded
	 */
	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		underlyingLayer.loadState(prefix, properties);
	}

	// Configuration

	@Override
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		underlyingLayer.configure(configRegistry, uiBindingRegistry);
		super.configure(configRegistry, uiBindingRegistry);
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return underlyingLayer.getLayerPainter();
	}

	// Command

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (super.doCommand(command)) {
			return true;
		}

		if (underlyingLayer != null) {
			return underlyingLayer.doCommand(command);
		}

		return false;
	}

	// Client area

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
		if (getUnderlyingLayer() != null) {
			getUnderlyingLayer().setClientAreaProvider(clientAreaProvider);
		}
	}

    // Horizontal features

	// Columns

	public int getColumnCount() {
		return underlyingLayer.getColumnCount();
	}

	public int getPreferredColumnCount() {
		return underlyingLayer.getPreferredColumnCount();
	}

	public int getColumnIndexByPosition(int columnPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		return underlyingLayer.getColumnIndexByPosition(underlyingColumnPosition);
	}

	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return localColumnPosition;
	}

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		return underlyingColumnPosition;
	}
	
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		Collection<Range> localColumnPositionRanges = new ArrayList<Range>();
		
		for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
			localColumnPositionRanges.add(
					new Range(
							underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.start),
							underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.end)
					)
			);
		}
		
		return localColumnPositionRanges;
	}

	// Width

	public int getWidth() {
		return underlyingLayer.getWidth();
	}

	public int getPreferredWidth() {
		return underlyingLayer.getPreferredWidth();
	}

    public int getColumnWidthByPosition(int columnPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		return underlyingLayer.getColumnWidthByPosition(underlyingColumnPosition);
	}

	// Column resize

	public boolean isColumnPositionResizable(int columnPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		return underlyingLayer.isColumnPositionResizable(underlyingColumnPosition);
	}

	// X

	public int getColumnPositionByX(int x) {
		return underlyingLayer.getColumnPositionByX(x);
	}

	public int getStartXOfColumnPosition(int columnPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		return underlyingLayer.getStartXOfColumnPosition(underlyingColumnPosition);
	}
	
	// Underlying

	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
		underlyingLayers.add(underlyingLayer);
		return underlyingLayers;
	}

	// Vertical features

	// Rows

	public int getRowCount() {
		return underlyingLayer.getRowCount();
	}

	public int getPreferredRowCount() {
		return underlyingLayer.getPreferredRowCount();
	}

	public int getRowIndexByPosition(int rowPosition) {
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		return underlyingLayer.getRowIndexByPosition(underlyingRowPosition);
	}

	public int localToUnderlyingRowPosition(int localRowPosition) {
		return localRowPosition;
	}

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		return underlyingRowPosition;
	}
	
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		Collection<Range> localRowPositionRanges = new ArrayList<Range>();
		
		for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
			localRowPositionRanges.add(
					new Range(
							underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.start),
							underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.end)
					)
			);
		}
		
		return localRowPositionRanges;
	}

	// Height

	public int getHeight() {
		return underlyingLayer.getHeight();
	}

	public int getPreferredHeight() {
		return underlyingLayer.getPreferredHeight();
	}

	public int getRowHeightByPosition(int rowPosition) {
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		return underlyingLayer.getRowHeightByPosition(underlyingRowPosition);
	}

	// Row resize

	public boolean isRowPositionResizable(int rowPosition) {
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		return underlyingLayer.isRowPositionResizable(underlyingRowPosition);
	}

	// Y

	public int getRowPositionByY(int y) {
		return underlyingLayer.getRowPositionByY(y);
	}

	public int getStartYOfRowPosition(int rowPosition) {
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		return underlyingLayer.getStartYOfRowPosition(underlyingRowPosition);
	}
	
	// Underlying

	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
		underlyingLayers.add(underlyingLayer);
		return underlyingLayers;
	}

    // Cell features

	@Override
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		ILayerCell cell = underlyingLayer.getCellByPosition(underlyingColumnPosition, underlyingRowPosition);
		if (cell != null) {
			cell = new TranslatedLayerCell(
					cell,
					this,
					underlyingToLocalColumnPosition(underlyingLayer, cell.getOriginColumnPosition()),
					underlyingToLocalRowPosition(underlyingLayer, cell.getOriginRowPosition()),
					underlyingToLocalColumnPosition(underlyingLayer, cell.getColumnPosition()),
					underlyingToLocalRowPosition(underlyingLayer, cell.getRowPosition())
			);
		}
		return cell;
	}
	
    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
    	return underlyingLayer.getDisplayModeByPosition(underlyingColumnPosition, underlyingRowPosition);
    }

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		LabelStack configLabels = underlyingLayer.getConfigLabelsByPosition(underlyingColumnPosition, underlyingRowPosition);
		IConfigLabelAccumulator configLabelAccumulator = getConfigLabelAccumulator();
		if (configLabelAccumulator != null) {
			configLabelAccumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
		}
		String regionName = getRegionName();
		if (regionName != null) {
			configLabels.addLabel(regionName);
		}
		return configLabels;
	}

	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		return underlyingLayer.getDataValueByPosition(underlyingColumnPosition, underlyingRowPosition);
	}
	
	@Override
	public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		return underlyingLayer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
	}

	// IRegionResolver

	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		LabelStack regionLabels = underlyingLayer.getRegionLabelsByXY(x, y);
		String regionName = getRegionName();
		if (regionName != null) {
			regionLabels.addLabel(regionName);
		}
		return regionLabels;
	}

	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer;
	}

}
