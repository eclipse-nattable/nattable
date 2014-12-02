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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;

public class MultiColumnReorderCommandHandler extends
        AbstractLayerCommandHandler<MultiColumnReorderCommand> {

    private final ColumnReorderLayer columnReorderLayer;

    public MultiColumnReorderCommandHandler(
            ColumnReorderLayer columnReorderLayer) {
        this.columnReorderLayer = columnReorderLayer;
    }

    @Override
    public Class<MultiColumnReorderCommand> getCommandClass() {
        return MultiColumnReorderCommand.class;
    }

    @Override
    protected boolean doCommand(MultiColumnReorderCommand command) {
        List<Integer> fromColumnPositions = command.getFromColumnPositions();
        int toColumnPosition = command.getToColumnPosition();
        boolean reorderToLeftEdge = command.isReorderToLeftEdge();

        this.columnReorderLayer.reorderMultipleColumnPositions(fromColumnPositions,
                toColumnPosition, reorderToLeftEdge);

        return true;
    }

}
