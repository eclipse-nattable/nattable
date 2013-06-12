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
 * Abstract base class for layers that expose transformed views of an underlying unique index layer. By default the
 * AbstractLayerTransform behaves as an identity transform of its underlying layer; that is, it exposes
 * its underlying layer as is without any changes. Subclasses are expected to override methods in this
 * class to implement specific kinds of layer transformations.
 */
public class AbstractIndexLayerTransform extends AbstractLayer implements IUniqueIndexLayer {


	private IUniqueIndexLayer underlyingLayer;


	public AbstractIndexLayerTransform() {
	}

	public AbstractIndexLayerTransform(IUniqueIndexLayer underlyingLayer) {
		setUnderlyingLayer(underlyingLayer);
	}


	protected void setUnderlyingLayer(IUniqueIndexLayer underlyingLayer) {
		if (this.underlyingLayer != null) {
			this.underlyingLayer.removeLayerListener(this);
		}
		this.underlyingLayer = underlyingLayer;
		this.underlyingLayer.setClientAreaProvider(getClientAreaProvider());
		this.underlyingLayer.addLayerListener(this);
	}

	protected final IUniqueIndexLayer getUnderlyingLayer() {
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
		underlyingLayer.saveState(prefix, properties);
		super.saveState(prefix, properties);
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
		super.configure(configRegistry, uiBindingRegistry);
		underlyingLayer.configure(configRegistry, uiBindingRegistry);
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return (layerPainter != null) ? layerPainter : underlyingLayer.getLayerPainter();
	}

	// Command

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (super.doCommand(command)) {
			return true;
		}

		return underlyingLayer.doCommand(command);
	}

	// Client area

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
		if (underlyingLayer != null) {
			underlyingLayer.setClientAreaProvider(clientAreaProvider);
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
		return underlyingLayer.getColumnIndexByPosition(
				localToUnderlyingColumnPosition(columnPosition) );
	}

	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return localColumnPosition;
	}

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		if (sourceUnderlyingLayer != underlyingLayer) {
			return -1;
		}
		return underlyingColumnPosition;
	}
	
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		Collection<Range> localColumnPositionRanges = new ArrayList<Range>(underlyingColumnPositionRanges.size());
		for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
			localColumnPositionRanges.add(new Range(
					underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.start),
					underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.end) ));
		}
		return localColumnPositionRanges;
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return underlyingToLocalColumnPosition(underlyingLayer, underlyingLayer.getColumnPositionByIndex(columnIndex));
	}

	// Width

	public int getWidth() {
		return underlyingLayer.getWidth();
	}

	public int getPreferredWidth() {
		return underlyingLayer.getPreferredWidth();
	}

	public int getColumnWidthByPosition(int columnPosition) {
		return underlyingLayer.getColumnWidthByPosition(
				localToUnderlyingColumnPosition(columnPosition) );
	}

	// Column resize

	public boolean isColumnPositionResizable(int columnPosition) {
		return underlyingLayer.isColumnPositionResizable(
				localToUnderlyingColumnPosition(columnPosition) );
	}

	// X

	public int getColumnPositionByX(int x) {
		return underlyingLayer.getColumnPositionByX(x);
	}

	public int getStartXOfColumnPosition(int columnPosition) {
		return underlyingLayer.getStartXOfColumnPosition(
				localToUnderlyingColumnPosition(columnPosition) );
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
		return underlyingLayer.getRowIndexByPosition(
				localToUnderlyingRowPosition(rowPosition) );
	}

	public int localToUnderlyingRowPosition(int localRowPosition) {
		return localRowPosition;
	}

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		if (sourceUnderlyingLayer != underlyingLayer) {
			return -1;
		}
		return underlyingRowPosition;
	}
	
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		Collection<Range> localRowPositionRanges = new ArrayList<Range>(underlyingRowPositionRanges.size());
		for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
			localRowPositionRanges.add(new Range(
					underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.start),
					underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.end) ));
		}
		return localRowPositionRanges;
	}

	public int getRowPositionByIndex(int rowIndex) {
		return underlyingToLocalRowPosition(underlyingLayer, underlyingLayer.getRowPositionByIndex(rowIndex));
	}

	// Height

	public int getHeight() {
		return underlyingLayer.getHeight();
	}

	public int getPreferredHeight() {
		return underlyingLayer.getPreferredHeight();
	}

	public int getRowHeightByPosition(int rowPosition) {
		return underlyingLayer.getRowHeightByPosition(
				localToUnderlyingRowPosition(rowPosition) );
	}

	// Row resize

	public boolean isRowPositionResizable(int rowPosition) {
		return underlyingLayer.isRowPositionResizable(
				localToUnderlyingRowPosition(rowPosition) );
	}

	// Y

	public int getRowPositionByY(int y) {
		return underlyingLayer.getRowPositionByY(y);
	}

	public int getStartYOfRowPosition(int rowPosition) {
		return underlyingLayer.getStartYOfRowPosition(
				localToUnderlyingRowPosition(rowPosition) );
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
		ILayerCell cell = underlyingLayer.getCellByPosition(
				localToUnderlyingColumnPosition(columnPosition),
				localToUnderlyingRowPosition(rowPosition));
		if (cell == null) {
			return null;
		}
		return new TranslatedLayerCell(cell, this,
				underlyingToLocalColumnPosition(underlyingLayer, cell.getOriginColumnPosition()),
				underlyingToLocalRowPosition(underlyingLayer, cell.getOriginRowPosition()),
				underlyingToLocalColumnPosition(underlyingLayer, cell.getColumnPosition()),
				underlyingToLocalRowPosition(underlyingLayer, cell.getRowPosition()) );
	}

	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getDisplayModeByPosition(
				localToUnderlyingColumnPosition(columnPosition),
				localToUnderlyingRowPosition(rowPosition) );
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack configLabels = underlyingLayer.getConfigLabelsByPosition(
				localToUnderlyingColumnPosition(columnPosition),
				localToUnderlyingRowPosition(rowPosition) );
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
		return underlyingLayer.getDataValueByPosition(
				localToUnderlyingColumnPosition(columnPosition),
				localToUnderlyingRowPosition(rowPosition) );
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
