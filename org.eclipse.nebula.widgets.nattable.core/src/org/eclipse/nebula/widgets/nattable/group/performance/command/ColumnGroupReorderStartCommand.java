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
 * Command to start a column group reordering via drag mode.
 * <p>
 * The command does not inherit the ColumnReorderStartCommand as then it would
 * be consumed first by the ColumnReorderLayer in the body layer stack and would
 * not come to the column header layer stack.
 *
 * @since 1.6
 */
public class ColumnGroupReorderStartCommand extends AbstractColumnCommand {

    private int level;

    /**
     *
     * @param layer
     *            The layer to which the position matches.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromColumnPosition
     *            The column position from which the reorder is started via
     *            drag.
     */
    public ColumnGroupReorderStartCommand(ILayer layer, int level, int fromColumnPosition) {
        super(layer, fromColumnPosition);
        this.level = level;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ColumnGroupReorderStartCommand(ColumnGroupReorderStartCommand command) {
        super(command);
        this.level = command.level;
    }

    /**
     *
     * @return The group level on which the group reorder should be performed.
     */
    public int getLevel() {
        return this.level;
    }

    @Override
    public ColumnGroupReorderStartCommand cloneCommand() {
        return new ColumnGroupReorderStartCommand(this);
    }

}
