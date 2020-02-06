/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
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
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;

/**
 * Specialization of the {@link MultiRowReorderCommand} that ensures on
 * conversion that the toPosition is between two groups.
 *
 * @since 1.6
 */
public class GroupMultiRowReorderCommand extends MultiRowReorderCommand {

    private Group groupToTop;
    private Group groupToBottom;

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param fromRowPositions
     *            The row positions to reorder.
     * @param toRowPosition
     *            The target row position to reorder to.
     * @since 2.0
     */
    public GroupMultiRowReorderCommand(ILayer layer, List<Integer> fromRowPositions, int toRowPosition) {
        this(layer,
                fromRowPositions,
                toRowPosition < layer.getRowCount() ? toRowPosition : toRowPosition - 1,
                toRowPosition < layer.getRowCount());
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
     * @since 2.0
     */
    public GroupMultiRowReorderCommand(
            ILayer layer,
            List<Integer> fromRowPositions,
            int toRowPosition,
            boolean reorderToTopEdge) {

        super(layer, fromRowPositions.stream().mapToInt(Integer::intValue).toArray(), toRowPosition, reorderToTopEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param fromRowPositions
     *            The row positions to reorder.
     * @param toRowPosition
     *            The target row position to reorder to.
     * @since 2.0
     */
    public GroupMultiRowReorderCommand(ILayer layer, int[] fromRowPositions, int toRowPosition) {
        this(layer,
                fromRowPositions,
                toRowPosition < layer.getRowCount() ? toRowPosition : toRowPosition - 1,
                toRowPosition < layer.getRowCount());
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
     * @since 2.0
     */
    public GroupMultiRowReorderCommand(
            ILayer layer,
            int[] fromRowPositions,
            int toRowPosition,
            boolean reorderToTopEdge) {

        super(layer, fromRowPositions, toRowPosition, reorderToTopEdge);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected GroupMultiRowReorderCommand(GroupMultiRowReorderCommand command) {
        super(command);
        this.groupToTop = command.groupToTop;
        this.groupToBottom = command.groupToBottom;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (super.convertToTargetLayer(targetLayer)) {
            // check if we need to update the toPosition which could be
            // necessary in case rows at the group beginning are hidden
            if (this.groupToBottom != null) {
                if (isReorderToTopEdge() && targetLayer instanceof IUniqueIndexLayer) {
                    int groupStartPosition = ((IUniqueIndexLayer) targetLayer).getRowPositionByIndex(this.groupToBottom.getStartIndex());
                    if (groupStartPosition >= 0 && groupStartPosition < getToRowPosition()) {
                        this.toRowPositionCoordinate.rowPosition = groupStartPosition;
                    }
                }
            } else if (this.groupToTop != null) {
                // check if there are positions for the group members that would
                // be more to the right. this could happen e.g. if rows at
                // the group end are hidden
                if (!isReorderToTopEdge() && targetLayer instanceof IUniqueIndexLayer) {
                    int groupEndPosition = this.groupToTop.getGroupEndPosition((IUniqueIndexLayer) targetLayer);
                    if (groupEndPosition >= 0 && groupEndPosition > getToRowPosition()) {
                        this.toRowPositionCoordinate.rowPosition = groupEndPosition;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @param groupToTop
     *            The {@link Group} that is on top of the toRowPosition. Needed
     *            to calculate the correct position to the bottom edge of a
     *            group in case of hidden rows.
     * @since 2.0
     */
    public void setGroupToTop(Group groupToTop) {
        this.groupToTop = groupToTop;
    }

    /**
     *
     * @param groupToBottom
     *            The {@link Group} that is at the toRowPosition. Needed to
     *            calculate the correct position to the top edge of a group in
     *            case of hidden rows.
     * @since 2.0
     */
    public void setGroupToBottom(Group groupToBottom) {
        this.groupToBottom = groupToBottom;
    }

    @Override
    public GroupMultiRowReorderCommand cloneCommand() {
        return new GroupMultiRowReorderCommand(this);
    }

}
