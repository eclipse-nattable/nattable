/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.layer;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Classes implementing this interface are responsible for painting to the
 * relevant {@link Device}. Every layer has a layer painter. A layer can
 * contribute to painting by providing its own painter.
 */
public interface ILayerPainter {

    /**
     * @param natLayer
     *            The layer to paint.
     * @param gc
     *            GC used for painting
     * @param xOffset
     *            x offset of the layer from the origin of the table
     * @param yOffset
     *            y offset of the layer from the origin of the table
     * @param rectangle
     *            area the layer can paint in
     * @param configuration
     *            {@link IConfigRegistry} in use by NatTable. Useful for looking
     *            up associated painters.
     */
    public void paintLayer(ILayer natLayer, GC gc,
            int xOffset, int yOffset,
            Rectangle rectangle, IConfigRegistry configuration);

    /**
     * This method is used to adjust the cell bounds when painting the layer.
     * This is most often used to reduce the size of the cell to accommodate
     * grid lines.
     *
     * @param columnPosition
     *            The column position.
     * @param rowPosition
     *            The row position.
     * @param cellBounds
     *            The actual cell bounds.
     * @return The adjusted cell bounds.
     */
    public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle cellBounds);

}
