/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * Command handler for the TreeExpandAllCommand.
 * <p>
 * Will search over the whole tree structure in the associated TreeLayer to
 * identify expandable nodes and expand them one after the other.
 *
 * @author Dirk Fauth
 *
 * @see TreeLayer
 * @see TreeExpandAllCommand
 *
 */
public class TreeExpandAllCommandHandler implements
        ILayerCommandHandler<TreeExpandAllCommand> {

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
    public TreeExpandAllCommandHandler(TreeLayer treeLayer) {
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
