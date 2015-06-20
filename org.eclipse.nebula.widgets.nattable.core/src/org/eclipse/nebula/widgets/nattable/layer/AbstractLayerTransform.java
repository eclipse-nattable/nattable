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
 * Abstract base class for layers that expose transformed views of an underlying
 * layer. By default the AbstractLayerTransform behaves as an identity transform
 * of its underlying layer; that is, it exposes its underlying layer as is
 * without any changes. Subclasses are expected to override methods in this
 * class to implement specific kinds of layer transformations.
 */
public abstract class AbstractLayerTransform extends AbstractLayer {

    protected ILayer underlyingLayer;

    public AbstractLayerTransform() {}

    public AbstractLayerTransform(ILayer underlyingLayer) {
        setUnderlyingLayer(underlyingLayer);
    }

    protected void setUnderlyingLayer(ILayer underlyingLayer) {
        this.underlyingLayer = underlyingLayer;
        this.underlyingLayer.setClientAreaProvider(getClientAreaProvider());
        this.underlyingLayer.addLayerListener(this);
    }

    protected ILayer getUnderlyingLayer() {
        return this.underlyingLayer;
    }

    // Dispose

    @Override
    public void dispose() {
        this.underlyingLayer.dispose();
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        this.underlyingLayer.saveState(prefix, properties);
    }

    /**
     * Underlying layers <i>must</i> load state first. If this is not done,
     * {@link IStructuralChangeEvent} from underlying layers will reset caches
     * after state has been loaded
     */
    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        this.underlyingLayer.loadState(prefix, properties);
    }

    // Configuration

    @Override
    public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
        this.underlyingLayer.configure(configRegistry, uiBindingRegistry);
        super.configure(configRegistry, uiBindingRegistry);
    }

    @Override
    public ILayerPainter getLayerPainter() {
        return this.underlyingLayer.getLayerPainter();
    }

    // Command

    @Override
    public boolean doCommand(ILayerCommand command) {
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
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
        if (getUnderlyingLayer() != null) {
            getUnderlyingLayer().setClientAreaProvider(clientAreaProvider);
        }
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.underlyingLayer.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.underlyingLayer.getPreferredColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        return this.underlyingLayer.getColumnIndexByPosition(underlyingColumnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return localColumnPosition;
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        return underlyingColumnPosition;
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
        Collection<Range> localColumnPositionRanges = new ArrayList<Range>();

        for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
            localColumnPositionRanges.add(new Range(
                    underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.start),
                    underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.end)));
        }

        return localColumnPositionRanges;
    }

    // Width

    @Override
    public int getWidth() {
        return this.underlyingLayer.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.underlyingLayer.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        return this.underlyingLayer.getColumnWidthByPosition(underlyingColumnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        return this.underlyingLayer.isColumnPositionResizable(underlyingColumnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.underlyingLayer.getColumnPositionByX(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        return this.underlyingLayer.getStartXOfColumnPosition(underlyingColumnPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
        Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
        underlyingLayers.add(this.underlyingLayer);
        return underlyingLayers;
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.underlyingLayer.getRowCount();
    }

    @Override
    public int getPreferredRowCount() {
        return this.underlyingLayer.getPreferredRowCount();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        return this.underlyingLayer.getRowIndexByPosition(underlyingRowPosition);
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        return localRowPosition;
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
        return underlyingRowPosition;
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingRowPositionRanges) {
        Collection<Range> localRowPositionRanges = new ArrayList<Range>();

        for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
            localRowPositionRanges.add(new Range(
                    underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.start),
                    underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.end)));
        }

        return localRowPositionRanges;
    }

    // Height

    @Override
    public int getHeight() {
        return this.underlyingLayer.getHeight();
    }

    @Override
    public int getPreferredHeight() {
        return this.underlyingLayer.getPreferredHeight();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        return this.underlyingLayer.getRowHeightByPosition(underlyingRowPosition);
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        return this.underlyingLayer.isRowPositionResizable(underlyingRowPosition);
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return this.underlyingLayer.getRowPositionByY(y);
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        return this.underlyingLayer.getStartYOfRowPosition(underlyingRowPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
        Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
        underlyingLayers.add(this.underlyingLayer);
        return underlyingLayers;
    }

    // Cell features

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        ILayerCell cell = this.underlyingLayer.getCellByPosition(underlyingColumnPosition, underlyingRowPosition);
        if (cell != null) {
            cell = new TranslatedLayerCell(cell, this,
                    underlyingToLocalColumnPosition(this.underlyingLayer, cell.getOriginColumnPosition()),
                    underlyingToLocalRowPosition(this.underlyingLayer, cell.getOriginRowPosition()),
                    underlyingToLocalColumnPosition(this.underlyingLayer, cell.getColumnPosition()),
                    underlyingToLocalRowPosition(this.underlyingLayer, cell.getRowPosition()));
        }
        return cell;
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        return this.underlyingLayer.getDisplayModeByPosition(underlyingColumnPosition, underlyingRowPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        LabelStack configLabels = this.underlyingLayer.getConfigLabelsByPosition(underlyingColumnPosition, underlyingRowPosition);
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

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
        int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
        return this.underlyingLayer.getDataValueByPosition(underlyingColumnPosition, underlyingRowPosition);
    }

    @Override
    public ICellPainter getCellPainter(
            int columnPosition, int rowPosition,
            ILayerCell cell, IConfigRegistry configRegistry) {
        return this.underlyingLayer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
    }

    // IRegionResolver

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        LabelStack regionLabels = this.underlyingLayer.getRegionLabelsByXY(x, y);
        String regionName = getRegionName();
        if (regionName != null) {
            regionLabels.addLabel(regionName);
        }
        return regionLabels;
    }

    @Override
    public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer;
    }

    @Override
    public boolean isDynamicSizeLayer() {
        return ((AbstractLayer) this.underlyingLayer).isDynamicSizeLayer();
    }

}
