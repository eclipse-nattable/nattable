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
 * Command handler to handle the {@link RowSizeResetCommand}.
 *
 * @since 1.6
 */
public class RowSizeResetCommandHandler extends AbstractLayerCommandHandler<RowSizeResetCommand> {

    private final DataLayer dataLayer;

    public RowSizeResetCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<RowSizeResetCommand> getCommandClass() {
        return RowSizeResetCommand.class;
    }

    @Override
    protected boolean doCommand(RowSizeResetCommand command) {
        this.dataLayer.resetRowSizeConfiguration(command.fireEvent);
        return true;
    }

}
