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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;

/**
 * Command handler for the {@link ResetColumnReorderCommand} to reset the column
 * reordering on the {@link ColumnReorderLayer}.
 *
 * @since 1.6
 */
public class ResetColumnReorderCommandHandler extends AbstractLayerCommandHandler<ResetColumnReorderCommand> {

    private final ColumnReorderLayer columnReorderLayer;

    public ResetColumnReorderCommandHandler(ColumnReorderLayer columnReorderLayer) {
        this.columnReorderLayer = columnReorderLayer;
    }

    @Override
    protected boolean doCommand(ResetColumnReorderCommand command) {
        this.columnReorderLayer.resetReorder();
        return true;
    }

    @Override
    public Class<ResetColumnReorderCommand> getCommandClass() {
        return ResetColumnReorderCommand.class;
    }

}
