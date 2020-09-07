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
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class InitializeAutoResizeRowsCommandHandler extends AbstractLayerCommandHandler<InitializeAutoResizeRowsCommand> {

    private SelectionLayer selectionLayer;

    public InitializeAutoResizeRowsCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public Class<InitializeAutoResizeRowsCommand> getCommandClass() {
        return InitializeAutoResizeRowsCommand.class;
    }

    @Override
    protected boolean doCommand(InitializeAutoResizeRowsCommand initCommand) {
        int rowPosition = initCommand.getRowPosition();

        if (this.selectionLayer.isRowPositionFullySelected(rowPosition)) {
            initCommand.setSelectedRowPositions(this.selectionLayer.getFullySelectedRowPositions());
        } else {
            initCommand.setSelectedRowPositions(new int[] { rowPosition });
        }

        // Fire command carrying the selected columns
        initCommand.getSourceLayer().doCommand(new AutoResizeRowsCommand(initCommand));
        return true;
    }

}
