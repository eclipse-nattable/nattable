/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
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
 * Paints the background of the cell with a gradient sweeping using the style
 * configuration. To configure the gradient sweeping the following style
 * attributes need to be configured in the {@link ConfigRegistry}:
 * <ul>
 * <li>{@link CellStyleAttributes#GRADIENT_FOREGROUND_COLOR} or
 * {@link CellStyleAttributes#FOREGROUND_COLOR}</li>
 * <li>{@link CellStyleAttributes#GRADIENT_BACKGROUND_COLOR} or
 * {@link CellStyleAttributes#BACKGROUND_COLOR}</li>
 * </ul>
 * If none of these values are registered in the {@link ConfigRegistry} the
 * painting is skipped.
 * <p>
 * Can be used as a cell painter or a decorator.
 *
 */
public class GradientBackgroundPainter extends CellPainterWrapper {

    /**
     * @param vertical
     *            if <code>true</code> sweeps from top to bottom, else sweeps
     *            from left to right. <code>false</code> is default
     */
    private boolean vertical;

    /**
     * Creates a {@link GradientBackgroundPainter} with a gradient sweeping from
     * left to right.
     */
    public GradientBackgroundPainter() {
        this(false);
    }

    /**
     * Creates a {@link GradientBackgroundPainter} where the sweeping direction
     * can be set.
     *
     * @param vertical
     *            if <code>true</code> sweeps from top to bottom, else sweeps
     *            from left to right. <code>false</code> is default
     */
    public GradientBackgroundPainter(boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * Creates a {@link GradientBackgroundPainter} as wrapper for the given
     * painter with a gradient sweeping from left to right.
     *
     * @param painter
     *            The {@link ICellPainter} that is wrapped by this
     *            {@link GradientBackgroundPainter}
     */
    public GradientBackgroundPainter(ICellPainter painter) {
        this(painter, false);
    }

    /**
     * Creates a {@link GradientBackgroundPainter} as wrapper for the given
     * painter where the sweeping direction can be set.
     *
     * @param painter
     *            The {@link ICellPainter} that is wrapped by this
     *            {@link GradientBackgroundPainter}
     * @param vertical
     *            if <code>true</code> sweeps from top to bottom, else sweeps
     *            from left to right. <code>false</code> is default
     */
    public GradientBackgroundPainter(ICellPainter painter, boolean vertical) {
        super(painter);
        this.vertical = vertical;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        Color foregroundColor = getForeGroundColour(cell, configRegistry);
        Color backgroundColor = getBackgroundColour(cell, configRegistry);
        if (backgroundColor != null && foregroundColor != null) {
            Color originalForeground = gc.getForeground();
            Color originalBackground = gc.getBackground();

            gc.setForeground(foregroundColor);
            gc.setBackground(backgroundColor);
            gc.fillGradientRectangle(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    this.vertical);

            gc.setForeground(originalForeground);
            gc.setBackground(originalBackground);
        }

        super.paintCell(cell, gc, bounds, configRegistry);
    }

    /**
     * Searches the foreground color to be used for gradient sweeping. First
     * checks the
     * {@link org.eclipse.nebula.widgets.nattable.config.ConfigRegistry} if
     * there is a value for the attribute
     * {@link CellStyleAttributes#GRADIENT_FOREGROUND_COLOR} is registered. If
     * there is one this value will be returned, if not it is checked if there
     * is a value registered for {@link CellStyleAttributes#FOREGROUND_COLOR}
     * and returned. If there is no value registered for any of these
     * attributes, <code>null</code> will be returned which will skip the
     * painting.
     *
     * @param cell
     *            The
     *            {@link org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell}
     *            for which the style attributes should be retrieved out of the
     *            {@link org.eclipse.nebula.widgets.nattable.config.ConfigRegistry}
     * @param configRegistry
     *            The
     *            {@link org.eclipse.nebula.widgets.nattable.config.ConfigRegistry}
     *            to retrieve the attribute values from.
     * @return The {@link Color} to use as foreground color of the gradient
     *         sweeping or <code>null</code> if none was configured.
     */
    protected Color getForeGroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
        Color fgColor = CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR);
        return fgColor != null ? fgColor : CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
    }

    /**
     * Searches the background color to be used for gradient sweeping. First
     * checks the
     * {@link org.eclipse.nebula.widgets.nattable.config.ConfigRegistry} if
     * there is a value for the attribute
     * {@link CellStyleAttributes#GRADIENT_BACKGROUND_COLOR} is registered. If
     * there is one this value will be returned, if not it is checked if there
     * is a value registered for {@link CellStyleAttributes#BACKGROUND_COLOR}
     * and returned. If there is no value registered for any of these
     * attributes, <code>null</code> will be returned which will skip the
     * painting.
     *
     * @param cell
     *            The
     *            {@link org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell}
     *            for which the style attributes should be retrieved out of the
     *            {@link org.eclipse.nebula.widgets.nattable.config.ConfigRegistry}
     * @param configRegistry
     *            The
     *            {@link org.eclipse.nebula.widgets.nattable.config.ConfigRegistry}
     *            to retrieve the attribute values from.
     * @return The {@link Color} to use as background color of the gradient
     *         sweeping or <code>null</code> if none was configured.
     */
    protected Color getBackgroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
        Color bgColor = CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR);
        return bgColor != null ? bgColor : CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
    }

    /**
     *
     * @return <code>true</code> if sweeps from top to bottom, else sweeps from
     *         left to right. Default is <code>false</code>
     * @since 1.4
     */
    public boolean isVertical() {
        return this.vertical;
    }

    /**
     *
     * @param vertical
     *            <code>true</code> if should sweep from top to bottom,
     *            <code>false</code> if it should sweep from left to right.
     *            Default is <code>false</code>
     * @since 1.4
     */
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }
}
