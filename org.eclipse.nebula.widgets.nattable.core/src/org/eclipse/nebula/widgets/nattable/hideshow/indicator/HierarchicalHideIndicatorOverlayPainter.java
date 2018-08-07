/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
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

import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialization of the {@link HideIndicatorOverlayPainter} that renders the
 * hide indicator in the level header columns of a
 * {@link HierarchicalTreeLayer}. The identification of the level header columns
 * is done via the cell label {@link HierarchicalTreeLayer#LEVEL_HEADER_CELL}.
 *
 * @since 1.6
 */
public class HierarchicalHideIndicatorOverlayPainter extends HideIndicatorOverlayPainter {

    protected HierarchicalTreeLayer treeLayer;

    /**
     * Creates a {@link HierarchicalHideIndicatorOverlayPainter} that renders
     * the hide indicator in the given given column header layer and the
     * {@link HierarchicalTreeLayer} level header columns.
     *
     * @param columnHeaderLayer
     *            The layer in the column header that should be used to
     *            determine the height of the hidden column indicator. Should be
     *            the top most layer in the column header region, e.g. the
     *            FilterRowHeaderComposite in case filtering is included. Can be
     *            <code>null</code> to avoid rendering of hidden column
     *            indicators.
     */
    public HierarchicalHideIndicatorOverlayPainter(ILayer columnHeaderLayer, HierarchicalTreeLayer treeLayer) {
        this(columnHeaderLayer, null, treeLayer);
    }

    /**
     * Creates a {@link HierarchicalHideIndicatorOverlayPainter} that renders
     * the hide indicator in the given header layers and the
     * {@link HierarchicalTreeLayer} level header columns.
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
    public HierarchicalHideIndicatorOverlayPainter(ILayer columnHeaderLayer, ILayer rowHeaderLayer, HierarchicalTreeLayer treeLayer) {
        super(columnHeaderLayer, rowHeaderLayer);
        this.treeLayer = treeLayer;
    }

    @Override
    protected void paintHiddenColumnIndicator(ILayer layer, GC gc, int xOffset, int yOffset, Rectangle rectangle) {
        if (this.columnHeaderLayer != null) {
            int lineAdjustment = gc.getLineWidth() % 2;
            int height = this.columnHeaderLayer.getHeight();

            for (int col = 0; col < layer.getColumnCount(); col++) {
                LabelStack configLabels = layer.getConfigLabelsByPosition(col, this.columnHeaderLayer.getRowCount());
                if (configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN)) {
                    // ensure that the current column and the left column belong
                    // to the same level
                    int currentLevel = this.treeLayer.getLevelByColumnIndex(layer.getColumnIndexByPosition(col));
                    int leftLevel = this.treeLayer.getLevelByColumnIndex(layer.getColumnIndexByPosition(col - 1));
                    if (currentLevel == leftLevel) {
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
                }

                if (configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN)) {
                    // ensure that the current column and the right column
                    // belong to the same level
                    int currentLevel = this.treeLayer.getLevelByColumnIndex(layer.getColumnIndexByPosition(col));
                    int rightPos = LayerUtil.convertColumnPosition(layer, col + 1, this.treeLayer);
                    int rightIndex = layer.getColumnIndexByPosition(rightPos);

                    int rightLevel = this.treeLayer.getLevelByColumnIndex(rightIndex);
                    if (currentLevel == rightLevel || (rightIndex < 0 && !this.treeLayer.isLevelHeaderColumn(rightPos))) {
                        // render the line on the right side of the last column
                        int x = layer.getStartXOfColumnPosition(col) + layer.getColumnWidthByPosition(col);
                        // adjust the rendering for the whole line width to
                        // avoid overlapping
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
    }

    @Override
    protected void paintHiddenRowIndicator(ILayer layer, GC gc, int xOffset, int yOffset, Rectangle rectangle) {
        // just in case there is a composition with a row header layer and a
        // HierarchicalTreeLayer
        super.paintHiddenRowIndicator(layer, gc, xOffset, yOffset, rectangle);

        int lineAdjustment = gc.getLineWidth() % 2;

        int startRow = this.columnHeaderLayer == null ? 0 : this.columnHeaderLayer.getRowCount();
        for (int col = 0; col < layer.getColumnCount(); col++) {
            LabelStack labels = layer.getConfigLabelsByPosition(col, startRow);
            if (labels.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL)) {
                for (int row = startRow; row < layer.getRowCount(); row++) {
                    // get labels for column + 1 to reach the body without
                    // the level header
                    LabelStack configLabels = layer.getConfigLabelsByPosition(col + 1, row);
                    if (configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN)) {
                        int y = layer.getStartYOfRowPosition(row);
                        if (this.columnHeaderLayer == null || y >= this.columnHeaderLayer.getHeight()) {
                            ILayerCell cell = layer.getCellByPosition(col, row);
                            int cellStart = layer.getStartYOfRowPosition(cell.getOriginRowPosition());
                            if (cellStart == y) {
                                int startX = layer.getStartXOfColumnPosition(col);
                                gc.drawLine(
                                        startX,
                                        y - lineAdjustment,
                                        startX + layer.getColumnWidthByPosition(col),
                                        y - lineAdjustment);
                            }
                        }
                    }

                    if (configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN)) {
                        int y = layer.getStartYOfRowPosition(row) + layer.getRowHeightByPosition(row);
                        // adjust the rendering for the whole line width to
                        // avoid overlapping
                        if (row == layer.getRowCount() - 1) {
                            lineAdjustment = (gc.getLineWidth() / 2) + lineAdjustment;
                        }
                        if (this.columnHeaderLayer == null || y >= this.columnHeaderLayer.getHeight()) {
                            ILayerCell cell = layer.getCellByPosition(col, row);
                            int cellStart = layer.getStartYOfRowPosition(cell.getOriginRowPosition());
                            if (cellStart == y) {
                                int startX = layer.getStartXOfColumnPosition(col);
                                gc.drawLine(
                                        startX,
                                        y - lineAdjustment,
                                        startX + layer.getColumnWidthByPosition(col),
                                        y - lineAdjustment);
                            }
                        }
                    }
                }
            }
        }
    }
}
