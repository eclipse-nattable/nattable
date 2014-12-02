/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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

public class RecalculateScrollBarsCommandHandler extends
        AbstractLayerCommandHandler<RecalculateScrollBarsCommand> {

    private final ViewportLayer viewportLayer;

    public RecalculateScrollBarsCommandHandler(ViewportLayer viewportLayer) {
        this.viewportLayer = viewportLayer;
    }

    @Override
    public Class<RecalculateScrollBarsCommand> getCommandClass() {
        return RecalculateScrollBarsCommand.class;
    }

    @Override
    protected boolean doCommand(RecalculateScrollBarsCommand command) {
        this.viewportLayer.recalculateScrollBars();
        return true;
    }

}
