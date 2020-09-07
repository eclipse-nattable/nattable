/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

/**
 * Command handler to handle the {@link RowHeightResetCommand}.
 *
 * @since 1.6
 */
public class RowHeightResetCommandHandler extends AbstractLayerCommandHandler<RowHeightResetCommand> {

    private final DataLayer dataLayer;

    public RowHeightResetCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<RowHeightResetCommand> getCommandClass() {
        return RowHeightResetCommand.class;
    }

    @Override
    protected boolean doCommand(RowHeightResetCommand command) {
        this.dataLayer.resetRowHeightConfiguration(command.fireEvent);
        return false;
    }

}
