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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to reorder a single column.
 */
public class ColumnReorderCommand implements ILayerCommand {

    private ColumnPositionCoordinate fromColumnPositionCoordinate;
    private ColumnPositionCoordinate toColumnPositionCoordinate;
    private boolean reorderToLeftEdge;

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param fromColumnPosition
     *            The column position to reorder.
     * @param toColumnPosition
     *            The target column position to reorder to.
     */
    public ColumnReorderCommand(ILayer layer, int fromColumnPosition, int toColumnPosition) {
        this.fromColumnPositionCoordinate = new ColumnPositionCoordinate(layer, fromColumnPosition);

        if (toColumnPosition < layer.getColumnCount()) {
            this.reorderToLeftEdge = true;
        } else {
            this.reorderToLeftEdge = false;
            toColumnPosition--;
        }

        this.toColumnPositionCoordinate = new ColumnPositionCoordinate(layer, toColumnPosition);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ColumnReorderCommand(ColumnReorderCommand command) {
        this.fromColumnPositionCoordinate = command.fromColumnPositionCoordinate;
        this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
        this.reorderToLeftEdge = command.reorderToLeftEdge;
    }

    /**
     *
     * @return The column position that is reordered.
     */
    public int getFromColumnPosition() {
        return this.fromColumnPositionCoordinate.getColumnPosition();
    }

    /**
     *
     * @param fromPosition
     *            The new fromColumnPosition.
     *
     * @since 1.6
     */
    public void updateFromColumnPosition(int fromPosition) {
        this.fromColumnPositionCoordinate.columnPosition = fromPosition;
    }

    /**
     *
     * @return The column position to which the column should be reordered to.
     */
    public int getToColumnPosition() {
        return this.toColumnPositionCoordinate.getColumnPosition();
    }

    /**
     *
     * @param toPosition
     *            The new toColumnPosition.
     *
     * @since 1.6
     */
    public void updateToColumnPosition(int toPosition) {
        this.toColumnPositionCoordinate.columnPosition = toPosition;
    }

    /**
     * Toggles the coordinate from left edge to right edge and vice versa. Will
     * not toggle if the coordinate is right edge of the last column or the left
     * edge of the first column.
     *
     * @since 1.6
     */
    public void toggleCoordinateByEdge() {
        if (this.reorderToLeftEdge
                && this.toColumnPositionCoordinate.columnPosition > 0) {
            this.toColumnPositionCoordinate.columnPosition--;
            this.reorderToLeftEdge = false;
        } else if (!this.reorderToLeftEdge
                && this.toColumnPositionCoordinate.columnPosition < this.toColumnPositionCoordinate.getLayer().getColumnCount() - 1) {
            this.toColumnPositionCoordinate.columnPosition++;
            this.reorderToLeftEdge = true;
        }
    }

    /**
     *
     * @return <code>true</code> if the reorder operation should be done on the
     *         left edge of the toColumnPosition, <code>false</code> if it
     *         should be reordered to the right edge.
     */
    public boolean isReorderToLeftEdge() {
        return this.reorderToLeftEdge;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetFromColumnPositionCoordinate =
                LayerCommandUtil.convertColumnPositionToTargetContext(this.fromColumnPositionCoordinate, targetLayer);
        ColumnPositionCoordinate targetToColumnPositionCoordinate =
                LayerCommandUtil.convertColumnPositionToTargetContext(this.toColumnPositionCoordinate, targetLayer);
        if (targetFromColumnPositionCoordinate != null
                && targetToColumnPositionCoordinate != null) {
            this.fromColumnPositionCoordinate = targetFromColumnPositionCoordinate;
            this.toColumnPositionCoordinate = targetToColumnPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ColumnReorderCommand cloneCommand() {
        return new ColumnReorderCommand(this);
    }

}
