/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;

/**
 * Command handler for the TreeExpandCollapseCommand and the
 * HierarchicalTreeExpandCollapseCommand.
 *
 * @see HierarchicalTreeLayer
 * @see TreeExpandCollapseCommand
 * @see HierarchicalTreeExpandCollapseCommand
 *
 * @since 1.6
 */
public class HierarchicalTreeExpandCollapseCommandHandler extends AbstractLayerCommandHandler<TreeExpandCollapseCommand> {

    /**
     * The HierarchicalTreeLayer to which this command handler is connected.
     */
    private final HierarchicalTreeLayer treeLayer;

    /**
     *
     * @param treeLayer
     *            The HierarchicalTreeLayer to which this command handler should
     *            be connected.
     */
    public HierarchicalTreeExpandCollapseCommandHandler(HierarchicalTreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public Class<TreeExpandCollapseCommand> getCommandClass() {
        return TreeExpandCollapseCommand.class;
    }

    @Override
    protected boolean doCommand(TreeExpandCollapseCommand command) {
        if (command instanceof HierarchicalTreeExpandCollapseCommand) {
            this.treeLayer.expandOrCollapse(
                    command.getColumnIndex(),
                    command.getParentIndex(),
                    ((HierarchicalTreeExpandCollapseCommand) command).getToLevel());
        } else {
            this.treeLayer.expandOrCollapse(
                    command.getColumnIndex(),
                    command.getParentIndex());
        }
        return true;
    }

}
