/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger reordering of a row group.
 * <p>
 * The command does not inherit the RowReorderCommand as then it would be
 * consumed first by the RowReorderLayer in the body layer stack and would not
 * come to the row header layer stack.
 *
 * @since 1.6
 */
public class RowGroupReorderCommand implements ILayerCommand {

    private int level;
    private RowPositionCoordinate fromRowPositionCoordinate;
    private RowPositionCoordinate toRowPositionCoordinate;
    private boolean reorderToTopEdge;
    private boolean performConversion;

    /**
     *
     * @param layer
     *            The layer to which the positions match.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromRowPosition
     *            The row position of the group that should be reordered.
     * @param toRowPosition
     *            The row position to which the reorder should be performed.
     */
    public RowGroupReorderCommand(ILayer layer, int level, int fromRowPosition, int toRowPosition) {
        this(layer, level, fromRowPosition, toRowPosition, true);
    }

    /**
     *
     * @param layer
     *            The layer to which the positions match.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromRowPosition
     *            The row position of the group that should be reordered.
     * @param toRowPosition
     *            The row position to which the reorder should be performed.
     * @param performConversion
     *            Configure whether a position conversion should be performed or
     *            not. If this value is set to <code>false</code>
     *            {@link #convertToTargetLayer(ILayer)} does not perform any
     *            logic and will always return <code>true</code>. In that case
     *            layer has to be the positionLayer of the
     *            ColumnGroupHeaderLayer, typically the SelectionLayer.
     *            Otherwise this command will not work correctly.
     */
    public RowGroupReorderCommand(ILayer layer, int level, int fromRowPosition, int toRowPosition, boolean performConversion) {
        this.fromRowPositionCoordinate = new RowPositionCoordinate(layer, fromRowPosition);

        this.level = level;

        if (toRowPosition < layer.getRowCount()) {
            this.reorderToTopEdge = true;
        } else {
            this.reorderToTopEdge = false;
            toRowPosition--;
        }

        this.toRowPositionCoordinate = new RowPositionCoordinate(layer, toRowPosition);
        this.performConversion = performConversion;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowGroupReorderCommand(RowGroupReorderCommand command) {
        this.level = command.level;
        this.fromRowPositionCoordinate = command.fromRowPositionCoordinate;
        this.toRowPositionCoordinate = command.toRowPositionCoordinate;
        this.reorderToTopEdge = command.reorderToTopEdge;
        this.performConversion = command.performConversion;
    }

    /**
     *
     * @return The group level on which the group reorder should be performed.
     */
    public int getLevel() {
        return this.level;
    }

    /**
     *
     * @return The row position of the group that should be reordered.
     */
    public int getFromRowPosition() {
        return this.fromRowPositionCoordinate.getRowPosition();
    }

    /**
     *
     * @return The row position to which the reorder should be performed.
     */
    public int getToRowPosition() {
        return this.toRowPositionCoordinate.getRowPosition();
    }

    /**
     *
     * @return <code>true</code> if the reorder should be performed to the top
     *         edge of the to position, <code>false</code> if the reorder should
     *         happen to the bottom edge, e.g. on reordering to the end of the
     *         table.
     */
    public boolean isReorderToTopEdge() {
        return this.reorderToTopEdge;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (this.performConversion) {
            RowPositionCoordinate targetFromRowPositionCoordinate =
                    LayerCommandUtil.convertRowPositionToTargetContext(this.fromRowPositionCoordinate, targetLayer);
            RowPositionCoordinate targetToRowPositionCoordinate =
                    LayerCommandUtil.convertRowPositionToTargetContext(this.toRowPositionCoordinate, targetLayer);
            if (targetFromRowPositionCoordinate != null
                    && targetToRowPositionCoordinate != null) {
                this.fromRowPositionCoordinate = targetFromRowPositionCoordinate;
                this.toRowPositionCoordinate = targetToRowPositionCoordinate;
                return true;
            } else {
                return false;
            }
        }

        // if we should not perform a conversion, we simply return true
        return true;
    }

    @Override
    public RowGroupReorderCommand cloneCommand() {
        return new RowGroupReorderCommand(this);
    }

}