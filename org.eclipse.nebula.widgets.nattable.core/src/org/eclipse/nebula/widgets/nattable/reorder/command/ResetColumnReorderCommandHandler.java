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
