/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com - Bug 449764
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460077
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.mode;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Drag mode that will implement the column resizing process.
 */
public class ColumnResizeDragMode implements IDragMode {

    private static final int DEFAULT_COLUMN_WIDTH_MINIMUM = 25;

    protected int columnPositionToResize;
    protected int originalColumnWidth;
    protected int startX;
    protected int currentX;
    protected int lastX = -1;
    protected int gridColumnStartX;

    protected boolean checkMinimumWidth = true;

    protected final IOverlayPainter overlayPainter = new ColumnResizeOverlayPainter();

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        natTable.forceFocus();
        this.columnPositionToResize =
                CellEdgeDetectUtil.getColumnPositionToResize(natTable, new Point(event.x, event.y));
        if (this.columnPositionToResize >= 0) {
            this.gridColumnStartX = natTable.getStartXOfColumnPosition(this.columnPositionToResize);
            this.originalColumnWidth = natTable.getColumnWidthByPosition(this.columnPositionToResize);
            this.startX = event.x;
            natTable.addOverlayPainter(this.overlayPainter);
        }
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        this.currentX = event.x;
        // redraw the space to the right of the last column
        // to be able to render the resize drag indicator
        natTable.repaintHorizontalLeftOver();

        if (this.checkMinimumWidth
                && this.currentX < this.gridColumnStartX + getColumnWidthMinimum()) {
            this.currentX = this.gridColumnStartX + getColumnWidthMinimum();
        } else {
            int overlayExtent = ColumnResizeOverlayPainter.COLUMN_RESIZE_OVERLAY_WIDTH / 2;

            Set<Integer> columnsToRepaint = new HashSet<Integer>();

            columnsToRepaint.add(natTable.getColumnPositionByX(this.currentX - overlayExtent));
            columnsToRepaint.add(natTable.getColumnPositionByX(this.currentX + overlayExtent));

            if (this.lastX >= 0) {
                columnsToRepaint.add(natTable.getColumnPositionByX(this.lastX - overlayExtent));
                columnsToRepaint.add(natTable.getColumnPositionByX(this.lastX + overlayExtent));
            }

            for (Integer columnToRepaint : columnsToRepaint) {
                if (columnToRepaint >= 0) {
                    natTable.repaintColumn(columnToRepaint.intValue());
                }
            }

            this.lastX = this.currentX;
        }
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        natTable.removeOverlayPainter(this.overlayPainter);
        updateColumnWidth(natTable, event);
    }

    private void updateColumnWidth(ILayer natLayer, MouseEvent e) {
        int dragWidth = e.x - this.startX;
        int newColumnWidth = this.originalColumnWidth + dragWidth;
        if (newColumnWidth < getColumnWidthMinimum()) {
            newColumnWidth = getColumnWidthMinimum();
        }
        natLayer.doCommand(
                new ColumnResizeCommand(
                        natLayer,
                        this.columnPositionToResize,
                        GUIHelper.convertVerticalDpiToPixel(newColumnWidth)));
    }

    // XXX: This method must ask the layer what it's minimum width is!
    /**
     * @since 1.3
     */
    protected int getColumnWidthMinimum() {
        return DEFAULT_COLUMN_WIDTH_MINIMUM;
    }

    private class ColumnResizeOverlayPainter implements IOverlayPainter {

        static final int COLUMN_RESIZE_OVERLAY_WIDTH = 2;

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            Color originalBackgroundColor = gc.getBackground();
            gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
            gc.fillRectangle(
                    ColumnResizeDragMode.this.currentX - (COLUMN_RESIZE_OVERLAY_WIDTH / 2),
                    0,
                    COLUMN_RESIZE_OVERLAY_WIDTH,
                    layer.getHeight());
            gc.setBackground(originalBackgroundColor);
        }
    }
}
