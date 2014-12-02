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
package org.eclipse.nebula.widgets.nattable.filterrow.event;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.events.MouseEvent;

/**
 * Specialization of a {@link CellPainterMouseEventMatcher} that only matches
 * for the filter row region if a filter is applied in the clicked cell and the
 * click was executed on the painted icon in that cell (usually the clear filter
 * icon).
 */
public class ClearFilterIconMouseEventMatcher extends
        CellPainterMouseEventMatcher {

    /**
     * Create a new {@link ClearFilterIconMouseEventMatcher} for the given
     * {@link FilterRowPainter}
     *
     * @param filterRowPainter
     *            The {@link FilterRowPainter} needed to determine the filter
     *            icon painter.
     */
    public ClearFilterIconMouseEventMatcher(FilterRowPainter filterRowPainter) {
        super(GridRegion.FILTER_ROW, MouseEventMatcher.LEFT_BUTTON,
                filterRowPainter.getFilterIconPainter().getClass());
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        ILayerCell cell = natTable.getCellByPosition(
                natTable.getColumnPositionByX(event.x),
                natTable.getRowPositionByY(event.y));

        if (cell == null)
            return false;

        return (super.matches(natTable, event, regionLabels) && ObjectUtils
                .isNotNull(cell.getDataValue()));
    }
}
