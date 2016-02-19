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

import org.eclipse.nebula.widgets.nattable.columnRename.DisplayColumnRenameDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHeaderCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHelper;
import org.eclipse.nebula.widgets.nattable.columnRename.event.RenameColumnHeaderEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;

/**
 * Responsible for rendering, event handling etc on the column headers.
 */
public class ColumnHeaderLayer extends DimensionallyDependentLayer {

    private final SelectionLayer[] selectionLayer;

    protected RenameColumnHelper renameColumnHelper;

    /**
     * Creates a column header layer using the default configuration and painter
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            The SelectionLayer needed to respond to selection events.
     */
    public ColumnHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency,
            SelectionLayer selectionLayer) {

        this(baseLayer, horizontalLayerDependency, selectionLayer, true);
    }

    /**
     * Creates a column header layer using the default configuration and painter
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            0 to multiple SelectionLayer needed to respond to selection
     *            events.
     * @since 1.4
     */
    public ColumnHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency,
            SelectionLayer... selectionLayer) {

        this(baseLayer, horizontalLayerDependency, selectionLayer, true);
    }

    /**
     * Creates a row header layer using the default painter.
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            The SelectionLayer needed to respond to selection events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     */
    public ColumnHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {

        this(baseLayer, horizontalLayerDependency, selectionLayer, useDefaultConfiguration, null);
    }

    /**
     * Creates a row header layer using the default painter.
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            0 to multiple SelectionLayer needed to respond to selection
     *            events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     * @since 1.4
     */
    public ColumnHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency,
            SelectionLayer[] selectionLayer,
            boolean useDefaultConfiguration) {

        this(baseLayer, horizontalLayerDependency, selectionLayer, useDefaultConfiguration, null);
    }

    /**
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            The SelectionLayer needed to respond to selection events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     * @param layerPainter
     *            The painter for this layer or <code>null</code> to use the
     *            painter of the base layer.
     */
    public ColumnHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration,
            ILayerPainter layerPainter) {

        this(baseLayer, horizontalLayerDependency,
                selectionLayer != null ? new SelectionLayer[] { selectionLayer } : new SelectionLayer[] {},
                useDefaultConfiguration, layerPainter);
    }

    /**
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            0 to multiple SelectionLayer needed to respond to selection
     *            events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     * @param layerPainter
     *            The painter for this layer or <code>null</code> to use the
     *            painter of the base layer.
     * @since 1.4
     */
    public ColumnHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency,
            SelectionLayer[] selectionLayer,
            boolean useDefaultConfiguration,
            ILayerPainter layerPainter) {

        super(baseLayer, horizontalLayerDependency, baseLayer);

        if (selectionLayer == null) {
            this.selectionLayer = new SelectionLayer[] {};
        } else {
            this.selectionLayer = selectionLayer;
        }

        this.layerPainter = layerPainter;

        this.renameColumnHelper = new RenameColumnHelper(this);
        registerPersistable(this.renameColumnHelper);

        for (SelectionLayer sl : this.selectionLayer) {
            sl.addLayerListener(new ColumnHeaderSelectionListener(this));
        }
        registerCommandHandlers();

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultColumnHeaderLayerConfiguration());
        }
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        String displayMode = super.getDisplayModeByPosition(columnPosition, rowPosition);
        if (this.selectionLayer.length > 0) {
            int selectionLayerColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, this.selectionLayer[0]);
            for (SelectionLayer sl : this.selectionLayer) {
                if (sl.isColumnPositionSelected(selectionLayerColumnPosition)) {
                    if (DisplayMode.HOVER.equals(displayMode)) {
                        return DisplayMode.SELECT_HOVER;
                    }
                    return DisplayMode.SELECT;
                }
            }
        }
        return displayMode;
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack labelStack = super.getConfigLabelsByPosition(columnPosition, rowPosition);

        if (this.selectionLayer.length > 0) {
            final int selectionLayerColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, this.selectionLayer[0]);
            boolean fullySelected = true;
            for (SelectionLayer sl : this.selectionLayer) {
                if (!sl.isColumnPositionFullySelected(selectionLayerColumnPosition)) {
                    fullySelected = false;
                    break;
                }
            }

            if (fullySelected) {
                labelStack.addLabel(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
            }
        }

        return labelStack;
    }

    @Deprecated
    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer[0];
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        if (isColumnRenamed(columnIndex)) {
            return getRenamedColumnLabelByIndex(columnIndex);
        }
        return super.getDataValueByPosition(columnPosition, rowPosition);
    }

    // Configuration

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new RenameColumnHeaderCommandHandler(this));
        registerCommandHandler(new DisplayColumnRenameDialogCommandHandler(this));
    }

    // Column header renaming

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                Collection<StructuralDiff> columnDiffs = structuralChangeEvent.getColumnDiffs();

                if (columnDiffs != null && !columnDiffs.isEmpty()
                        && !StructuralChangeEventHelper.isReorder(columnDiffs)) {
                    this.renameColumnHelper.handleStructuralChanges(columnDiffs);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    /**
     * @return column header as defined by the data source
     */
    public String getOriginalColumnLabel(int columnPosition) {
        Object dataValue = super.getDataValueByPosition(columnPosition, 0);
        return dataValue != null ? dataValue.toString() : ""; //$NON-NLS-1$
    }

    /**
     * @return renamed column header if the column has been renamed, NULL
     *         otherwise
     */
    public String getRenamedColumnLabel(int columnPosition) {
        int index = getColumnIndexByPosition(columnPosition);
        return getRenamedColumnLabelByIndex(index);
    }

    /**
     * @return renamed column header if the column has been renamed, NULL
     *         otherwise
     */
    public String getRenamedColumnLabelByIndex(int columnIndex) {
        return this.renameColumnHelper.getRenamedColumnLabel(columnIndex);
    }

    /**
     * @return TRUE if the column at the given index has been given a custom
     *         name by the user.
     */
    public boolean isColumnRenamed(int columnIndex) {
        return this.renameColumnHelper.isColumnRenamed(columnIndex);
    }

    public boolean renameColumnPosition(int columnPosition, String customColumnName) {
        boolean renamed = this.renameColumnHelper.renameColumnPosition(columnPosition, customColumnName);
        if (renamed) {
            fireLayerEvent(new RenameColumnHeaderEvent(this, columnPosition));
        }
        return renamed;
    }

    public boolean renameColumnIndex(int columnIndex, String customColumnName) {
        boolean renamed = this.renameColumnHelper.renameColumnIndex(columnIndex, customColumnName);
        if (renamed) {
            // search for the bottom layer in the horizontal dependency to
            // create the event for index and correct layer
            ILayer baseLayer = getHorizontalLayerDependency();
            while (baseLayer.getUnderlyingLayerByPosition(0, 0) != null) {
                baseLayer = baseLayer.getUnderlyingLayerByPosition(0, 0);
            }

            baseLayer.fireLayerEvent(new RenameColumnHeaderEvent(baseLayer, columnIndex));
        }
        return renamed;
    }

}
