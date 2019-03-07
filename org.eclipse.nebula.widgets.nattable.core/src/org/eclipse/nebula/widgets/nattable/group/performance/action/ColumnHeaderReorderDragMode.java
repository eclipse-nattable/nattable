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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Extends the regular column drag functionality to work with column groups. It
 * does the following checks:
 * <ol>
 * <li>Checks that the destination is not part of a Unbreakable column
 * group</li>
 * <li>Checks if the destination is between two adjoining column groups</li>
 * </ol>
 *
 * @since 1.6
 */
public class ColumnHeaderReorderDragMode extends ColumnReorderDragMode {

    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;

    protected int dragFromGridRowPosition;

    /**
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to which this drag mode
     *            should be assigned to.
     */
    public ColumnHeaderReorderDragMode(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        super.mouseDown(natTable, event);

        ILayerCell cell = natTable.getCellByPosition(
                natTable.getColumnPositionByX(this.initialEvent.x),
                natTable.getRowPositionByY(this.initialEvent.y));
        if (cell != null) {
            this.dragFromGridRowPosition = cell.getOriginRowPosition() + cell.getRowSpan() - 1;
        } else {
            this.dragFromGridRowPosition = -1;
        }
    }

    @Override
    public boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
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
        for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
            GroupModel model = this.columnGroupHeaderLayer.getGroupModel(level);
            if (model.isPartOfAnUnbreakableGroup(fromPosition)) {
                int toCheck = toPosition;
                if (toPosition < 0 && toGridColumnPosition == natLayer.getColumnCount()) {
                    toCheck = LayerUtil.convertColumnPosition(natLayer, toGridColumnPosition - 1, this.columnGroupHeaderLayer.getPositionLayer());
                }
                MoveDirectionEnum moveDirection = ColumnGroupUtils.getMoveDirection(fromPosition, toCheck);
                toCheck = MoveDirectionEnum.RIGHT == moveDirection ? toCheck - 1 : toCheck;
                return ColumnGroupUtils.isInTheSameGroup(
                        this.columnGroupHeaderLayer,
                        level, fromPosition,
                        toCheck);
            }
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
}
