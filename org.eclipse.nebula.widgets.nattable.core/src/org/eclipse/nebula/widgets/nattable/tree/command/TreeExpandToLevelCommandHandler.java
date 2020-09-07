/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * Command handler for the TreeExpandLevelCommand.
 * <p>
 * Will search over the whole tree structure in the associated TreeLayer to
 * identify expandable nodes and expand them one after the other.
 *
 * @see TreeLayer
 * @see TreeExpandToLevelCommand
 *
 */
public class TreeExpandToLevelCommandHandler extends AbstractLayerCommandHandler<TreeExpandToLevelCommand> {

    /**
     * The TreeLayer to which this command handler is connected.
     */
    private final TreeLayer treeLayer;

    /**
     *
     * @param treeLayer
     *            The TreeLayer to which this command handler should be
     *            connected.
     */
    public TreeExpandToLevelCommandHandler(TreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public boolean doCommand(TreeExpandToLevelCommand command) {
        if (command.getParentIndex() == null) {
            this.treeLayer.expandAllToLevel(command.getLevel());
        } else {
            this.treeLayer.expandTreeRowToLevel(command.getParentIndex(), command.getLevel());
        }
        return true;
    }

    @Override
    public Class<TreeExpandToLevelCommand> getCommandClass() {
        return TreeExpandToLevelCommand.class;
    }

}
