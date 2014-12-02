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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;

public class ReorderColumnGroupEndCommandHandler extends
        AbstractLayerCommandHandler<ReorderColumnGroupEndCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public ReorderColumnGroupEndCommandHandler(
            ColumnGroupReorderLayer columnGroupReorderLayer) {
        this.columnGroupReorderLayer = columnGroupReorderLayer;
    }

    @Override
    public Class<ReorderColumnGroupEndCommand> getCommandClass() {
        return ReorderColumnGroupEndCommand.class;
    }

    @Override
    protected boolean doCommand(ReorderColumnGroupEndCommand command) {
        int toColumnPosition = command.getToColumnPosition();

        // Bug 437744
        // if not reorderToLeftEdge we increase toColumnPosition by 1
        // as the following processing is calculating the reorderToLeftEdge
        // value out of the given toColumnPosition and the column count
        if (!command.isReorderToLeftEdge())
            toColumnPosition++;

        return this.columnGroupReorderLayer.reorderColumnGroup(
                this.columnGroupReorderLayer.getReorderFromColumnPosition(),
                toColumnPosition);
    }

}
