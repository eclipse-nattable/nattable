/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to end a column group reordering via drag mode.
 * <p>
 * The command does not inherit the ColumnReorderEndCommand as then it would be
 * consumed first by the ColumnReorderLayer in the body layer stack and would
 * not come to the column header layer stack.
 *
 * @since 1.6
 */
public class ColumnGroupReorderEndCommand extends AbstractColumnCommand {

    private int level;
    private boolean reorderToLeftEdge;

    /**
     *
     * @param layer
     *            The layer to which the position matches.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param toColumnPosition
     *            The column position to which the reorder should be performed
     *            on drag end.
     */
    public ColumnGroupReorderEndCommand(ILayer layer, int level, int toColumnPosition) {
        super(layer, toColumnPosition < layer.getColumnCount() ? toColumnPosition : (toColumnPosition - 1));
        this.level = level;

        if (toColumnPosition < layer.getColumnCount()) {
            this.reorderToLeftEdge = true;
        } else {
            this.reorderToLeftEdge = false;
        }
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ColumnGroupReorderEndCommand(ColumnGroupReorderEndCommand command) {
        super(command);
        this.level = command.level;
        this.reorderToLeftEdge = command.reorderToLeftEdge;
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
     * @return <code>true</code> if the reorder should be performed to the left
     *         edge of the to position, <code>false</code> if the reorder should
     *         happen to the right edge, e.g. on reordering to the end of the
     *         table.
     */
    public boolean isReorderToLeftEdge() {
        return this.reorderToLeftEdge;
    }

    @Override
    public ColumnGroupReorderEndCommand cloneCommand() {
        return new ColumnGroupReorderEndCommand(this);
    }

}
