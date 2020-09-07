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

public class ColumnReorderCommandHandler extends AbstractLayerCommandHandler<ColumnReorderCommand> {

    private final ColumnReorderLayer columnReorderLayer;

    public ColumnReorderCommandHandler(ColumnReorderLayer columnReorderLayer) {
        this.columnReorderLayer = columnReorderLayer;
    }

    @Override
    public Class<ColumnReorderCommand> getCommandClass() {
        return ColumnReorderCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnReorderCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();
        int toColumnPosition = command.getToColumnPosition();
        boolean reorderToLeftEdge = command.isReorderToLeftEdge();

        this.columnReorderLayer.reorderColumnPosition(
                fromColumnPosition,
                toColumnPosition,
                reorderToLeftEdge);

        return true;
    }

}
