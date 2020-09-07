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

public class RowReorderEndCommandHandler extends AbstractLayerCommandHandler<RowReorderEndCommand> {

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
                this.rowReorderLayer.getReorderFromRowPosition(),
                toRowPosition,
                reorderToTopEdge);

        return true;
    }

}
