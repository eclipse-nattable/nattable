/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;

/**
 * Layer for the top left header corner of the grid layer
 */
public class CornerLayer extends DimensionallyDependentLayer {

    /**
     * Creates a corner header layer using the default configuration and painter
     *
     * @param baseLayer
     *            The data provider for this layer
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            row header layer
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            column header layer
     */
    public CornerLayer(IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency, ILayer verticalLayerDependency) {
        super(baseLayer, horizontalLayerDependency, verticalLayerDependency);
    }

    /**
     * @param baseLayer
     *            The data provider for this layer
     * @param horizontalLayerDependency
     *            The layer to link the horizontal dimension to, typically the
     *            row header layer
     * @param verticalLayerDependency
     *            The layer to link the vertical dimension to, typically the
     *            column header layer
     * @param useDefaultConfiguration
     *            If default configuration should be applied to this layer (at
     *            moment none)
     * @param layerPainter
     *            The painter for this layer or <code>null</code> to use the
     *            painter of the base layer
     */
    public CornerLayer(IUniqueIndexLayer baseLayer,
            ILayer horizontalLayerDependency, ILayer verticalLayerDependency,
            boolean useDefaultConfiguration, ILayerPainter layerPainter) {
        super(baseLayer, horizontalLayerDependency, verticalLayerDependency);

        this.layerPainter = layerPainter;
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        return new LayerCell(this, 0, 0, columnPosition, rowPosition,
                getHorizontalLayerDependency().getColumnCount(),
                getVerticalLayerDependency().getRowCount());
    }

}
