/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * Helper class for fill handle paint operations.
 *
 * @since 2.5
 */
public final class FillHandleLayerPainterHelper {

    private FillHandleLayerPainterHelper() {
        // private empty constructor for helper class
    }

    /**
     * Get the border style that should be used to render the border for cells
     * that are currently part of the fill handle region. Checks the
     * {@link IConfigRegistry} for a registered {@link IStyle} for the
     * {@link FillHandleConfigAttributes#FILL_HANDLE_REGION_BORDER_STYLE} label.
     * If none is registered, a default line style will be returned.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the style information
     *            from.
     *
     * @return The border style that should be used
     */
    public static BorderStyle getHandleRegionBorderStyle(IConfigRegistry configRegistry) {
        BorderStyle borderStyle = configRegistry.getConfigAttribute(
                FillHandleConfigAttributes.FILL_HANDLE_REGION_BORDER_STYLE,
                DisplayMode.NORMAL);

        // if there is no border style configured, use the default
        if (borderStyle == null) {
            borderStyle = new BorderStyle(2, GUIHelper.getColor(0, 125, 10), LineStyleEnum.SOLID, BorderModeEnum.INTERNAL);
        }

        return borderStyle;
    }

    /**
     * Returns the color that should be used to render the fill handle. If the
     * {@link IConfigRegistry} is <code>null</code> or does not contain
     * configurations for the color of the fill handle, a default dark green
     * color is used.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to determine the configured
     *            fill handle color. Can be <code>null</code> which results in
     *            returning a default dark green color.
     *
     * @return the color that should be used
     */
    public static Color getHandleColor(IConfigRegistry configRegistry) {
        if (configRegistry != null) {
            Color color = configRegistry.getConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_COLOR,
                    DisplayMode.NORMAL);

            if (color != null) {
                return color;
            }
        }
        return GUIHelper.getColor(0, 125, 10);
    }

    /**
     * Returns the border style that should be used to render the border of the
     * fill handle. If the {@link IConfigRegistry} is <code>null</code> or does
     * not contain configurations for styling the border of the fill handle, a
     * default style is used.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to determine the configured
     *            fill handle border style. Can be <code>null</code> which
     *            results in returning a default style.
     *
     * @return the border style that should be used
     */
    public static BorderStyle getHandleBorderStyle(IConfigRegistry configRegistry) {
        if (configRegistry != null) {
            BorderStyle borderStyle = configRegistry.getConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_BORDER_STYLE,
                    DisplayMode.NORMAL);

            if (borderStyle != null) {
                return borderStyle;
            }
        }
        return new BorderStyle(1, GUIHelper.COLOR_WHITE, LineStyleEnum.SOLID, BorderModeEnum.CENTERED);
    }

    /**
     * Get the border style that should be used to render the border for cells
     * that are currently copied to the {@link InternalCellClipboard}. Checks
     * the {@link ConfigRegistry} for a registered {@link IStyle} for the
     * {@link SelectionStyleLabels#COPY_BORDER_STYLE} label. If none is
     * registered, a default line style will be used to render the border.
     *
     * @param configRegistry
     *            The {@link ConfigRegistry} to retrieve the style information
     *            from.
     *
     * @return the border style that should be used
     */
    public static BorderStyle getCopyBorderStyle(IConfigRegistry configRegistry) {
        IStyle cellStyle = configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                DisplayMode.NORMAL,
                SelectionStyleLabels.COPY_BORDER_STYLE);
        BorderStyle borderStyle = cellStyle != null ? cellStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE) : null;

        // if there is no border style configured, use the default
        if (borderStyle == null) {
            borderStyle = new BorderStyle(1, GUIHelper.COLOR_BLACK, LineStyleEnum.DASHED, BorderModeEnum.CENTERED);
        }

        return borderStyle;
    }

    /**
     * Get the size that should be used to render the fill handle. If the
     * {@link IConfigRegistry} is <code>null</code> or does not contain
     * configurations for the size of the fill handle, a default size is used.
     *
     * @param configRegistry
     *            The {@link ConfigRegistry} to retrieve the style information
     *            from.
     * @return the size of the fill handle.
     */
    public static Point getHandleSize(IConfigRegistry configRegistry) {
        if (configRegistry != null) {
            Point size = configRegistry.getConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_SIZE,
                    DisplayMode.NORMAL);

            if (size != null) {
                return size;
            }
        }

        return new Point(7, 7);
    }

}
