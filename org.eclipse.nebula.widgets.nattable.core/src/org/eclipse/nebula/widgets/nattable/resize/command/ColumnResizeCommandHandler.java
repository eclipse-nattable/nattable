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

public class ColumnResizeCommandHandler extends AbstractLayerCommandHandler<ColumnResizeCommand> {

    private final DataLayer dataLayer;

    public ColumnResizeCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public Class<ColumnResizeCommand> getCommandClass() {
        return ColumnResizeCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnResizeCommand command) {
        int newColumnWidth = command.downScaleValue()
                ? this.dataLayer.downScaleColumnWidth(command.getNewColumnWidth())
                : command.getNewColumnWidth();
        this.dataLayer.setColumnWidthByPosition(command.getColumnPosition(), newColumnWidth);
        return true;
    }

}
