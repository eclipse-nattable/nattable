/*****************************************************************************
 * Copyright (c) 2015, 2016 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *      Loris Securo <lorissek@gmail.com> - Bug 499513, 499551, 500764, 500800
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BorderPainter.BorderCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.GraphicsUtils;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
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
 * Extended {@link SelectionLayerPainter} that renders an additional border
 * around cells that are selected via fill handle. By default the additional
 * fill handle border style is a green solid 2 pixel sized line. This
 * {@link BorderStyle} can be configured via IConfigRegistry using the config
 * label {@link FillHandleConfigAttributes#FILL_HANDLE_REGION_BORDER_STYLE}.
 * <p>
 * You can also register a different cell style for cells in the fill handle
 * region by configuring a style for the label
 * {@link SelectionStyleLabels#FILL_HANDLE_REGION}
 * </p>
 * <p>
 * This {@link ILayerPainter} also renders a border around cells that are
 * currently copied to the {@link InternalCellClipboard}. For this an
 * {@link InternalCellClipboard} needs to be set to this painter. Note that a
 * global instance of {@link InternalCellClipboard} can be retrieved via
 * {@link NatTable#getInternalCellClipboard()}.
 * </p>
 *
 * @see FillHandleConfigAttributes#FILL_HANDLE_REGION_BORDER_STYLE
 * @see SelectionStyleLabels#FILL_HANDLE_REGION
 *
 * @since 1.4
 */
public class FillHandleLayerPainter extends SelectionLayerPainter {

    /**
     * The bounds of the current visible selection handle or <code>null</code>
     * if no fill handle is currently rendered.
     */
    protected Rectangle handleBounds;

    /**
     * The {@link InternalCellClipboard} that is used to identify whether a cell
     * is currently copied. Can be <code>null</code> to disable special
     * rendering of copied cells.
     */
    protected InternalCellClipboard clipboard;

    /**
     * Create a SelectionLayerPainter that renders gray grid lines and uses the
     * default clipping behavior.
     */
    public FillHandleLayerPainter() {
        super();
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders grid lines in the
     * specified color and uses the default clipping behavior.
     *
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public FillHandleLayerPainter(final Color gridColor) {
        super(gridColor);
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders gray grid lines and
     * uses the specified clipping behavior.
     *
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
    public FillHandleLayerPainter(boolean clipLeft, boolean clipTop) {
        this(GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders grid lines in the
     * specified color and uses the specified clipping behavior.
     *
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
    public FillHandleLayerPainter(final Color gridColor, boolean clipLeft, boolean clipTop) {
        super(gridColor, clipLeft, clipTop);
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders gray grid lines and
     * uses the default clipping behavior. It also renders a border around
     * internally copied cells.
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that stores the cells that
     *            are currently copied.
     */
    public FillHandleLayerPainter(InternalCellClipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders grid lines in the
     * specified color and uses the default clipping behavior.
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that stores the cells that
     *            are currently copied.
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public FillHandleLayerPainter(InternalCellClipboard clipboard, final Color gridColor) {
        super(gridColor);
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders gray grid lines and
     * uses the specified clipping behavior.
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
    public FillHandleLayerPainter(InternalCellClipboard clipboard,
            boolean clipLeft, boolean clipTop) {
        this(clipboard, GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    /**
     * Create an {@link FillHandleLayerPainter} that renders grid lines in the
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
    public FillHandleLayerPainter(InternalCellClipboard clipboard, final Color gridColor,
            boolean clipLeft, boolean clipTop) {
        super(gridColor, clipLeft, clipTop);
        this.clipboard = clipboard;
    }

    @Override
    public void paintLayer(
            ILayer natLayer, GC gc,
            int xOffset, int yOffset, Rectangle pixelRectangle,
            IConfigRegistry configRegistry) {

        Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
        int columnPositionOffset = positionRectangle.x;
        int rowPositionOffset = positionRectangle.y;

        super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);

        ILayerCell fillHandleCell = null;

        BorderCell[][] borderCells = new BorderCell[positionRectangle.height][positionRectangle.width];
        boolean atLeastOne = false;

        for (int columnPosition = columnPositionOffset, ix = 0; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++, ix++) {
            for (int rowPosition = rowPositionOffset, iy = 0; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++, iy++) {

                boolean insideBorder = false;
                Rectangle cellBounds = null;

                ILayerCell currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
                if (currentCell != null) {

                    cellBounds = currentCell.getBounds();

                    if (isFillHandleRegion(currentCell)) {
                        insideBorder = true;
                        atLeastOne = true;
                    }

                    if (fillHandleCell == null && isFillHandleCell(currentCell)) {
                        fillHandleCell = currentCell;
                    }
                }

                Rectangle fixedBounds = fixBoundsInGridLines(cellBounds, xOffset, yOffset);
                BorderCell borderCell = new BorderCell(fixedBounds, insideBorder);
                borderCells[iy][ix] = borderCell;
            }
        }

        if (atLeastOne) {
            // Save gc settings
            int originalLineStyle = gc.getLineStyle();
            int originalLineWidth = gc.getLineWidth();
            Color originalForeground = gc.getForeground();

            BorderStyle fillHandleRegionBorderStyle = getHandleRegionBorderStyle(configRegistry);

            BorderPainter borderPainter = new BorderPainter(borderCells, fillHandleRegionBorderStyle);
            borderPainter.paintBorder(gc);

            // Restore original gc settings
            gc.setLineStyle(originalLineStyle);
            gc.setLineWidth(originalLineWidth);
            gc.setForeground(originalForeground);
        }

        // paint the border around the copied cells if a clipboard is set
        if (this.clipboard != null && this.clipboard.getCopiedCells() != null) {
            paintCopyBorder(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);
        }

        // in case of single cell update, the fill handle might (partially)
        // disappear if it's in an adjacent cell; so we check the adjacent cells
        // to find it and eventually repaint it
        if (fillHandleCell == null && positionRectangle.width <= 2 && positionRectangle.height <= 2) {
            for (int columnPosition = columnPositionOffset - 1; columnPosition < columnPositionOffset + positionRectangle.width + 1 && fillHandleCell == null; columnPosition++) {
                for (int rowPosition = rowPositionOffset - 1; rowPosition < rowPositionOffset + positionRectangle.height + 1 && fillHandleCell == null; rowPosition++) {
                    ILayerCell currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
                    if (currentCell != null) {
                        if (isFillHandleCell(currentCell)) {
                            fillHandleCell = currentCell;
                        }
                    }
                }
            }
        }

        if (fillHandleCell != null) {
            paintFillHandle(fillHandleCell, gc, xOffset, yOffset, configRegistry);
        } else {
            // set the local stored bounds to null as no handle is rendered and
            // therefore event matchers shouldn't react anymore
            this.handleBounds = null;
        }
    }

    protected void paintFillHandle(
            ILayerCell fillHandleCell, GC gc,
            int xOffset, int yOffset,
            IConfigRegistry configRegistry) {

        // Save gc settings
        Color originalBackground = gc.getBackground();
        Rectangle originalClipping = gc.getClipping();

        Rectangle bounds = fillHandleCell.getBounds();

        int fillHandleSize = GUIHelper.convertHorizontalPixelToDpi(7);

        // positions offset starting from the lower right corner of the fill
        // handle cell
        int fillHandleOffsetX = -GUIHelper.convertHorizontalPixelToDpi(4);
        int fillHandleOffsetY = -GUIHelper.convertHorizontalPixelToDpi(4);

        Rectangle handleInterior = new Rectangle(
                bounds.x + bounds.width + fillHandleOffsetX,
                bounds.y + bounds.height + fillHandleOffsetY,
                fillHandleSize,
                fillHandleSize);

        BorderStyle borderStyle = getHandleBorderStyle(configRegistry);

        this.handleBounds = GraphicsUtils.getResultingExternalBounds(handleInterior, borderStyle);

        // how much we need to increment the gc clipping to paint the whole
        // fill handle
        int clippingWidthIncrement = Math.max(0, (this.handleBounds.x + this.handleBounds.width) - (originalClipping.x + originalClipping.width));
        int clippingHeightIncrement = Math.max(0, (this.handleBounds.y + this.handleBounds.height) - (originalClipping.y + originalClipping.height));

        if (clippingWidthIncrement > 0 || clippingHeightIncrement > 0) {
            gc.setClipping(originalClipping.x, originalClipping.y,
                    originalClipping.width + clippingWidthIncrement,
                    originalClipping.height + clippingHeightIncrement);
        }

        Color color = getHandleColor(configRegistry);
        gc.setBackground(color);

        GraphicsUtils.fillRectangle(gc, handleInterior);
        GraphicsUtils.drawRectangle(gc, handleInterior, borderStyle);

        // Restore original gc settings
        gc.setBackground(originalBackground);
        gc.setClipping(originalClipping);
    }

    protected void paintCopyBorder(
            ILayer natLayer, GC gc,
            int xOffset, int yOffset, Rectangle pixelRectangle,
            IConfigRegistry configRegistry) {

        Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
        int columnPositionOffset = positionRectangle.x;
        int rowPositionOffset = positionRectangle.y;

        // Save gc settings
        int originalLineStyle = gc.getLineStyle();
        Color originalForeground = gc.getForeground();

        applyCopyBorderStyle(gc, configRegistry);

        int x0 = 0;
        int x1 = 0;
        int y0 = 0;
        int y1 = 0;
        boolean isFirst = true;
        for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
            for (ILayerCell cell : cells) {
                if (isFirst) {
                    x0 = cell.getBounds().x;
                    x1 = cell.getBounds().x + cell.getBounds().width;
                    y0 = cell.getBounds().y;
                    y1 = cell.getBounds().y + cell.getBounds().height;
                    isFirst = false;
                } else {
                    x0 = Math.min(x0, cell.getBounds().x);
                    x1 = Math.max(x1, cell.getBounds().x + cell.getBounds().width);
                    y0 = Math.min(y0, cell.getBounds().y);
                    y1 = Math.max(y1, cell.getBounds().y + cell.getBounds().height);
                }
            }
        }

        x0 += xOffset - columnPositionOffset;
        x1 += xOffset - columnPositionOffset;
        y0 += yOffset - rowPositionOffset;
        y1 += yOffset - rowPositionOffset;

        gc.drawLine(x0, y0, x0, y1);
        gc.drawLine(x0, y0, x1, y0);
        gc.drawLine(x0, y1, x1, y1);
        gc.drawLine(x1, y0, x1, y1);

        // Restore original gc settings
        gc.setLineStyle(originalLineStyle);
        gc.setForeground(originalForeground);
    }

    /**
     *
     * @param cell
     *            The {@link ILayerCell} to check.
     * @return <code>true</code> if the cell is part of the fill handle region,
     *         <code>false</code> if not.
     */
    protected boolean isFillHandleRegion(ILayerCell cell) {
        return (cell != null) ? cell.getConfigLabels().hasLabel(SelectionStyleLabels.FILL_HANDLE_REGION) : false;
    }

    /**
     *
     * @param cell
     *            The {@link ILayerCell} to check.
     * @return <code>true</code> if the cell is the bottom right cell in a fill
     *         region, <code>false</code> if not.
     */
    protected boolean isFillHandleCell(ILayerCell cell) {
        return (cell != null) ? cell.getConfigLabels().hasLabel(SelectionStyleLabels.FILL_HANDLE_CELL) : false;
    }

    /**
     * Apply the border style that should be used to render the border for cells
     * that are currently part of the fill handle region. Checks the
     * {@link IConfigRegistry} for a registered {@link IStyle} for the
     * {@link FillHandleConfigAttributes#FILL_HANDLE_REGION_BORDER_STYLE} label.
     * If none is registered, a default line style will be used to render the
     * border.
     *
     * @param gc
     *            The current {@link GC} that is used for rendering.
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the style information
     *            from.
     * @deprecated Use {@link #getHandleRegionBorderStyle} instead.
     */
    @Deprecated
    protected void applyHandleBorderStyle(GC gc, IConfigRegistry configRegistry) {
        BorderStyle borderStyle = configRegistry.getConfigAttribute(
                FillHandleConfigAttributes.FILL_HANDLE_REGION_BORDER_STYLE,
                DisplayMode.NORMAL);

        // if there is no border style configured, use the default
        if (borderStyle == null) {
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setLineWidth(2);
            gc.setForeground(GUIHelper.getColor(0, 125, 10));
        } else {
            gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
            gc.setLineWidth(borderStyle.getThickness());
            gc.setForeground(borderStyle.getColor());
        }
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
     *
     * @since 1.5
     */
    protected BorderStyle getHandleRegionBorderStyle(IConfigRegistry configRegistry) {
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
     * Apply the style for rendering the fill handle. If the
     * {@link IConfigRegistry} is <code>null</code> or does not contain
     * configurations for styling the fill handle, the default style is used.
     * The default is a dark green square with a white solid one pixel width
     * line.
     *
     * @param gc
     *            The {@link GC} to set the styles to.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to determine the configured
     *            fill handle style. Can be <code>null</code> which results in
     *            using the default style of a green square with white 1 pixel
     *            solid line border.
     * @deprecated Use {@link #getHandleColor} and {@link #getHandleBorderStyle}
     *             instead.
     */
    @Deprecated
    protected void applyHandleStyle(GC gc, IConfigRegistry configRegistry) {
        if (configRegistry != null) {
            BorderStyle borderStyle = configRegistry.getConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_BORDER_STYLE,
                    DisplayMode.NORMAL);

            Color color = configRegistry.getConfigAttribute(
                    FillHandleConfigAttributes.FILL_HANDLE_COLOR,
                    DisplayMode.NORMAL);

            if (color != null) {
                gc.setBackground(color);
            } else {
                // set default color
                gc.setBackground(GUIHelper.getColor(0, 125, 10));
            }

            if (borderStyle != null) {
                gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
                gc.setLineWidth(borderStyle.getThickness());
                gc.setForeground(borderStyle.getColor());
            } else {
                gc.setLineStyle(SWT.LINE_SOLID);
                gc.setLineWidth(1);
                gc.setForeground(GUIHelper.COLOR_WHITE);
            }
        } else {
            // set default border style
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setLineWidth(1);
            gc.setForeground(GUIHelper.COLOR_WHITE);
            // set default color
            gc.setBackground(GUIHelper.getColor(0, 125, 10));
        }
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
     *
     * @since 1.5
     */
    protected Color getHandleColor(IConfigRegistry configRegistry) {
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
     *
     * @since 1.5
     */
    protected BorderStyle getHandleBorderStyle(IConfigRegistry configRegistry) {
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
     * Apply the border style that should be used to render the border for cells
     * that are currently copied to the {@link InternalCellClipboard}. Checks
     * the {@link IConfigRegistry} for a registered {@link IStyle} for the
     * {@link SelectionStyleLabels#COPY_BORDER_STYLE} label. If none is
     * registered, a default line style will be used to render the border.
     *
     * @param gc
     *            The current {@link GC} that is used for rendering.
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the style information
     *            from.
     */
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
     *
     * @return The bounds of the current visible selection handle or
     *         <code>null</code> if no fill handle is currently rendered.
     */
    public Rectangle getSelectionHandleBounds() {
        return this.handleBounds;
    }

    /**
     *
     * @return The {@link InternalCellClipboard} that is used to identify
     *         whether a cell is currently copied or <code>null</code> if
     *         special rendering of copied cells is disabled.
     */
    public InternalCellClipboard getClipboard() {
        return this.clipboard;
    }

    /**
     *
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used to
     *            identify whether a cell is currently copied or
     *            <code>null</code> to disable special rendering of copied
     *            cells.
     */
    public void setClipboard(InternalCellClipboard clipboard) {
        this.clipboard = clipboard;
    }

}
