/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnRename;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;

/**
 * Handles renaming of columns. Registered with the {@link ColumnHeaderLayer}.
 */
public class RenameColumnHeaderCommandHandler extends
        AbstractLayerCommandHandler<RenameColumnHeaderCommand> {

    ColumnHeaderLayer columnHeaderLayer;

    public RenameColumnHeaderCommandHandler(ColumnHeaderLayer columnHeaderLayer) {
        this.columnHeaderLayer = columnHeaderLayer;
    }

    @Override
    protected boolean doCommand(RenameColumnHeaderCommand command) {
        return this.columnHeaderLayer.renameColumnPosition(
                command.getColumnPosition(), command.getCustomColumnName());
    }

    @Override
    public Class<RenameColumnHeaderCommand> getCommandClass() {
        return RenameColumnHeaderCommand.class;
    }

}
