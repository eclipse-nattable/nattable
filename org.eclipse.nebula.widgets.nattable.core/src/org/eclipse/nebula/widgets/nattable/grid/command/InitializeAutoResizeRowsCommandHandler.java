/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class InitializeAutoResizeRowsCommandHandler extends
        AbstractLayerCommandHandler<InitializeAutoResizeRowsCommand> {

    private SelectionLayer selectionLayer;

    public InitializeAutoResizeRowsCommandHandler(SelectionLayer selectionLayer) {
        super();
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
            initCommand.setSelectedRowPositions(this.selectionLayer
                    .getFullySelectedRowPositions());
        } else {
            initCommand.setSelectedRowPositions(new int[] { rowPosition });
        }

        // Fire command carrying the selected columns
        initCommand.getSourceLayer().doCommand(
                new AutoResizeRowsCommand(initCommand));
        return true;
    }

}
