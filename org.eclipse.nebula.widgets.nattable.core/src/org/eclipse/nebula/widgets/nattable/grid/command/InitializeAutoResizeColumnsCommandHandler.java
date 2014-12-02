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
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class InitializeAutoResizeColumnsCommandHandler extends
        AbstractLayerCommandHandler<InitializeAutoResizeColumnsCommand> {

    private SelectionLayer selectionLayer;

    public InitializeAutoResizeColumnsCommandHandler(
            SelectionLayer selectionLayer) {
        super();
        this.selectionLayer = selectionLayer;
    }

    @Override
    public Class<InitializeAutoResizeColumnsCommand> getCommandClass() {
        return InitializeAutoResizeColumnsCommand.class;
    }

    @Override
    protected boolean doCommand(InitializeAutoResizeColumnsCommand initCommand) {
        int columnPosition = initCommand.getColumnPosition();
        if (this.selectionLayer.isColumnPositionFullySelected(columnPosition)) {
            initCommand.setSelectedColumnPositions(this.selectionLayer
                    .getFullySelectedColumnPositions());
        } else {
            initCommand
                    .setSelectedColumnPositions(new int[] { columnPosition });
        }

        // Fire command carrying the selected columns
        initCommand.getSourceLayer().doCommand(
                new AutoResizeColumnsCommand(initCommand));
        return true;
    }

}
