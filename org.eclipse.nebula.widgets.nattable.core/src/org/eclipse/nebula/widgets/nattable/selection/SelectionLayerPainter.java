/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     neal zhang <nujiah001@126.com> - change some methods and fields visibility
 *     Loris Securo <lorissek@gmail.com> - Bug 500750
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.function.Function;

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

        // nothing to draw, we exit
        if (positionRectangle.width <= 0 || positionRectangle.height <= 0) {
            return;
        }

        final int columnOffset = natLayer.getColumnPositionByX(xOffset);
        final int rowOffset = natLayer.getRowPositionByY(yOffset);
        BorderCell[][] borderCells = getBorderCells(natLayer, xOffset, yOffset, positionRectangle, cell -> (cell.getColumnPosition() >= columnOffset
                && cell.getRowPosition() >= rowOffset)
                && (DisplayMode.SELECT.equals(cell.getDisplayMode())
                        || DisplayMode.SELECT_HOVER.equals(cell.getDisplayMode())));

        if (borderCells != null) {
            // Save gc settings
            int originalLineStyle = gc.getLineStyle();
            int originalLineWidth = gc.getLineWidth();
            Color originalForeground = gc.getForeground();

            BorderStyle borderStyle = getBorderStyle(configRegistry);

            // on a single cell update we only need to repaint the internal
            // borders of the
            // external cells
            PaintModeEnum paintMode = (positionRectangle.width <= 2 && positionRectangle.height <= 2)
                    ? PaintModeEnum.NO_EXTERNAL_BORDERS
                    : PaintModeEnum.ALL;

            BorderPainter borderPainter = new BorderPainter(borderCells, borderStyle, paintMode);
            borderPainter.paintBorder(gc);

            // Restore original gc settings
            gc.setLineStyle(originalLineStyle);
            gc.setLineWidth(originalLineWidth);
            gc.setForeground(originalForeground);
        }
    }

    /**
     * Calculate the cells around which borders should be painted.
     *
     * @param natLayer
     *            The layer that is painted.
     * @param xOffset
     *            of the layer from the origin of the table
     * @param yOffset
     *            of the layer from the origin of the table
     * @param positionRectangle
     *            The calculated position rectangle for the pixel rectangle that
     *            should be painted.
     * @param function
     *            The function that is used to determine if a border should be
     *            applied to a cell or not.
     * @return The {@link BorderCell}s around which the border should be painted
     *         or <code>null</code> if no border rendering is necessary.
     *
     * @since 2.0
     */
    protected BorderCell[][] getBorderCells(ILayer natLayer, int xOffset, int yOffset, Rectangle positionRectangle, Function<ILayerCell, Boolean> function) {

        BorderCell[][] borderCells;
        boolean atLeastOne = false;

        int columnPositionOffset = positionRectangle.x;
        int columnPositionEnd = columnPositionOffset + positionRectangle.width;
        int rectangleWidth = positionRectangle.width;
        if ((positionRectangle.width + positionRectangle.x) <= natLayer.getColumnCount()) {
            columnPositionOffset--;
            columnPositionEnd++;
            rectangleWidth += 2;
        }
        // we are going to read also adjacent cells in the extremities to ensure
        // that external borders are rendered correctly for single cell updates
        int rowPositionOffset = positionRectangle.y;
        int rowPositionEnd = rowPositionOffset + positionRectangle.height;
        int rectangleHeight = positionRectangle.height;
        if ((positionRectangle.height + positionRectangle.y) <= natLayer.getRowCount()) {
            rowPositionOffset--;
            rowPositionEnd++;
            rectangleHeight += 2;
        }

        borderCells = new BorderCell[rectangleHeight][rectangleWidth];

        for (int columnPosition = columnPositionOffset, ix = 0; columnPosition < columnPositionEnd; columnPosition++, ix++) {
            for (int rowPosition = rowPositionOffset, iy = 0; rowPosition < rowPositionEnd; rowPosition++, iy++) {

                boolean insideBorder = false;
                Rectangle cellBounds = null;

                ILayerCell currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
                if (currentCell != null) {

                    // In case of spanned cells the border painter needs to
                    // know the bounds of adjacent cells even if they are
                    // not selected. This is the reason why we get the
                    // bounds also for non selected cells.

                    cellBounds = currentCell.getBounds();

                    // the cell should be considered only if it is in our
                    // layer
                    boolean toBeConsidered = isInCurrentLayer(ix, iy, xOffset, yOffset, cellBounds, borderCells);

                    if (toBeConsidered && function.apply(currentCell)) {
                        insideBorder = true;
                        atLeastOne = true;
                    }
                }

                Rectangle fixedBounds = fixBoundsInGridLines(cellBounds, xOffset, yOffset);
                BorderCell borderCell = new BorderCell(fixedBounds, insideBorder);
                borderCells[iy][ix] = borderCell;
            }
        }

        return atLeastOne ? borderCells : null;
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
