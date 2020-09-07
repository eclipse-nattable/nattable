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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;

public class ReorderColumnGroupStartCommandHandler extends
        AbstractLayerCommandHandler<ReorderColumnGroupStartCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public ReorderColumnGroupStartCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
        this.columnGroupReorderLayer = columnGroupReorderLayer;
    }

    @Override
    public Class<ReorderColumnGroupStartCommand> getCommandClass() {
        return ReorderColumnGroupStartCommand.class;
    }

    @Override
    protected boolean doCommand(ReorderColumnGroupStartCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();

        this.columnGroupReorderLayer.setReorderFromColumnPosition(fromColumnPosition);

        return true;
    }

}
