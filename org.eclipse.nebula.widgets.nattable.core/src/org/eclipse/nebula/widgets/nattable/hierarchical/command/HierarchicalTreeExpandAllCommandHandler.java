/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
