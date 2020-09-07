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
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;

public class ViewportSelectRowCommandHandler extends AbstractLayerCommandHandler<ViewportSelectRowCommand> {

    private final AbstractLayer viewportLayer;

    public ViewportSelectRowCommandHandler(AbstractLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<ViewportSelectRowCommand> getCommandClass() {
        return ViewportSelectRowCommand.class;
    }

    @Override
    protected boolean doCommand(ViewportSelectRowCommand command) {
        this.viewportLayer.doCommand(
                new SelectRowsCommand(
                        this.viewportLayer,
                        0,
                        command.getRowPosition(),
                        command.isWithShiftMask(),
                        command.isWithControlMask()));
        return true;
    }

}
