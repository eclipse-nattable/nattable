/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;

public class ReorderColumnsAndGroupsCommandHandler extends
        AbstractLayerCommandHandler<ReorderColumnsAndGroupsCommand> {

    private final ColumnGroupReorderLayer columnGroupReorderLayer;

    public ReorderColumnsAndGroupsCommandHandler(
            ColumnGroupReorderLayer columnGroupReorderLayer) {
        this.columnGroupReorderLayer = columnGroupReorderLayer;
    }

    @Override
    public Class<ReorderColumnsAndGroupsCommand> getCommandClass() {
        return ReorderColumnsAndGroupsCommand.class;
    }

    /**
     * Check if any column belongs to a group. If yes, add all columns in that
     * group. Assumes that the 'toLocation' is not inside another group
     */
    @Override
    protected boolean doCommand(ReorderColumnsAndGroupsCommand command) {
        final ILayer underlyingLayer = this.columnGroupReorderLayer
                .getUnderlyingLayer();
        List<String> groupsProcessed = new ArrayList<String>();

        List<Integer> fromColumnPositions = command.getFromColumnPositions();
        List<Integer> fromColumnPositionsWithGroupColumns = new ArrayList<Integer>();

        for (Integer fromColumnPosition : fromColumnPositions) {
            int fromColumnIndex = underlyingLayer
                    .getColumnIndexByPosition(fromColumnPosition.intValue());

            ColumnGroupModel model = this.columnGroupReorderLayer.getModel();
            if (model.isPartOfAGroup(fromColumnIndex)) {
                String groupName = model.getColumnGroupByIndex(fromColumnIndex)
                        .getName();
                if (!groupsProcessed.contains(groupName)) {
                    groupsProcessed.add(groupName);
                    fromColumnPositionsWithGroupColumns
                            .addAll(this.columnGroupReorderLayer
                                    .getColumnGroupPositions(fromColumnIndex));
                }
            } else {
                fromColumnPositionsWithGroupColumns.add(fromColumnPosition);
            }
        }

        return underlyingLayer.doCommand(new MultiColumnReorderCommand(
                this.columnGroupReorderLayer, fromColumnPositionsWithGroupColumns,
                command.getToColumnPosition(), command.isReorderToLeftEdge()));
    }

}
