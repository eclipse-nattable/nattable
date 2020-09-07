/*******************************************************************************
 * Copyright (c) 2015, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;

/**
 * @since 1.3
 */
public class GroupColumnReorderStartCommandHandler extends AbstractLayerCommandHandler<ColumnReorderStartCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public GroupColumnReorderStartCommandHandler(ColumnGroupReorderLayer columnReorderLayer) {
        this.columnGroupReorderLayer = columnReorderLayer;
    }

    @Override
    public Class<ColumnReorderStartCommand> getCommandClass() {
        return ColumnReorderStartCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnReorderStartCommand command) {
        int fromColumnPosition = command.getFromColumnPosition();

        this.columnGroupReorderLayer.setReorderFromColumnPosition(fromColumnPosition);

        // we need to remember the from position in order to update the
        // ColumnGroupModel
        // the command shouldn't get consumed here, since the reordering needs
        // to take place in the ColumnReorderLayer
        return false;
    }

}
