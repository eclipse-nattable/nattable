/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

public class RowReorderEndCommandHandler extends
        AbstractLayerCommandHandler<RowReorderEndCommand> {

    private final RowReorderLayer rowReorderLayer;

    public RowReorderEndCommandHandler(RowReorderLayer rowReorderLayer) {
        this.rowReorderLayer = rowReorderLayer;
    }

    @Override
    public Class<RowReorderEndCommand> getCommandClass() {
        return RowReorderEndCommand.class;
    }

    @Override
    protected boolean doCommand(RowReorderEndCommand command) {
        int toRowPosition = command.getToRowPosition();
        boolean reorderToTopEdge = command.isReorderToTopEdge();

        this.rowReorderLayer.reorderRowPosition(
                this.rowReorderLayer.getReorderFromRowPosition(), toRowPosition,
                reorderToTopEdge);

        return true;
    }

}
