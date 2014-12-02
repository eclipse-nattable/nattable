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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;

public class MultiColumnHideCommandHandler extends
        AbstractLayerCommandHandler<MultiColumnHideCommand> {

    private final ColumnHideShowLayer columnHideShowLayer;

    public MultiColumnHideCommandHandler(ColumnHideShowLayer columnHideShowLayer) {
        this.columnHideShowLayer = columnHideShowLayer;
    }

    @Override
    public Class<MultiColumnHideCommand> getCommandClass() {
        return MultiColumnHideCommand.class;
    }

    @Override
    protected boolean doCommand(MultiColumnHideCommand command) {
        this.columnHideShowLayer.hideColumnPositions(command.getColumnPositions());
        return true;
    }

}
