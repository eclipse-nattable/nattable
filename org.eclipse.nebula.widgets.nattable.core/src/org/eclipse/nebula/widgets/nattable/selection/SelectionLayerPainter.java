/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     neal zhang <nujiah001@126.com> - change some methods and fields visibility
 *     Loris Securo <lorissek@gmail.com> - Bug 500750
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter.BorderCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter.PaintModeEnum;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialised GridLineCellLayerPainter that renders an additional border around
 * selected cells. By default the additional selection anchor border style is
 * black dotted one pixel sized line. This style can be configured via
 * ConfigRegistry.
 *
 * @see SelectionStyleLabels#SELECTION_ANCHOR_GRID_LINE_STYLE
 */
public class SelectionLayerPainter extends GridLineCellLayerPainter {

    /**
     * Create a SelectionLayerPainter that renders grid lines in the specified
     * color and uses the default clipping behaviour.
     *
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public SelectionLayerPainter(final Color gridColor) {
        super(gridColor);
    }

    /**
     * Create a SelectionLayerPainter that renders gray grid lines and uses the
     * default clipping behaviour.
     */
    public SelectionLayerPainter() {
        super();
    }

    /**
     * Create a SelectionLayerPainter that renders grid lines in the specified
     * color and uses the specified clipping behaviour.
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
    public SelectionLayerPainter(final Color gridColor, boolean clipLeft, boolean clipTop) {
        super(gridColor, clipLeft, clipTop);
    }

    /**
     * Create a SelectionLayerPainter that renders gray grid lines and uses the
     * specified clipping behaviour.
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
    public SelectionLayerPainter(boolean clipLeft, boolean clipTop) {
        this(GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    @Override
    public void paintLayer(
            ILayer natLayer, GC gc,
            int xOffset, int yOffset, Rectangle pixelRectangle,
            IConfigRegistry configRegistry) {

        super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);

        Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
        int columnPositionOffset = positionRectangle.x;
        int rowPositionOffset = positionRectangle.y;

        // nothing to draw, we exit
        if (positionRectangle.width <= 0 || positionRectangle.height <= 0) {
            return;
        }

        BorderCell[][] borderCells;
        boolean atLeastOne = false;
        PaintModeEnum paintMode;

        // tentative way to know that this is a single cell update
        if (positionRectangle.width <= 2 && positionRectangle.height <= 2) {

            // In order to correctly paint the selection borders in case of
            // single cell updates we need to consider also the adjacent cells.
            // Therefore we try to retrieve also cells that are outside the
            // pixelRectangle but still inside our layer.

            // +2 because we are going to read also adjacent cells in the
            // extremities
            borderCells = new BorderCell[positionRectangle.height + 2][positionRectangle.width + 2];

            // we need to repaint only the internal borders of the external
            // cells
            paintMode = PaintModeEnum.NO_EXTERNAL_BORDERS;

            // -1/+1 because we are going to read also adjacent cells in the
            // extremities
            for (int columnPosition = columnPositionOffset - 1, ix = 0; columnPosition < columnPositionOffset + positionRectangle.width + 1; columnPosition++, ix++) {
                for (int rowPosition = rowPositionOffset - 1, iy = 0; rowPosition < rowPositionOffset + positionRectangle.height + 1; rowPosition++, iy++) {

                    boolean insideBorder = false;
                    Rectangle cellBounds = null;

                    ILayerCell currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
                    if (currentCell != null) {

                        cellBounds = currentCell.getBounds();

                        // the cell should be considered only if it is in our
                        // layer
                        boolean toBeConsidered = isInCurrentLayer(ix, iy, xOffset, yOffset, cellBounds, borderCells);

                        if (toBeConsidered && isSelected(currentCell)) {
                            insideBorder = true;
                            atLeastOne = true;
                        }
                    }

                    Rectangle fixedBounds = fixBoundsInGridLines(cellBounds, xOffset, yOffset);
                    BorderCell borderCell = new BorderCell(fixedBounds, insideBorder);
                    borderCells[iy][ix] = borderCell;

                }
            }
        } else {

            borderCells = new BorderCell[positionRectangle.height][positionRectangle.width];
            paintMode = PaintModeEnum.ALL;

            for (int columnPosition = columnPositionOffset, ix = 0; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++, ix++) {
                for (int rowPosition = rowPositionOffset, iy = 0; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++, iy++) {

                    boolean insideBorder = false;
                    Rectangle cellBounds = null;

                    ILayerCell currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
                    if (currentCell != null) {

                        // In case of spanned cells the border painter needs to
                        // know the bounds of adjacent cells even if they are
                        // not selected. This is the reason why we get the
                        // bounds also for non selected cells.

                        cellBounds = currentCell.getBounds();

                        if (isSelected(currentCell)) {
                            insideBorder = true;
                            atLeastOne = true;
                        }
                    }

                    Rectangle fixedBounds = fixBoundsInGridLines(cellBounds, xOffset, yOffset);
                    BorderCell borderCell = new BorderCell(fixedBounds, insideBorder);
                    borderCells[iy][ix] = borderCell;

                }
            }
        }

        if (atLeastOne) {
            // Save gc settings
            int originalLineStyle = gc.getLineStyle();
            int originalLineWidth = gc.getLineWidth();
            Color originalForeground = gc.getForeground();

            BorderStyle borderStyle = getBorderStyle(configRegistry);

            BorderPainter borderPainter = new BorderPainter(borderCells, borderStyle, paintMode);
            borderPainter.paintBorder(gc);

            // Restore original gc settings
            gc.setLineStyle(originalLineStyle);
            gc.setLineWidth(originalLineWidth);
            gc.setForeground(originalForeground);
        }
    }

    private boolean isSelected(ILayerCell cell) {
        return (cell.getDisplayMode() == DisplayMode.SELECT
                || cell.getDisplayMode() == DisplayMode.SELECT_HOVER);
    }

    /**
     * Returns a rectangle that will cover the left and top grid lines, if they
     * are present.
     *
     * @param cellBounds
     *            the rectangle that needs to be considered
     * @param xOffset
     *            the starting x coordinate of the area we can draw on. The fix
     *            will not be applied if the <code>cellBounds</code> are placed
     *            on this limit.
     * @param yOffset
     *            the starting y coordinate of the area we can draw on. The fix
     *            will not be applied if the <code>cellBounds</code> are placed
     *            on this limit.
     *
     * @since 1.5
     */
    protected Rectangle fixBoundsInGridLines(Rectangle cellBounds, int xOffset, int yOffset) {

        if (cellBounds == null) {
            return null;
        }

        Rectangle fixedBounds = new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);

        // when grid lines are rendered we want the border
        // to cover them, otherwise we remain inside the
        // cell
        if (this.renderGridLines && fixedBounds.x != 0 && fixedBounds.x != xOffset) {
            fixedBounds.x--;
            fixedBounds.width++;
        }
        if (this.renderGridLines && fixedBounds.y != 0 && fixedBounds.y != yOffset) {
            fixedBounds.y--;
            fixedBounds.height++;
        }

        return fixedBounds;
    }

    /**
     * Tries to detect if the cell is part of the current layer. It does so
     * using xOffset/yOffset (which are not affected by single cell updates) and
     * detecting overlapping of cells, which should not be possible in the same
     * layer. It's not perfect, there might be false positives.
     *
     * @since 1.5
     */
    protected boolean isInCurrentLayer(int ix, int iy, int xOffset, int yOffset, Rectangle cellBounds, BorderCell[][] borderCells) {

        // If the cell bounds are not inside the x/y offset, we consider it part
        // of another layer
        if (ix == 0) {
            if (cellBounds.x + cellBounds.width <= xOffset) {
                return false;
            }
        }
        if (iy == 0) {
            if (cellBounds.y + cellBounds.height <= yOffset) {
                return false;
            }
        }

        // if the previous cell is overlapping the current cell we consider it
        // part of another layer
        if (ix == 1) {
            if (borderCells[iy][ix - 1].isInsideBorder) {
                Rectangle prevCellBounds = borderCells[iy][ix - 1].bounds;
                if (prevCellBounds.x + prevCellBounds.width > cellBounds.x) {
                    borderCells[iy][ix - 1].isInsideBorder = false;
                }
            }
        }
        if (iy == 1) {
            if (borderCells[iy - 1][ix].isInsideBorder) {
                Rectangle prevCellBounds = borderCells[iy - 1][ix].bounds;
                if (prevCellBounds.y + prevCellBounds.height > cellBounds.y) {
                    borderCells[iy - 1][ix].isInsideBorder = false;
                }
            }
        }

        // it's an external cell and it's getting overlapped by the previous
        // cell, we consider it part of another layer
        if (ix == borderCells[iy].length - 1) {
            Rectangle prevCellBounds = borderCells[iy][ix - 1].bounds;
            if (prevCellBounds.x + prevCellBounds.width > cellBounds.x) {
                return false;
            }
        }
        if (iy == borderCells.length - 1) {
            Rectangle prevCellBounds = borderCells[iy - 1][ix].bounds;
            if (prevCellBounds.y + prevCellBounds.height > cellBounds.y) {
                return false;
            }
        }
        return true;
    }

    /**
     * @deprecated Use {@link #getBorderStyle} instead.
     */
    @Deprecated
    protected void applyBorderStyle(GC gc, IConfigRegistry configRegistry) {
        BorderStyle borderStyle = configRegistry.getConfigAttribute(
                SelectionConfigAttributes.SELECTION_GRID_LINE_STYLE,
                DisplayMode.SELECT);

        // check for backwards compatibility style configuration
        if (borderStyle == null) {
            // Note: If there is no style configured for the
            // SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE
            // label, the style configured for DisplayMode.SELECT will be
            // retrieved by this call.
            // Ensure that the selection style configuration does not contain a
            // border style configuration to avoid strange rendering behavior.
            // By default there is no border configuration added, so there
            // shouldn't be issues with backwards compatibility. And if there
            // are some, they can be solved easily by adding the necessary
            // border style configuration.
            IStyle cellStyle = configRegistry.getConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE);
            borderStyle = cellStyle != null ? cellStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE) : null;
        }

        // if there is no border style configured, use the default one for
        // backwards compatibility
        if (borderStyle == null) {
            gc.setLineStyle(SWT.LINE_CUSTOM);
            gc.setLineDash(new int[] { 1, 1 });
            gc.setForeground(GUIHelper.COLOR_BLACK);
        } else {
            gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
            gc.setLineWidth(borderStyle.getThickness());
            gc.setForeground(borderStyle.getColor());
        }
    }

    /**
     * Get the border style that should be used to render the border for cells
     * that are currently selected. Checks the {@link IConfigRegistry} for a
     * registered {@link IStyle} for the
     * {@link SelectionConfigAttributes#SELECTION_GRID_LINE_STYLE} label or the
     * {@link SelectionStyleLabels#SELECTION_ANCHOR_GRID_LINE_STYLE} label. If
     * none is registered, a default line style will be returned.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the style information
     *            from.
     *
     * @return The border style that should be used
     *
     * @since 1.5
     */
    protected BorderStyle getBorderStyle(IConfigRegistry configRegistry) {
        BorderStyle borderStyle = configRegistry.getConfigAttribute(
                SelectionConfigAttributes.SELECTION_GRID_LINE_STYLE,
                DisplayMode.SELECT);

        // check for backwards compatibility style configuration
        if (borderStyle == null) {
            // Note: If there is no style configured for the
            // SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE
            // label, the style configured for DisplayMode.SELECT will be
            // retrieved by this call.
            // Ensure that the selection style configuration does not contain a
            // border style configuration to avoid strange rendering behavior.
            // By default there is no border configuration added, so there
            // shouldn't be issues with backwards compatibility. And if there
            // are some, they can be solved easily by adding the necessary
            // border style configuration.
            IStyle cellStyle = configRegistry.getConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    DisplayMode.SELECT,
                    SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE);
            borderStyle = cellStyle != null ? cellStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE) : null;
        }

        // if there is no border style configured, use the default
        if (borderStyle == null) {
            borderStyle = new BorderStyle(1, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED, BorderModeEnum.CENTERED);
        }

        return borderStyle;
    }

}
