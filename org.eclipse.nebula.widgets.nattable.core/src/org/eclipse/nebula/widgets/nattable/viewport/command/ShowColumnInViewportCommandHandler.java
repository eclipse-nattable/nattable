/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ShowColumnInViewportCommandHandler extends AbstractLayerCommandHandler<ShowColumnInViewportCommand> {

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
        if (command.convertToTargetLayer(this.viewportLayer.getScrollableLayer())) {
            this.viewportLayer.moveColumnPositionIntoViewport(command.getColumnPosition());
            return true;
        }
        return false;
    }

}
