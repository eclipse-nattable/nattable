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
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * {@link MouseEventMatcher} that checks for cell edges at column borders and
 * for the existence of column hide indicator labels.
 *
 * @since 1.6
 */
public class ColumnHideIndicatorEventMatcher extends MouseEventMatcher {

    protected ILayer columnHeaderLayer;

    /**
     *
     * @param stateMask
     *            The state of the keyboard modifier keys and mouse masks at the
     *            time the event was generated to match.
     * @param eventRegion
     *            The region label of the region in which the event should be
     *            processed only. Typically {@link GridRegion#COLUMN_HEADER}.
     * @param button
     *            The mouse button that should be pressed to match.
     * @param columnHeaderLayer
     *            The layer in the column header that should be used to
     *            determine the height of the hidden column indicator. Should be
     *            the top most layer in the column header region, e.g. the
     *            FilterRowHeaderComposite in case filtering is included. Can be
     *            <code>null</code> which leads to label inspection of the table
     *            row the mouse cursor moves over.
     */
    public ColumnHideIndicatorEventMatcher(
            int stateMask, String eventRegion, int button, ILayer columnHeaderLayer) {
        super(stateMask, eventRegion, button);
        this.columnHeaderLayer = columnHeaderLayer;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        return super.matches(natTable, event, regionLabels)
                && hasHiddenColumns(natTable, event);
    }

    private boolean hasHiddenColumns(ILayer natLayer, MouseEvent event) {
        int columnPosition = CellEdgeDetectUtil.getColumnPosition(natLayer, new Point(event.x, event.y));

        if (columnPosition < 0) {
            return false;
        } else {
            NatEventData eventData = NatEventData.createInstanceFromEvent(event);
            LabelStack customLabels = natLayer.getConfigLabelsByPosition(
                    eventData.getColumnPosition(),
                    this.columnHeaderLayer != null ? this.columnHeaderLayer.getRowCount() : eventData.getRowPosition());

            return customLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN)
                    || customLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN);
        }
    }

}
