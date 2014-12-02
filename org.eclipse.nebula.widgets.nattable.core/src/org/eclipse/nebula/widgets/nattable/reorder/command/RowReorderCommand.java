/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command for reordering a row.
 *
 * @author Dirk Fauth
 *
 */
public class RowReorderCommand implements ILayerCommand {

    /**
     * The coordinate of the row that should be reordered
     */
    private RowPositionCoordinate fromRowPositionCoordinate;
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
     *            The layer the positions are related to
     * @param fromRowPosition
     *            The position of the row that should be reordered
     * @param toRowPosition
     *            The position of the row to which the dragged row should be
     *            dropped
     */
    public RowReorderCommand(ILayer layer, int fromRowPosition,
            int toRowPosition) {
        this.fromRowPositionCoordinate = new RowPositionCoordinate(layer,
                fromRowPosition);

        if (toRowPosition < layer.getRowCount()) {
            this.reorderToTopEdge = true;
        } else {
            this.reorderToTopEdge = false;
            toRowPosition--;
        }

        this.toRowPositionCoordinate = new RowPositionCoordinate(layer,
                toRowPosition);
    }

    /**
     * Constructor used for cloning purposes
     *
     * @param command
     *            The command which is base for the new one
     */
    protected RowReorderCommand(RowReorderCommand command) {
        this.fromRowPositionCoordinate = command.fromRowPositionCoordinate;
        this.toRowPositionCoordinate = command.toRowPositionCoordinate;
        this.reorderToTopEdge = command.reorderToTopEdge;
    }

    /**
     * @return The position of the row that should be reordered
     */
    public int getFromRowPosition() {
        return this.fromRowPositionCoordinate.getRowPosition();
    }

    /**
     * @return The position of the row to which the dragged row should be
     *         dropped
     */
    public int getToRowPosition() {
        return this.toRowPositionCoordinate.getRowPosition();
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
        RowPositionCoordinate targetFromRowPositionCoordinate = LayerCommandUtil
                .convertRowPositionToTargetContext(this.fromRowPositionCoordinate,
                        targetLayer);
        RowPositionCoordinate targetToRowPositionCoordinate = LayerCommandUtil
                .convertRowPositionToTargetContext(this.toRowPositionCoordinate,
                        targetLayer);
        if (targetFromRowPositionCoordinate != null
                && targetToRowPositionCoordinate != null) {
            this.fromRowPositionCoordinate = targetFromRowPositionCoordinate;
            this.toRowPositionCoordinate = targetToRowPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public RowReorderCommand cloneCommand() {
        return new RowReorderCommand(this);
    }

}
