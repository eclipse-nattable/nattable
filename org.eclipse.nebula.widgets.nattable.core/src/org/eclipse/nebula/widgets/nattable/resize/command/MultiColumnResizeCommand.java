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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger resizing of multiple columns.
 */
public class MultiColumnResizeCommand extends AbstractMultiColumnCommand {

    private int commonColumnWidth = -1;
    protected Map<ColumnPositionCoordinate, Integer> colPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>();
    private final boolean downScale;

    /**
     * Create a {@link MultiColumnResizeCommand} to resize multiple columns,
     * where each column is resized to the same width, e.g. during a drag
     * resize. The given column width will be taken as is without scaling.
     *
     * @param layer
     *            The {@link ILayer} to which the column positions correlate.
     * @param columnPositions
     *            The positions of the columns that should be resized.
     * @param commonColumnWidth
     *            The column width that should be applied to all given columns.
     */
    public MultiColumnResizeCommand(ILayer layer, int[] columnPositions, int commonColumnWidth) {
        this(layer, columnPositions, commonColumnWidth, false);
    }

    /**
     * Create a {@link MultiColumnResizeCommand} to resize multiple columns,
     * where each column is resized to the same width, e.g. during a drag
     * resize.
     *
     * @param layer
     *            The {@link ILayer} to which the column positions correlate.
     * @param columnPositions
     *            The positions of the columns that should be resized.
     * @param commonColumnWidth
     *            The column width that should be applied to all given columns.
     * @param downScale
     *            <code>true</code> if the commonColumnWidth value should be
     *            down scaled according to the scaling level, <code>false</code>
     *            if the value should be taken as is.
     *
     * @since 1.6
     */
    public MultiColumnResizeCommand(ILayer layer, int[] columnPositions, int commonColumnWidth, boolean downScale) {
        super(layer, columnPositions);
        this.commonColumnWidth = commonColumnWidth;
        this.downScale = downScale;
    }

    /**
     * Create a {@link MultiColumnResizeCommand} to resize multiple columns,
     * where each column can be resized to a different size, e.g. during auto
     * resize. The given column width will be taken as is without scaling.
     *
     * @param layer
     *            The {@link ILayer} to which the column positions correlate.
     * @param columnPositions
     *            The positions of the columns that should be resized.
     * @param columnWidths
     *            The new widths that should be applied to the given columns.
     *            The indexes in the arrays need to correlate.
     */
    public MultiColumnResizeCommand(ILayer layer, int[] columnPositions, int[] columnWidths) {
        this(layer, columnPositions, columnWidths, false);
    }

    /**
     * Create a {@link MultiColumnResizeCommand} to resize multiple columns,
     * where each column can be resized to a different size, e.g. during auto
     * resize.
     *
     * @param layer
     *            The {@link ILayer} to which the column positions correlate.
     * @param columnPositions
     *            The positions of the columns that should be resized.
     * @param columnWidths
     *            The new widths that should be applied to the given columns.
     *            The indexes in the arrays need to correlate.
     * @param downScale
     *            <code>true</code> if the columnWidths value should be down
     *            scaled according to the scaling level, <code>false</code> if
     *            the value should be taken as is.
     *
     * @since 1.6
     */
    public MultiColumnResizeCommand(ILayer layer, int[] columnPositions, int[] columnWidths, boolean downScale) {
        super(layer, columnPositions);
        for (int i = 0; i < columnPositions.length; i++) {
            this.colPositionToWidth.put(
                    new ColumnPositionCoordinate(layer, columnPositions[i]),
                    Integer.valueOf(columnWidths[i]));
        }
        this.downScale = downScale;
    }

    /**
     * Constructor used to clone the given command.
     *
     * @param command
     *            The command to clone.
     */
    protected MultiColumnResizeCommand(MultiColumnResizeCommand command) {
        super(command);
        this.commonColumnWidth = command.commonColumnWidth;
        this.colPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>(command.colPositionToWidth);
        this.downScale = command.downScale;
    }

    /**
     *
     * @return The common column width if all columns should be resized to the
     *         same width, or -1 if the columns should be resized to different
     *         values.
     */
    public int getCommonColumnWidth() {
        return this.commonColumnWidth;
    }

    /**
     *
     * @param columnPosition
     *            The column position for which the new width is requested.
     * @return The new column width for the requested position.
     */
    public int getColumnWidth(int columnPosition) {
        for (ColumnPositionCoordinate columnPositionCoordinate : this.colPositionToWidth.keySet()) {
            if (columnPositionCoordinate.getColumnPosition() == columnPosition) {
                return this.colPositionToWidth.get(columnPositionCoordinate).intValue();
            }
        }
        return this.commonColumnWidth;
    }

    /**
     *
     * @return <code>true</code> if the column width value should be down scaled
     *         according to the scaling level, <code>false</code> if the value
     *         should be taken as is.
     *
     * @since 1.6
     */
    public boolean downScaleValue() {
        return this.downScale;
    }

    /**
     * Convert the column positions to the target layer. Ensure that the width
     * associated with the column is now associated with the converted column
     * position.
     */
    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Map<ColumnPositionCoordinate, Integer> newColPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>();

        for (ColumnPositionCoordinate columnPositionCoordinate : this.colPositionToWidth.keySet()) {
            ColumnPositionCoordinate convertedColumnPositionCoordinate =
                    LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
            if (convertedColumnPositionCoordinate != null) {
                newColPositionToWidth.put(
                        convertedColumnPositionCoordinate,
                        this.colPositionToWidth.get(columnPositionCoordinate));
            }
        }

        if (super.convertToTargetLayer(targetLayer)) {
            this.colPositionToWidth = newColPositionToWidth;
            return true;
        }
        return false;
    }

    @Override
    public MultiColumnResizeCommand cloneCommand() {
        return new MultiColumnResizeCommand(this);
    }
}
