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
package org.eclipse.nebula.widgets.nattable.group.performance.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the column group
 * header.
 * <p>
 * It overrides the isValidTargetColumnPosition() to calculate if a destination
 * position is valid for the column group to be reordered to.
 * <p>
 * Example, a column group cannot be reordered to be inside another column
 * group.
 *
 * @since 1.6
 */
public class ColumnGroupHeaderReorderDragMode extends ColumnReorderDragMode {

    protected final ColumnGroupHeaderLayer columnGroupHeaderLayer;

    protected int level;

    /**
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to which this drag mode
     *            should be assigned to.
     */
    public ColumnGroupHeaderReorderDragMode(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        this.natTable = natTable;
        this.initialEvent = event;
        this.currentEvent = this.initialEvent;
        this.dragFromGridColumnPosition = this.natTable.getColumnPositionByX(this.initialEvent.x);

        int dragFromGridRowPosition = this.natTable.getRowPositionByY(this.initialEvent.y);
        RowPositionCoordinate convertedRow =
                LayerCommandUtil.convertRowPositionToTargetContext(
                        new RowPositionCoordinate(natTable, dragFromGridRowPosition),
                        this.columnGroupHeaderLayer);
        this.level = this.columnGroupHeaderLayer.getLevelForRowPosition(convertedRow.getRowPosition());

        natTable.addOverlayPainter(this.targetOverlayPainter);

        natTable.doCommand(new ClearAllSelectionsCommand());

        fireMoveStartCommand(natTable, this.dragFromGridColumnPosition);
    }

    /**
     * Work off the event coordinates since the drag
     * {@link ColumnReorderDragMode} adjusts the 'to' column positions (for on
     * screen semantics)
     */
    @Override
    protected boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
        if (this.currentEvent != null) {
            // if this method was triggered by a mouse event, we determine the
            // to column position by the event
            // if there is no current mouse event referenced it means the
            // reorder is triggered programmatically
            toGridColumnPosition = natLayer.getColumnPositionByX(this.currentEvent.x);
        }

        // the drag mode is triggered on the top most layer, e.g. the NatTable
        // itself, therefore the grid position needs to be converted to the
        // column group header, but as this one is not an IUniqueIndexLayer, we
        // need to convert directly to the corresponding position layer
        int toPosition = LayerUtil.convertColumnPosition(natLayer, toGridColumnPosition, this.columnGroupHeaderLayer.getPositionLayer());

        // if reordered to the beginning or the start, the position is valid
        if (toPosition == 0 || toPosition == this.columnGroupHeaderLayer.getPositionLayer().getColumnCount() - 1) {
            return true;
        }

        // ensure that the target position is valid on every level
        for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
            GroupModel model = this.columnGroupHeaderLayer.getGroupModel(level);

            boolean betweenTwoGroups = false;
            if (this.currentEvent != null) {
                int minX = this.currentEvent.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                int maxX = this.currentEvent.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                betweenTwoGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, this.columnGroupHeaderLayer, level);
            }

            if (!betweenTwoGroups) {
                if (model.isPartOfAnUnbreakableGroup(toPosition)) {
                    return false;
                }
            }
        }

        return true;

    }

    @Override
    protected void fireMoveStartCommand(NatTable natTable, int dragFromGridColumnPosition) {
        natTable.doCommand(new ColumnGroupReorderStartCommand(natTable, dragFromGridColumnPosition, this.level));
    }

    @Override
    protected void fireMoveEndCommand(NatTable natTable, int dragToGridColumnPosition) {
        natTable.doCommand(new ColumnGroupReorderEndCommand(natTable, dragToGridColumnPosition, this.level));
    }
}
