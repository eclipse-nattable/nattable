/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

public class ColumnGroupExpandCollapseCommandHandler extends
        AbstractLayerCommandHandler<ColumnGroupExpandCollapseCommand> {

    private final ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;

    public ColumnGroupExpandCollapseCommandHandler(
            ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer) {
        this.columnGroupExpandCollapseLayer = columnGroupExpandCollapseLayer;
    }

    @Override
    public Class<ColumnGroupExpandCollapseCommand> getCommandClass() {
        return ColumnGroupExpandCollapseCommand.class;
    }

    @Override
    protected boolean doCommand(ColumnGroupExpandCollapseCommand command) {

        int columnIndex = this.columnGroupExpandCollapseLayer
                .getColumnIndexByPosition(command.getColumnPosition());
        ColumnGroupModel model = this.columnGroupExpandCollapseLayer
                .getModel(command.getRowPosition());
        ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

        // if group of columnIndex is not collapseable return without any
        // further operation ...
        if (columnGroup == null || !columnGroup.isCollapseable()) {
            return true;
        }

        List<Integer> columnIndexes = new ArrayList<Integer>(
                columnGroup.getMembers());
        columnIndexes.removeAll(columnGroup.getStaticColumnIndexes());

        boolean wasCollapsed = columnGroup.isCollapsed();

        if (wasCollapsed) {
            // we need to cleanup the column position list before we toggle
            // because the columns are hidden before the toggle and will be
            // visible afterwards
            cleanupColumnIndexes(columnIndexes);
        }

        columnGroup.toggleCollapsed();

        if (!wasCollapsed) {
            // we need to cleanup the column position list after we toggle
            // because the columns are hidden now
            cleanupColumnIndexes(columnIndexes);
        }

        ILayerEvent event;
        if (wasCollapsed) {
            event = new ShowColumnPositionsEvent(
                    this.columnGroupExpandCollapseLayer, columnIndexes);
        } else {
            event = new HideColumnPositionsEvent(
                    this.columnGroupExpandCollapseLayer, columnIndexes);
        }

        this.columnGroupExpandCollapseLayer.fireLayerEvent(event);

        return true;
    }

    /**
     * Will clean up the given list of column indexes for a column group, so
     * only those column indexes will stay in the list that are relevant for the
     * hide/show column events.
     *
     * @param columnIndexes
     *            The column indexes to cleanup.
     */
    private void cleanupColumnIndexes(List<Integer> columnIndexes) {
        for (Iterator<Integer> it = columnIndexes.iterator(); it.hasNext();) {
            Integer columnIndex = it.next();

            if (!this.columnGroupExpandCollapseLayer
                    .isColumnIndexHidden(columnIndex)) {
                it.remove();
            }
        }
    }
}
