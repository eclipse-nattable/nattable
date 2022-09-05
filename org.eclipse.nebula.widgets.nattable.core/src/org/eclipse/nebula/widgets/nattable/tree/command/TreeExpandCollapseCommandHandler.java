/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.tree.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

public class TreeExpandCollapseCommandHandler extends AbstractLayerCommandHandler<TreeExpandCollapseCommand> {

    private final TreeLayer treeLayer;

    public TreeExpandCollapseCommandHandler(TreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public Class<TreeExpandCollapseCommand> getCommandClass() {
        return TreeExpandCollapseCommand.class;
    }

    @Override
    protected boolean doCommand(TreeExpandCollapseCommand command) {
        int parentIndex = command.getParentIndex();

        if (command.getParentIndex() < 0) {
            // if the parent index is a negative value, the tree cell is a
            // spanned cell whose origin row was scrolled out of the visible
            // area, therefore we need to find the correct parent index now
            int rowPos = this.treeLayer.getRowPositionByIndex(command.getParentIndex() * -1);
            int colPos = this.treeLayer.isUseTreeColumnIndex() ? this.treeLayer.getColumnPositionByIndex(0) : 0;

            ILayerCell cell = this.treeLayer.getCellByPosition(colPos, rowPos);
            parentIndex = this.treeLayer.getRowIndexByPosition(cell.getOriginRowPosition());
        }

        this.treeLayer.expandOrCollapseIndex(parentIndex);
        return true;
    }

}
