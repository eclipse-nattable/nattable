/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NatTableConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Helper class to handle cell styles.
 */
public final class CellStyleUtil {

    private CellStyleUtil() {
        // empty default constructor
    }

    /**
     * Return an {@link IStyle} for a cell that contains the transitive style
     * informations.
     *
     * @param cell
     *            The {@link ILayerCell} for which the style information is
     *            requested.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the transitive
     *            style information.
     * @return The transitive style information for the given cell.
     */
    public static IStyle getCellStyle(ILayerCell cell, IConfigRegistry configRegistry) {
        return new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
    }

    /**
     * Calculates the padding needed at the left for horizontal alignment.
     *
     * @param cellStyle
     *            The style information from which the horizontal alignment
     *            configuration is extracted.
     * @param rectangle
     *            The rectangle to paint into.
     * @param contentWidth
     *            The width of the content that should be painted into the
     *            rectangle.
     * @return The padding needed at the left to align the content horizontally.
     */
    public static int getHorizontalAlignmentPadding(
            IStyle cellStyle,
            Rectangle rectangle,
            int contentWidth) {

        HorizontalAlignmentEnum horizontalAlignment =
                cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
        return getHorizontalAlignmentPadding(horizontalAlignment, rectangle, contentWidth);
    }

    /**
     * Calculates the padding needed at the left for horizontal alignment.
     *
     * @param horizontalAlignment
     *            The horizontal alignment that should be applied. Defaults to
     *            {@link HorizontalAlignmentEnum#CENTER} if <code>null</code>.
     * @param rectangle
     *            The rectangle to paint into.
     * @param contentWidth
     *            The width of the content that should be painted into the
     *            rectangle.
     * @return The padding needed at the left to align the content horizontally.
     */
    public static int getHorizontalAlignmentPadding(
            HorizontalAlignmentEnum horizontalAlignment,
            Rectangle rectangle,
            int contentWidth) {

        if (horizontalAlignment == null) {
            horizontalAlignment = HorizontalAlignmentEnum.CENTER;
        }

        int padding = 0;

        switch (horizontalAlignment) {
            case CENTER:
                padding = (rectangle.width - contentWidth) / 2;
                break;
            case RIGHT:
                padding = rectangle.width - contentWidth;
                break;
        }

        if (padding < 0) {
            padding = 0;
        }

        return padding;
    }

    /**
     * Calculates the padding needed at the top for vertical alignment.
     *
     * @param cellStyle
     *            The style information from which the vertical alignment
     *            configuration is extracted.
     * @param rectangle
     *            The rectangle to paint into.
     * @param contentHeight
     *            The height of the content that should be painted into the
     *            rectangle.
     * @return The padding needed at the top to align the content vertically.
     */
    public static int getVerticalAlignmentPadding(
            IStyle cellStyle,
            Rectangle rectangle,
            int contentHeight) {

        VerticalAlignmentEnum verticalAlignment =
                cellStyle.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
        return getVerticalAlignmentPadding(verticalAlignment, rectangle, contentHeight);
    }

    /**
     * Calculate padding needed at the top to align vertically. Defaults to
     * MIDDLE vertical alignment.
     */
    /**
     * Calculates the padding needed at the top for vertical alignment.
     *
     * @param verticalAlignment
     *            The vertical alignment that should be applied. Defaults to
     *            {@link VerticalAlignmentEnum#MIDDLE} if <code>null</code>.
     * @param rectangle
     *            The rectangle to paint into.
     * @param contentHeight
     *            The height of the content that should be painted into the
     *            rectangle.
     * @return The padding needed at the top to align the content vertically.
     */
    public static int getVerticalAlignmentPadding(
            VerticalAlignmentEnum verticalAlignment,
            Rectangle rectangle,
            int contentHeight) {

        if (verticalAlignment == null) {
            verticalAlignment = VerticalAlignmentEnum.MIDDLE;
        }

        int padding = 0;

        switch (verticalAlignment) {
            case MIDDLE:
                padding = (rectangle.height - contentHeight) / 2;
                break;
            case BOTTOM:
                padding = rectangle.height - contentHeight;
                break;
        }

        if (padding < 0) {
            padding = 0;
        }

        return padding;
    }

    /**
     * Collects all background colors configured for the given
     * {@link ILayerCell}.
     *
     * @param cell
     *            The {@link ILayerCell} for which the background colors are
     *            requested.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the transitive
     *            style information.
     * @param displayMode
     *            The {@link DisplayMode} for which the background color
     *            configuration is requested.
     * @return The collection of all background colors that are configured
     *         transitively for the given cell.
     */
    public static List<Color> getAllBackgroundColors(
            ILayerCell cell,
            IConfigRegistry configRegistry,
            String displayMode) {

        ArrayList<Color> colors = new ArrayList<>();

        for (String configLabel : cell.getConfigLabels().getLabels()) {
            IStyle cellStyle = configRegistry.getSpecificConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    displayMode,
                    configLabel);

            if (cellStyle != null) {
                Color color = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
                if (color != null) {
                    colors.add(color);
                }
            }
        }

        return colors;
    }

    /**
     * Extracts the {@link Font} from the given cell style and returns a scaled
     * variant if needed.
     *
     * @param cellStyle
     *            The {@link IStyle} to extract the {@link Font} from.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to check for the scaling
     *            factor.
     * @return The {@link Font} to use.
     * @since 2.0
     */
    public static Font getFont(IStyle cellStyle, IConfigRegistry configRegistry) {
        // check if a scaling factor is registered
        Float scalingFactor = configRegistry != null
                ? configRegistry.getConfigAttribute(
                        NatTableConfigAttributes.FONT_SCALING_FACTOR,
                        DisplayMode.NORMAL)
                : null;

        return GUIHelper.getScaledFont(
                cellStyle.getAttributeValue(CellStyleAttributes.FONT),
                scalingFactor != null ? scalingFactor.floatValue() : 1.0f);
    }
}
