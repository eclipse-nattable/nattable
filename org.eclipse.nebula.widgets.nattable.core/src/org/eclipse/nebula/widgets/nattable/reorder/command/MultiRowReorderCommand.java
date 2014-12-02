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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command for reordering rows.
 *
 * @author Dirk Fauth
 *
 */
public class MultiRowReorderCommand implements ILayerCommand {

    /**
     * The coordinates of the rows that should be reordered
     */
    private List<RowPositionCoordinate> fromRowPositionCoordinates;
    /**
     * The coordinate of the row to which the dragged rows should be dropped
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
     * @param fromRowPositions
     *            The positions of the rows that should be reordered
     * @param toRowPosition
     *            The position of the row to which the dragged row should be
     *            dropped
     */
    public MultiRowReorderCommand(ILayer layer, List<Integer> fromRowPositions,
            int toRowPosition) {
        this(layer, fromRowPositions,
                toRowPosition < layer.getRowCount() ? toRowPosition
                        : toRowPosition - 1, toRowPosition < layer
                        .getRowCount());
    }

    /**
     *
     * @param layer
     *            The layer the positions are related to
     * @param fromRowPositions
     *            The positions of the rows that should be reordered
     * @param toRowPosition
     *            The position of the row to which the dragged row should be
     *            dropped
     * @param reorderToTopEdge
     *            Flag to indicate if the row is dragged to the top edge of the
     *            layer
     */
    public MultiRowReorderCommand(ILayer layer, List<Integer> fromRowPositions,
            int toRowPosition, boolean reorderToTopEdge) {
        this.fromRowPositionCoordinates = new ArrayList<RowPositionCoordinate>();
        for (Integer fromRowPosition : fromRowPositions) {
            this.fromRowPositionCoordinates.add(new RowPositionCoordinate(layer,
                    fromRowPosition));
        }

        this.toRowPositionCoordinate = new RowPositionCoordinate(layer,
                toRowPosition);

        this.reorderToTopEdge = reorderToTopEdge;
    }

    /**
     * Constructor used for cloning purposes
     *
     * @param command
     *            The command which is base for the new one
     */
    protected MultiRowReorderCommand(MultiRowReorderCommand command) {
        this.fromRowPositionCoordinates = new ArrayList<RowPositionCoordinate>(
                command.fromRowPositionCoordinates);
        this.toRowPositionCoordinate = command.toRowPositionCoordinate;
        this.reorderToTopEdge = command.reorderToTopEdge;
    }

    /**
     * @return The positions of the rows that should be reordered
     */
    public List<Integer> getFromRowPositions() {
        List<Integer> fromRowPositions = new ArrayList<Integer>();
        for (RowPositionCoordinate fromRowPositionCoordinate : this.fromRowPositionCoordinates) {
            fromRowPositions.add(fromRowPositionCoordinate.getRowPosition());
        }
        return fromRowPositions;
    }

    /**
     * @return The position of the row to which the dragged rows should be
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
        List<RowPositionCoordinate> convertedFromRowPositionCoordinates = new ArrayList<RowPositionCoordinate>();

        for (RowPositionCoordinate fromRowPositionCoordinate : this.fromRowPositionCoordinates) {
            RowPositionCoordinate convertedFromRowPositionCoordinate = LayerCommandUtil
                    .convertRowPositionToTargetContext(
                            fromRowPositionCoordinate, targetLayer);
            if (convertedFromRowPositionCoordinate != null) {
                convertedFromRowPositionCoordinates
                        .add(convertedFromRowPositionCoordinate);
            }
        }

        RowPositionCoordinate targetToRowPositionCoordinate = LayerCommandUtil
                .convertRowPositionToTargetContext(this.toRowPositionCoordinate,
                        targetLayer);

        if (convertedFromRowPositionCoordinates.size() > 0
                && targetToRowPositionCoordinate != null) {
            this.fromRowPositionCoordinates = convertedFromRowPositionCoordinates;
            this.toRowPositionCoordinate = targetToRowPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MultiRowReorderCommand cloneCommand() {
        return new MultiRowReorderCommand(this);
    }

}
