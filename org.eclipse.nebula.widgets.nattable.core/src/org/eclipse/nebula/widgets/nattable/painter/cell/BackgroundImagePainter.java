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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Paints the cell background using an image. Image is repeated to cover the
 * background. Similar to HTML table painting.
 */
public class BackgroundImagePainter extends CellPainterWrapper {

    public final Color separatorColor;
    private Image bgImage;

    /**
     * @param bgImage
     *            to be used for painting the background
     * @since 1.4
     */
    public BackgroundImagePainter(Image bgImage) {
        this(null, bgImage, null);
    }

    /**
     * @param interiorPainter
     *            used for painting the cell contents
     * @param bgImage
     *            to be used for painting the background
     */
    public BackgroundImagePainter(ICellPainter interiorPainter, Image bgImage) {
        this(interiorPainter, bgImage, null);
    }

    /**
     * @param bgImage
     *            to be used for painting the background
     * @param separatorColor
     *            to be used for drawing left and right borders for the cell.
     *            Set to null if the borders are not required.
     * @since 1.4
     */
    public BackgroundImagePainter(Image bgImage, Color separatorColor) {
        this(null, bgImage, separatorColor);
    }

    /**
     * @param interiorPainter
     *            used for painting the cell contents
     * @param bgImage
     *            to be used for painting the background
     * @param separatorColor
     *            to be used for drawing left and right borders for the cell.
     *            Set to null if the borders are not required.
     */
    public BackgroundImagePainter(ICellPainter interiorPainter, Image bgImage, Color separatorColor) {
        super(interiorPainter);
        this.bgImage = bgImage;
        this.separatorColor = separatorColor;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return super.getPreferredWidth(cell, gc, configRegistry) + 4;
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return super.getPreferredHeight(cell, gc, configRegistry) + 4;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
        if (this.bgImage != null) {
            // Save GC settings
            Color originalBackground = gc.getBackground();
            Color originalForeground = gc.getForeground();

            Pattern pattern = new Pattern(Display.getCurrent(), this.bgImage);
            gc.setBackgroundPattern(pattern);

            gc.fillRectangle(rectangle);

            gc.setBackgroundPattern(null);
            pattern.dispose();

            if (isNotNull(this.separatorColor)) {
                gc.setForeground(this.separatorColor);
                gc.drawLine(
                        rectangle.x - 1,
                        rectangle.y,
                        rectangle.x - 1,
                        rectangle.y + rectangle.height);
                gc.drawLine(
                        rectangle.x - 1 + rectangle.width,
                        rectangle.y,
                        rectangle.x - 1 + rectangle.width,
                        rectangle.y + rectangle.height);
            }

            // Restore original GC settings
            gc.setBackground(originalBackground);
            gc.setForeground(originalForeground);
        }

        // Draw interior
        Rectangle interiorBounds = new Rectangle(
                rectangle.x + 2,
                rectangle.y + 2,
                rectangle.width - 4,
                rectangle.height - 4);
        super.paintCell(cell, gc, interiorBounds, configRegistry);
    }

    /**
     *
     * @return The {@link Image} that is used to render the background.
     * @since 1.4
     */
    public Image getBackgroundImage() {
        return this.bgImage;
    }

    /**
     *
     * @param bgImage
     *            The {@link Image} that should be used to render the
     *            background.
     * @since 1.4
     */
    public void setBackgroundImage(Image bgImage) {
        this.bgImage = bgImage;
    }
}
