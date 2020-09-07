/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

public class MultiRowReorderCommandHandler extends AbstractLayerCommandHandler<MultiRowReorderCommand> {

    private final RowReorderLayer rowReorderLayer;

    public MultiRowReorderCommandHandler(RowReorderLayer rowReorderLayer) {
        this.rowReorderLayer = rowReorderLayer;
    }

    @Override
    protected boolean doCommand(MultiRowReorderCommand command) {
        int[] fromRowPositions = command.getFromRowPositionsArray();
        int toRowPosition = command.getToRowPosition();
        boolean reorderToTopEdge = command.isReorderToTopEdge();

        if (!command.isReorderByIndex()) {
            this.rowReorderLayer.reorderMultipleRowPositions(
                    fromRowPositions,
                    toRowPosition,
                    reorderToTopEdge);
        } else {
            this.rowReorderLayer.reorderMultipleRowIndexes(
                    fromRowPositions,
                    toRowPosition,
                    reorderToTopEdge);
        }

        return true;
    }

    @Override
    public Class<MultiRowReorderCommand> getCommandClass() {
        return MultiRowReorderCommand.class;
    }

}
