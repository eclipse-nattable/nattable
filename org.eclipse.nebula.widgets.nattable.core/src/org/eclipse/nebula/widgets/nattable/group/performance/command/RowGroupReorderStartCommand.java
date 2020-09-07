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
 * Command to start a row group reordering via drag mode.
 * <p>
 * The command does not inherit the RowReorderStartCommand as then it would be
 * consumed first by the RowReorderLayer in the body layer stack and would not
 * come to the row header layer stack.
 *
 * @since 1.6
 */
public class RowGroupReorderStartCommand extends AbstractRowCommand {

    private int level;

    /**
     *
     * @param layer
     *            The layer to which the position matches.
     * @param level
     *            The group level on which the group reorder should be
     *            performed.
     * @param fromRowPosition
     *            The row position from which the reorder is started via drag.
     */
    public RowGroupReorderStartCommand(ILayer layer, int level, int fromRowPosition) {
        super(layer, fromRowPosition);
        this.level = level;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowGroupReorderStartCommand(RowGroupReorderStartCommand command) {
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
    public RowGroupReorderStartCommand cloneCommand() {
        return new RowGroupReorderStartCommand(this);
    }

}
