/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

/**
 * Command handler to handle the {@link ColumnSizeResetCommand}.
 *
 * @since 1.6
 */
public class ColumnSizeResetCommandHandler extends AbstractLayerCommandHandler<ColumnSizeResetCommand> {

    private final DataLayer dataLayer;

    public ColumnSizeResetCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<ColumnSizeResetCommand> getCommandClass() {
        return ColumnSizeResetCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnSizeResetCommand command) {
        this.dataLayer.resetColumnSizeConfiguration(command.fireEvent);
        return true;
    }

}
