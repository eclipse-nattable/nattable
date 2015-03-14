/*******************************************************************************
 * Copyright (c) 2012, 2015 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 462143
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ViewportDragCommandHandler extends AbstractLayerCommandHandler<ViewportDragCommand> {

    private ViewportLayer viewportLayer;

    public ViewportDragCommandHandler(ViewportLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<ViewportDragCommand> getCommandClass() {
        return ViewportDragCommand.class;
    }

    @Override
    protected boolean doCommand(ViewportDragCommand command) {
        if (command.isConfiguredForMoveDirection()) {
            this.viewportLayer.drag(command.getHorizontal(), command.getVertical());
        }
        else {
            this.viewportLayer.drag(command.getX(), command.getY());
        }
        return true;
    }

}
