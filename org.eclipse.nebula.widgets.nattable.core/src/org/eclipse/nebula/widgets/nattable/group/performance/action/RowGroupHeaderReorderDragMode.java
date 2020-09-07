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
package org.eclipse.nebula.widgets.nattable.group.performance.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.reorder.action.RowReorderDragMode;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderStartCommand;
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
 * Default {@link IDragMode} invoked for 'left click + drag' on the row group
 * header.
 * <p>
 * It overrides the isValidTargetRowPosition() to calculate if a destination
 * position is valid for the row group to be reordered to.
 * <p>
 * Example, a row group cannot be reordered to be inside another row group.
 *
 * @since 1.6
 */
public class RowGroupHeaderReorderDragMode extends RowReorderDragMode {

    protected final RowGroupHeaderLayer rowGroupHeaderLayer;

    protected int level;
    protected int dragFromGridColumnPosition;

    /**
     *
     * @param columnGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} to which this drag mode should
     *            be assigned to.
     */
    public RowGroupHeaderReorderDragMode(RowGroupHeaderLayer columnGroupHeaderLayer) {
        this.rowGroupHeaderLayer = columnGroupHeaderLayer;
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
                        this.rowGroupHeaderLayer);

        ColumnPositionCoordinate convertedColumn =
                LayerCommandUtil.convertColumnPositionToTargetContext(
                        new ColumnPositionCoordinate(natTable, this.dragFromGridColumnPosition),
                        this.rowGroupHeaderLayer);
        calculateLevel(convertedColumn.getColumnPosition(), convertedRow.getRowPosition());

        natTable.addOverlayPainter(this.targetOverlayPainter);

        natTable.doCommand(new ClearAllSelectionsCommand());

        fireMoveStartCommand(natTable, this.dragFromGridRowPosition);
    }

    @Override
    protected boolean isValidTargetRowPosition(ILayer natLayer, int fromGridRowPosition, int toGridRowPosition) {
        // check if the reordered level supports reordering
        if (!this.rowGroupHeaderLayer.isReorderSupportedOnLevel(this.level)) {
            return false;
        }

        if (this.currentEvent != null) {
            // if this method was triggered by a mouse event, we determine the
            // to row position by the event
            // if there is no current mouse event referenced it means the
            // reorder is triggered programmatically
            CellEdgeEnum moveDirection = getMoveDirection(this.currentEvent.y);
            toGridRowPosition = getDragToGridRowPosition(
                    moveDirection,
                    this.natTable.getColumnPositionByX(this.currentEvent.y));
        }

        // the drag mode is triggered on the top most layer, e.g. the NatTable
        // itself, therefore the grid position needs to be converted to the
        // row group header, but as this one is not an IUniqueIndexLayer, we
        // need to convert directly to the corresponding position layer
        int toPosition = LayerUtil.convertRowPosition(natLayer, toGridRowPosition, this.rowGroupHeaderLayer.getPositionLayer());

        // Allow moving within the unbreakable group
        int fromPosition = this.rowGroupHeaderLayer.getReorderFromRowPosition();

        if (this.level >= 0) {
            for (int lvl = (this.level + 1); lvl < this.rowGroupHeaderLayer.getLevelCount(); lvl++) {
                GroupModel model = this.rowGroupHeaderLayer.getGroupModel(lvl);
                if (model.isPartOfAnUnbreakableGroup(fromPosition)) {
                    int toCheck = toPosition;
                    if (toPosition < 0 && toGridRowPosition == natLayer.getColumnCount()) {
                        toCheck = LayerUtil.convertRowPosition(natLayer, toGridRowPosition - 1, this.rowGroupHeaderLayer.getPositionLayer());
                    } else {
                        MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromPosition, toCheck);
                        toCheck = MoveDirectionEnum.DOWN == moveDirection ? toCheck - 1 : toCheck;
                    }
                    return RowGroupUtils.isInTheSameGroup(
                            this.rowGroupHeaderLayer,
                            lvl, fromPosition,
                            toCheck);
                }
            }

            // ensure that the target position is valid on every level above
            for (int lvl = (this.level + 1); lvl < this.rowGroupHeaderLayer.getLevelCount(); lvl++) {
                GroupModel model = this.rowGroupHeaderLayer.getGroupModel(lvl);

                boolean betweenTwoGroups = false;
                if (this.currentEvent != null) {
                    int minX = this.currentEvent.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                    int maxX = this.currentEvent.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
                    betweenTwoGroups = RowGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, this.rowGroupHeaderLayer, lvl);
                }

                if (!betweenTwoGroups) {
                    if (model.isPartOfAnUnbreakableGroup(toPosition)) {
                        return false;
                    }
                }
            }

            return RowGroupUtils.isBetweenTwoGroups(
                    this.rowGroupHeaderLayer,
                    this.level,
                    toPosition,
                    toPosition < this.rowGroupHeaderLayer.getColumnCount(),
                    PositionUtil.getVerticalMoveDirection(fromGridRowPosition, toGridRowPosition));
        } else {
            // we perform row reorder
            // ensure that the target position is valid on every level
            for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
                if (!isValidTargetRowPosition(natLayer, fromGridRowPosition, toGridRowPosition, level, fromPosition, toPosition)) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Test if the reorder is valid for the given level.
     *
     * @param natLayer
     *            The layer on which the drag operation is triggered, typically
     *            the NatTable instance.
     * @param fromGridRowPosition
     *            The from position related to the given natLayer.
     * @param toGridRowPosition
     *            The to position related to the given natLayer.
     * @param level
     *            The grouping level for which the check should be performed.
     * @param fromPosition
     *            The from position related to the positionLayer of the
     *            {@link RowGroupHeaderLayer}.
     * @param toPosition
     *            The to position related to the positionLayer of the
     *            {@link RowGroupHeaderLayer}.
     * @return <code>true</code> if the reorder would be valid on the specified
     *         level, <code>false</code> if not.
     */
    protected boolean isValidTargetRowPosition(
            ILayer natLayer, int fromGridRowPosition, int toGridRowPosition,
            int level, int fromPosition, int toPosition) {

        GroupModel model = this.rowGroupHeaderLayer.getGroupModel(level);

        // check only in case the group is an unbreakable group or the group
        // contains only one column, as reordering a column in such a group
        // is like reordering the whole group and does not break the group
        if (model.isPartOfAnUnbreakableGroup(fromPosition)
                && model.getGroupByPosition(fromPosition).getOriginalSpan() > 1) {
            int toCheck = toPosition;
            if (toPosition < 0 && toGridRowPosition == natLayer.getColumnCount()) {
                toCheck = LayerUtil.convertRowPosition(natLayer, toGridRowPosition - 1, this.rowGroupHeaderLayer.getPositionLayer());
            } else {
                MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromPosition, toCheck);
                toCheck = MoveDirectionEnum.DOWN == moveDirection ? toCheck - 1 : toCheck;
            }

            // Allow moving within the unbreakable group
            return RowGroupUtils.isInTheSameGroup(
                    this.rowGroupHeaderLayer,
                    level, fromPosition,
                    toCheck);
        }

        boolean betweenTwoGroups = false;
        if (this.currentEvent != null) {
            int minX = this.currentEvent.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
            int maxX = this.currentEvent.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
            betweenTwoGroups = RowGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, this.rowGroupHeaderLayer, level);
        }

        if (!betweenTwoGroups) {
            if (model.isPartOfAnUnbreakableGroup(toPosition)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void fireMoveStartCommand(NatTable natTable, int dragFromGridRowPosition) {
        if (this.level >= 0) {
            natTable.doCommand(new RowGroupReorderStartCommand(natTable, this.level, dragFromGridRowPosition));
        } else {
            natTable.doCommand(new RowReorderStartCommand(natTable, dragFromGridRowPosition));
        }
    }

    @Override
    protected void fireMoveEndCommand(NatTable natTable, int dragToGridRowPosition) {
        if (this.level >= 0) {
            natTable.doCommand(new RowGroupReorderEndCommand(natTable, this.level, dragToGridRowPosition));
        } else {
            natTable.doCommand(new RowReorderEndCommand(natTable, dragToGridRowPosition));
        }
    }

    @Override
    protected CellEdgeEnum getMoveDirection(int y) {
        ILayerCell cell = getRowCell(y);
        if (cell != null) {
            Rectangle selectedRowHeaderRect = cell.getBounds();
            return CellEdgeDetectUtil.getVerticalCellEdge(
                    selectedRowHeaderRect,
                    new Point(this.natTable.getStartXOfColumnPosition(this.dragFromGridColumnPosition), y));
        }

        return null;
    }

    @Override
    protected ILayerCell getRowCell(int y) {
        int gridRowPosition = this.natTable.getRowPositionByY(y);
        return this.natTable.getCellByPosition(this.dragFromGridColumnPosition, gridRowPosition);
    }

    /**
     * Calculate the group level and based on that the real drag from grid
     * column position that is reordered. Needed in case there is no group at
     * the coordinate level and therefore a spanning indicates a group at a
     * lower level.
     *
     * @param columnPosition
     *            The column position from which the drag was started. Needs to
     *            be related to the columnGroupHeaderLayer.
     * @param rowPosition
     *            The row position from which the drag was started. Needed to
     *            check if there is a group at the calculated level. Needs to be
     *            related to the columnGroupHeaderLayer.
     */
    protected void calculateLevel(int columnPosition, int rowPosition) {
        this.level = this.rowGroupHeaderLayer.getLevelForColumnPosition(columnPosition);

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(this.level, rowPosition);
        while (group == null && this.level > 0) {
            // decrease the level and increase the from column position as we
            // need
            // to check one column below
            this.level--;
            this.dragFromGridColumnPosition++;
            group = this.rowGroupHeaderLayer.getGroupByPosition(this.level, rowPosition);
        }

        if (group == null) {
            // no group found, so we set the level to -1 and trigger row
            // reordering instead of row group reordering in further steps
            this.level = -1;
            this.dragFromGridColumnPosition = this.rowGroupHeaderLayer.getColumnCount() - 1;
        }
    }
}
