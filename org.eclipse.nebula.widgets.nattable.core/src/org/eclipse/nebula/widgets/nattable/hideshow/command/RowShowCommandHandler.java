/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     *            The {@link IRowHideShowLayer} to which this command handler
     *            should be registered.
     * @since 2.0
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
