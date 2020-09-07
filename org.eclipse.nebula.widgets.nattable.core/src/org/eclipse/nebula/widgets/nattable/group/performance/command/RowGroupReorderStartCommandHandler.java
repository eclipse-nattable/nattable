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
 * Command handler for the {@link RowGroupReorderStartCommand}. Needed for
 * reordering via drag mode for setting the from position. This is necessary as
 * on drag the viewport could scroll and therefore the real from position could
 * not be determined anymore.
 *
 * @see RowGroupReorderEndCommandHandler
 *
 * @since 1.6
 */
public class RowGroupReorderStartCommandHandler extends AbstractLayerCommandHandler<RowGroupReorderStartCommand> {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    public RowGroupReorderStartCommandHandler(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(RowGroupReorderStartCommand command) {
        int fromRowPosition = command.getRowPosition();

        this.rowGroupHeaderLayer.setReorderFromRowPosition(
                LayerUtil.convertRowPosition(this.rowGroupHeaderLayer, fromRowPosition, this.rowGroupHeaderLayer.getPositionLayer()));

        return true;
    }

    @Override
    public Class<RowGroupReorderStartCommand> getCommandClass() {
        return RowGroupReorderStartCommand.class;
    }

}
