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
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

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
    protected int dragFromGridRowPosition;

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

        this.dragFromGridRowPosition = this.natTable.getRowPositionByY(this.initialEvent.y);
        RowPositionCoordinate convertedRow =
                LayerCommandUtil.convertRowPositionToTargetContext(
                        new RowPositionCoordinate(natTable, this.dragFromGridRowPosition),
                        this.columnGroupHeaderLayer);

        ColumnPositionCoordinate convertedColumn =
                LayerCommandUtil.convertColumnPositionToTargetContext(
                        new ColumnPositionCoordinate(natTable, this.dragFromGridColumnPosition),
                        this.columnGroupHeaderLayer);
        calculateLevel(convertedRow.getRowPosition(), convertedColumn.getColumnPosition());

        natTable.addOverlayPainter(this.targetOverlayPainter);

        natTable.doCommand(new ClearAllSelectionsCommand());

        fireMoveStartCommand(natTable, this.dragFromGridColumnPosition);
    }

    @Override
    protected boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
        // check if the reordered level supports reordering
        if (!this.columnGroupHeaderLayer.isReorderSupportedOnLevel(this.level)) {
            return false;
        }

        if (this.currentEvent != null) {
            // if this method was triggered by a mouse event, we determine the
            // to column position by the event
            // if there is no current mouse event referenced it means the
            // reorder is triggered programmatically
            CellEdgeEnum moveDirection = getMoveDirection(this.currentEvent.x);
            toGridColumnPosition = getDragToGridColumnPosition(
                    moveDirection,
                    this.natTable.getColumnPositionByX(this.currentEvent.x));
        }

        // the drag mode is triggered on the top most layer, e.g. the NatTable
        // itself, therefore the grid position needs to be converted to the
        // column group header, but as this one is not an IUniqueIndexLayer, we
        // need to convert directly to the corresponding position layer
        int toPosition = LayerUtil.convertColumnPosition(natLayer, toGridColumnPosition, this.columnGroupHeaderLayer.getPositionLayer());

        // Allow moving within the unbreakable group
        int fromPosition = this.columnGroupHeaderLayer.getReorderFromColumnPosition();
        for (int lvl = (this.level + 1); lvl < this.columnGroupHeaderLayer.getLevelCount(); lvl++) {
            GroupModel model = this.columnGroupHeaderLayer.getGroupModel(lvl);
            if (model.isPartOfAnUnbreakableGroup(fromPosition)) {
                int toCheck = toPosition;
                if (toPosition < 0 && toGridColumnPosition == natLayer.getColumnCount()) {
                    toCheck = LayerUtil.convertColumnPosition(natLayer, toGridColumnPosition - 1, this.columnGroupHeaderLayer.getPositionLayer());
                } else {
                    MoveDirectionEnum moveDirection = PositionUtil.getHorizontalMoveDirection(fromPosition, toCheck);
                    toCheck = MoveDirectionEnum.RIGHT == moveDirection ? toCheck - 1 : toCheck;
                }
                return ColumnGroupUtils.isInTheSameGroup(
                        this.columnGroupHeaderLayer,
                        lvl, fromPosition,
                        toCheck);
            }
        }

        // ensure that the target position is valid on every level above
        for (int lvl = (this.level + 1); lvl < this.columnGroupHeaderLayer.getLevelCount(); lvl++) {
            GroupModel model = this.columnGroupHeaderLayer.getGroupModel(lvl);

            boolean betweenTwoGroups = false;
            if (this.currentEvent != null) {
                int minX = this.currentEvent.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                int maxX = this.currentEvent.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                betweenTwoGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, this.columnGroupHeaderLayer, lvl);
            }

            if (!betweenTwoGroups) {
                if (model.isPartOfAnUnbreakableGroup(toPosition)) {
                    return false;
                }
            }
        }

        return ColumnGroupUtils.isBetweenTwoGroups(
                this.columnGroupHeaderLayer,
                this.level,
                toPosition,
                toPosition < this.columnGroupHeaderLayer.getColumnCount(),
                PositionUtil.getHorizontalMoveDirection(fromGridColumnPosition, toGridColumnPosition));
    }

    @Override
    protected void fireMoveStartCommand(NatTable natTable, int dragFromGridColumnPosition) {
        if (this.level >= 0) {
            natTable.doCommand(new ColumnGroupReorderStartCommand(natTable, this.level, dragFromGridColumnPosition));
        } else {
            natTable.doCommand(new ColumnReorderStartCommand(natTable, dragFromGridColumnPosition));
        }
    }

    @Override
    protected void fireMoveEndCommand(NatTable natTable, int dragToGridColumnPosition) {
        if (this.level >= 0) {
            natTable.doCommand(new ColumnGroupReorderEndCommand(natTable, this.level, dragToGridColumnPosition));
        } else {
            natTable.doCommand(new ColumnReorderEndCommand(natTable, dragToGridColumnPosition));
        }
    }

    @Override
    protected CellEdgeEnum getMoveDirection(int x) {
        ILayerCell cell = getColumnCell(x);
        if (cell != null) {
            Rectangle selectedColumnHeaderRect = cell.getBounds();
            return CellEdgeDetectUtil.getHorizontalCellEdge(
                    selectedColumnHeaderRect,
                    new Point(x, this.natTable.getStartYOfRowPosition(this.dragFromGridRowPosition)));
        }

        return null;
    }

    @Override
    protected ILayerCell getColumnCell(int x) {
        int gridColumnPosition = this.natTable.getColumnPositionByX(x);
        return this.natTable.getCellByPosition(gridColumnPosition, this.dragFromGridRowPosition);
    }

    /**
     * Calculate the group level and based on that the real drag from grid row
     * position that is reordered. Needed in case there is no group at the
     * coordinate level and therefore a spanning indicates a group at a lower
     * level.
     *
     * @param rowPosition
     *            The row position from which the drag was started. Needs to be
     *            related to the columnGroupHeaderLayer.
     * @param columnPosition
     *            The column position from which the drag was started. Needed to
     *            check if there is a group at the calculated level. Needs to be
     *            related to the columnGroupHeaderLayer.
     */
    protected void calculateLevel(int rowPosition, int columnPosition) {
        this.level = this.columnGroupHeaderLayer.getLevelForRowPosition(rowPosition);

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(this.level, columnPosition);
        while (group == null && this.level > 0) {
            // decrease the level and increase the from row position as we need
            // to check one row below
            this.level--;
            this.dragFromGridRowPosition++;
            group = this.columnGroupHeaderLayer.getGroupByPosition(this.level, columnPosition);
        }

        if (group == null) {
            // no group found, so we set the level to -1 and trigger column
            // reordering instead of column group reordering in further steps
            this.level = -1;
        }
    }
}
