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
package org.eclipse.nebula.widgets.nattable.tree.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
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
        this.treeLayer.expandOrCollapseIndex(parentIndex);
        return true;
    }

}
