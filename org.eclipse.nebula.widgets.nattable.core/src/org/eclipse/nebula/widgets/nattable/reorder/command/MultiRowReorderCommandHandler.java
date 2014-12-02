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

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

public class MultiRowReorderCommandHandler extends
        AbstractLayerCommandHandler<MultiRowReorderCommand> {

    private final RowReorderLayer rowReorderLayer;

    public MultiRowReorderCommandHandler(RowReorderLayer rowReorderLayer) {
        this.rowReorderLayer = rowReorderLayer;
    }

    @Override
    public Class<MultiRowReorderCommand> getCommandClass() {
        return MultiRowReorderCommand.class;
    }

    @Override
    protected boolean doCommand(MultiRowReorderCommand command) {
        List<Integer> fromRowPositions = command.getFromRowPositions();
        int toRowPosition = command.getToRowPosition();
        boolean reorderToTopEdge = command.isReorderToTopEdge();

        this.rowReorderLayer.reorderMultipleRowPositions(fromRowPositions,
                toRowPosition, reorderToTopEdge);

        return true;
    }

}
