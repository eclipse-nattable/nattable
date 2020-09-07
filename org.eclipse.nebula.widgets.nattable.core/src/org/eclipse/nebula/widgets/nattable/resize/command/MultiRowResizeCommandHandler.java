/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;

public class MultiRowResizeCommandHandler extends AbstractLayerCommandHandler<MultiRowResizeCommand> {

    private final DataLayer dataLayer;

    public MultiRowResizeCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<MultiRowResizeCommand> getCommandClass() {
        return MultiRowResizeCommand.class;
    }

    @Override
    protected boolean doCommand(MultiRowResizeCommand command) {
        int[] rowPositions = command.getRowPositionsArray();

        for (int rowPosition : rowPositions) {
            int newRowHeight = command.downScaleValue()
                    ? this.dataLayer.downScaleRowHeight(command.getRowHeight(rowPosition))
                    : command.getRowHeight(rowPosition);

            this.dataLayer.setRowHeightByPosition(rowPosition, newRowHeight, false);
        }

        List<Range> ranges = PositionUtil.getRanges(rowPositions);
        for (Range range : ranges) {
            this.dataLayer.fireLayerEvent(new RowResizeEvent(this.dataLayer, range));
        }

        return true;
    }

}
