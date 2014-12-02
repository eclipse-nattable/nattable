/*******************************************************************************
 * Copyright (c) 2012, 2013 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.InvertedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Rectangle;

public class InvertedLayer implements IUniqueIndexLayer {

    private IUniqueIndexLayer underlyingLayer;

    public InvertedLayer(IUniqueIndexLayer underlyingLayer) {
        this.underlyingLayer = underlyingLayer;
    }

    // ILayerListener

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        this.underlyingLayer.handleLayerEvent(event);
    }

    // IPersistable

    @Override
    public void saveState(String prefix, Properties properties) {
        this.underlyingLayer.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.underlyingLayer.loadState(prefix, properties);
    }

    // Dispose

    @Override
    public void dispose() {
        this.underlyingLayer.dispose();
    }

    // Persistence

    @Override
    public void registerPersistable(IPersistable persistable) {
        this.underlyingLayer.registerPersistable(persistable);
    }

    @Override
    public void unregisterPersistable(IPersistable persistable) {
        this.underlyingLayer.unregisterPersistable(persistable);
    }

    // Configuration

    @Override
    public void configure(ConfigRegistry configRegistry,
            UiBindingRegistry uiBindingRegistry) {
        this.underlyingLayer.configure(configRegistry, uiBindingRegistry);
    }

    // Region

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        return this.underlyingLayer.getRegionLabelsByXY(y, x);
    }

    // Commands

    @Override
    public boolean doCommand(ILayerCommand command) {
        return this.underlyingLayer.doCommand(command);
    }

    @Override
    public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
        this.underlyingLayer.registerCommandHandler(commandHandler);
    }

    @Override
    public void unregisterCommandHandler(
            Class<? extends ILayerCommand> commandClass) {
        this.underlyingLayer.unregisterCommandHandler(commandClass);
    }

    // Events

    @Override
    public void fireLayerEvent(ILayerEvent event) {
        this.underlyingLayer.fireLayerEvent(event);
    }

    @Override
    public void addLayerListener(ILayerListener listener) {
        this.underlyingLayer.addLayerListener(listener);
    }

    @Override
    public void removeLayerListener(ILayerListener listener) {
        this.underlyingLayer.removeLayerListener(listener);
    }

    @Override
    public boolean hasLayerListener(
            Class<? extends ILayerListener> layerListenerClass) {
        return this.underlyingLayer.hasLayerListener(layerListenerClass);
    }

    @Override
    public ILayerPainter getLayerPainter() {
        return this.underlyingLayer.getLayerPainter();
    }

    // Client area

    @Override
    public IClientAreaProvider getClientAreaProvider() {
        return this.underlyingLayer.getClientAreaProvider();
    }

    @Override
    public void setClientAreaProvider(
            final IClientAreaProvider clientAreaProvider) {
        this.underlyingLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return InvertUtil.invertRectangle(clientAreaProvider
                        .getClientArea());
            }
        });
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.underlyingLayer.getRowCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.underlyingLayer.getPreferredRowCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return this.underlyingLayer.getRowIndexByPosition(columnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return this.underlyingLayer
                .localToUnderlyingRowPosition(localColumnPosition);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {
        return this.underlyingLayer.underlyingToLocalRowPosition(
                sourceUnderlyingLayer, underlyingColumnPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        return this.underlyingLayer.underlyingToLocalRowPositions(
                sourceUnderlyingLayer, underlyingColumnPositionRanges);
    }

    // Width

    @Override
    public int getWidth() {
        return this.underlyingLayer.getHeight();
    }

    @Override
    public int getPreferredWidth() {
        return this.underlyingLayer.getPreferredHeight();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.underlyingLayer.getRowHeightByPosition(columnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.underlyingLayer.isRowPositionResizable(columnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.underlyingLayer.getRowPositionByY(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.underlyingLayer.getStartYOfRowPosition(columnPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(
            int columnPosition) {
        return this.underlyingLayer.getUnderlyingLayersByRowPosition(columnPosition);
    }

    // Unique index

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.underlyingLayer.getRowPositionByIndex(columnIndex);
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.underlyingLayer.getColumnCount();
    }

    @Override
    public int getPreferredRowCount() {
        return this.underlyingLayer.getPreferredColumnCount();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        return this.underlyingLayer.getColumnIndexByPosition(rowPosition);
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        return this.underlyingLayer
                .localToUnderlyingColumnPosition(localRowPosition);
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer,
            int underlyingRowPosition) {
        return this.underlyingLayer.underlyingToLocalColumnPosition(
                sourceUnderlyingLayer, underlyingRowPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingRowPositionRanges) {
        return this.underlyingLayer.underlyingToLocalColumnPositions(
                sourceUnderlyingLayer, underlyingRowPositionRanges);
    }

    // Height

    @Override
    public int getHeight() {
        return this.underlyingLayer.getWidth();
    }

    @Override
    public int getPreferredHeight() {
        return this.underlyingLayer.getPreferredWidth();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        return this.underlyingLayer.getColumnWidthByPosition(rowPosition);
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        return this.underlyingLayer.isColumnPositionResizable(rowPosition);
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return this.underlyingLayer.getColumnPositionByX(y);
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        return this.underlyingLayer.getStartXOfColumnPosition(rowPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
        return this.underlyingLayer.getUnderlyingLayersByColumnPosition(rowPosition);
    }

    // Unique index

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.underlyingLayer.getColumnPositionByIndex(rowIndex);
    }

    // Cell features

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        ILayerCell cell = this.underlyingLayer.getCellByPosition(rowPosition,
                columnPosition);
        if (cell != null)
            return new InvertedLayerCell(cell);
        else
            return null;
        // return underlyingLayer.getCellByPosition(rowPosition,
        // columnPosition);
    }

    @Override
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
        return InvertUtil.invertRectangle(this.underlyingLayer.getBoundsByPosition(
                rowPosition, columnPosition));
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getDisplayModeByPosition(rowPosition,
                columnPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition,
            int rowPosition) {
        return this.underlyingLayer.getConfigLabelsByPosition(rowPosition,
                columnPosition);
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getDataValueByPosition(rowPosition,
                columnPosition);
    }

    @Override
    public ILayer getUnderlyingLayerByPosition(int columnPosition,
            int rowPosition) {
        return this.underlyingLayer.getUnderlyingLayerByPosition(rowPosition,
                columnPosition);
    }

    @Override
    public ICellPainter getCellPainter(int columnPosition, int rowPosition,
            ILayerCell cell, IConfigRegistry configRegistry) {
        return this.underlyingLayer.getCellPainter(rowPosition, columnPosition,
                cell, configRegistry);
    }

}
