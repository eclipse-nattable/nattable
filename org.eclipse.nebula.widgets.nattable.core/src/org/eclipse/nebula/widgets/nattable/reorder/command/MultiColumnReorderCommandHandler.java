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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;

public class MultiColumnReorderCommandHandler extends AbstractLayerCommandHandler<MultiColumnReorderCommand> {

    private final ColumnReorderLayer columnReorderLayer;

    public MultiColumnReorderCommandHandler(ColumnReorderLayer columnReorderLayer) {
        this.columnReorderLayer = columnReorderLayer;
    }

    @Override
    protected boolean doCommand(MultiColumnReorderCommand command) {
        int[] fromColumnPositions = command.getFromColumnPositionsArray();
        int toColumnPosition = command.getToColumnPosition();
        boolean reorderToLeftEdge = command.isReorderToLeftEdge();

        if (!command.isReorderByIndex()) {
            this.columnReorderLayer.reorderMultipleColumnPositions(
                    fromColumnPositions,
                    toColumnPosition,
                    reorderToLeftEdge);
        } else {
            this.columnReorderLayer.reorderMultipleColumnIndexes(
                    fromColumnPositions,
                    toColumnPosition,
                    reorderToLeftEdge);
        }

        return true;
    }

    @Override
    public Class<MultiColumnReorderCommand> getCommandClass() {
        return MultiColumnReorderCommand.class;
    }

}
