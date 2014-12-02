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
package org.eclipse.nebula.widgets.nattable.group.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupEndCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupStartCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the column group
 * header.
 * <p>
 * It overrides the isValidTargetColumnPosition() to calculate if a destination
 * position is valid for the column group to be reordered to.
 * <p>
 * Example, a column group cannot only be reordered to be inside another column
 * group.
 * <p>
 * See ColumnGroupHeaderReorderDragModeTest
 */
public class ColumnGroupHeaderReorderDragMode extends ColumnReorderDragMode {

    private final ColumnGroupModel model;

    public ColumnGroupHeaderReorderDragMode(ColumnGroupModel model) {
        this.model = model;
    }

    /**
     * Work off the event coordinates since the drag
     * {@link ColumnReorderDragMode} adjusts the 'to' column positions (for on
     * screen semantics)
     */
    @Override
    protected boolean isValidTargetColumnPosition(ILayer natLayer,
            int fromGridColumnPosition, int toGridColumnPosition) {
        if (this.currentEvent != null) {
            // if this method was triggered by a mouse event, we determine the
            // to column position by the event
            // if there is no current mouse event referenced it means the
            // reorder is triggered programmatically
            toGridColumnPosition = natLayer
                    .getColumnPositionByX(this.currentEvent.x);
        }
        int toColumnIndex = natLayer
                .getColumnIndexByPosition(toGridColumnPosition);

        boolean betweenGroups = false;
        if (this.currentEvent != null) {
            int minX = this.currentEvent.x
                    - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
            int maxX = this.currentEvent.x
                    + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
            betweenGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX,
                    maxX, this.model);
        }

        return toColumnIndex == 0 || (!this.model.isPartOfAGroup(toColumnIndex))
                || betweenGroups;
    }

    @Override
    protected void fireMoveStartCommand(NatTable natTable,
            int dragFromGridColumnPosition) {
        natTable.doCommand(new ReorderColumnGroupStartCommand(natTable,
                dragFromGridColumnPosition));
    }

    @Override
    protected void fireMoveEndCommand(NatTable natTable,
            int dragToGridColumnPosition) {
        natTable.doCommand(new ReorderColumnGroupEndCommand(natTable,
                dragToGridColumnPosition));
    }
}
