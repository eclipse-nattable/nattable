/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.hierarchical.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;

/**
 * Command handler for the TreeExpandAllCommand.
 * <p>
 * Will search over the whole tree structure in the associated TreeLayer to
 * identify expandable nodes and expand them one after the other.
 * </p>
 *
 * @see HierarchicalTreeLayer
 * @see TreeExpandAllCommand
 *
 * @since 1.6
 */
public class HierarchicalTreeExpandAllCommandHandler implements ILayerCommandHandler<TreeExpandAllCommand> {

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
    public HierarchicalTreeExpandAllCommandHandler(HierarchicalTreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, TreeExpandAllCommand command) {
        this.treeLayer.expandAll();
        return true;
    }

    @Override
    public Class<TreeExpandAllCommand> getCommandClass() {
        return TreeExpandAllCommand.class;
    }

}
