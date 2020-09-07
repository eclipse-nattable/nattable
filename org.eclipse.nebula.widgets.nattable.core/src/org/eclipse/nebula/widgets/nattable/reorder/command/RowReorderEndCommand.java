/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.action.RowReorderDragMode;

/**
 * Command to end row reordering. Will transport the position of the row to
 * which the dragged row should be dropped.
 *
 * @see RowReorderDragMode
 * @see RowReorderStartCommand
 */
public class RowReorderEndCommand implements ILayerCommand {

    /**
     * The coordinate of the row to which the dragged row should be dropped
     */
    private RowPositionCoordinate toRowPositionCoordinate;
    /**
     * Flag to indicate if the row is dragged to the top edge of the layer.
     * Needed for the special case when the reordering is performed to the
     * bottom edge.
     */
    private boolean reorderToTopEdge;

    /**
     *
     * @param layer
     *            The layer the position is related to
     * @param toRowPosition
     *            The position of the row to which the dragged row should be
     *            dropped
     */
    public RowReorderEndCommand(ILayer layer, int toRowPosition) {
        if (toRowPosition < layer.getRowCount()) {
            this.reorderToTopEdge = true;
        } else {
            this.reorderToTopEdge = false;
            toRowPosition--;
        }

        this.toRowPositionCoordinate = new RowPositionCoordinate(layer, toRowPosition);
    }

    /**
     * Constructor used for cloning purposes
     *
     * @param command
     *            The command which is base for the new one
     */
    protected RowReorderEndCommand(RowReorderEndCommand command) {
        this.toRowPositionCoordinate = command.toRowPositionCoordinate;
        this.reorderToTopEdge = command.reorderToTopEdge;
    }

    /**
     * @return The position of the row to which the dragged row should be
     *         dropped
     */
    public int getToRowPosition() {
        return this.toRowPositionCoordinate.getRowPosition();
    }

    /**
     *
     * @param toPosition
     *            The new toRowPosition.
     *
     * @since 1.6
     */
    public void updateToRowPosition(int toPosition) {
        this.toRowPositionCoordinate.rowPosition = toPosition;
    }

    /**
     * Toggles the coordinate from top edge to bottom edge and vice versa. Will
     * not toggle if the coordinate is bottom edge of the last row or the top
     * edge of the first row.
     *
     * @since 1.6
     */
    public void toggleCoordinateByEdge() {
        if (this.reorderToTopEdge
                && this.toRowPositionCoordinate.rowPosition > 0) {
            this.toRowPositionCoordinate.rowPosition--;
            this.reorderToTopEdge = false;
        } else if (!this.reorderToTopEdge
                && this.toRowPositionCoordinate.rowPosition < this.toRowPositionCoordinate.getLayer().getRowCount() - 1) {
            this.toRowPositionCoordinate.rowPosition++;
            this.reorderToTopEdge = true;
        }
    }

    /**
     * @return Flag to indicate if the row is dragged to the top edge of the
     *         layer.
     */
    public boolean isReorderToTopEdge() {
        return this.reorderToTopEdge;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        RowPositionCoordinate targetToRowPositionCoordinate =
                LayerCommandUtil.convertRowPositionToTargetContext(this.toRowPositionCoordinate, targetLayer);
        if (targetToRowPositionCoordinate != null) {
            this.toRowPositionCoordinate = targetToRowPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public RowReorderEndCommand cloneCommand() {
        return new RowReorderEndCommand(this);
    }

}
