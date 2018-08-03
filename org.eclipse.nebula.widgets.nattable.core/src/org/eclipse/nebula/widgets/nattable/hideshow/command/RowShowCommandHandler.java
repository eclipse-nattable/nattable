/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.IRowHideShowLayer;

/**
 * CommandHandler for the {@link RowShowCommand} that shows one hidden adjacent
 * column next to the transported column position.
 *
 * @since 1.6
 */
public class RowShowCommandHandler extends AbstractLayerCommandHandler<RowShowCommand> {

    private final IRowHideShowLayer rowHideShowLayer;

    /**
     *
     * @param rowHideShowLayer
     *            The {@link IRowHideShowLayer} on which this command handler
     *            should operate.
     */
    public RowShowCommandHandler(IRowHideShowLayer rowHideShowLayer) {
        this.rowHideShowLayer = rowHideShowLayer;
    }

    @Override
    public Class<RowShowCommand> getCommandClass() {
        return RowShowCommand.class;
    }

    @Override
    protected boolean doCommand(RowShowCommand command) {
        this.rowHideShowLayer.showRowPosition(
                command.getRowPosition(),
                command.isShowTopPosition(),
                command.isShowAll());
        return true;
    }

}
