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
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
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

        for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
            // as we are registered on the positionLayer, there is no need
            // for transformation

            int toPositionToCheck = toRowPosition;
            if (MoveDirectionEnum.DOWN == moveDirection && reorderToTopEdge) {
                toPositionToCheck--;
            }

            Group toGroup = this.rowGroupHeaderLayer.getGroupByPosition(level, toPositionToCheck);
            if (toGroup != null && MoveDirectionEnum.DOWN == moveDirection && toGroup.isGroupEnd(toPositionToCheck)) {
                command.toggleCoordinateByEdge();
            }
        }

        return false;
    }

    @Override
    public Class<MultiRowReorderCommand> getCommandClass() {
        return MultiRowReorderCommand.class;
    }
}
