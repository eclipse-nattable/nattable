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
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ShowRowInViewportCommandHandler extends
        AbstractLayerCommandHandler<ShowRowInViewportCommand> {

    private final ViewportLayer viewportLayer;

    public ShowRowInViewportCommandHandler(ViewportLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<ShowRowInViewportCommand> getCommandClass() {
        return ShowRowInViewportCommand.class;
    }

    @Override
    protected boolean doCommand(ShowRowInViewportCommand command) {
        this.viewportLayer.moveRowPositionIntoViewport(command.getRowPosition());
        return true;
    }

}
