/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - added automatic size calculation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - added scaling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Paints an image. If no image is provided, it will attempt to look up an image
 * from the cell style.
 */
public class ImagePainter extends BackgroundPainter {

    private Image image;
    private final boolean paintBg;

    protected boolean calculateByWidth;
    protected boolean calculateByHeight;

    public ImagePainter() {
        this(null);
    }

    public ImagePainter(Image image) {
        this(image, true);
    }

    /**
     * @since 1.4
     */
    public ImagePainter(boolean paintBg) {
        this.paintBg = paintBg;
    }

    public ImagePainter(Image image, boolean paintBg) {
        this.image = image;
        this.paintBg = paintBg;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        Image image = getImage(cell, configRegistry);
        if (image != null) {
            return image.getBounds().width;
        } else {
            return 0;
        }
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        Image image = getImage(cell, configRegistry);
        if (image != null) {
            return image.getBounds().height;
        } else {
            return 0;
        }
    }

    @Override
    public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc,
            Rectangle bounds, IConfigRegistry configRegistry) {

        Image image = getImage(cell, configRegistry);
        if (image != null) {
            Rectangle imageBounds = image.getBounds();
            IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
            int x0 = bounds.x
                    + CellStyleUtil.getHorizontalAlignmentPadding(
                            cellStyle, bounds, imageBounds.width);
            int y0 = bounds.y
                    + CellStyleUtil.getVerticalAlignmentPadding(
                            cellStyle, bounds, imageBounds.height);
            if (x >= x0 && x < x0 + imageBounds.width
                    && y >= y0 && y < y0 + imageBounds.height) {
                return super.getCellPainterAt(x, y, cell, gc, bounds, configRegistry);
            }
        }
        return null;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        if (this.paintBg) {
            super.paintCell(cell, gc, bounds, configRegistry);
        }

        Image image = getImage(cell, configRegistry);
        if (image != null) {
            Rectangle imageBounds = image.getBounds();
            IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);

            int contentHeight = imageBounds.height;
            if (this.calculateByHeight && (contentHeight > bounds.height)) {
                int contentToCellDiff = (cell.getBounds().height - bounds.height);
                ILayer layer = cell.getLayer();
                layer.doCommand(new RowResizeCommand(
                        layer,
                        cell.getRowPosition(),
                        contentHeight + contentToCellDiff));
            }

            int contentWidth = imageBounds.width;
            if (this.calculateByWidth && (contentWidth > bounds.width)) {
                int contentToCellDiff = (cell.getBounds().width - bounds.width);
                ILayer layer = cell.getLayer();
                layer.doCommand(new ColumnResizeCommand(
                        layer,
                        cell.getColumnPosition(),
                        contentWidth + contentToCellDiff));
            }

            gc.drawImage(
                    image,
                    bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width),
                    bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height));
        }
    }

    /**
     *
     * @param cell
     *            The {@link ILayerCell} for which this {@link ImagePainter} is
     *            called.
     * @param configRegistry
     *            The current {@link IConfigRegistry} to retrieve the cell style
     *            information from.
     * @return The {@link Image} that should be painted by this
     *         {@link ImagePainter}.
     */
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        return this.image != null ? this.image : CellStyleUtil.getCellStyle(
                cell, configRegistry).getAttributeValue(CellStyleAttributes.IMAGE);
    }

    /**
     * @return <code>true</code> if this {@link ImagePainter} is resizing the
     *         cell width to show the whole configured image, <code>false</code>
     *         if the cell width is not touched by this painter.
     */
    public boolean isCalculateByWidth() {
        return this.calculateByWidth;
    }

    /**
     * Configure whether the {@link ImagePainter} should calculate the cell
     * dimensions by containing image width. This means the <b>width</b> of the
     * cell is calculated by image width.
     *
     * @param calculateByWidth
     *            <code>true</code> to calculate and modify the cell dimension
     *            according to the image width, <code>false</code> to not
     *            modifying the cell dimensions.
     */
    public void setCalculateByWidth(boolean calculateByWidth) {
        this.calculateByWidth = calculateByWidth;
    }

    /**
     * @return <code>true</code> if this {@link ImagePainter} is resizing the
     *         cell height to show the whole configured image,
     *         <code>false</code> if the cell height is not touched by this
     *         painter.
     */
    public boolean isCalculateByHeight() {
        return this.calculateByHeight;
    }

    /**
     * Configure whether the {@link ImagePainter} should calculate the cell
     * dimensions by containing image height. This means the <b>height</b> of
     * the cell is calculated by image height.
     *
     * @param calculateByHeight
     *            <code>true</code> to calculate and modify the cell dimension
     *            according to the image height, <code>false</code> to not
     *            modifying the cell dimensions.
     */
    public void setCalculateByHeight(boolean calculateByHeight) {
        this.calculateByHeight = calculateByHeight;
    }

}
