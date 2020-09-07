/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to end a row group reordering via drag mode.
 * <p>
 * The command does not inherit the RowReorderEndCommand as then it would be
 * consumed first by the RowReorderLayer in the body layer stack and would not
 * come to the row header layer stack.
 *
 * @since 1.6
 */
public class RowGroupReorderEndCommand extends AbstractRowCommand {

    private int level;
    private boolean reorderToTopEdge;

    /**
     *
     * @param layer
     *            The layer to which the position matches.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param toRowPosition
     *            The row position to which the reorder should be performed on
     *            drag end.
     */
    public RowGroupReorderEndCommand(ILayer layer, int level, int toRowPosition) {
        super(layer, toRowPosition < layer.getRowCount() ? toRowPosition : (toRowPosition - 1));
        this.level = level;

        if (toRowPosition < layer.getRowCount()) {
            this.reorderToTopEdge = true;
        } else {
            this.reorderToTopEdge = false;
        }
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowGroupReorderEndCommand(RowGroupReorderEndCommand command) {
        super(command);
        this.level = command.level;
        this.reorderToTopEdge = command.reorderToTopEdge;
    }

    /**
     *
     * @return The group level on which the group reorder should be performed.
     */
    public int getLevel() {
        return this.level;
    }

    /**
     *
     * @return <code>true</code> if the reorder should be performed to the top
     *         edge of the to position, <code>false</code> if the reorder should
     *         happen to the bottom edge, e.g. on reordering to the end of the
     *         table.
     */
    public boolean isReorderToTopEdge() {
        return this.reorderToTopEdge;
    }

    @Override
    public RowGroupReorderEndCommand cloneCommand() {
        return new RowGroupReorderEndCommand(this);
    }

}
