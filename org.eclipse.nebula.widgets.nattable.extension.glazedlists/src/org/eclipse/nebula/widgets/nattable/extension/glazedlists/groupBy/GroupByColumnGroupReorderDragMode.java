/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.action.ColumnHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * Specialization of {@link ColumnHeaderReorderDragMode} that avoids rendering
 * of the column drag lines in case the mouse moves over the groupBy region by
 * supporting handling of column groups.
 *
 * @since 1.6
 */
public class GroupByColumnGroupReorderDragMode extends ColumnHeaderReorderDragMode {

    /**
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to which this drag mode
     *            should be assigned to.
     */
    public GroupByColumnGroupReorderDragMode(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        super(columnGroupHeaderLayer);
    }

    @Override
    public boolean isValidTargetColumnPosition(ILayer natLayer, int dragFromGridColumnPosition, int dragToGridColumnPosition) {
        // Suppress reorder if cursor is over the group by region
        LabelStack regionLabels = natLayer.getRegionLabelsByXY(this.currentEvent.x, this.currentEvent.y);
        if (regionLabels != null && !regionLabels.hasLabel(GroupByHeaderLayer.GROUP_BY_REGION)) {
            return super.isValidTargetColumnPosition(natLayer, dragFromGridColumnPosition, dragToGridColumnPosition);
        }
        return false;
    }

}
