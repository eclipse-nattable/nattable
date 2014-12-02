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
 * Command to end row reordering. Will transport the position of the row to
 * which the dragged row should be dropped.
 *
 * @author Dirk Fauth
 *
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

        this.toRowPositionCoordinate = new RowPositionCoordinate(layer,
                toRowPosition);
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
     * @return Flag to indicate if the row is dragged to the top edge of the
     *         layer.
     */
    public boolean isReorderToTopEdge() {
        return this.reorderToTopEdge;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        RowPositionCoordinate targetToRowPositionCoordinate = LayerCommandUtil
                .convertRowPositionToTargetContext(this.toRowPositionCoordinate,
                        targetLayer);
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
