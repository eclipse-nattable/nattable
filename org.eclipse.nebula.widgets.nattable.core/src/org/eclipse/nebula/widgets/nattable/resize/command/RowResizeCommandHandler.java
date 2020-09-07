/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

public class RowResizeCommandHandler extends AbstractLayerCommandHandler<RowResizeCommand> {

    private final DataLayer dataLayer;

    public RowResizeCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<RowResizeCommand> getCommandClass() {
        return RowResizeCommand.class;
    }

    @Override
    protected boolean doCommand(RowResizeCommand command) {
        int newRowHeight = command.downScaleValue()
                ? this.dataLayer.downScaleRowHeight(command.getNewHeight())
                : command.getNewHeight();
        this.dataLayer.setRowHeightByPosition(command.getRowPosition(), newRowHeight);
        return true;
    }

}
