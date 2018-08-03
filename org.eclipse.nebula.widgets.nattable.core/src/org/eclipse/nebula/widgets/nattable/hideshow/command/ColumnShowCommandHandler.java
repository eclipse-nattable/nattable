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
import org.eclipse.nebula.widgets.nattable.hideshow.IColumnHideShowLayer;

/**
 * CommandHandler for the {@link ColumnShowCommand} that shows one hidden
 * adjacent column next to the transported column position.
 *
 * @since 1.6
 */
public class ColumnShowCommandHandler extends AbstractLayerCommandHandler<ColumnShowCommand> {

    private final IColumnHideShowLayer columnHideShowLayer;

    /**
     *
     * @param columnHideShowLayer
     *            The {@link IColumnHideShowLayer} on which this command handler
     *            should operate.
     */
    public ColumnShowCommandHandler(IColumnHideShowLayer columnHideShowLayer) {
        this.columnHideShowLayer = columnHideShowLayer;
    }

    @Override
    public Class<ColumnShowCommand> getCommandClass() {
        return ColumnShowCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnShowCommand command) {
        this.columnHideShowLayer.showColumnPosition(
                command.getColumnPosition(),
                command.isShowLeftPosition(),
                command.isShowAll());
        return true;
    }

}
