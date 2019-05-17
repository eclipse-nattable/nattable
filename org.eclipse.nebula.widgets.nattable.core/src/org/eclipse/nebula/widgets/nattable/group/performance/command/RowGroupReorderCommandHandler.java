/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;

/**
 * Command handler for the {@link RowGroupReorderCommand}.
 *
 * @since 1.6
 */
public class RowGroupReorderCommandHandler extends AbstractLayerCommandHandler<RowGroupReorderCommand> {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    public RowGroupReorderCommandHandler(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(RowGroupReorderCommand command) {
        int level = command.getLevel();
        int fromRowPosition = command.getFromRowPosition();
        int toRowPosition = command.getToRowPosition();

        if (!command.isReorderToTopEdge()) {
            // needed to set the reorderToTopEdge parameter correct on creating
            // the MultiRowReorderCommand
            toRowPosition++;
        }

        return this.rowGroupHeaderLayer.reorderRowGroup(level, fromRowPosition, toRowPosition);
    }

    @Override
    public Class<RowGroupReorderCommand> getCommandClass() {
        return RowGroupReorderCommand.class;
    }

}
