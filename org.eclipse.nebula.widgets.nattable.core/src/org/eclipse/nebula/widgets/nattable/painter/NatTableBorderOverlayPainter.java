/*******************************************************************************
 * Copyright (c) 2013, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * IOverlayPainter that renders the left and the top border of a NatTable.
 * <p>
 * This is necessary because by default the left and the top borders are not
 * rendered so it is possible to create composite stacks that do not create
 * double sized borders (e.g. between row header and body). Mainly the issue
 * results because of several offset calculations all over the NatTable
 * rendering code.
 * <p>
 * For completeness this painter is also able to render the right and the bottom
 * border to ensure that the border has the same color around the NatTable
 * without changing the grid line color.
 */
public class NatTableBorderOverlayPainter implements IOverlayPainter2 {

    /**
     * The color that should be used to render the border around the NatTable.
     */
    private final Color borderColor;

    /**
     * Flag to specify whether to render all border lines or only the left and
     * the top border. By default only the left and the top border lines are
     * rendered as the right and the bottom border lines are already rendered by
     * existing painters.
     */
    private final boolean renderAllBorderLines;

    /**
     * The IConfigRegistry that should be used to retrieve the color to use for
     * rendering the border. If set to <code>null</code>, the configured color
     * will be used.
     */
    private final IConfigRegistry configRegistry;

    /**
     * Creates a NatTableBorderOverlayPainter that paints gray border lines to
     * the top and to the left.
     */
    public NatTableBorderOverlayPainter() {
        this(GUIHelper.COLOR_GRAY);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints by default gray border
     * lines to the top and to the left.
     *
     * @param configRegistry
     *            The IConfigRegistry to dynamically load the grid line color.
     *            If a configuration value is found, it will be used instead of
     *            the gray default color.
     */
    public NatTableBorderOverlayPainter(IConfigRegistry configRegistry) {
        this(GUIHelper.COLOR_GRAY, configRegistry);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints gray border lines.
     *
     * @param renderAllBorderLines
     *            <code>true</code> if all border lines should be rendered,
     *            <code>false</code> if only the left and the top border line
     *            need to be rendered.
     */
    public NatTableBorderOverlayPainter(final boolean renderAllBorderLines) {
        this(GUIHelper.COLOR_GRAY, renderAllBorderLines);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints gray border lines by
     * default.
     *
     * @param renderAllBorderLines
     *            <code>true</code> if all border lines should be rendered,
     *            <code>false</code> if only the left and the top border line
     *            need to be rendered.
     * @param configRegistry
     *            The IConfigRegistry to dynamically load the grid line color.
     *            If a configuration value is found, it will be used instead of
     *            the gray default color.
     */
    public NatTableBorderOverlayPainter(final boolean renderAllBorderLines,
            IConfigRegistry configRegistry) {
        this(GUIHelper.COLOR_GRAY, renderAllBorderLines, configRegistry);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints border lines to the
     * top and to the left.
     *
     * @param borderColor
     *            The color that should be used to render the border lines.
     */
    public NatTableBorderOverlayPainter(final Color borderColor) {
        this(borderColor, false);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints border lines to the
     * top and to the left.
     *
     * @param borderColor
     *            The default color that should be used to render the border
     *            lines.
     * @param configRegistry
     *            The IConfigRegistry to dynamically load the grid line color.
     *            If a configuration value is found, it will be used instead of
     *            the given default color.
     */
    public NatTableBorderOverlayPainter(final Color borderColor,
            IConfigRegistry configRegistry) {
        this(borderColor, false, configRegistry);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints border lines.
     *
     * @param borderColor
     *            The color that should be used to render the border lines.
     * @param renderAllBorderLines
     *            <code>true</code> if all border lines should be rendered,
     *            <code>false</code> if only the left and the top border line
     *            need to be rendered.
     */
    public NatTableBorderOverlayPainter(final Color borderColor,
            final boolean renderAllBorderLines) {
        this(borderColor, renderAllBorderLines, null);
    }

    /**
     * Creates a NatTableBorderOverlayPainter that paints border lines.
     *
     * @param borderColor
     *            The color that should be used to render the border lines.
     * @param renderAllBorderLines
     *            <code>true</code> if all border lines should be rendered,
     *            <code>false</code> if only the left and the top border line
     *            need to be rendered.
     */
    public NatTableBorderOverlayPainter(final Color borderColor,
            final boolean renderAllBorderLines, IConfigRegistry configRegistry) {
        this.borderColor = borderColor;
        this.renderAllBorderLines = renderAllBorderLines;
        this.configRegistry = configRegistry;
    }

    @Override
    public void paintOverlay(GC gc, ILayer layer) {
        paintOverlay(layer, gc, 0, 0, new Rectangle(0, 0, layer.getWidth(), layer.getHeight()));
    }

    /**
     * @since 1.4
     */
    @Override
    public void paintOverlay(ILayer layer, GC gc, int xOffset, int yOffset, Rectangle rectangle) {
        Color beforeColor = gc.getForeground();

        gc.setForeground(getBorderColor());

        int x = rectangle.x;
        int y = rectangle.y;
        int width = rectangle.x + Math.min(rectangle.width, layer.getWidth() - rectangle.x);
        int height = rectangle.y + Math.min(rectangle.height, layer.getHeight() - rectangle.y);

        if (rectangle.x == 0) {
            gc.drawLine(x, y, x, height - 1);
        }

        if (rectangle.y == 0) {
            gc.drawLine(x, y, width - 1, y);
        }

        if (this.renderAllBorderLines) {
            if (width >= layer.getWidth()) {
                gc.drawLine(width - 1, y, width - 1, height - 1);
            }

            if (height >= layer.getHeight()) {
                gc.drawLine(x, height - 1, width - 1, height - 1);
            }
        }

        gc.setForeground(beforeColor);
    }

    /**
     * Checks if a IConfigRegistry is set to this NatTableBorderOverlayPainter
     * and will try to extract the configuration value for
     * {@link CellConfigAttributes#GRID_LINE_COLOR}. If there is no
     * IConfigRegistry set or there is no value for the attribute in the set
     * IConfigRegistry, the Color set as member will be used.
     *
     * @return The Color that will be used to render the grid lines.
     *
     * @since 1.4
     */
    public Color getBorderColor() {
        if (this.configRegistry != null) {
            Color bColor = this.configRegistry.getConfigAttribute(
                    CellConfigAttributes.GRID_LINE_COLOR, DisplayMode.NORMAL);
            if (bColor != null)
                return bColor;
        }
        return this.borderColor;
    }
}
