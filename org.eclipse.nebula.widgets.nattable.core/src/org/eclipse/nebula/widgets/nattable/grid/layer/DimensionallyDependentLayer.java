/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.layer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.AbstractRegionCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

/**
 * <p>
 * A DimensionallyDependentLayer is a layer whose horizontal and vertical
 * dimensions are dependent on the horizontal and vertical dimensions of other
 * layers. A DimensionallyDependentLayer takes three constructor parameters: the
 * horizontal layer that the DimensionallyDependentLayer's horizontal dimension
 * is linked to, the vertical layer that the DimensionallyDependentLayer is
 * linked to, and a base layer to which all non-dimensionally related ILayer
 * method calls will be delegated to (e.g. command, event methods)
 * </p>
 * <p>
 * Prime examples of dimensionally dependent layers are the column header and
 * row header layers. For example, the column header layer's horizontal
 * dimension is linked to the body layer's horizontal dimension. This means that
 * whatever columns are shown in the body area will also be shown in the column
 * header area, and vice versa. Note that the column header layer maintains its
 * own vertical dimension, however, so it's vertical layer dependency would be a
 * separate data layer. The same is true for the row header layer, only with the
 * vertical instead of the horizontal dimension. The constructors for the column
 * header and row header layers would therefore look something like this:
 * </p>
 *
 * <pre>
 * ILayer columnHeaderLayer = new DimensionallyDependentLayer(
 *         columnHeaderRowDataLayer, bodyLayer, columnHeaderRowDataLayer);
 * ILayer rowHeaderLayer = new DimensionallyDependentLayer(
 *         rowHeaderColumnDataLayer, bodyLayer, rowHeaderColumnDataLayer);
 * </pre>
 */
public class DimensionallyDependentLayer extends AbstractLayer {

    private final IUniqueIndexLayer baseLayer;
    private ILayer horizontalLayerDependency;
    private ILayer verticalLayerDependency;
    private IClientAreaProvider clientAreaProvider;

    protected DimensionallyDependentLayer(IUniqueIndexLayer baseLayer) {
        this.baseLayer = baseLayer;
        this.baseLayer.addLayerListener(this);
    }

    public DimensionallyDependentLayer(
            IUniqueIndexLayer baseLayer, ILayer horizontalLayerDependency, ILayer verticalLayerDependency) {

        this.baseLayer = baseLayer;
        this.baseLayer.addLayerListener(this);

        setHorizontalLayerDependency(horizontalLayerDependency);
        setVerticalLayerDependency(verticalLayerDependency);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        this.baseLayer.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        this.baseLayer.loadState(prefix, properties);
    }

    // Configuration

    @Override
    public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
        this.baseLayer.configure(configRegistry, uiBindingRegistry);
        super.configure(configRegistry, uiBindingRegistry);
    }

    // Dependent layer accessors

    public void setHorizontalLayerDependency(ILayer horizontalLayerDependency) {
        this.horizontalLayerDependency = horizontalLayerDependency;

        // this.horizontalLayerDependency.addLayerListener(new ILayerListener()
        // {
        //
        // @Override
        // public void handleLayerEvent(ILayerEvent event) {
        // if (event instanceof IStructuralChangeEvent) {
        // // TODO refresh horizontal structure
        // }
        // }
        //
        // });
    }

    public void setVerticalLayerDependency(ILayer verticalLayerDependency) {
        this.verticalLayerDependency = verticalLayerDependency;

        // this.verticalLayerDependency.addLayerListener(new ILayerListener() {
        //
        // @Override
        // public void handleLayerEvent(ILayerEvent event) {
        // if (event instanceof IStructuralChangeEvent) {
        // // TODO refresh vertical structure
        // }
        // }
        //
        // });
    }

    public ILayer getHorizontalLayerDependency() {
        return this.horizontalLayerDependency;
    }

    public ILayer getVerticalLayerDependency() {
        return this.verticalLayerDependency;
    }

    public IUniqueIndexLayer getBaseLayer() {
        return this.baseLayer;
    }

    // Commands

    @Override
    public boolean doCommand(ILayerCommand command) {
        // Invoke command handler(s) on the Dimensionally dependent layer
        ILayerCommand clonedCommand = command.cloneCommand();
        if (super.doCommand(clonedCommand)) {
            return true;
        }

        // in case we have a command for a specific region we need to ensure
        // that no other regions are affected
        if (!(command instanceof AbstractRegionCommand)) {
            clonedCommand = command.cloneCommand();
            if (this.horizontalLayerDependency.doCommand(clonedCommand)) {
                return true;
            }

            clonedCommand = command.cloneCommand();
            if (this.verticalLayerDependency.doCommand(clonedCommand)) {
                return true;
            }
        }

        return this.baseLayer.doCommand(command);
    }

    // Events

    @Override
    public ILayerPainter getLayerPainter() {
        return (this.layerPainter != null) ? this.layerPainter : this.baseLayer.getLayerPainter();
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.horizontalLayerDependency.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.horizontalLayerDependency.getPreferredColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return this.horizontalLayerDependency.getColumnIndexByPosition(columnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return this.horizontalLayerDependency.localToUnderlyingColumnPosition(localColumnPosition);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        if (sourceUnderlyingLayer == this.horizontalLayerDependency) {
            return underlyingColumnPosition;
        }
        return this.horizontalLayerDependency
                .underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {

        if (sourceUnderlyingLayer == this.horizontalLayerDependency) {
            return underlyingColumnPositionRanges;
        }
        return this.horizontalLayerDependency
                .underlyingToLocalColumnPositions(sourceUnderlyingLayer, underlyingColumnPositionRanges);
    }

    // Width

    @Override
    public int getWidth() {
        return this.horizontalLayerDependency.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.horizontalLayerDependency.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.horizontalLayerDependency.getColumnWidthByPosition(columnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.horizontalLayerDependency.isColumnPositionResizable(columnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.horizontalLayerDependency.getColumnPositionByX(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.horizontalLayerDependency.getStartXOfColumnPosition(columnPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
        Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
        underlyingLayers.add(this.baseLayer);
        return underlyingLayers;
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.verticalLayerDependency.getRowCount();
    }

    @Override
    public int getPreferredRowCount() {
        return this.verticalLayerDependency.getPreferredRowCount();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        return this.verticalLayerDependency.getRowIndexByPosition(rowPosition);
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        return this.verticalLayerDependency.localToUnderlyingRowPosition(localRowPosition);
    }

    @Override
    public int underlyingToLocalRowPosition(
            ILayer sourceUnderlyingLayer, int underlyingRowPosition) {

        if (sourceUnderlyingLayer == this.verticalLayerDependency) {
            return underlyingRowPosition;
        }
        return this.verticalLayerDependency
                .underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {

        if (sourceUnderlyingLayer == this.verticalLayerDependency) {
            return underlyingRowPositionRanges;
        }
        return this.verticalLayerDependency
                .underlyingToLocalRowPositions(sourceUnderlyingLayer, underlyingRowPositionRanges);
    }

    // Height

    @Override
    public int getHeight() {
        return this.verticalLayerDependency.getHeight();
    }

    @Override
    public int getPreferredHeight() {
        return this.verticalLayerDependency.getPreferredHeight();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        return this.verticalLayerDependency.getRowHeightByPosition(rowPosition);
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        return this.verticalLayerDependency.isRowPositionResizable(rowPosition);
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return this.verticalLayerDependency.getRowPositionByY(y);
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        return this.verticalLayerDependency.getStartYOfRowPosition(rowPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
        Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
        underlyingLayers.add(this.baseLayer);
        return underlyingLayers;
    }

    // Cell features

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        int baseColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, this.baseLayer);
        int baseRowPosition = LayerUtil.convertRowPosition(this, rowPosition, this.baseLayer);
        return this.baseLayer.getDisplayModeByPosition(baseColumnPosition, baseRowPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        int baseColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, this.baseLayer);
        int baseRowPosition = LayerUtil.convertRowPosition(this, rowPosition, this.baseLayer);
        LabelStack labelStack = this.baseLayer.getConfigLabelsByPosition(baseColumnPosition, baseRowPosition);

        IConfigLabelAccumulator configLabelAccumulator = getConfigLabelAccumulator();
        if (configLabelAccumulator != null) {
            configLabelAccumulator.accumulateConfigLabels(labelStack, columnPosition, rowPosition);
        }

        return labelStack;
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int baseColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, this.baseLayer);
        int baseRowPosition = LayerUtil.convertRowPosition(this, rowPosition, this.baseLayer);
        return this.baseLayer.getDataValueByPosition(baseColumnPosition, baseRowPosition);
    }

    // IRegionResolver

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        return this.baseLayer.getRegionLabelsByXY(x, y);
    }

    @Override
    public IClientAreaProvider getClientAreaProvider() {
        return this.clientAreaProvider;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        this.clientAreaProvider = clientAreaProvider;
    }

    @Override
    public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
        return this.baseLayer;
    }

}
