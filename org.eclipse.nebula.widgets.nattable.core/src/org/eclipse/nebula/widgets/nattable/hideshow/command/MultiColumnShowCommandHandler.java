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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;

public class MultiColumnShowCommandHandler extends
        AbstractLayerCommandHandler<MultiColumnShowCommand> {

    private final ColumnHideShowLayer columnHideShowLayer;

    public MultiColumnShowCommandHandler(ColumnHideShowLayer columnHideShowLayer) {
        this.columnHideShowLayer = columnHideShowLayer;
    }

    @Override
    public Class<MultiColumnShowCommand> getCommandClass() {
        return MultiColumnShowCommand.class;
    }

    @Override
    protected boolean doCommand(MultiColumnShowCommand command) {
        this.columnHideShowLayer.showColumnIndexes(command.getColumnIndexes());
        return true;
    }

}
