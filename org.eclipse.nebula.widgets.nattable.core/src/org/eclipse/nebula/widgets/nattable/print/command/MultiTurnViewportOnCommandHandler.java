/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.print.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * ILayerCommandHandler for the TurnViewportOnCommand that needs to be used with
 * split viewports. As the command is consumed by the ViewportLayer and it is
 * also not necessary to process the command any further, this handler is used
 * to ensure that all viewports in the composition get the chance to process the
 * command.
 */
public class MultiTurnViewportOnCommandHandler implements ILayerCommandHandler<TurnViewportOnCommand> {

    private ViewportLayer[] viewports;

    public MultiTurnViewportOnCommandHandler(ViewportLayer... viewports) {
        this.viewports = viewports;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, TurnViewportOnCommand command) {
        for (ViewportLayer layer : this.viewports) {
            // simply delegate the command to all registered viewports
            layer.doCommand(command);
        }
        return true;
    }

    @Override
    public Class<TurnViewportOnCommand> getCommandClass() {
        return TurnViewportOnCommand.class;
    }

}
