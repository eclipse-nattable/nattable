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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Matches a mouse click on a given cell painter within a cell.
 */
public class CellPainterMouseEventMatcher extends MouseEventMatcher {

    private ICellPainter targetCellPainter;
    private Class<? extends ICellPainter> targetCellPainterClass;

    public CellPainterMouseEventMatcher(String regionName, int button,
            ICellPainter targetCellPainter) {
        super(regionName, button);
        this.targetCellPainter = targetCellPainter;
    }

    public CellPainterMouseEventMatcher(String regionName, int button,
            Class<? extends ICellPainter> targetCellPainterClass) {
        super(regionName, button);
        this.targetCellPainterClass = targetCellPainterClass;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        if (super.matches(natTable, event, regionLabels)) {
            int columnPosition = natTable.getColumnPositionByX(event.x);
            int rowPosition = natTable.getRowPositionByY(event.y);

            ILayerCell cell = natTable.getCellByPosition(columnPosition,
                    rowPosition);

            // Bug 407598: only perform a check if the click in the body region
            // was performed on a cell
            // cell == null can happen if the viewport is quite large and
            // contains not enough cells to fill it.
            if (cell != null) {
                IConfigRegistry configRegistry = natTable.getConfigRegistry();
                ICellPainter cellPainter = cell.getLayer().getCellPainter(
                        columnPosition, rowPosition, cell, configRegistry);

                GC gc = new GC(natTable.getDisplay());
                try {
                    Rectangle adjustedCellBounds = natTable.getLayerPainter()
                            .adjustCellBounds(columnPosition, rowPosition,
                                    cell.getBounds());

                    ICellPainter clickedCellPainter = cellPainter
                            .getCellPainterAt(event.x, event.y, cell, gc,
                                    adjustedCellBounds, configRegistry);
                    if (clickedCellPainter != null) {
                        if ((this.targetCellPainter != null && this.targetCellPainter == clickedCellPainter)
                                || (this.targetCellPainterClass != null && this.targetCellPainterClass
                                        .isInstance(clickedCellPainter))) {
                            return true;
                        }
                    }
                } finally {
                    gc.dispose();
                }
            }
        }
        return false;
    }

}
