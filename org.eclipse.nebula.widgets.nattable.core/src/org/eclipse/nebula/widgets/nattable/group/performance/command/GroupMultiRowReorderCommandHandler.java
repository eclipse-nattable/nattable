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
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;

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

        if (!RowGroupUtils.isBetweenTwoGroups(
                this.rowGroupHeaderLayer,
                toRowPosition,
                reorderToTopEdge,
                PositionUtil.getVerticalMoveDirection(fromRowPositions.get(0), toRowPosition))) {

            for (int fromRowPosition : fromRowPositions) {
                if (!RowGroupUtils.isReorderValid(this.rowGroupHeaderLayer, fromRowPosition, toRowPosition, reorderToTopEdge)) {
                    // consume as the reorder is not valid
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Class<MultiRowReorderCommand> getCommandClass() {
        return MultiRowReorderCommand.class;
    }
}
