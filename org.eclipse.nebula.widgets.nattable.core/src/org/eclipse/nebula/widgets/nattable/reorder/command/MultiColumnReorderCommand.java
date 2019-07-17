/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to reorder multiple columns at once.
 */
public class MultiColumnReorderCommand implements ILayerCommand {

    private List<ColumnPositionCoordinate> fromColumnPositionCoordinates;
    private ColumnPositionCoordinate toColumnPositionCoordinate;
    private boolean reorderToLeftEdge;
    private boolean reorderByIndex = false;

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param fromColumnPositions
     *            The column positions to reorder.
     * @param toColumnPosition
     *            The target column position to reorder to.
     */
    public MultiColumnReorderCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPosition) {
        this(layer,
                fromColumnPositions,
                toColumnPosition < layer.getColumnCount() ? toColumnPosition : toColumnPosition - 1,
                toColumnPosition < layer.getColumnCount());
    }

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param fromColumnPositions
     *            The column positions to reorder.
     * @param toColumnPosition
     *            The target column position to reorder to.
     * @param reorderToLeftEdge
     *            <code>true</code> if the reorder operation should be done on
     *            the left edge of the toColumnPosition, <code>false</code> if
     *            it should be reordered to the right edge.
     */
    public MultiColumnReorderCommand(
            ILayer layer,
            List<Integer> fromColumnPositions,
            int toColumnPosition,
            boolean reorderToLeftEdge) {

        this.fromColumnPositionCoordinates =
                new ArrayList<ColumnPositionCoordinate>(fromColumnPositions.size());
        for (Integer fromColumnPosition : fromColumnPositions) {
            this.fromColumnPositionCoordinates.add(
                    new ColumnPositionCoordinate(layer, fromColumnPosition));
        }

        this.toColumnPositionCoordinate = new ColumnPositionCoordinate(layer, toColumnPosition);

        this.reorderToLeftEdge = reorderToLeftEdge;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected MultiColumnReorderCommand(MultiColumnReorderCommand command) {
        this.fromColumnPositionCoordinates =
                new ArrayList<ColumnPositionCoordinate>(command.fromColumnPositionCoordinates);
        this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
        this.reorderToLeftEdge = command.reorderToLeftEdge;
        this.reorderByIndex = command.reorderByIndex;
    }

    /**
     * Returns the column positions that should be reordered on the layer where
     * this command is processed. Can be the column indexes if
     * {@link #reorderByIndex} is set to <code>true</code>.
     *
     * @return The column positions that should be reordered.
     */
    public List<Integer> getFromColumnPositions() {
        List<Integer> fromColumnPositions =
                new ArrayList<Integer>(this.fromColumnPositionCoordinates.size());
        for (ColumnPositionCoordinate fromColumnPositionCoordinate : this.fromColumnPositionCoordinates) {
            fromColumnPositions.add(fromColumnPositionCoordinate.getColumnPosition());
        }
        return fromColumnPositions;
    }

    /**
     *
     * @return The column position to which the columns should be reordered.
     */
    public int getToColumnPosition() {
        return this.toColumnPositionCoordinate.getColumnPosition();
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

    /**
     *
     * @return <code>true</code> if the carried from columns are treated as
     *         indexes, <code>false</code> if they are treated as positions.
     *         Default is <code>false</code>.
     * @since 1.6
     */
    public boolean isReorderByIndex() {
        return this.reorderByIndex;
    }

    /**
     * Configure how the carried columns should be treated. By default they are
     * treated as positions and converted to the local layer. Setting this value
     * to <code>true</code> will treat the columns as indexes which will avoid
     * conversion. This can be useful to reorder hidden columns for example.
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
        List<ColumnPositionCoordinate> convertedFromColumnPositionCoordinates =
                new ArrayList<ColumnPositionCoordinate>(this.fromColumnPositionCoordinates.size());

        for (ColumnPositionCoordinate fromColumnPositionCoordinate : this.fromColumnPositionCoordinates) {
            if (!this.reorderByIndex) {
                ColumnPositionCoordinate convertedFromColumnPositionCoordinate =
                        LayerCommandUtil.convertColumnPositionToTargetContext(fromColumnPositionCoordinate, targetLayer);
                if (convertedFromColumnPositionCoordinate != null) {
                    convertedFromColumnPositionCoordinates.add(convertedFromColumnPositionCoordinate);
                }
            } else {
                convertedFromColumnPositionCoordinates.add(fromColumnPositionCoordinate);
            }
        }

        ColumnPositionCoordinate targetToColumnPositionCoordinate =
                LayerCommandUtil.convertColumnPositionToTargetContext(this.toColumnPositionCoordinate, targetLayer);

        if (convertedFromColumnPositionCoordinates.size() > 0
                && targetToColumnPositionCoordinate != null) {
            this.fromColumnPositionCoordinates = convertedFromColumnPositionCoordinates;
            this.toColumnPositionCoordinate = targetToColumnPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MultiColumnReorderCommand cloneCommand() {
        return new MultiColumnReorderCommand(this);
    }

}
