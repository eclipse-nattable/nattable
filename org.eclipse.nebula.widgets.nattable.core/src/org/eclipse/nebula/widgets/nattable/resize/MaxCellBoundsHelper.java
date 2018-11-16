/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.command.AutoResizeColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.command.AutoResizeRowCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Does the calculations needed for auto resizing feature Helper class for
 * {@link AutoResizeColumnCommandHandler} and
 * {@link AutoResizeRowCommandHandler}
 */
public class MaxCellBoundsHelper {

    /**
     * Calculates the preferred column widths of the given columns based on the
     * given {@link IConfigRegistry}. The preferred column width is the width
     * needed at minimum to fit all the contents horizontally.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to get the required configuration
     *            values.
     * @param gcFactory
     *            The {@link GCFactory} for creating a temporary {@link GC}
     *            needed for UI related calculations without blocking the UI
     *            thread.
     * @param layer
     *            The layer to which the column positions match.
     * @param columnPositions
     *            The column positions for which the preferred width should be
     *            calculated.
     * @return The preferred column widths of the given columns or
     *         <code>null</code> if an error occurred on processing.
     */
    public static int[] getPreferredColumnWidths(
            IConfigRegistry configRegistry, GCFactory gcFactory, ILayer layer, int[] columnPositions) {

        GC gc = gcFactory.createGC();
        if (gc != null) {
            int[] columnWidths = new int[columnPositions.length];
            for (int i = 0; i < columnPositions.length; i++) {
                columnWidths[i] = getPreferredColumnWidth(layer, columnPositions[i], configRegistry, gc);
            }
            gc.dispose();

            return columnWidths;
        } else {
            return null;
        }
    }

    /**
     * Calculates the minimum width (in pixels) required to display the complete
     * contents of the cells in a column. Takes into account the font settings
     * and display type conversion.
     */
    private static int getPreferredColumnWidth(
            ILayer layer, int columnPosition, IConfigRegistry configRegistry, GC gc) {

        ICellPainter painter;
        int maxWidth = 0;
        ILayerCell cell;

        for (int rowPosition = 0; rowPosition < layer.getRowCount(); rowPosition++) {
            cell = layer.getCellByPosition(columnPosition, rowPosition);
            if (cell != null) {
                boolean atEndOfCellSpan = cell.getOriginColumnPosition()
                        + cell.getColumnSpan() - 1 == columnPosition;
                if (atEndOfCellSpan) {
                    painter = layer.getCellPainter(cell.getColumnPosition(),
                            cell.getRowPosition(), cell, configRegistry);
                    if (painter != null) {
                        int preferredWidth = painter.getPreferredWidth(cell,
                                gc, configRegistry);

                        // Adjust width
                        Rectangle bounds = cell.getBounds();
                        bounds.width = preferredWidth;
                        Rectangle adjustedCellBounds = cell
                                .getLayer()
                                .getLayerPainter()
                                .adjustCellBounds(columnPosition, rowPosition,
                                        bounds);
                        preferredWidth += preferredWidth
                                - adjustedCellBounds.width;

                        if (cell.getColumnSpan() > 1) {
                            int columnStartX = layer
                                    .getStartXOfColumnPosition(columnPosition);
                            int cellStartX = layer
                                    .getStartXOfColumnPosition(cell
                                            .getOriginColumnPosition());
                            preferredWidth = Math.max(0, preferredWidth
                                    - (columnStartX - cellStartX));
                        }

                        maxWidth = (preferredWidth > maxWidth) ? preferredWidth
                                : maxWidth;
                    }
                }
            }
        }

        return maxWidth;
    }

    /**
     * Calculates the preferred row heights of the given rows based on the given
     * {@link IConfigRegistry}. The preferred row height is the height needed at
     * minimum to fit all the content vertically.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} to get the required configuration
     *            values.
     * @param gcFactory
     *            The {@link GCFactory} for creating a temporary {@link GC}
     *            needed for UI related calculations without blocking the UI
     *            thread.
     * @param layer
     *            The layer to which the row positions match.
     * @param rowPositions
     *            The row positions for which the preferred height should be
     *            calculated.
     * @return The preferred row heights of the given rows or <code>null</code>
     *         if an error occurred on processing.
     */
    public static int[] getPreferredRowHeights(
            IConfigRegistry configRegistry, GCFactory gcFactory, ILayer layer, int[] rowPositions) {

        GC gc = gcFactory.createGC();
        if (gc != null) {
            int[] rowHeights = new int[rowPositions.length];
            for (int i = 0; i < rowPositions.length; i++) {
                rowHeights[i] = getPreferredRowHeight(layer, rowPositions[i], configRegistry, gc);
            }
            gc.dispose();

            return rowHeights;
        } else {
            return null;
        }
    }

    private static int getPreferredRowHeight(
            ILayer layer, int rowPosition, IConfigRegistry configRegistry, GC gc) {

        int maxHeight = 0;
        ICellPainter painter;
        ILayerCell cell;

        for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
            cell = layer.getCellByPosition(columnPosition, rowPosition);
            if (cell != null) {
                boolean atEndOfCellSpan = cell.getOriginRowPosition()
                        + cell.getRowSpan() - 1 == rowPosition;
                if (atEndOfCellSpan) {
                    painter = layer.getCellPainter(cell.getColumnPosition(),
                            cell.getRowPosition(), cell, configRegistry);
                    if (painter != null) {
                        int preferredHeight = painter.getPreferredHeight(cell,
                                gc, configRegistry);

                        // Adjust height
                        Rectangle bounds = cell.getBounds();
                        bounds.height = preferredHeight;
                        Rectangle adjustedCellBounds = cell
                                .getLayer()
                                .getLayerPainter()
                                .adjustCellBounds(columnPosition, rowPosition,
                                        bounds);
                        preferredHeight += preferredHeight
                                - adjustedCellBounds.height;

                        if (cell.getColumnSpan() > 1) {
                            int rowStartY = layer
                                    .getStartYOfRowPosition(rowPosition);
                            int cellStartY = layer.getStartYOfRowPosition(cell
                                    .getOriginRowPosition());
                            preferredHeight = Math.max(0, preferredHeight
                                    - (rowStartY - cellStartY));
                        }

                        maxHeight = (preferredHeight > maxHeight) ? preferredHeight
                                : maxHeight;
                    }
                }
            }
        }

        return maxHeight;
    }

    /**
     * Traverse the two arrays and return the greater element in each index
     * position.
     */
    public static int[] greater(int[] array1, int[] array2) {
        int resultSize = (array1.length < array2.length) ? array1.length
                : array2.length;
        int[] result = new int[resultSize];

        for (int i = 0; i < resultSize; i++) {
            result[i] = (array1[i] > array2[i]) ? array1[i] : array2[i];
        }
        return result;
    }
}
