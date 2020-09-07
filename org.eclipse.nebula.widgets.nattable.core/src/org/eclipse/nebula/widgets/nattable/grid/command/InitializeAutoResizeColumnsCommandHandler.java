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
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class InitializeAutoResizeColumnsCommandHandler extends AbstractLayerCommandHandler<InitializeAutoResizeColumnsCommand> {

    private SelectionLayer selectionLayer;

    public InitializeAutoResizeColumnsCommandHandler(SelectionLayer selectionLayer) {
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
            initCommand.setSelectedColumnPositions(this.selectionLayer.getFullySelectedColumnPositions());
        } else {
            initCommand.setSelectedColumnPositions(new int[] { columnPosition });
        }

        // Fire command carrying the selected columns
        initCommand.getSourceLayer().doCommand(new AutoResizeColumnsCommand(initCommand));
        return true;
    }

}
