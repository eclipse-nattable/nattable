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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractCellPainter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Painter fixture for unit tests. Helps probe state after a paint call has been
 * made to it. Does not do any actual painting.
 */
public class CellPainterFixture extends AbstractCellPainter {

    private ILayerCell cell;
    private Rectangle bounds;
    private IConfigRegistry configRegistry;
    private boolean painted;

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return cell.getBounds().height;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return cell.getBounds().width;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        this.painted = true;
        this.cell = cell;
        this.bounds = bounds;
        this.configRegistry = configRegistry;
    }

    // Getters

    public ILayerCell getLastPaintedCell() {
        return this.cell;
    }

    public Rectangle getLastPaintedBounds() {
        return this.bounds;
    }

    public IConfigRegistry getLastPaintedConfigRegistry() {
        return this.configRegistry;
    }

    public boolean isPainted() {
        return this.painted;
    }

}
