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
import org.eclipse.nebula.widgets.nattable.hideshow.IRowHideShowCommandLayer;

public class MultiRowShowCommandHandler extends
        AbstractLayerCommandHandler<MultiRowShowCommand> {

    private final IRowHideShowCommandLayer rowHideShowLayer;

    public MultiRowShowCommandHandler(IRowHideShowCommandLayer rowHideShowLayer) {
        this.rowHideShowLayer = rowHideShowLayer;
    }

    @Override
    public Class<MultiRowShowCommand> getCommandClass() {
        return MultiRowShowCommand.class;
    }

    @Override
    protected boolean doCommand(MultiRowShowCommand command) {
        this.rowHideShowLayer.showRowIndexes(command.getRowIndexes());
        return true;
    }

}
