/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class AnyCommandHandlerFixture implements ILayerCommandHandler<ILayerCommand> {

    private ILayerCommand commadHandled;
    private int numberOfCommandsHandled;

    @Override
    public boolean doCommand(ILayer targetLayer, ILayerCommand command) {
        this.commadHandled = command;
        this.numberOfCommandsHandled++;
        return true;
    }

    @Override
    public Class<ILayerCommand> getCommandClass() {
        return ILayerCommand.class;
    }

    public ILayerCommand getCommadHandled() {
        return this.commadHandled;
    }

    public int getNumberOfCommandsHandled() {
        return this.numberOfCommandsHandled;
    }
}
