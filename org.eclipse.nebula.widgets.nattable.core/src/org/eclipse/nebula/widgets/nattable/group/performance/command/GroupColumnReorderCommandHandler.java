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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
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
            int fromIndex = this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(fromColumnPosition);
            int toIndex = this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(toColumnPosition);
            MoveDirectionEnum moveDirection = ColumnGroupUtils.getMoveDirection(fromColumnPosition, toColumnPosition);
            boolean updateToPosition = false;
            for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
                // as we are registered on the positionLayer, there is no need
                // for transformation
                Group fromGroup = this.columnGroupHeaderLayer.getGroupByPosition(fromColumnPosition);
                if (fromGroup != null && fromGroup.isCollapsed()) {

                    int toPositionToCheck = toColumnPosition;
                    if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge) {
                        toPositionToCheck--;
                    }

                    // if we are not reordering inside a collapsed group we need
                    // to expand first to ensure consistency of the GroupModel
                    if (!ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, level, fromColumnPosition, toPositionToCheck)
                            || (fromGroup.getStartIndex() != fromGroup.getVisibleStartIndex() && fromColumnPosition == toColumnPosition)) {
                        this.columnGroupHeaderLayer.expandGroup(this.columnGroupHeaderLayer.getGroupModel(level), fromGroup);
                        updateToPosition = true;
                    }
                }
            }
            if (updateToPosition) {
                // we update the toColumnPosition because we expanded a group
                if (moveDirection != MoveDirectionEnum.RIGHT) {
                    command.updateFromColumnPosition(this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(fromIndex));
                } else {
                    command.updateToColumnPosition(this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(toIndex));
                }
            }
        }

        // return false means process further and do not consume
        return !isValid;
    }

    @Override
    public Class<ColumnReorderCommand> getCommandClass() {
        return ColumnReorderCommand.class;
    }
}