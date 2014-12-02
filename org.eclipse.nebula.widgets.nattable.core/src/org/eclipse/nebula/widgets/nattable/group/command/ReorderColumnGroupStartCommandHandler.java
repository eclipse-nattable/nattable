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

public class ReorderColumnGroupStartCommandHandler extends
        AbstractLayerCommandHandler<ReorderColumnGroupStartCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public ReorderColumnGroupStartCommandHandler(
            ColumnGroupReorderLayer columnGroupReorderLayer) {
        this.columnGroupReorderLayer = columnGroupReorderLayer;
    }

    @Override
    public Class<ReorderColumnGroupStartCommand> getCommandClass() {
        return ReorderColumnGroupStartCommand.class;
    }

    @Override
    protected boolean doCommand(ReorderColumnGroupStartCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();

        this.columnGroupReorderLayer
                .setReorderFromColumnPosition(fromColumnPosition);

        return true;
    }

}
