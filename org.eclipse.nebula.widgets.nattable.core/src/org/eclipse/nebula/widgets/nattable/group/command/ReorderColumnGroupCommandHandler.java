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

public class ReorderColumnGroupCommandHandler extends
        AbstractLayerCommandHandler<ReorderColumnGroupCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public ReorderColumnGroupCommandHandler(
            ColumnGroupReorderLayer columnGroupReorderLayer) {
        this.columnGroupReorderLayer = columnGroupReorderLayer;
    }

    @Override
    public Class<ReorderColumnGroupCommand> getCommandClass() {
        return ReorderColumnGroupCommand.class;
    }

    @Override
    protected boolean doCommand(ReorderColumnGroupCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();
        int toColumnPosition = command.getToColumnPosition();

        return this.columnGroupReorderLayer.reorderColumnGroup(fromColumnPosition,
                toColumnPosition);
    }

}
