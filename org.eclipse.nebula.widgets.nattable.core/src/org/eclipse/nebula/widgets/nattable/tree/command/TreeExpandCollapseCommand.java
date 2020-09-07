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

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * Command to expand/collapse a tree node.
 */
public class TreeExpandCollapseCommand extends AbstractContextFreeCommand {

    private final int parentIndex;
    private final int columnIndex;

    /**
     * Create a {@link TreeExpandCollapseCommand} for the given row index. By
     * using this constructor there is no information about the column that was
     * clicked. This is sufficient for the default tree implementation, but will
     * not work correctly with a hierarchical tree.
     *
     * @param parentIndex
     *            The index of the row that represents a tree node that should
     *            be expanded/collapsed.
     */
    public TreeExpandCollapseCommand(int parentIndex) {
        this.parentIndex = parentIndex;
        this.columnIndex = -1;
    }

    /**
     * Create a {@link TreeExpandCollapseCommand} for the given row and column
     * index. This constructor needs to be used for hierarchical tree
     * implementations as the tree node to expand/collapse can only be
     * identified via the combination of row and column.
     *
     * @param parentIndex
     *            The index of the row that represents a tree node that should
     *            be expanded/collapsed.
     * @param columnIndex
     *            The index of the column that represents a tree node that
     *            should be expanded/collapsed.
     * @since 1.6
     */
    public TreeExpandCollapseCommand(int parentIndex, int columnIndex) {
        this.parentIndex = parentIndex;
        this.columnIndex = columnIndex;
    }

    /**
     * Constructor used for cloning.
     *
     * @param command
     *            The command to clone.
     */
    protected TreeExpandCollapseCommand(TreeExpandCollapseCommand command) {
        this.parentIndex = command.parentIndex;
        this.columnIndex = command.columnIndex;
    }

    /**
     *
     * @return The index of the row that represents a tree node that should be
     *         expanded/collapsed.
     */
    public int getParentIndex() {
        return this.parentIndex;
    }

    /**
     *
     * @return The index of the column that represents a tree node that should
     *         be expanded/collapsed.
     * @since 1.6
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }
}
