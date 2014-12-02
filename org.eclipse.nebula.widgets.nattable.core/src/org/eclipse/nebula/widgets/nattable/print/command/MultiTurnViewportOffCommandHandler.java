/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.print.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * ILayerCommandHandler for the TurnViewportOffCommand that needs to be used
 * with split viewports. As the command is consumed by the ViewportLayer and it
 * is also not necessary to process the command any further, this handler is
 * used to ensure that all viewports in the composition get the chance to
 * process the command.
 *
 * @author Dirk Fauth
 *
 */
public class MultiTurnViewportOffCommandHandler implements
        ILayerCommandHandler<TurnViewportOffCommand> {

    private ViewportLayer[] viewports;

    public MultiTurnViewportOffCommandHandler(ViewportLayer... viewports) {
        this.viewports = viewports;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, TurnViewportOffCommand command) {
        for (ViewportLayer layer : this.viewports) {
            // simply delegate the command to all registered viewports
            layer.doCommand(command);
        }
        return true;
    }

    @Override
    public Class<TurnViewportOffCommand> getCommandClass() {
        return TurnViewportOffCommand.class;
    }

}
