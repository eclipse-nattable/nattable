/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommand;

/**
 * Command handler for the TreeExpandToLevelCommand.
 *
 * @see HierarchicalTreeLayer
 * @see TreeExpandToLevelCommand
 *
 * @since 1.6
 */
public class HierarchicalTreeExpandToLevelCommandHandler extends AbstractLayerCommandHandler<TreeExpandToLevelCommand> {

    /**
     * The HierarchicalTreeLayer to which this command handler is connected.
     */
    private final HierarchicalTreeLayer treeLayer;

    /**
     *
     * @param treeLayer
     *            The TreeLayer to which this command handler should be
     *            connected.
     */
    public HierarchicalTreeExpandToLevelCommandHandler(HierarchicalTreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public boolean doCommand(TreeExpandToLevelCommand command) {
        if (command.getParentIndex() == null) {
            this.treeLayer.expandAllToLevel(command.getLevel());
        } else {
            // TODO extend the command to accept also a column index
            this.treeLayer.expandOrCollapse(0, command.getParentIndex(), command.getLevel());
        }
        return true;
    }

    @Override
    public Class<TreeExpandToLevelCommand> getCommandClass() {
        return TreeExpandToLevelCommand.class;
    }

}