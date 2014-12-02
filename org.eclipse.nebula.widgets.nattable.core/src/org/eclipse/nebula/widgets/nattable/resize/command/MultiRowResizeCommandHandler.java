/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;

public class MultiRowResizeCommandHandler extends
        AbstractLayerCommandHandler<MultiRowResizeCommand> {

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
        List<Integer> rowPositions = new ArrayList<Integer>();

        for (int rowPosition : command.getRowPositions()) {
            rowPositions.add(rowPosition);
            this.dataLayer.setRowHeightByPosition(rowPosition,
                    command.getRowHeight(rowPosition), false);
        }

        List<Range> ranges = PositionUtil.getRanges(rowPositions);
        for (Range range : ranges) {
            this.dataLayer.fireLayerEvent(new RowResizeEvent(this.dataLayer, range));
        }

        return true;
    }

}
