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
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command handler for the {@link RowReorderCommand} that is registered on the
 * positionLayer of the {@link RowGroupHeaderLayer} to avoid handling in case
 * the reordering would break an unbreakable group.
 *
 * @since 1.6
 */
public class GroupRowReorderCommandHandler extends AbstractLayerCommandHandler<RowReorderCommand> {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    public GroupRowReorderCommandHandler(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(RowReorderCommand command) {
        int fromRowPosition = command.getFromRowPosition();
        int toRowPosition = command.getToRowPosition();
        boolean reorderToTopEdge = command.isReorderToTopEdge();

        boolean isValid = RowGroupUtils.isReorderValid(this.rowGroupHeaderLayer, fromRowPosition, toRowPosition, reorderToTopEdge);

        // only if we visibly reorder we need to check the collapsed state and
        // expand in order to update the GroupModel correctly
        if (isValid) {
            int fromIndex = this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(fromRowPosition);
            int toIndex = this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(toRowPosition);
            MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromRowPosition, toRowPosition);
            boolean updateToPosition = false;
            for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
                // as we are registered on the positionLayer, there is no need
                // for transformation
                Group fromGroup = this.rowGroupHeaderLayer.getGroupByPosition(fromRowPosition);
                if (fromGroup != null && fromGroup.isCollapsed()) {

                    int toPositionToCheck = toRowPosition;
                    if (MoveDirectionEnum.DOWN == moveDirection && reorderToTopEdge) {
                        toPositionToCheck--;
                    }

                    // if we are not reordering inside a collapsed group we need
                    // to expand first to ensure consistency of the GroupModel
                    if (!RowGroupUtils.isInTheSameGroup(this.rowGroupHeaderLayer, level, fromRowPosition, toPositionToCheck)
                            || (fromGroup.getStartIndex() != fromGroup.getVisibleStartIndex() && fromRowPosition == toRowPosition)) {
                        this.rowGroupHeaderLayer.expandGroup(this.rowGroupHeaderLayer.getGroupModel(level), fromGroup);
                        updateToPosition = true;
                    }
                }
            }
            if (updateToPosition) {
                // we update the toColumnPosition because we expanded a group
                if (moveDirection != MoveDirectionEnum.DOWN) {
                    command.updateFromRowPosition(this.rowGroupHeaderLayer.getPositionLayer().getRowPositionByIndex(fromIndex));
                } else {
                    command.updateToRowPosition(this.rowGroupHeaderLayer.getPositionLayer().getRowPositionByIndex(toIndex));
                }
            }
        }

        // return false means process further and do not consume
        return !isValid;
    }

    @Override
    public Class<RowReorderCommand> getCommandClass() {
        return RowReorderCommand.class;
    }
}