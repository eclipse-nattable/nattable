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

import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;

/**
 * Command to expand or collapse a node in the HierarchicalTreeLayer.
 * Specialization of the {@link TreeExpandCollapseCommand} that adds the option
 * to specify to which level the node should be expanded.
 *
 * @see HierarchicalTreeLayer
 * @see HierarchicalTreeExpandCollapseCommandHandler
 *
 * @since 1.6
 */
public class HierarchicalTreeExpandCollapseCommand extends TreeExpandCollapseCommand {

    private final int toLevel;

    /**
     * Creates a command that will expand/collapse the node at the given
     * coordinates.
     *
     * @param rowIndex
     *            The row index of the coordinate that should be
     *            expanded/collapsed.
     * @param columnIndex
     *            The column index of the coordinate that should be
     *            expanded/collapsed.
     */
    public HierarchicalTreeExpandCollapseCommand(int rowIndex, int columnIndex) {
        this(rowIndex, columnIndex, -1);
    }

    /**
     * Creates a command that will expand/collapse the node at the given
     * coordinates and subsequent nodes if specified by the level parameter.
     *
     * @param rowIndex
     *            The row index of the coordinate that should be
     *            expanded/collapsed.
     * @param columnIndex
     *            The column index of the coordinate that should be
     *            expanded/collapsed.
     * @param toLevel
     *            The level to which the node should be expanded to, or -1 if
     *            only the given node should be expanded.
     */
    public HierarchicalTreeExpandCollapseCommand(int rowIndex, int columnIndex, int toLevel) {
        super(rowIndex, columnIndex);
        this.toLevel = toLevel;
    }

    /**
     *
     * @return The level to which the node should be expanded to, or -1 if only
     *         the given node should be expanded.
     */
    public int getToLevel() {
        return this.toLevel;
    }
}
