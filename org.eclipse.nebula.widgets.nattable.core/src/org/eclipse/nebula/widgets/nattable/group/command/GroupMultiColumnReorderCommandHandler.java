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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460052
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;

public class GroupMultiColumnReorderCommandHandler extends AbstractLayerCommandHandler<MultiColumnReorderCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public GroupMultiColumnReorderCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
        this.columnGroupReorderLayer = columnGroupReorderLayer;
    }

    @Override
    public Class<MultiColumnReorderCommand> getCommandClass() {
        return MultiColumnReorderCommand.class;
    }

    @Override
    protected boolean doCommand(MultiColumnReorderCommand command) {
        int toColumnPosition = command.getToColumnPosition();

        List<Integer> fromColumnPositions = command.getFromColumnPositions();

        return this.columnGroupReorderLayer.updateColumnGroupModel(
                fromColumnPositions, toColumnPosition, command.isReorderToLeftEdge());
    }
}
