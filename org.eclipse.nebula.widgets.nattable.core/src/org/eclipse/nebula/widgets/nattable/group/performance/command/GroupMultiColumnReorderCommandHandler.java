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

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
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

        for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
            // as we are registered on the positionLayer, there is no need
            // for transformation

            int toPositionToCheck = toColumnPosition;
            if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge) {
                toPositionToCheck--;
            }

            Group toGroup = this.columnGroupHeaderLayer.getGroupByPosition(toPositionToCheck);
            if (toGroup != null && MoveDirectionEnum.RIGHT == moveDirection && toGroup.isGroupEnd(toPositionToCheck)) {
                command.toggleCoordinateByEdge();
            }
        }

        return false;
    }

    @Override
    public Class<MultiColumnReorderCommand> getCommandClass() {
        return MultiColumnReorderCommand.class;
    }
}
