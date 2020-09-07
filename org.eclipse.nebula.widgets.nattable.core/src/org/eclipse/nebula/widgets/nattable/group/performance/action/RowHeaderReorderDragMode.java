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
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.RowGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.reorder.action.RowReorderDragMode;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Extends the regular row drag functionality to work with row groups. It does
 * the following checks:
 * <ol>
 * <li>Checks that the destination is not part of a Unbreakable row group</li>
 * <li>Checks if the destination is between two adjoining row groups</li>
 * </ol>
 *
 * @since 1.6
 */
public class RowHeaderReorderDragMode extends RowReorderDragMode {

    private final RowGroupHeaderLayer rowGroupHeaderLayer;

    protected int dragFromGridColumnPosition;

    /**
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} to which this drag mode should
     *            be assigned to.
     */
    public RowHeaderReorderDragMode(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        super.mouseDown(natTable, event);

        ILayerCell cell = natTable.getCellByPosition(
                natTable.getColumnPositionByX(this.initialEvent.x),
                natTable.getRowPositionByY(this.initialEvent.y));
        if (cell != null) {
            this.dragFromGridColumnPosition = cell.getOriginColumnPosition() + cell.getColumnSpan() - 1;
        } else {
            this.dragFromGridColumnPosition = -1;
        }
    }

    @Override
    public boolean isValidTargetRowPosition(ILayer natLayer, int fromGridRowPosition, int toGridRowPosition) {
        if (this.currentEvent != null) {
            // if this method was triggered by a mouse event, we determine the
            // to row position by the event
            // if there is no current mouse event referenced it means the
            // reorder is triggered programmatically
            CellEdgeEnum moveDirection = getMoveDirection(this.currentEvent.y);
            toGridRowPosition = getDragToGridRowPosition(
                    moveDirection,
                    this.natTable.getRowPositionByY(this.currentEvent.y));
        }

        // the drag mode is triggered on the top most layer, e.g. the NatTable
        // itself, therefore the grid position needs to be converted to the
        // row group header, but as this one is not an IUniqueIndexLayer, we
        // need to convert directly to the corresponding position layer
        int toPosition = LayerUtil.convertRowPosition(natLayer, toGridRowPosition, this.rowGroupHeaderLayer.getPositionLayer());

        int fromPosition = this.rowGroupHeaderLayer.getReorderFromRowPosition();

        // ensure that the target position is valid on every level
        for (int level = 0; level < this.rowGroupHeaderLayer.getLevelCount(); level++) {
            if (!isValidTargetRowPosition(natLayer, fromGridRowPosition, toGridRowPosition, level, fromPosition, toPosition)) {
                return false;
            }
        }

        return true;
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
}
