/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Loris Securo <lorissek@gmail.com> - Bug 499513
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class GridLineCellLayerPainter extends CellLayerPainter {

    private final Color gridColor;

    /**
     * @since 1.5
     */
    protected boolean renderGridLines = true;

    /**
     * @since 1.4
     */
    protected Integer gridLineWidth = 1;

    /**
     * Create a GridLineCellLayerPainter that renders grid lines in the
     * specified color and uses the default clipping behaviour.
     *
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public GridLineCellLayerPainter(final Color gridColor) {
        this.gridColor = gridColor;
    }

    /**
     * Create a GridLineCellLayerPainter that renders gray grid lines and uses
     * the default clipping behaviour.
     */
    public GridLineCellLayerPainter() {
        this.gridColor = GUIHelper.COLOR_GRAY;
    }

    /**
     * Create a GridLineCellLayerPainter that renders grid lines in the
     * specified color and uses the specified clipping behaviour.
     *
     * @param gridColor
     *            The color that should be used to render the grid lines.
     * @param clipLeft
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the left cell will be clipped, if set to
     *            <code>false</code> the right cell will be clipped. The default
     *            value is <code>false</code>.
     * @param clipTop
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the top cell will be clipped, if set to
     *            <code>false</code> the bottom cell will be clipped. The
     *            default value is <code>false</code>.
     */
    public GridLineCellLayerPainter(final Color gridColor, boolean clipLeft, boolean clipTop) {
        super(clipLeft, clipTop);
        this.gridColor = gridColor;
    }

    /**
     * Create a GridLineCellLayerPainter that renders gray grid lines and uses
     * the specified clipping behaviour.
     *
     * @param clipLeft
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the left cell will be clipped, if set to
     *            <code>false</code> the right cell will be clipped. The default
     *            value is <code>false</code>.
     * @param clipTop
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the top cell will be clipped, if set to
     *            <code>false</code> the bottom cell will be clipped. The
     *            default value is <code>false</code>.
     */
    public GridLineCellLayerPainter(boolean clipLeft, boolean clipTop) {
        this(GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    /**
     * @return The local configured color that is used to render the grid lines.
     */
    public Color getGridColor() {
        return this.gridColor;
    }

    @Override
    public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset,
            Rectangle rectangle, IConfigRegistry configRegistry) {
        Boolean renderConfig = null;
        LabelStack stack = natLayer.getRegionLabelsByXY(xOffset, yOffset);
        List<String> labels = new ArrayList<String>();
        if (stack != null) {
            labels = stack.getLabels();
            // check if there is a configuration telling to not rendering grid
            // lines
            renderConfig = configRegistry.getConfigAttribute(
                    CellConfigAttributes.RENDER_GRID_LINES,
                    DisplayMode.NORMAL,
                    labels);
        }

        this.renderGridLines = (renderConfig != null) ? renderConfig : true;

        // Draw GridLines
        if (this.renderGridLines) {
            // check if there is a configuration for the grid line width
            Integer width = configRegistry.getConfigAttribute(
                    CellConfigAttributes.GRID_LINE_WIDTH,
                    DisplayMode.NORMAL,
                    labels);
            this.gridLineWidth = (width != null) ? width : 1;

            int oldLineWidth = gc.getLineWidth();
            gc.setLineWidth(this.gridLineWidth);
            drawGridLines(natLayer, gc, rectangle, configRegistry, labels);
            gc.setLineWidth(oldLineWidth);
        }

        super.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);
    }

    @Override
    public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle bounds) {
        Integer adjustment = this.renderGridLines ? this.gridLineWidth : 0;

        int startAdjustment = (adjustment == 1) ? 0 : Math.round(adjustment.floatValue() / 2);
        int sizeAdjustment = (adjustment == 1) ? 1 : Math.round(adjustment.floatValue() / 2);

        return new Rectangle(
                bounds.x - startAdjustment,
                bounds.y - startAdjustment,
                Math.max(bounds.width - sizeAdjustment, 0),
                Math.max(bounds.height - sizeAdjustment, 0));
    }

    /**
     * @deprecated Use
     *             {@link #drawGridLines(ILayer, GC, Rectangle, IConfigRegistry, List)}
     *             with specifying the label stack
     */
    @Deprecated
    protected void drawGridLines(ILayer natLayer, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
        drawGridLines(natLayer, gc, rectangle, configRegistry, new ArrayList<String>());
    }

    /**
     * @since 1.4
     */
    protected void drawGridLines(ILayer natLayer, GC gc, Rectangle rectangle, IConfigRegistry configRegistry, List<String> labels) {
        Color gColor = configRegistry.getConfigAttribute(
                CellConfigAttributes.GRID_LINE_COLOR,
                DisplayMode.NORMAL,
                labels);
        gc.setForeground(gColor != null ? gColor : this.gridColor);

        int adjustment = (this.gridLineWidth == 1) ? 1 : Math.round(this.gridLineWidth.floatValue() / 2);

        drawHorizontalLines(natLayer, gc, rectangle, adjustment);
        drawVerticalLines(natLayer, gc, rectangle, adjustment);
    }

    private void drawHorizontalLines(ILayer natLayer, GC gc, Rectangle rectangle, int adjustment) {
        int endX = rectangle.x + Math.min(natLayer.getWidth() - adjustment, rectangle.width);

        // this can happen on resizing if there is no CompositeLayer involved
        // without this check grid line fragments may be rendered below the last
        // column
        if (endX > natLayer.getWidth())
            return;

        int rowPositionByY = natLayer.getRowPositionByY(rectangle.y + rectangle.height);
        int maxRowPosition = rowPositionByY > 0
                ? Math.min(natLayer.getRowCount(), rowPositionByY) : natLayer.getRowCount();
        for (int rowPosition = natLayer.getRowPositionByY(rectangle.y); rowPosition < maxRowPosition; rowPosition++) {
            final int size = natLayer.getRowHeightByPosition(rowPosition);
            if (size > 0) {
                int y = natLayer.getStartYOfRowPosition(rowPosition) + size - adjustment;

                gc.drawLine(rectangle.x, y, endX, y);
            }
        }
    }

    private void drawVerticalLines(ILayer natLayer, GC gc, Rectangle rectangle, int adjustment) {
        int endY = rectangle.y + Math.min(natLayer.getHeight() - adjustment, rectangle.height);

        // this can happen on resizing if there is no CompositeLayer involved
        // without this check grid line fragments may be rendered below the last
        // row
        if (endY > natLayer.getHeight())
            return;

        int columnPositionByX = natLayer.getColumnPositionByX(rectangle.x + rectangle.width);
        int maxColumnPosition = columnPositionByX > 0
                ? Math.min(natLayer.getColumnCount(), columnPositionByX) : natLayer.getColumnCount();
        for (int columnPosition = natLayer.getColumnPositionByX(rectangle.x); columnPosition < maxColumnPosition; columnPosition++) {
            final int size = natLayer.getColumnWidthByPosition(columnPosition);
            if (size > 0) {
                int x = natLayer.getStartXOfColumnPosition(columnPosition) + size - adjustment;

                gc.drawLine(x, rectangle.y, x, endY);
            }
        }
    }

}
