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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command handler for the {@link MultiRowReorderCommand} that is registered on
 * the positionLayer of the {@link RowGroupHeaderLayer} to avoid handling in
 * case the reordering would break an unbreakable group.
 *
 * @since 1.6
 */
public class GroupMultiRowReorderCommandHandler extends AbstractLayerCommandHandler<MultiRowReorderCommand> {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    public GroupMultiRowReorderCommandHandler(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(MultiRowReorderCommand command) {
        List<Integer> fromRowPositions = command.getFromRowPositions();
        int toRowPosition = command.getToRowPosition();
        boolean reorderToTopEdge = command.isReorderToTopEdge();

        MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromRowPositions.get(0), toRowPosition);

        if (!RowGroupUtils.isBetweenTwoGroups(
                this.rowGroupHeaderLayer,
                toRowPosition,
                reorderToTopEdge,
                moveDirection)) {

            for (int fromRowPosition : fromRowPositions) {
                if (!RowGroupUtils.isReorderValid(this.rowGroupHeaderLayer, fromRowPosition, toRowPosition, reorderToTopEdge)) {
                    // consume as the reorder is not valid
                    return true;
                }
            }
        }

        // as we are registered on the positionLayer, there is no need
        // for transformation

        int toPositionToCheck = toRowPosition;
        if (MoveDirectionEnum.DOWN == moveDirection && reorderToTopEdge) {
            toPositionToCheck--;
        }

        // check if there are collapsed from groups
        Map<GroupModel, Set<Group>> collapsed = new HashMap<>();
        for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
            GroupModel model = this.rowGroupHeaderLayer.getGroupModel(level);
            for (int fromRowPosition : fromRowPositions) {
                Group fromGroup = this.rowGroupHeaderLayer.getGroupByPosition(level, fromRowPosition);
                if (fromGroup != null && fromGroup.isCollapsed()) {
                    Set<Group> collapsedGroups = collapsed.get(model);
                    if (collapsedGroups == null) {
                        collapsedGroups = new HashSet<>();
                        collapsed.put(model, collapsedGroups);
                    }
                    collapsedGroups.add(fromGroup);
                }
            }

        }
        // if there are collapsed groups collect from indexes and to index
        if (!collapsed.isEmpty()) {
            int[] fromIndexes = fromRowPositions.stream()
                    .mapToInt(this.rowGroupHeaderLayer.getPositionLayer()::getRowIndexByPosition)
                    .toArray();
            int toIndex = this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(toRowPosition);

            // expand all collapsed groups
            collapsed.forEach((model, collapsedGroups) -> {
                collapsedGroups.forEach(group -> this.rowGroupHeaderLayer.expandGroup(model, group));
            });

            // update fromColumnPositions and toColumnPosition
            int[] fromPositions = Arrays.stream(fromIndexes)
                    .map(this.rowGroupHeaderLayer.getPositionLayer()::getRowPositionByIndex)
                    .toArray();
            command.updateFromRowPositions(fromPositions);

            toPositionToCheck = this.rowGroupHeaderLayer.getPositionLayer().getRowPositionByIndex(toIndex);
            command.updateToRowPosition(toPositionToCheck);
            if (MoveDirectionEnum.RIGHT == moveDirection && reorderToTopEdge) {
                toPositionToCheck--;
            }
        }

        boolean toggleCoordinateByEdge = false;
        Group groupToEnd = null;
        Group groupToStart = null;

        for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
            Group toGroup = this.rowGroupHeaderLayer.getGroupByPosition(level, toPositionToCheck);
            if (toGroup != null && MoveDirectionEnum.DOWN == moveDirection && toGroup.isGroupEnd(toPositionToCheck)) {
                toggleCoordinateByEdge = true;
                if (toGroup.isUnbreakable() && toGroup.getVisibleSpan() < toGroup.getOriginalSpan()) {
                    groupToEnd = toGroup;
                }
            } else if (toGroup != null
                    && MoveDirectionEnum.UP == moveDirection
                    && toGroup.isGroupStart(toPositionToCheck)
                    && toGroup.isUnbreakable()
                    && toGroup.getVisibleSpan() < toGroup.getOriginalSpan()) {
                groupToStart = toGroup;
            }
        }

        if (toggleCoordinateByEdge) {
            command.toggleCoordinateByEdge();
        }

        if (groupToEnd != null) {
            // if the group is unbreakable and the visible span is
            // smaller than the original span, it could be that
            // positions at the end are hidden and the reorder to
            // the right edge could lead to a broken group
            return this.rowGroupHeaderLayer.getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                    new MultiRowReorderToGroupEndCommand(command, groupToEnd));
        } else if (groupToStart != null) {
            // if the group is unbreakable and the visible span is
            // smaller than the original span, it could be that
            // positions at the start are hidden and the reorder to
            // the left edge could lead to a broken group
            return this.rowGroupHeaderLayer.getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                    new MultiRowReorderToGroupStartCommand(command, groupToStart));
        }

        return false;
    }

    @Override
    public Class<MultiRowReorderCommand> getCommandClass() {
        return MultiRowReorderCommand.class;
    }

    /**
     * Specialization of the {@link MultiRowReorderCommand} to be able to
     * reorder a row to the start of a {@link Group} even the first rows in the
     * {@link Group} are hidden.
     *
     * @since 2.0
     */
    class MultiRowReorderToGroupStartCommand extends MultiRowReorderCommand {

        private final Group group;

        public MultiRowReorderToGroupStartCommand(MultiRowReorderCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected MultiRowReorderToGroupStartCommand(MultiRowReorderToGroupStartCommand command) {
            super(command);
            this.group = command.group;
        }

        @Override
        public boolean convertToTargetLayer(ILayer targetLayer) {
            boolean convert = super.convertToTargetLayer(targetLayer);

            // check if there are positions for the group members that would be
            // more to the left. this could happen e.g. if rows at the group
            // start are hidden
            if (convert && isReorderToTopEdge() && targetLayer instanceof IUniqueIndexLayer) {
                int groupStartPosition = ((IUniqueIndexLayer) targetLayer).getRowPositionByIndex(this.group.getStartIndex());
                if (groupStartPosition >= 0 && groupStartPosition < getToRowPosition()) {
                    this.toRowPositionCoordinate.rowPosition = groupStartPosition;
                }
            }

            return convert;
        }

        @Override
        public MultiRowReorderToGroupStartCommand cloneCommand() {
            return new MultiRowReorderToGroupStartCommand(this);
        }
    }

    /**
     * Specialization of the {@link MultiRowReorderCommand} to be able to
     * reorder a row to the end of a {@link Group} even the last rows in the
     * {@link Group} are hidden.
     *
     * @since 2.0
     */
    class MultiRowReorderToGroupEndCommand extends MultiRowReorderCommand {

        private final Group group;

        public MultiRowReorderToGroupEndCommand(MultiRowReorderCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected MultiRowReorderToGroupEndCommand(MultiRowReorderToGroupEndCommand command) {
            super(command);
            this.group = command.group;
        }

        @Override
        public boolean convertToTargetLayer(ILayer targetLayer) {
            boolean convert = super.convertToTargetLayer(targetLayer);

            // check if there are positions for the group members that would be
            // more to the right. this could happen e.g. if rows at the group
            // end are hidden
            if (convert && !isReorderToTopEdge() && targetLayer instanceof IUniqueIndexLayer) {
                int groupEndPosition = this.group.getGroupEndPosition((IUniqueIndexLayer) targetLayer);
                if (groupEndPosition >= 0 && groupEndPosition > getToRowPosition()) {
                    this.toRowPositionCoordinate.rowPosition = groupEndPosition;
                }
            }

            return convert;
        }

        @Override
        public MultiRowReorderToGroupEndCommand cloneCommand() {
            return new MultiRowReorderToGroupEndCommand(this);
        }
    }

}
