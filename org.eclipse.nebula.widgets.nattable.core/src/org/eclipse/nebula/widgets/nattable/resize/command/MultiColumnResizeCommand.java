/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class MultiColumnResizeCommand extends AbstractMultiColumnCommand {

    private int commonColumnWidth = -1;
    protected Map<ColumnPositionCoordinate, Integer> colPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>();

    /**
     * All columns are being resized to the same size e.g. during a drag resize
     */
    public MultiColumnResizeCommand(ILayer layer, int[] columnPositions,
            int commonColumnWidth) {
        super(layer, columnPositions);
        this.commonColumnWidth = commonColumnWidth;
    }

    /**
     * Each column is being resized to a different size e.g. during auto resize
     */
    public MultiColumnResizeCommand(ILayer layer, int[] columnPositions,
            int[] columnWidths) {
        super(layer, columnPositions);
        for (int i = 0; i < columnPositions.length; i++) {
            this.colPositionToWidth.put(new ColumnPositionCoordinate(layer,
                    columnPositions[i]), Integer.valueOf(columnWidths[i]));
        }
    }

    protected MultiColumnResizeCommand(MultiColumnResizeCommand command) {
        super(command);
        this.commonColumnWidth = command.commonColumnWidth;
        this.colPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>(
                command.colPositionToWidth);
    }

    public int getCommonColumnWidth() {
        return this.commonColumnWidth;
    }

    public int getColumnWidth(int columnPosition) {
        for (ColumnPositionCoordinate columnPositionCoordinate : this.colPositionToWidth
                .keySet()) {
            if (columnPositionCoordinate.getColumnPosition() == columnPosition) {
                return this.colPositionToWidth.get(columnPositionCoordinate)
                        .intValue();
            }
        }
        return this.commonColumnWidth;
    }

    /**
     * Convert the column positions to the target layer. Ensure that the width
     * associated with the column is now associated with the converted column
     * position.
     */
    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Map<ColumnPositionCoordinate, Integer> newColPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>();

        for (ColumnPositionCoordinate columnPositionCoordinate : this.colPositionToWidth
                .keySet()) {
            ColumnPositionCoordinate convertedColumnPositionCoordinate = LayerCommandUtil
                    .convertColumnPositionToTargetContext(
                            columnPositionCoordinate, targetLayer);
            if (convertedColumnPositionCoordinate != null) {
                newColPositionToWidth.put(convertedColumnPositionCoordinate,
                        this.colPositionToWidth.get(columnPositionCoordinate));
            }
        }

        if (super.convertToTargetLayer(targetLayer)) {
            this.colPositionToWidth = newColPositionToWidth;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MultiColumnResizeCommand cloneCommand() {
        return new MultiColumnResizeCommand(this);
    }
}
