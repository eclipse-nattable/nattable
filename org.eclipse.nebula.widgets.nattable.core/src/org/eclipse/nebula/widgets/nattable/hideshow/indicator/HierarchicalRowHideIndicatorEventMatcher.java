/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.indicator;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * {@link MouseEventMatcher} that checks for cell edges at row borders and for
 * the existence of row hide indicator labels in the level headers of a
 * HierarchicalTreeLayer.
 *
 * @since 1.6
 */
public class HierarchicalRowHideIndicatorEventMatcher extends MouseEventMatcher {

    /**
     *
     * @param stateMask
     *            The state of the keyboard modifier keys and mouse masks at the
     *            time the event was generated to match.
     * @param button
     *            The mouse button that should be pressed to match.
     */
    public HierarchicalRowHideIndicatorEventMatcher(int stateMask, int button) {
        super(stateMask, GridRegion.BODY, button);
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        return super.matches(natTable, event, regionLabels)
                && hasHiddenRows(natTable, event);
    }

    private boolean hasHiddenRows(ILayer natLayer, MouseEvent event) {
        int rowPosition = CellEdgeDetectUtil.getRowPosition(natLayer, new Point(event.x, event.y));

        if (rowPosition < 0) {
            return false;
        } else {
            NatEventData eventData = NatEventData.createInstanceFromEvent(event);
            LabelStack labels = natLayer.getConfigLabelsByPosition(
                    eventData.getColumnPosition(),
                    eventData.getRowPosition());

            if (labels.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL)) {
                LabelStack bodyLabels = natLayer.getConfigLabelsByPosition(
                        eventData.getColumnPosition() + 1,
                        eventData.getRowPosition());
                return bodyLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN)
                        || bodyLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN);
            }
            return false;
        }
    }

}
