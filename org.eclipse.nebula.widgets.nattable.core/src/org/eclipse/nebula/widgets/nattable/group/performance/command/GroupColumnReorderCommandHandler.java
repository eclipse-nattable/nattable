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

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command handler for the {@link ColumnReorderCommand} that is registered on
 * the positionLayer of the {@link ColumnGroupHeaderLayer} to avoid handling in
 * case the reordering would break an unbreakable group.
 *
 * @since 1.6
 */
public class GroupColumnReorderCommandHandler extends AbstractLayerCommandHandler<ColumnReorderCommand> {

    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;

    public GroupColumnReorderCommandHandler(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(ColumnReorderCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();
        int toColumnPosition = command.getToColumnPosition();
        boolean reorderToLeftEdge = command.isReorderToLeftEdge();

        boolean isValid = ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, fromColumnPosition, toColumnPosition, reorderToLeftEdge);

        // only if we visibly reorder we need to check the collapsed state and
        // expand in order to update the GroupModel correctly
        if (isValid) {
            // as we are registered on the positionLayer, there is no need
            // for transformation

            int fromIndex = this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(fromColumnPosition);
            int toIndex = this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(toColumnPosition);
            MoveDirectionEnum moveDirection = PositionUtil.getHorizontalMoveDirection(fromColumnPosition, toColumnPosition);
            int fromPositionToCheck = fromColumnPosition;
            int toPositionToCheck = toColumnPosition;
            if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge) {
                toPositionToCheck--;
            }

            boolean toggleCoordinateByEdge = false;
            Group groupToEnd = null;
            Group groupToStart = null;

            for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {

                Group fromGroup = this.columnGroupHeaderLayer.getGroupByPosition(level, fromPositionToCheck);
                Group toGroup = this.columnGroupHeaderLayer.getGroupByPosition(level, toPositionToCheck);
                if (fromGroup != null
                        && fromGroup.isCollapsed()
                        && (!ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, level, fromPositionToCheck, toPositionToCheck)
                                || fromPositionToCheck == toPositionToCheck)) {
                    // if we are not reordering inside a collapsed group we need
                    // to expand first to ensure consistency of the GroupModel
                    this.columnGroupHeaderLayer.expandGroup(this.columnGroupHeaderLayer.getGroupModel(level), fromGroup);

                    // we update the column positions because we expanded a
                    // group
                    if (moveDirection != MoveDirectionEnum.RIGHT) {
                        fromPositionToCheck = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(fromIndex);
                        command.updateFromColumnPosition(fromPositionToCheck);
                    } else {
                        fromPositionToCheck = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(fromIndex);
                        command.updateFromColumnPosition(fromPositionToCheck);
                        toPositionToCheck = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(toIndex);
                        command.updateToColumnPosition(toPositionToCheck);
                        if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge) {
                            toPositionToCheck--;
                        }
                    }
                }

                if (toGroup != null
                        && MoveDirectionEnum.RIGHT == moveDirection
                        && toGroup.isGroupEnd(toPositionToCheck)) {
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
                        new ColumnReorderToGroupEndCommand(command, groupToEnd));
            } else if (groupToStart != null) {
                // if the group is unbreakable and the visible span is
                // smaller than the original span, it could be that
                // positions at the start are hidden and the reorder to
                // the left edge could lead to a broken group
                return this.columnGroupHeaderLayer.getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                        new ColumnReorderToGroupStartCommand(command, groupToStart));
            }
        }

        // return false means process further and do not consume
        return !isValid;
    }

    @Override
    public Class<ColumnReorderCommand> getCommandClass() {
        return ColumnReorderCommand.class;
    }

    /**
     * Specialization of the {@link ColumnReorderCommand} to be able to reorder
     * a column to the start of a {@link Group} even the first columns in the
     * {@link Group} are hidden.
     *
     * @since 2.0
     */
    class ColumnReorderToGroupStartCommand extends ColumnReorderCommand {

        private final Group group;

        public ColumnReorderToGroupStartCommand(ColumnReorderCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected ColumnReorderToGroupStartCommand(ColumnReorderToGroupStartCommand command) {
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
                    updateToColumnPosition(groupStartPosition);
                }
            }

            return convert;
        }

        @Override
        public ColumnReorderToGroupStartCommand cloneCommand() {
            return new ColumnReorderToGroupStartCommand(this);
        }
    }

    /**
     * Specialization of the {@link ColumnReorderCommand} to be able to reorder
     * a column to the end of a {@link Group} even the last columns in the
     * {@link Group} are hidden.
     *
     * @since 2.0
     */
    class ColumnReorderToGroupEndCommand extends ColumnReorderCommand {

        private final Group group;

        public ColumnReorderToGroupEndCommand(ColumnReorderCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected ColumnReorderToGroupEndCommand(ColumnReorderToGroupEndCommand command) {
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
                    updateToColumnPosition(groupEndPosition);
                }
            }

            return convert;
        }

        @Override
        public ColumnReorderToGroupEndCommand cloneCommand() {
            return new ColumnReorderToGroupEndCommand(this);
        }
    }

}