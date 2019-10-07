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
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command handler for the {@link RowReorderEndCommand} that is registered on
 * the positionLayer of the {@link RowGroupHeaderLayer} to avoid handling in
 * case the reordering would break an unbreakable group.
 *
 * @since 1.6
 */
public class GroupRowReorderEndCommandHandler extends AbstractLayerCommandHandler<RowReorderEndCommand> {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    public GroupRowReorderEndCommandHandler(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(RowReorderEndCommand command) {
        int fromRowPosition = this.rowGroupHeaderLayer.getReorderFromRowPosition();
        int toRowPosition = command.getToRowPosition();
        boolean reorderToTopEdge = command.isReorderToTopEdge();

        boolean isValid = RowGroupUtils.isReorderValid(this.rowGroupHeaderLayer, fromRowPosition, toRowPosition, reorderToTopEdge);

        // only if we visibly reorder we need to check the collapsed state and
        // expand in order to update the GroupModel correctly
        if (isValid) {
            // as we are registered on the positionLayer, there is no need
            // for transformation

            int fromIndex = this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(fromRowPosition);
            int toIndex = this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(toRowPosition);
            MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromRowPosition, toRowPosition);
            int fromPositionToCheck = fromRowPosition;
            int toPositionToCheck = toRowPosition;
            if (MoveDirectionEnum.DOWN == moveDirection && reorderToTopEdge) {
                toPositionToCheck--;
            }

            boolean toggleCoordinateByEdge = false;
            Group groupToEnd = null;
            Group groupToStart = null;

            for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
                Group fromGroup = this.rowGroupHeaderLayer.getGroupByPosition(level, fromPositionToCheck);
                Group toGroup = this.rowGroupHeaderLayer.getGroupByPosition(level, toPositionToCheck);

                if (fromGroup != null
                        && fromGroup.isCollapsed()
                        && (!RowGroupUtils.isInTheSameGroup(this.rowGroupHeaderLayer, level, fromPositionToCheck, toPositionToCheck)
                                || fromPositionToCheck == toPositionToCheck)) {
                    // if we are not reordering inside a collapsed group we need
                    // to expand first to ensure consistency of the GroupModel
                    this.rowGroupHeaderLayer.expandGroup(this.rowGroupHeaderLayer.getGroupModel(level), fromGroup);

                    // we update the toRowPosition because we expanded a group
                    if (moveDirection != MoveDirectionEnum.DOWN) {
                        fromPositionToCheck = this.rowGroupHeaderLayer.getPositionLayer().getRowPositionByIndex(fromIndex);
                        this.rowGroupHeaderLayer.setReorderFromRowPosition(fromPositionToCheck);
                    } else {
                        toPositionToCheck = this.rowGroupHeaderLayer.getPositionLayer().getRowPositionByIndex(toIndex);
                        command.updateToRowPosition(toPositionToCheck);
                        if (MoveDirectionEnum.DOWN == moveDirection && reorderToTopEdge) {
                            toPositionToCheck--;
                        }
                    }
                }

                if (toGroup != null
                        && MoveDirectionEnum.DOWN == moveDirection
                        && toGroup.isGroupEnd(toPositionToCheck)) {
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
                        new RowReorderEndToGroupEndCommand(command, groupToEnd));
            } else if (groupToStart != null) {
                // if the group is unbreakable and the visible span is
                // smaller than the original span, it could be that
                // positions at the start are hidden and the reorder to
                // the left edge could lead to a broken group
                return this.rowGroupHeaderLayer.getPositionLayer().getUnderlyingLayerByPosition(0, 0).doCommand(
                        new RowReorderEndToGroupStartCommand(command, groupToStart));
            }
        }

        // return false means process further and do not consume
        return !isValid;
    }

    @Override
    public Class<RowReorderEndCommand> getCommandClass() {
        return RowReorderEndCommand.class;
    }

    /**
     * Specialization of the {@link RowReorderEndCommand} to be able to reorder
     * a row to the start of a {@link Group} even the first rows in the
     * {@link Group} are hidden.
     *
     * @since 2.0
     */
    class RowReorderEndToGroupStartCommand extends RowReorderEndCommand {

        private final Group group;

        public RowReorderEndToGroupStartCommand(RowReorderEndCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected RowReorderEndToGroupStartCommand(RowReorderEndToGroupStartCommand command) {
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
                    updateToRowPosition(groupStartPosition);
                }
            }

            return convert;
        }

        @Override
        public RowReorderEndToGroupStartCommand cloneCommand() {
            return new RowReorderEndToGroupStartCommand(this);
        }
    }

    /**
     * Specialization of the {@link RowReorderEndCommand} to be able to reorder
     * a row to the end of a {@link Group} even the last rows in the
     * {@link Group} are hidden.
     *
     * @since 2.0
     */
    class RowReorderEndToGroupEndCommand extends RowReorderEndCommand {

        private final Group group;

        public RowReorderEndToGroupEndCommand(RowReorderEndCommand command, Group group) {
            super(command);
            this.group = group;
        }

        /**
         * Clone constructor.
         *
         * @param command
         *            The command to clone.
         */
        protected RowReorderEndToGroupEndCommand(RowReorderEndToGroupEndCommand command) {
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
                    updateToRowPosition(groupEndPosition);
                }
            }

            return convert;
        }

        @Override
        public RowReorderEndToGroupEndCommand cloneCommand() {
            return new RowReorderEndToGroupEndCommand(this);
        }
    }

}