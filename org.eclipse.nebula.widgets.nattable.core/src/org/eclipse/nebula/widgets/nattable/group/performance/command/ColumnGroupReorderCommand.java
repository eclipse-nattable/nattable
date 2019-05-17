/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger reordering of a column group.
 * <p>
 * The command does not inherit the ColumnReorderCommand as then it would be
 * consumed first by the ColumnReorderLayer in the body layer stack and would
 * not come to the column header layer stack.
 *
 * @since 1.6
 */
public class ColumnGroupReorderCommand implements ILayerCommand {

    private int level;
    private ColumnPositionCoordinate fromColumnPositionCoordinate;
    private ColumnPositionCoordinate toColumnPositionCoordinate;
    private boolean reorderToLeftEdge;
    private boolean performConversion;

    /**
     *
     * @param layer
     *            The layer to which the positions match.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromColumnPosition
     *            The column position of the group that should be reordered.
     * @param toColumnPosition
     *            The column position to which the reorder should be performed.
     */
    public ColumnGroupReorderCommand(ILayer layer, int level, int fromColumnPosition, int toColumnPosition) {
        this(layer, level, fromColumnPosition, toColumnPosition, true);
    }

    /**
     *
     * @param layer
     *            The layer to which the positions match.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromColumnPosition
     *            The column position of the group that should be reordered.
     * @param toColumnPosition
     *            The column position to which the reorder should be performed.
     * @param performConversion
     *            Configure whether a position conversion should be performed or
     *            not. If this value is set to <code>false</code>
     *            {@link #convertToTargetLayer(ILayer)} does not perform any
     *            logic and will always return <code>true</code>. In that case
     *            layer has to be the positionLayer of the
     *            ColumnGroupHeaderLayer, typically the SelectionLayer.
     *            Otherwise this command will not work correctly.
     */
    public ColumnGroupReorderCommand(ILayer layer, int level, int fromColumnPosition, int toColumnPosition, boolean performConversion) {
        this.fromColumnPositionCoordinate = new ColumnPositionCoordinate(layer, fromColumnPosition);

        this.level = level;

        if (toColumnPosition < layer.getColumnCount()) {
            this.reorderToLeftEdge = true;
        } else {
            this.reorderToLeftEdge = false;
            toColumnPosition--;
        }

        this.toColumnPositionCoordinate = new ColumnPositionCoordinate(layer, toColumnPosition);
        this.performConversion = performConversion;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ColumnGroupReorderCommand(ColumnGroupReorderCommand command) {
        this.level = command.level;
        this.fromColumnPositionCoordinate = command.fromColumnPositionCoordinate;
        this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
        this.reorderToLeftEdge = command.reorderToLeftEdge;
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
     * @return The column position of the group that should be reordered.
     */
    public int getFromColumnPosition() {
        return this.fromColumnPositionCoordinate.getColumnPosition();
    }

    /**
     *
     * @return The column position to which the reorder should be performed.
     */
    public int getToColumnPosition() {
        return this.toColumnPositionCoordinate.getColumnPosition();
    }

    /**
     *
     * @return <code>true</code> if the reorder should be performed to the left
     *         edge of the to position, <code>false</code> if the reorder should
     *         happen to the right edge, e.g. on reordering to the end of the
     *         table.
     */
    public boolean isReorderToLeftEdge() {
        return this.reorderToLeftEdge;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (this.performConversion) {
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

        // if we should not perform a conversion, we simply return true
        return true;
    }

    @Override
    public ColumnGroupReorderCommand cloneCommand() {
        return new ColumnGroupReorderCommand(this);
    }

}