/*****************************************************************************
 * Copyright (c) 2015, 2019 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.formula;

import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter.BorderCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter.PaintModeEnum;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
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
 * Specialized {@link SelectionLayerPainter} that renders a border around cells
 * that are currently in the stored in the {@link InternalCellClipboard}.
 * <p>
 * Note: Needs to be set to the {@link SelectionLayer} in order to work
 * correctly.
 * </p>
 *
 * @since 1.4
 */
public class CopySelectionLayerPainter extends SelectionLayerPainter {

    protected InternalCellClipboard clipboard;

    /**
     * Create a {@link CopySelectionLayerPainter} that renders gray grid lines
     * and uses the default clipping behavior.
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that stores the cells that
     *            are currently copied.
     */
    public CopySelectionLayerPainter(InternalCellClipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * Create a {@link CopySelectionLayerPainter} that renders gray grid lines
     * and uses the default clipping behavior.
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that stores the cells that
     *            are currently copied.
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public CopySelectionLayerPainter(InternalCellClipboard clipboard, final Color gridColor) {
        this.clipboard = clipboard;
    }

    /**
     * Create a {@link CopySelectionLayerPainter} that renders grid lines in the
     * specified color and uses the specified clipping behavior.
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that stores the cells that
     *            are currently copied.
     * @param gridColor
     *            The color that should be used to render the grid lines.
     * @param clipLeft
     *            Configure the rendering behavior when cells overlap. If set to
     *            <code>true</code> the left cell will be clipped, if set to
     *            <code>false</code> the right cell will be clipped. The default
     *            value is <code>false</code>.
     * @param clipTop
     *            Configure the rendering behavior when cells overlap. If set to
     *            <code>true</code> the top cell will be clipped, if set to
     *            <code>false</code> the bottom cell will be clipped. The
     *            default value is <code>false</code>.
     */
    public CopySelectionLayerPainter(InternalCellClipboard clipboard, final Color gridColor, boolean clipLeft, boolean clipTop) {
        super(gridColor, clipLeft, clipTop);
        this.clipboard = clipboard;
    }

    /**
     * Create a {@link CopySelectionLayerPainter} that renders gray grid lines
     * and uses the specified clipping behavior.
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that stores the cells that
     *            are currently copied.
     * @param clipLeft
     *            Configure the rendering behavior when cells overlap. If set to
     *            <code>true</code> the left cell will be clipped, if set to
     *            <code>false</code> the right cell will be clipped. The default
     *            value is <code>false</code>.
     * @param clipTop
     *            Configure the rendering behavior when cells overlap. If set to
     *            <code>true</code> the top cell will be clipped, if set to
     *            <code>false</code> the bottom cell will be clipped. The
     *            default value is <code>false</code>.
     */
    public CopySelectionLayerPainter(InternalCellClipboard clipboard, boolean clipLeft, boolean clipTop) {
        this(clipboard, GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    @Override
    public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset,
            Rectangle pixelRectangle, IConfigRegistry configRegistry) {

        super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);

        if (this.clipboard.getCopiedCells() != null) {
            Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);

            // nothing to draw, we exit
            if (positionRectangle.width <= 0 || positionRectangle.height <= 0) {
                return;
            }

            BorderCell[][] borderCells = getBorderCells(natLayer, xOffset, yOffset, positionRectangle, new ApplyBorderFunction() {

                @Override
                public boolean applyBorder(ILayerCell cell) {
                    if (cell.getColumnIndex() >= 0
                            && cell.getRowIndex() >= 0) {
                        ColumnPositionCoordinate convertedColumn = null;
                        RowPositionCoordinate convertedRow = null;
                        ILayerCell convertedCell = null;
                        boolean convertedCalculated = false;

                        for (ILayerCell[] cells : CopySelectionLayerPainter.this.clipboard.getCopiedCells()) {
                            for (ILayerCell copyCell : cells) {
                                if (copyCell != null) {
                                    if (!convertedCalculated) {
                                        convertedColumn = LayerCommandUtil.convertColumnPositionToTargetContext(
                                                new ColumnPositionCoordinate(cell.getLayer(), cell.getColumnPosition()),
                                                copyCell.getLayer());
                                        convertedRow = LayerCommandUtil.convertRowPositionToTargetContext(
                                                new RowPositionCoordinate(cell.getLayer(), cell.getRowPosition()),
                                                copyCell.getLayer());
                                        if (convertedColumn != null
                                                && convertedRow != null) {
                                            convertedCell = convertedColumn.getLayer().getCellByPosition(
                                                    convertedColumn.getColumnPosition(),
                                                    convertedRow.getRowPosition());
                                        }
                                        convertedCalculated = true;
                                    }

                                    if (convertedCell != null
                                            && convertedCell.getOriginColumnPosition() == copyCell.getOriginColumnPosition()
                                            && convertedCell.getOriginRowPosition() == copyCell.getOriginRowPosition()) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
            });

            if (borderCells != null) {
                // Save gc settings
                int originalLineStyle = gc.getLineStyle();
                int originalLineWidth = gc.getLineWidth();
                Color originalForeground = gc.getForeground();

                BorderStyle borderStyle = getCopyBorderStyle(configRegistry);

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
    }

    /**
     * Apply the border style that should be used to render the border for cells
     * that are currently copied to the {@link InternalCellClipboard}. Checks
     * the {@link ConfigRegistry} for a registered {@link IStyle} for the
     * {@link SelectionStyleLabels#COPY_BORDER_STYLE} label. If none is
     * registered, a default line style will be used to render the border.
     *
     * @param gc
     *            The current {@link GC} that is used for rendering.
     * @param configRegistry
     *            The {@link ConfigRegistry} to retrieve the style information
     *            from.
     */
    @Deprecated
    protected void applyCopyBorderStyle(GC gc, IConfigRegistry configRegistry) {
        IStyle cellStyle = configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                DisplayMode.NORMAL,
                SelectionStyleLabels.COPY_BORDER_STYLE);
        BorderStyle borderStyle = cellStyle != null ? cellStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE) : null;

        // if there is no border style configured, use the default
        if (borderStyle == null) {
            gc.setLineStyle(SWT.LINE_DASH);
            gc.setLineDash(new int[] { 2, 2 });
            gc.setForeground(GUIHelper.COLOR_BLACK);
        } else {
            gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
            gc.setLineWidth(borderStyle.getThickness());
            gc.setForeground(borderStyle.getColor());
        }
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
     * @return The {@link BorderStyle} that should be used for rendering the
     *         copy border.
     * @since 1.6
     */
    protected BorderStyle getCopyBorderStyle(IConfigRegistry configRegistry) {
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

}
