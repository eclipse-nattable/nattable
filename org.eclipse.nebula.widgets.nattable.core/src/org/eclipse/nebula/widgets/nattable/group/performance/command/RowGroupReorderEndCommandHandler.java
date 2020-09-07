/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;

/**
 * Command handler for the {@link RowGroupReorderEndCommand}. Needed for
 * reordering via drag mode.
 *
 * @see RowGroupReorderStartCommandHandler
 *
 * @since 1.6
 */
public class RowGroupReorderEndCommandHandler extends AbstractLayerCommandHandler<RowGroupReorderEndCommand> {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    public RowGroupReorderEndCommandHandler(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(RowGroupReorderEndCommand command) {
        int toRowPosition =
                LayerUtil.convertRowPosition(this.rowGroupHeaderLayer, command.getRowPosition(), this.rowGroupHeaderLayer.getPositionLayer());

        // if not reorderToTopEdge we increase toRowPosition by 1
        // as the following processing is calculating the reorderToTopEdge
        // value out of the given toRowPosition and the row count
        if (!command.isReorderToTopEdge()) {
            toRowPosition++;
        }

        return this.rowGroupHeaderLayer.reorderRowGroup(
                command.getLevel(),
                this.rowGroupHeaderLayer.getReorderFromRowPosition(),
                toRowPosition);
    }

    @Override
    public Class<RowGroupReorderEndCommand> getCommandClass() {
        return RowGroupReorderEndCommand.class;
    }

}
