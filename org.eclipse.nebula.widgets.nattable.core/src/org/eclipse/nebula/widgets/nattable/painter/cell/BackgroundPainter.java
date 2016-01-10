/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Paints the background of the cell using the color from the cell style. If no
 * background color is registered in the {@link ConfigRegistry} the painting is
 * skipped.
 * <p>
 * Example: The {@link TextPainter} inherits this and uses the paint method in
 * this class to paint the background of the cell.
 *
 * Can be used as a cell painter or a decorator.
 */
public class BackgroundPainter extends CellPainterWrapper {

    public BackgroundPainter() {}

    public BackgroundPainter(ICellPainter painter) {
        super(painter);
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        Color backgroundColor = getBackgroundColour(cell, configRegistry);
        if (backgroundColor != null) {
            Color originalBackground = gc.getBackground();

            gc.setBackground(backgroundColor);
            gc.fillRectangle(bounds);

            gc.setBackground(originalBackground);
        }

        super.paintCell(cell, gc, bounds, configRegistry);
    }

    protected Color getBackgroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
        return CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
    }
}
