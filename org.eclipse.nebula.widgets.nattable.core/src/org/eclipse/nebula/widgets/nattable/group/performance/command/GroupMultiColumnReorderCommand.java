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

import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;

/**
 * Specialization of the {@link MultiColumnReorderCommand} that ensures on
 * conversion that the toPosition is between two groups.
 *
 * @since 1.6
 */
public class GroupMultiColumnReorderCommand extends MultiColumnReorderCommand {

    private Group groupToRight;

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param fromColumnPositions
     *            The column positions to reorder.
     * @param toColumnPosition
     *            The target column position to reorder to.
     * @param groupToRight
     *            The {@link Group} that is at the toColumnPosition.
     */
    public GroupMultiColumnReorderCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPosition, Group groupToRight) {
        this(layer,
                fromColumnPositions,
                toColumnPosition < layer.getColumnCount() ? toColumnPosition : toColumnPosition - 1,
                toColumnPosition < layer.getColumnCount(),
                groupToRight);
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
     * @param groupToRight
     *            The {@link Group} that is at the toColumnPosition.
     */
    public GroupMultiColumnReorderCommand(
            ILayer layer,
            List<Integer> fromColumnPositions,
            int toColumnPosition,
            boolean reorderToLeftEdge,
            Group groupToRight) {

        super(layer, fromColumnPositions, toColumnPosition, reorderToLeftEdge);
        this.groupToRight = groupToRight;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected GroupMultiColumnReorderCommand(GroupMultiColumnReorderCommand command) {
        super(command);
        this.groupToRight = command.groupToRight;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (super.convertToTargetLayer(targetLayer)) {
            // check if we need to update the toPosition which could be
            // necessary in case columns at the group beginning are hidden
            if (this.groupToRight != null) {
                int toLeftPosition = this.toColumnPositionCoordinate.columnPosition - 1;
                int toLeftIndex = this.toColumnPositionCoordinate.getLayer().getColumnIndexByPosition(toLeftPosition);
                while (this.groupToRight.hasMember(toLeftIndex)) {
                    this.toColumnPositionCoordinate.columnPosition--;
                    toLeftPosition--;
                    toLeftIndex = this.toColumnPositionCoordinate.getLayer().getColumnIndexByPosition(toLeftPosition);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public GroupMultiColumnReorderCommand cloneCommand() {
        return new GroupMultiColumnReorderCommand(this);
    }

}
