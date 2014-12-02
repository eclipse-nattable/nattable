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
package org.eclipse.nebula.widgets.nattable.painter.cell.decorator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class XPBackgroundDecorator extends BackgroundPainter {

    public final Color separatorColor;

    public final Color gradientColor1;
    public final Color gradientColor2;
    public final Color gradientColor3;

    public final Color highlightColor1;
    public final Color highlightColor2;
    public final Color highlightColor3;

    public XPBackgroundDecorator(ICellPainter interiorPainter) {
        super(interiorPainter);

        this.separatorColor = GUIHelper.getColor(199, 197, 178);

        this.gradientColor1 = GUIHelper.getColor(226, 222, 205);
        this.gradientColor2 = GUIHelper.getColor(214, 210, 194);
        this.gradientColor3 = GUIHelper.getColor(203, 199, 184);

        this.highlightColor1 = GUIHelper.getColor(250, 171, 0);
        this.highlightColor2 = GUIHelper.getColor(252, 194, 71);
        this.highlightColor3 = GUIHelper.getColor(250, 178, 24);
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry) {
        return super.getPreferredWidth(cell, gc, configRegistry) + 4;
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc,
            IConfigRegistry configRegistry) {
        return super.getPreferredHeight(cell, gc, configRegistry) + 4;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle,
            IConfigRegistry configRegistry) {
        // Draw background
        super.paintCell(cell, gc, rectangle, configRegistry);

        // Draw interior
        Rectangle interiorBounds = new Rectangle(rectangle.x + 2,
                rectangle.y + 2, rectangle.width - 4, rectangle.height - 4);
        super.paintCell(cell, gc, interiorBounds, configRegistry);

        // Save GC settings
        Color originalBackground = gc.getBackground();
        Color originalForeground = gc.getForeground();

        // Draw separator
        int x = rectangle.x;
        gc.setForeground(GUIHelper.COLOR_WHITE);
        gc.drawLine(x, rectangle.y + 3, x, rectangle.y + rectangle.height - 6);

        x = rectangle.x + rectangle.width - 1;
        gc.setForeground(this.separatorColor);
        gc.drawLine(x, rectangle.y + 3, x, rectangle.y + rectangle.height - 6);

        // Restore GC settings
        gc.setBackground(originalBackground);
        gc.setForeground(originalForeground);

        // Draw bottom edge
        boolean isHighlight = false;

        int y = rectangle.y + rectangle.height - 3;
        gc.setForeground(isHighlight ? this.highlightColor1 : this.gradientColor1);
        gc.drawLine(rectangle.x, y, rectangle.x + rectangle.width, y);

        y++;
        gc.setForeground(isHighlight ? this.highlightColor2 : this.gradientColor2);
        gc.drawLine(rectangle.x, y, rectangle.x + rectangle.width, y);

        y++;
        gc.setForeground(isHighlight ? this.highlightColor3 : this.gradientColor3);
        gc.drawLine(rectangle.x, y, rectangle.x + rectangle.width, y);
    }

}
