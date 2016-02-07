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

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;

/**
 * Layer for the row header of the grid layer.
 */
public class RowHeaderLayer extends DimensionallyDependentLayer {

    private final SelectionLayer[] selectionLayer;

    /**
     * Creates a row header layer using the default configuration and painter.
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            The SelectionLayer needed to respond to selection events.
     */
    public RowHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer verticalLayerDependency,
            SelectionLayer selectionLayer) {
        this(baseLayer, verticalLayerDependency, selectionLayer, true);
    }

    /**
     * Creates a row header layer using the default configuration and painter.
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            0 to multiple SelectionLayer needed to respond to selection
     *            events.
     * @since 1.4
     */
    public RowHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer verticalLayerDependency,
            SelectionLayer... selectionLayer) {
        this(baseLayer, verticalLayerDependency, selectionLayer, true);
    }

    /**
     * Creates a row header layer using the default painter.
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            The SelectionLayer needed to respond to selection events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     */
    public RowHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer verticalLayerDependency,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {
        this(baseLayer, verticalLayerDependency, selectionLayer, useDefaultConfiguration, null);
    }

    /**
     * Creates a row header layer using the default painter.
     *
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            0 to multiple SelectionLayer needed to respond to selection
     *            events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     * @since 1.4
     */
    public RowHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer verticalLayerDependency,
            SelectionLayer[] selectionLayer,
            boolean useDefaultConfiguration) {
        this(baseLayer, verticalLayerDependency, selectionLayer, useDefaultConfiguration, null);
    }

    /**
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            The SelectionLayer needed to respond to selection events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     * @param layerPainter
     *            The painter for this layer or <code>null</code> to use the
     *            painter of the base layer
     */
    public RowHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer verticalLayerDependency,
            SelectionLayer selectionLayer,
            boolean useDefaultConfiguration,
            ILayerPainter layerPainter) {

        this(baseLayer, verticalLayerDependency,
                selectionLayer != null ? new SelectionLayer[] { selectionLayer } : new SelectionLayer[] {},
                useDefaultConfiguration, layerPainter);
    }

    /**
     * @param baseLayer
     *            The base layer for this layer, typically a DataLayer.
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            body layer.
     * @param selectionLayer
     *            0 to multiple SelectionLayer needed to respond to selection
     *            events.
     * @param useDefaultConfiguration
     *            Flag to configure whether to use the default configuration or
     *            not.
     * @param layerPainter
     *            The painter for this layer or <code>null</code> to use the
     *            painter of the base layer
     * @since 1.4
     */
    public RowHeaderLayer(
            IUniqueIndexLayer baseLayer,
            ILayer verticalLayerDependency,
            SelectionLayer[] selectionLayer,
            boolean useDefaultConfiguration,
            ILayerPainter layerPainter) {

        super(baseLayer, baseLayer, verticalLayerDependency);

        if (selectionLayer == null) {
            this.selectionLayer = new SelectionLayer[] {};
        } else {
            this.selectionLayer = selectionLayer;
        }

        this.layerPainter = layerPainter;

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultRowHeaderLayerConfiguration());
        }
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        String displayMode = super.getDisplayModeByPosition(columnPosition, rowPosition);
        if (this.selectionLayer.length > 0) {
            int selectionLayerRowPosition = LayerUtil.convertRowPosition(this, rowPosition, this.selectionLayer[0]);
            for (SelectionLayer sl : this.selectionLayer) {
                if (sl.isRowPositionSelected(selectionLayerRowPosition)) {
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
            final int selectionLayerRowPosition = LayerUtil.convertRowPosition(this, rowPosition, this.selectionLayer[0]);
            boolean fullySelected = true;
            for (SelectionLayer sl : this.selectionLayer) {
                if (!sl.isRowPositionFullySelected(selectionLayerRowPosition)) {
                    fullySelected = false;
                    break;
                }
            }

            if (fullySelected) {
                labelStack.addLabel(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
            }
        }

        return labelStack;
    }

}
