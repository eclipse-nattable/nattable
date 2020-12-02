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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command handler for the {@link MultiColumnReorderCommand} that is registered
 * on the positionLayer of the {@link ColumnGroupHeaderLayer} to avoid handling
 * in case the reordering would break an unbreakable group.
 *
 * @since 1.6
 */
public class GroupMultiColumnReorderCommandHandler extends AbstractLayerCommandHandler<MultiColumnReorderCommand> {

    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;

    public GroupMultiColumnReorderCommandHandler(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(MultiColumnReorderCommand command) {
        List<Integer> fromColumnPositions = command.getFromColumnPositions();
        int toColumnPosition = command.getToColumnPosition();
        boolean reorderToLeftEdge = command.isReorderToLeftEdge();

        MoveDirectionEnum moveDirection = PositionUtil.getHorizontalMoveDirection(fromColumnPositions.get(0), toColumnPosition);

        if (!ColumnGroupUtils.isBetweenTwoGroups(
                this.columnGroupHeaderLayer,
                toColumnPosition,
                reorderToLeftEdge,
                moveDirection)) {

            for (int fromColumnPosition : fromColumnPositions) {
                if (!ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, fromColumnPosition, toColumnPosition, reorderToLeftEdge)) {
                    // consume as the reorder is not valid
                    return true;
                }
            }
        }

        // as we are registered on the positionLayer, there is no need
        // for transformation

        int toPositionToCheck = toColumnPosition;
        if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge) {
            toPositionToCheck--;
        }

        // check if there are collapsed from groups
        Map<GroupModel, Set<Group>> collapsed = new HashMap<>();
        for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
            GroupModel model = this.columnGroupHeaderLayer.getGroupModel(level);
            for (int fromColumnPosition : fromColumnPositions) {
                Group fromGroup = this.columnGroupHeaderLayer.getGroupByPosition(level, fromColumnPosition);
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
            int[] fromIndexes = fromColumnPositions.stream()
                    .mapToInt(this.columnGroupHeaderLayer.getPositionLayer()::getColumnIndexByPosition)
                    .toArray();
            int toIndex = this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(toColumnPosition);

            // expand all collapsed groups
            collapsed.forEach((model, collapsedGroups) -> collapsedGroups.forEach(group -> this.columnGroupHeaderLayer.expandGroup(model, group)));

            // update fromColumnPositions and toColumnPosition
            int[] fromPositions = Arrays.stream(fromIndexes)
                    .map(this.columnGroupHeaderLayer.getPositionLayer()::getColumnPositionByIndex)
                    .toArray();
            command.updateFromColumnPositions(fromPositions);

            toPositionToCheck = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(toIndex);
            command.updateToColumnPosition(toPositionToCheck);
            if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge) {
                toPositionToCheck--;
            }
        }

        boolean toggleCoordinateByEdge = false;
        Group groupToEnd = null;
        Group groupToStart = null;

        for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
            Group toGroup = this.columnGroupHeaderLayer.getGroupByPosition(level, toPositionToCheck);
            if (toGroup != null && MoveDirectionEnum.RIGHT == moveDirection && toGroup.isGroupEnd(toPositionToCheck)) {
                toggleCoordinateByEdge = true;
                if (toGroup.isUnbreakable() && toGroup.getVisibleSpan() < toGroup.getOriginalSpan()) {
                    groupToEnd = toGroup;
                }
            } else if (toGroup != null
                    && MoveDirectionEnum.LEFT == moveDirection
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
            return this.columnGroupHeaderLayer.getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                    new MultiColumnReorderToGroupEndCommand(command, groupToEnd));
        } else if (groupToStart != null) {
            // if the group is unbreakable and the visible span is
            // smaller than the original span, it could be that
            // positions at the start are hidden and the reorder to
            // the left edge could lead to a broken group
            return this.columnGroupHeaderLayer.getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                    new MultiColumnReorderToGroupStartCommand(command, groupToStart));
        }

        return false;
    }

    @Override
    public Class<MultiColumnReorderCommand> getCommandClass() {
        return MultiColumnReorderCommand.class;
    }

    /**
     * Specialization of the {@link MultiColumnReorderCommand} to be able to
     * reorder a column to the start of a {@link Group} even the first columns
     * in the {@link Group} are hidden.
     *
     * @since 2.0
     */
    class MultiColumnReorderToGroupStartCommand extends MultiColumnReorderCommand {

        private final Group group;

        public MultiColumnReorderToGroupStartCommand(MultiColumnReorderCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected MultiColumnReorderToGroupStartCommand(MultiColumnReorderToGroupStartCommand command) {
            super(command);
            this.group = command.group;
        }

        @Override
        public boolean convertToTargetLayer(ILayer targetLayer) {
            boolean convert = super.convertToTargetLayer(targetLayer);

            // check if there are positions for the group members that would be
            // more to the left. this could happen e.g. if columns at the group
            // start are hidden
            if (convert && isReorderToLeftEdge() && targetLayer instanceof IUniqueIndexLayer) {
                int groupStartPosition = ((IUniqueIndexLayer) targetLayer).getColumnPositionByIndex(this.group.getStartIndex());
                if (groupStartPosition >= 0 && groupStartPosition < getToColumnPosition()) {
                    this.toColumnPositionCoordinate.columnPosition = groupStartPosition;
                }
            }

            return convert;
        }

        @Override
        public MultiColumnReorderToGroupStartCommand cloneCommand() {
            return new MultiColumnReorderToGroupStartCommand(this);
        }
    }

    /**
     * Specialization of the {@link MultiColumnReorderCommand} to be able to
     * reorder a column to the end of a {@link Group} even the last columns in
     * the {@link Group} are hidden.
     *
     * @since 2.0
     */
    class MultiColumnReorderToGroupEndCommand extends MultiColumnReorderCommand {

        private final Group group;

        public MultiColumnReorderToGroupEndCommand(MultiColumnReorderCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected MultiColumnReorderToGroupEndCommand(MultiColumnReorderToGroupEndCommand command) {
            super(command);
            this.group = command.group;
        }

        @Override
        public boolean convertToTargetLayer(ILayer targetLayer) {
            boolean convert = super.convertToTargetLayer(targetLayer);

            // check if there are positions for the group members that would be
            // more to the right. this could happen e.g. if columns at the group
            // end are hidden
            if (convert && !isReorderToLeftEdge() && targetLayer instanceof IUniqueIndexLayer) {
                int groupEndPosition = this.group.getGroupEndPosition((IUniqueIndexLayer) targetLayer);
                if (groupEndPosition >= 0 && groupEndPosition > getToColumnPosition()) {
                    this.toColumnPositionCoordinate.columnPosition = groupEndPosition;
                }
            }

            return convert;
        }

        @Override
        public MultiColumnReorderToGroupEndCommand cloneCommand() {
            return new MultiColumnReorderToGroupEndCommand(this);
        }
    }

}
