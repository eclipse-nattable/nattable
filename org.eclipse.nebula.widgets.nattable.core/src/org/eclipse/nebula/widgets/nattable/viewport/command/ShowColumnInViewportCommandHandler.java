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

public class ShowColumnInViewportCommandHandler extends
        AbstractLayerCommandHandler<ShowColumnInViewportCommand> {

    private final ViewportLayer viewportLayer;

    public ShowColumnInViewportCommandHandler(ViewportLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<ShowColumnInViewportCommand> getCommandClass() {
        return ShowColumnInViewportCommand.class;
    }

    @Override
    protected boolean doCommand(ShowColumnInViewportCommand command) {
        this.viewportLayer.moveColumnPositionIntoViewport(command
                .getColumnPosition());
        return true;
    }

}
