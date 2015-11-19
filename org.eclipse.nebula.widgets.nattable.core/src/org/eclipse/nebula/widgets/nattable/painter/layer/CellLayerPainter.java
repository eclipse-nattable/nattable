/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class CellLayerPainter implements ILayerPainter {

    private ILayer natLayer;
    private Map<Integer, Integer> horizontalPositionToPixelMap;
    private Map<Integer, Integer> verticalPositionToPixelMap;

    private final boolean clipLeft;
    private final boolean clipTop;

    /**
     * Create a default CellLayerPainter with default clipping behaviour.
     */
    public CellLayerPainter() {
        this(false, false);
    }

    /**
     * Create a CellLayerPainter with specified clipping behaviour.
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
    public CellLayerPainter(boolean clipLeft, boolean clipTop) {
        this.clipLeft = clipLeft;
        this.clipTop = clipTop;
    }

    @Override
    public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset,
            Rectangle pixelRectangle, IConfigRegistry configRegistry) {
        if (pixelRectangle.width <= 0 || pixelRectangle.height <= 0) {
            return;
        }

        this.natLayer = natLayer;
        Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);

        calculateDimensionInfo(positionRectangle);

        Collection<ILayerCell> spannedCells = new HashSet<ILayerCell>();

        for (int columnPosition = positionRectangle.x; columnPosition < positionRectangle.x
                + positionRectangle.width; columnPosition++) {
            for (int rowPosition = positionRectangle.y; rowPosition < positionRectangle.y
                    + positionRectangle.height; rowPosition++) {
                if (columnPosition == -1 || rowPosition == -1) {
                    continue;
                }
                ILayerCell cell = natLayer.getCellByPosition(columnPosition, rowPosition);
                if (cell != null) {
                    if (cell.isSpannedCell()) {
                        spannedCells.add(cell);
                    } else {
                        paintCell(cell, gc, configRegistry);
                    }
                }
            }
        }

        for (ILayerCell cell : spannedCells) {
            paintCell(cell, gc, configRegistry);
        }
    }

    /**
     * Determines the rendering behavior when two cells overlap. If
     * <code>true</code>, the left cell will be clipped. If <code>false</code>,
     * the right cell will be clipped. Typically this value is changed in
     * conjunction with split viewports.
     *
     * @param position
     *            The column position for which the clipping behaviour is
     *            requested. By default for all columns the same clipping
     *            behaviour is used. Only for special cases like split viewports
     *            with one header, per position a different behaviour may be
     *            needed.
     */
    protected boolean isClipLeft(int position) {
        return this.clipLeft;
    }

    /**
     * Determines the rendering behavior when two cells overlap. If
     * <code>true</code>, the top cell will be clipped. If <code>false</code>,
     * the bottom cell will be clipped. Typically this value is changed in
     * conjunction with split viewports.
     *
     * @param position
     *            The row position for which the clipping behaviour is
     *            requested. By default for all rows the same clipping behaviour
     *            is used. Only for special cases like split viewports with one
     *            header, per position a different behaviour may be needed.
     */
    protected boolean isClipTop(int position) {
        return this.clipTop;
    }

    private void calculateDimensionInfo(Rectangle positionRectangle) {
        {
            this.horizontalPositionToPixelMap = new HashMap<Integer, Integer>();
            final int startPosition = positionRectangle.x;
            final int endPosition = startPosition + positionRectangle.width;
            int previousEndX = (startPosition > 0)
                    ? this.natLayer.getStartXOfColumnPosition(startPosition - 1)
                            + this.natLayer.getColumnWidthByPosition(startPosition - 1)
                    : Integer.MIN_VALUE;
            for (int position = startPosition; position < endPosition; position++) {
                int startX = this.natLayer.getStartXOfColumnPosition(position);
                this.horizontalPositionToPixelMap.put(
                        position,
                        isClipLeft(position) ? startX : Math.max(startX, previousEndX));
                previousEndX = startX + this.natLayer.getColumnWidthByPosition(position);
            }
            if (endPosition < this.natLayer.getColumnCount()) {
                int startX = this.natLayer.getStartXOfColumnPosition(endPosition);
                this.horizontalPositionToPixelMap.put(endPosition, Math.max(startX, previousEndX));
            }
        }
        {
            this.verticalPositionToPixelMap = new HashMap<Integer, Integer>();
            final int startPosition = positionRectangle.y;
            final int endPosition = startPosition + positionRectangle.height;
            int previousEndY = (startPosition > 0)
                    ? this.natLayer.getStartYOfRowPosition(startPosition - 1)
                            + this.natLayer.getRowHeightByPosition(startPosition - 1)
                    : Integer.MIN_VALUE;
            for (int position = startPosition; position < endPosition; position++) {
                int startY = this.natLayer.getStartYOfRowPosition(position);
                this.verticalPositionToPixelMap.put(
                        position,
                        isClipTop(position) ? startY : Math.max(startY, previousEndY));
                previousEndY = startY + this.natLayer.getRowHeightByPosition(position);
            }
            if (endPosition < this.natLayer.getRowCount()) {
                int startY = this.natLayer.getStartYOfRowPosition(endPosition);
                this.verticalPositionToPixelMap.put(endPosition, Math.max(startY, previousEndY));
            }
        }
    }

    @Override
    public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle cellBounds) {
        return cellBounds;
    }

    protected Rectangle getPositionRectangleFromPixelRectangle(ILayer natLayer, Rectangle pixelRectangle) {
        int columnPositionOffset = natLayer.getColumnPositionByX(pixelRectangle.x);
        int rowPositionOffset = natLayer.getRowPositionByY(pixelRectangle.y);
        int numColumns = natLayer.getColumnPositionByX(
                Math.min(natLayer.getWidth(), pixelRectangle.x + pixelRectangle.width) - 1)
                - columnPositionOffset + 1;
        int numRows = natLayer.getRowPositionByY(
                Math.min(natLayer.getHeight(), pixelRectangle.y + pixelRectangle.height) - 1)
                - rowPositionOffset + 1;

        return new Rectangle(
                columnPositionOffset,
                rowPositionOffset,
                numColumns,
                numRows);
    }

    protected void paintCell(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        ILayer layer = cell.getLayer();
        int columnPosition = cell.getColumnPosition();
        int rowPosition = cell.getRowPosition();
        ICellPainter cellPainter = layer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
        Rectangle adjustedCellBounds = layer
                .getLayerPainter()
                .adjustCellBounds(columnPosition, rowPosition, cell.getBounds());
        if (cellPainter != null) {
            Rectangle originalClipping = gc.getClipping();

            int startX = getStartXOfColumnPosition(columnPosition);
            int startY = getStartYOfRowPosition(rowPosition);

            int endX = getStartXOfColumnPosition(cell.getOriginColumnPosition() + cell.getColumnSpan());
            int endY = getStartYOfRowPosition(cell.getOriginRowPosition() + cell.getRowSpan());

            Rectangle cellClipBounds = originalClipping.intersection(
                    new Rectangle(startX, startY, endX - startX, endY - startY));
            gc.setClipping(cellClipBounds.intersection(adjustedCellBounds));

            cellPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);

            gc.setClipping(originalClipping);
        }
    }

    protected int getStartXOfColumnPosition(final int columnPosition) {
        if (columnPosition < this.natLayer.getColumnCount()) {
            Integer start = this.horizontalPositionToPixelMap.get(columnPosition);
            if (start == null) {
                start = this.natLayer.getStartXOfColumnPosition(columnPosition);
                if (columnPosition > 0) {
                    int start2 = this.natLayer.getStartXOfColumnPosition(columnPosition - 1)
                            + this.natLayer.getColumnWidthByPosition(columnPosition - 1);
                    if (start2 > start.intValue()) {
                        start = start2;
                    }
                }
                this.horizontalPositionToPixelMap.put(columnPosition, start);
            }
            return start.intValue();
        } else {
            return this.natLayer.getWidth();
        }
    }

    protected int getStartYOfRowPosition(final int rowPosition) {
        if (rowPosition < this.natLayer.getRowCount()) {
            Integer start = this.verticalPositionToPixelMap.get(rowPosition);
            if (start == null) {
                start = this.natLayer.getStartYOfRowPosition(rowPosition);
                if (rowPosition > 0) {
                    int start2 = this.natLayer.getStartYOfRowPosition(rowPosition - 1)
                            + this.natLayer.getRowHeightByPosition(rowPosition - 1);
                    if (start2 > start.intValue()) {
                        start = start2;
                    }
                }
                this.verticalPositionToPixelMap.put(rowPosition, start);
            }
            return start.intValue();
        } else {
            return this.natLayer.getHeight();
        }
    }

}
