/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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

public class ShowCellInViewportCommandHandler extends AbstractLayerCommandHandler<ShowCellInViewportCommand> {

    private final ViewportLayer viewportLayer;

    public ShowCellInViewportCommandHandler(ViewportLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<ShowCellInViewportCommand> getCommandClass() {
        return ShowCellInViewportCommand.class;
    }

    @Override
    protected boolean doCommand(ShowCellInViewportCommand command) {
        this.viewportLayer.moveCellPositionIntoViewport(command.getColumnPosition(), command.getRowPosition());
        return true;
    }

}
