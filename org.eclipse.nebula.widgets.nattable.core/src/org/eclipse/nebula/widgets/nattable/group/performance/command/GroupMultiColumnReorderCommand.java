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
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;

/**
 * Specialization of the {@link MultiColumnReorderCommand} that ensures on
 * conversion that the toPosition is between two groups.
 *
 * @since 1.6
 */
public class GroupMultiColumnReorderCommand extends MultiColumnReorderCommand {

    private Group groupToLeft;
    private Group groupToRight;

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param fromColumnPositions
     *            The column positions to reorder.
     * @param toColumnPosition
     *            The target column position to reorder to.
     * @since 2.0
     */
    public GroupMultiColumnReorderCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPosition) {
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
     * @since 2.0
     */
    public GroupMultiColumnReorderCommand(
            ILayer layer,
            List<Integer> fromColumnPositions,
            int toColumnPosition,
            boolean reorderToLeftEdge) {

        super(layer, fromColumnPositions, toColumnPosition, reorderToLeftEdge);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected GroupMultiColumnReorderCommand(GroupMultiColumnReorderCommand command) {
        super(command);
        this.groupToLeft = command.groupToLeft;
        this.groupToRight = command.groupToRight;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (super.convertToTargetLayer(targetLayer)) {
            // check if we need to update the toPosition which could be
            // necessary in case columns at the group beginning are hidden
            if (this.groupToRight != null) {
                if (isReorderToLeftEdge() && targetLayer instanceof IUniqueIndexLayer) {
                    int groupStartPosition = ((IUniqueIndexLayer) targetLayer).getColumnPositionByIndex(this.groupToRight.getStartIndex());
                    if (groupStartPosition >= 0 && groupStartPosition < getToColumnPosition()) {
                        this.toColumnPositionCoordinate.columnPosition = groupStartPosition;
                    }
                }
            } else if (this.groupToLeft != null) {
                // check if there are positions for the group members that would
                // be more to the right. this could happen e.g. if columns at
                // the group end are hidden
                if (!isReorderToLeftEdge() && targetLayer instanceof IUniqueIndexLayer) {
                    int groupEndPosition = this.groupToLeft.getGroupEndPosition((IUniqueIndexLayer) targetLayer);
                    if (groupEndPosition >= 0 && groupEndPosition > getToColumnPosition()) {
                        this.toColumnPositionCoordinate.columnPosition = groupEndPosition;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @param groupToLeft
     *            The {@link Group} that is left of the toColumnPosition. Needed
     *            to calculate the correct position to the right edge of a group
     *            in case of hidden columns.
     * @since 2.0
     */
    public void setGroupToLeft(Group groupToLeft) {
        this.groupToLeft = groupToLeft;
    }

    /**
     *
     * @param groupToRight
     *            The {@link Group} that is at the toColumnPosition. Needed to
     *            calculate the correct position to the left edge of a group in
     *            case of hidden columns.
     * @since 2.0
     */
    public void setGroupToRight(Group groupToRight) {
        this.groupToRight = groupToRight;
    }

    @Override
    public GroupMultiColumnReorderCommand cloneCommand() {
        return new GroupMultiColumnReorderCommand(this);
    }

}
