/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * Command to expand all nodes to a specified level in a tree.
 *
 * @see TreeLayer
 * @see TreeExpandToLevelCommandHandler
 */
public class TreeExpandToLevelCommand extends AbstractContextFreeCommand {

    private final int level;

    /**
     * Create a {@link TreeExpandToLevelCommand} that expands the nodes in a
     * tree to the given level. Nodes below the given level will not be expanded
     * and stay collapsed.
     *
     * @param level
     *            The level to which the tree should be expanded.
     */
    public TreeExpandToLevelCommand(int level) {
        this.level = level;
    }

    /**
     *
     * @return The level to which the tree should be expanded.
     */
    public int getLevel() {
        return this.level;
    }
}
