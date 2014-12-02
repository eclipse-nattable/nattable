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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Implementations are responsible for painting a cell.
 *
 * Custom {@link ICellPainter} can be registered in the {@link IConfigRegistry}.
 * This is a mechanism for plugging in custom cell painting.
 *
 * @see PercentageBarCellPainter
 */
public interface ICellPainter {

    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds,
            IConfigRegistry configRegistry);

    /**
     * Get the preferred width of the cell when rendered by this painter. Used
     * for auto-resize.
     *
     * @param cell
     *            The cell for which the preferred width is requested.
     * @param gc
     *            The GC that is used for rendering.
     * @param configRegistry
     *            The IConfigRegistry that contains the configuration used for
     *            rendering.
     * @return The preferred width of the given cell when rendered by this
     *         painter.
     */
    public int getPreferredWidth(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry);

    /**
     * Get the preferred height of the cell when rendered by this painter. Used
     * for auto-resize.
     *
     * @param cell
     *            The cell for which the preferred height is requested.
     * @param gc
     *            The GC that is used for rendering.
     * @param configRegistry
     *            The IConfigRegistry that contains the configuration used for
     *            rendering.
     * @return The preferred height of the given cell when rendered by this
     *         painter.
     */
    public int getPreferredHeight(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry);

    public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc,
            Rectangle adjustedCellBounds, IConfigRegistry configRegistry);

}
