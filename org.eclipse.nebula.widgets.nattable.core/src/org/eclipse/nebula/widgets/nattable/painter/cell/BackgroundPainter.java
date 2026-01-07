/*******************************************************************************
 * Copyright (c) 2012, 2026 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
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

    public BackgroundPainter() {
    }

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

    /**
     * Get the background color for the given cell.
     *
     * @param cell
     *            The {@link ILayerCell} to get the background color for.
     * @param configRegistry
     *            The {@link ConfigRegistry} needed to retrieve the color.
     * @return The background color.
     */
    protected Color getBackgroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
        Boolean blend = configRegistry.getConfigAttribute(
                CellConfigAttributes.BLEND_SELECTION_BACKGROUND,
                DisplayMode.NORMAL,
                cell.getConfigLabels());
        // if blending is enabled and the cell is selected, return blended color
        if (blend != null && blend
                && (cell.getDisplayMode() == DisplayMode.SELECT || cell.getDisplayMode() == DisplayMode.SELECT_HOVER)) {
            return getBlendedBackgroundColour(cell, configRegistry);
        }

        return CellStyleUtil
                .getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
    }

    /**
     * Get the blended background color for the given cell by blending the
     * normal and the select background colors.
     *
     * @param cell
     *            The {@link ILayerCell} to get the blended background color
     *            for.
     * @param configRegistry
     *            The {@link ConfigRegistry} needed to retrieve the colors.
     * @return The blended background color.
     *
     * @since 2.7
     */
    protected Color getBlendedBackgroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
        // get background color for DisplayMode.NORMAL
        Color normalBackground =
                new CellStyleProxy(
                        configRegistry,
                        DisplayMode.NORMAL,
                        cell.getConfigLabels())
                                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

        // get background color for DisplayMode.SELECT or DisplayMode.SELECT_HOVER
        Color selectBackground =
                new CellStyleProxy(
                        configRegistry,
                        cell.getDisplayMode(),
                        cell.getConfigLabels())
                                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

        // blend both colors
        RGB background = normalBackground.getRGB();
        background = GUIHelper.blend(background, selectBackground.getRGB());
        return GUIHelper.getColor(background);
    }
}
