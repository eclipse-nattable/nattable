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

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

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

        // if reordered to the beginning or the end, the position is valid
        if (toPosition == 0 || toPosition == this.columnGroupHeaderLayer.getPositionLayer().getColumnCount() - 1) {
            return true;
        }

        // Allow moving within the unbreakable group
        for (int level = 0; level < this.columnGroupHeaderLayer.getLevelCount(); level++) {
            GroupModel model = this.columnGroupHeaderLayer.getGroupModel(level);
            if (model.isPartOfAnUnbreakableGroup(this.columnGroupHeaderLayer.getReorderFromColumnPosition())) {
                return ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, level, this.columnGroupHeaderLayer.getReorderFromColumnPosition(), toPosition);
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
}
