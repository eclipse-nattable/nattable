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
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

/**
 * Command handler for the {@link ResetRowReorderCommand} to reset the column
 * reordering on the {@link RowReorderLayer}.
 *
 * @since 1.6
 */
public class ResetRowReorderCommandHandler extends AbstractLayerCommandHandler<ResetRowReorderCommand> {

    private final RowReorderLayer rowReorderLayer;

    public ResetRowReorderCommandHandler(RowReorderLayer columnReorderLayer) {
        this.rowReorderLayer = columnReorderLayer;
    }

    @Override
    protected boolean doCommand(ResetRowReorderCommand command) {
        this.rowReorderLayer.resetReorder();
        return true;
    }

    @Override
    public Class<ResetRowReorderCommand> getCommandClass() {
        return ResetRowReorderCommand.class;
    }

}
