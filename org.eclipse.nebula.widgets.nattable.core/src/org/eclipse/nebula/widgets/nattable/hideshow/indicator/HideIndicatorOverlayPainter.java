/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.indicator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter2;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Overlay painter that is used to render an indicator for hidden columns or
 * rows.
 *
 * @since 1.6
 */
public class HideIndicatorOverlayPainter implements IOverlayPainter2 {

    protected ILayer columnHeaderLayer;
    protected ILayer rowHeaderLayer;

    /**
     * The color that should be used to render the hide indicator.
     */
    private Color indicatorColor = GUIHelper.COLOR_DARK_GRAY;
    /**
     * The line width that should be used to render the hide indicator.
     */
    private int lineWidth = 3;
    /**
     * The {@link IConfigRegistry} that should be used to retrieve the color to
     * use for rendering the hide indicator. If set to <code>null</code>, the
     * locally set color will be used.
     */
    protected IConfigRegistry configRegistry;

    /**
     *
     * @param columnHeaderLayer
     *            The layer in the column header that should be used to
     *            determine the height of the hidden column indicator. Should be
     *            the top most layer in the column header region, e.g. the
     *            FilterRowHeaderComposite in case filtering is included. Can be
     *            <code>null</code> to avoid rendering of hidden column
     *            indicators.
     * @param rowHeaderLayer
     *            The layer in the row header that should be used to determine
     *            the width of the hidden row indicator. Should be the top most
     *            layer in the row header region. Can be <code>null</code> to
     *            avoid rendering of hidden row indicators.
     */
    public HideIndicatorOverlayPainter(ILayer columnHeaderLayer, ILayer rowHeaderLayer) {
        this.columnHeaderLayer = columnHeaderLayer;
        this.rowHeaderLayer = rowHeaderLayer;
    }

    @Override
    public void paintOverlay(GC gc, ILayer layer) {
        paintOverlay(layer, gc, 0, 0, new Rectangle(0, 0, layer.getWidth(), layer.getHeight()));
    }

    @Override
    public void paintOverlay(ILayer layer, GC gc, int xOffset, int yOffset, Rectangle rectangle) {
        Color originalForeground = gc.getForeground();
        int originalLineWidth = gc.getLineWidth();

        gc.setForeground(getIndicatorColor());
        gc.setLineWidth(GUIHelper.convertHorizontalPixelToDpi(getIndicatorLineWidth(), this.configRegistry));

        paintHiddenColumnIndicator(layer, gc, xOffset, yOffset, rectangle);
        paintHiddenRowIndicator(layer, gc, xOffset, yOffset, rectangle);

        gc.setForeground(originalForeground);
        gc.setLineWidth(originalLineWidth);
    }

    /**
     * Renders the indicator for hidden columns. By default it renders only if a
     * layer is provided as column header layer.
     *
     * @param layer
     *            The layer as base for the overlay rendering.
     * @param gc
     *            The GC.
     * @param xOffset
     *            The x offset.
     * @param yOffset
     *            The y offset.
     * @param rectangle
     *            The print bounds for the rendering action.
     */
    protected void paintHiddenColumnIndicator(ILayer layer, GC gc, int xOffset, int yOffset, Rectangle rectangle) {
        if (this.columnHeaderLayer != null) {
            int lineAdjustment = gc.getLineWidth() % 2;
            int height = this.columnHeaderLayer.getHeight();

            for (int col = 0; col < layer.getColumnCount(); col++) {
                LabelStack configLabels = layer.getConfigLabelsByPosition(col, this.columnHeaderLayer.getRowCount());
                if (configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN)) {
                    int x = layer.getStartXOfColumnPosition(col);
                    if (this.rowHeaderLayer == null || x >= this.rowHeaderLayer.getWidth()) {
                        int start = rectangle.y;
                        for (int i = 0; i < this.columnHeaderLayer.getRowCount(); i++) {
                            ILayerCell cell = layer.getCellByPosition(col, i);
                            int cellStart = layer.getStartXOfColumnPosition(cell.getOriginColumnPosition());
                            if (cellStart < x
                                    && ((this.rowHeaderLayer != null && x > this.rowHeaderLayer.getWidth())
                                            || (this.rowHeaderLayer == null && x > 0))) {
                                start += layer.getRowHeightByPosition(i);
                            }
                        }
                        gc.drawLine(x - lineAdjustment, start, x - lineAdjustment, height);
                    }
                }

                if (configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN)) {
                    // render the line on the right side of the last column
                    int x = layer.getStartXOfColumnPosition(col) + layer.getColumnWidthByPosition(col);
                    // adjust the rendering for the whole line width to avoid
                    // overlapping
                    if (col == layer.getColumnCount() - 1) {
                        lineAdjustment = (gc.getLineWidth() / 2) + lineAdjustment;
                    }
                    if (this.rowHeaderLayer == null || x >= this.rowHeaderLayer.getWidth()) {
                        int start = rectangle.y;
                        for (int i = 0; i < this.columnHeaderLayer.getRowCount(); i++) {
                            ILayerCell cell = layer.getCellByPosition(col + 1, i);
                            if (cell != null
                                    && cell.getOriginColumnPosition() < cell.getColumnPosition()
                                    && x < (cell.getBounds().x + cell.getBounds().width)) {
                                start += layer.getRowHeightByPosition(i);
                            }
                        }
                        gc.drawLine(x - lineAdjustment, start, x - lineAdjustment, height - 1);
                    }
                }
            }
        }
    }

    /**
     * Renders the indicator for hidden rows. By default it renders only if a
     * layer is provided as row header layer.
     *
     * @param layer
     *            The layer as base for the overlay rendering.
     * @param gc
     *            The GC.
     * @param xOffset
     *            The x offset.
     * @param yOffset
     *            The y offset.
     * @param rectangle
     *            The print bounds for the rendering action.
     */
    protected void paintHiddenRowIndicator(ILayer layer, GC gc, int xOffset, int yOffset, Rectangle rectangle) {
        if (this.rowHeaderLayer != null) {
            int lineAdjustment = gc.getLineWidth() % 2;
            int width = this.rowHeaderLayer.getWidth();

            for (int row = 0; row < layer.getRowCount(); row++) {
                LabelStack configLabels = layer.getConfigLabelsByPosition(this.rowHeaderLayer.getColumnCount(), row);
                if (configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN)) {
                    int y = layer.getStartYOfRowPosition(row);
                    if (this.columnHeaderLayer == null || y >= this.columnHeaderLayer.getHeight()) {
                        int start = rectangle.x;
                        for (int i = 0; i < this.rowHeaderLayer.getColumnCount(); i++) {
                            ILayerCell cell = layer.getCellByPosition(i, row);
                            int cellStart = layer.getStartYOfRowPosition(cell.getOriginRowPosition());
                            if (cellStart < y
                                    && ((this.columnHeaderLayer != null && y > this.columnHeaderLayer.getHeight())
                                            || (this.columnHeaderLayer == null && y > 0))) {
                                start += layer.getColumnWidthByPosition(i);
                            }
                        }
                        gc.drawLine(start, y - lineAdjustment, width, y - lineAdjustment);
                    }
                }

                if (configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN)) {
                    // render the line at the bottom side of the last row
                    int y = layer.getStartYOfRowPosition(row) + layer.getRowHeightByPosition(row);
                    // adjust the rendering for the whole line width to avoid
                    // overlapping
                    if (row == layer.getRowCount() - 1) {
                        lineAdjustment = (gc.getLineWidth() / 2) + lineAdjustment;
                    }
                    if (this.columnHeaderLayer == null || y >= this.columnHeaderLayer.getHeight()) {
                        int start = rectangle.x;
                        for (int i = 0; i < this.rowHeaderLayer.getColumnCount(); i++) {
                            ILayerCell cell = layer.getCellByPosition(i, row + 1);
                            if (cell != null && cell.getOriginRowPosition() < cell.getRowPosition()) {
                                start += layer.getColumnWidthByPosition(i);
                            }
                        }
                        gc.drawLine(start, y - lineAdjustment, width, y - lineAdjustment);
                    }
                }
            }
        }
    }

    /**
     * Checks if a {@link IConfigRegistry} is set to this
     * HideIndicatorOverlayPainter and will try to extract the configuration
     * value for {@link HideIndicatorConfigAttributes#HIDE_INDICATOR_COLOR}. If
     * there is no IConfigRegistry set or there is no value for the attribute in
     * the set IConfigRegistry, the Color set as member will be used.
     *
     * @return The Color that will be used to render the hide indicator.
     */
    public Color getIndicatorColor() {
        if (this.configRegistry != null) {
            Color color = this.configRegistry.getConfigAttribute(
                    HideIndicatorConfigAttributes.HIDE_INDICATOR_COLOR,
                    DisplayMode.NORMAL);
            if (color != null) {
                return color;
            }
        }
        return this.indicatorColor;
    }

    /**
     *
     * @param indicatorColor
     *            The Color that should be used to render the hide indicator.
     *            <code>null</code> values will be ignored.
     */
    public void setIndicatorColor(Color indicatorColor) {
        if (indicatorColor != null) {
            this.indicatorColor = indicatorColor;
        }
    }

    /**
     * Checks if a {@link IConfigRegistry} is set to this
     * HideIndicatorOverlayPainter and will try to extract the configuration
     * value for
     * {@link HideIndicatorConfigAttributes#HIDE_INDICATOR_LINE_WIDTH}. If there
     * is no IConfigRegistry set or there is no value for the attribute in the
     * set IConfigRegistry, the line width set as member will be used.
     *
     * @return The line width that will be used to render the hide indicator.
     */
    public int getIndicatorLineWidth() {
        if (this.configRegistry != null) {
            Integer width = this.configRegistry.getConfigAttribute(
                    HideIndicatorConfigAttributes.HIDE_INDICATOR_LINE_WIDTH,
                    DisplayMode.NORMAL);

            if (width != null) {
                return width;
            }
        }
        return this.lineWidth;
    }

    /**
     *
     * @param lineWidth
     *            The line width that should be used to render the hide
     *            indicator.
     */
    public void setIndicatorLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Sets the {@link IConfigRegistry} to this HideIndicatorOverlayPainter. By
     * setting it the values for indicator color and indicator width will be
     * read out of the {@link IConfigRegistry} and not used from the local
     * member variables.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the configuration
     *            values for the hide indicator.
     *
     * @see HideIndicatorConfigAttributes#HIDE_INDICATOR_COLOR
     * @see HideIndicatorConfigAttributes#HIDE_INDICATOR_LINE_WIDTH
     */
    public void setConfigRegistry(IConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
    }
}
