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
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;

/**
 * Specialization of the {@link MultiRowReorderCommand} that ensures on
 * conversion that the toPosition is between two groups.
 *
 * @since 1.6
 */
public class GroupMultiRowReorderCommand extends MultiRowReorderCommand {

    private Group groupToBottom;

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param fromRowPositions
     *            The row positions to reorder.
     * @param toRowPosition
     *            The target row position to reorder to.
     * @param groupToBottom
     *            The {@link Group} that is at the toRowPosition.
     */
    public GroupMultiRowReorderCommand(ILayer layer, List<Integer> fromRowPositions, int toRowPosition, Group groupToBottom) {
        this(layer,
                fromRowPositions,
                toRowPosition < layer.getRowCount() ? toRowPosition : toRowPosition - 1,
                toRowPosition < layer.getRowCount(),
                groupToBottom);
    }

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param fromRowPositions
     *            The row positions to reorder.
     * @param toRowPosition
     *            The target row position to reorder to.
     * @param reorderToTopEdge
     *            <code>true</code> if the reorder operation should be done on
     *            the top edge of the toRowPosition, <code>false</code> if it
     *            should be reordered to the bottom edge.
     * @param groupToBottom
     *            The {@link Group} that is at the toRowPosition.
     */
    public GroupMultiRowReorderCommand(
            ILayer layer,
            List<Integer> fromRowPositions,
            int toRowPosition,
            boolean reorderToTopEdge,
            Group groupToBottom) {

        super(layer, fromRowPositions, toRowPosition, reorderToTopEdge);
        this.groupToBottom = groupToBottom;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected GroupMultiRowReorderCommand(GroupMultiRowReorderCommand command) {
        super(command);
        this.groupToBottom = command.groupToBottom;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (super.convertToTargetLayer(targetLayer)) {
            // check if we need to update the toPosition which could be
            // necessary in case rows at the group beginning are hidden
            if (this.groupToBottom != null) {
                int toTopPosition = this.toRowPositionCoordinate.rowPosition - 1;
                int toTopIndex = this.toRowPositionCoordinate.getLayer().getRowIndexByPosition(toTopPosition);
                while (this.groupToBottom.hasMember(toTopIndex)) {
                    this.toRowPositionCoordinate.rowPosition--;
                    toTopPosition--;
                    toTopIndex = this.toRowPositionCoordinate.getLayer().getRowIndexByPosition(toTopPosition);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public GroupMultiRowReorderCommand cloneCommand() {
        return new GroupMultiRowReorderCommand(this);
    }

}
