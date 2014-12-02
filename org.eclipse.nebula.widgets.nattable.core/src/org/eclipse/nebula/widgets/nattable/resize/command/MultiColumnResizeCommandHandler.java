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
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;

public class MultiColumnResizeCommandHandler extends
        AbstractLayerCommandHandler<MultiColumnResizeCommand> {

    private final DataLayer dataLayer;

    public MultiColumnResizeCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<MultiColumnResizeCommand> getCommandClass() {
        return MultiColumnResizeCommand.class;
    }

    @Override
    protected boolean doCommand(MultiColumnResizeCommand command) {
        List<Integer> columnPositions = new ArrayList<Integer>();

        for (int columnPosition : command.getColumnPositions()) {
            columnPositions.add(columnPosition);
            this.dataLayer.setColumnWidthByPosition(columnPosition,
                    command.getColumnWidth(columnPosition), false);
        }

        List<Range> ranges = PositionUtil.getRanges(columnPositions);
        for (Range range : ranges) {
            this.dataLayer.fireLayerEvent(new ColumnResizeEvent(this.dataLayer, range));
        }

        return true;
    }

}
