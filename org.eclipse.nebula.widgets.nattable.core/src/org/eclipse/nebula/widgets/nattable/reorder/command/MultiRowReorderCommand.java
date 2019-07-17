/*******************************************************************************
 * Copyright (c) 2013, 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
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
     * Flag to indicate if the carried from rows are treated as indexes, or if
     * they are treated as positions.
     */
    private boolean reorderByIndex = false;

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
    public MultiRowReorderCommand(ILayer layer, List<Integer> fromRowPositions, int toRowPosition) {
        this(layer,
                fromRowPositions,
                toRowPosition < layer.getRowCount() ? toRowPosition : toRowPosition - 1,
                toRowPosition < layer.getRowCount());
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
    public MultiRowReorderCommand(ILayer layer,
            List<Integer> fromRowPositions,
            int toRowPosition,
            boolean reorderToTopEdge) {

        this.fromRowPositionCoordinates = new ArrayList<RowPositionCoordinate>(fromRowPositions.size());
        for (Integer fromRowPosition : fromRowPositions) {
            this.fromRowPositionCoordinates.add(new RowPositionCoordinate(layer, fromRowPosition));
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
        this.fromRowPositionCoordinates = new ArrayList<RowPositionCoordinate>(command.fromRowPositionCoordinates);
        this.toRowPositionCoordinate = command.toRowPositionCoordinate;
        this.reorderToTopEdge = command.reorderToTopEdge;
    }

    /**
     * @return The positions of the rows that should be reordered
     */
    public List<Integer> getFromRowPositions() {
        List<Integer> fromRowPositions = new ArrayList<Integer>(this.fromRowPositionCoordinates.size());
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

    /**
     *
     * @return <code>true</code> if the carried from rows are treated as
     *         indexes, <code>false</code> if they are treated as positions.
     *         Default is <code>false</code>.
     * @since 1.6
     */
    public boolean isReorderByIndex() {
        return this.reorderByIndex;
    }

    /**
     * Configure how the carried rows should be treated. By default they are
     * treated as positions and converted to the local layer. Setting this value
     * to <code>true</code> will treat the rows as indexes which will avoid
     * conversion. This can be useful to reorder hidden rows for example.
     *
     * @param reorderByIndex
     *            <code>true</code> if the carried from columns should be
     *            treated as indexes, <code>false</code> if they should be
     *            treated as positions.
     * @since 1.6
     */
    public void setReorderByIndex(boolean reorderByIndex) {
        this.reorderByIndex = reorderByIndex;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        List<RowPositionCoordinate> convertedFromRowPositionCoordinates =
                new ArrayList<RowPositionCoordinate>(this.fromRowPositionCoordinates.size());

        for (RowPositionCoordinate fromRowPositionCoordinate : this.fromRowPositionCoordinates) {
            if (!this.reorderByIndex) {
                RowPositionCoordinate convertedFromRowPositionCoordinate =
                        LayerCommandUtil.convertRowPositionToTargetContext(fromRowPositionCoordinate, targetLayer);
                if (convertedFromRowPositionCoordinate != null) {
                    convertedFromRowPositionCoordinates.add(convertedFromRowPositionCoordinate);
                }
            } else {
                convertedFromRowPositionCoordinates.add(fromRowPositionCoordinate);
            }
        }

        RowPositionCoordinate targetToRowPositionCoordinate =
                LayerCommandUtil.convertRowPositionToTargetContext(this.toRowPositionCoordinate, targetLayer);

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
