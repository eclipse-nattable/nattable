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

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;

public class ColumnReorderStartCommandHandler extends
        AbstractLayerCommandHandler<ColumnReorderStartCommand> {

    private final ColumnReorderLayer columnReorderLayer;

    public ColumnReorderStartCommandHandler(
            ColumnReorderLayer columnReorderLayer) {
        this.columnReorderLayer = columnReorderLayer;
    }

    @Override
    public Class<ColumnReorderStartCommand> getCommandClass() {
        return ColumnReorderStartCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnReorderStartCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();

        this.columnReorderLayer.setReorderFromColumnPosition(fromColumnPosition);

        return true;
    }

}
