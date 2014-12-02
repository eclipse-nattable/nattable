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
package org.eclipse.nebula.widgets.nattable.sort.event;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * Matches a click on the column header, except if the click is on the column
 * edge.
 */
public class ColumnHeaderClickEventMatcher extends MouseEventMatcher {

    public ColumnHeaderClickEventMatcher(int stateMask, int button) {
        super(stateMask, GridRegion.COLUMN_HEADER, button);
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        return super.matches(natTable, event, regionLabels)
                && isNearTheHeaderEdge(natTable, event)
                && isNotFilterRegion(regionLabels)
                && isNotColumnGroupRegion(regionLabels);
    }

    private boolean isNearTheHeaderEdge(ILayer natLayer, MouseEvent event) {
        CellEdgeEnum cellEdge = CellEdgeDetectUtil.getHorizontalCellEdge(
                natLayer, new Point(event.x, event.y),
                GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE);
        return cellEdge == CellEdgeEnum.NONE;
    }

    private boolean isNotFilterRegion(LabelStack regionLabels) {
        if (isNotNull(regionLabels)) {
            return !regionLabels.getLabels().contains(GridRegion.FILTER_ROW);
        }
        return true;
    }

    // added this additional check because of Bug 428901
    private boolean isNotColumnGroupRegion(LabelStack regionLabels) {
        if (isNotNull(regionLabels)) {
            return !regionLabels.getLabels().contains(
                    GridRegion.COLUMN_GROUP_HEADER);
        }
        return true;
    }
}
