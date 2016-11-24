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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.mode;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Drag mode that will implement the row resizing process.
 */
public class RowResizeDragMode implements IDragMode {

    private static final int DEFAULT_ROW_HEIGHT_MINIMUM = 18;

    protected int gridRowPositionToResize;
    protected int originalRowHeight;
    protected int startY;
    protected int currentY;
    protected int lastY = -1;
    protected int gridRowStartY;

    protected boolean checkMinimumWidth = true;

    protected final IOverlayPainter overlayPainter = new RowResizeOverlayPainter();

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        natTable.forceFocus();
        this.gridRowPositionToResize =
                CellEdgeDetectUtil.getRowPositionToResize(natTable, new Point(event.x, event.y));
        if (this.gridRowPositionToResize > 0) {
            this.gridRowStartY = natTable.getStartYOfRowPosition(this.gridRowPositionToResize);
            this.originalRowHeight = natTable.getRowHeightByPosition(this.gridRowPositionToResize);
            this.startY = event.y;
            natTable.addOverlayPainter(this.overlayPainter);
        }
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        this.currentY = event.y;
        // redraw the space after the bottom of the last row
        // to be able to render the resize drag indicator
        natTable.repaintVerticalLeftOver();

        if (this.checkMinimumWidth
                && this.currentY < this.gridRowStartY + getRowHeightMinimum()) {
            this.currentY = this.gridRowStartY + getRowHeightMinimum();
        } else {
            int overlayExtent = RowResizeOverlayPainter.ROW_RESIZE_OVERLAY_HEIGHT / 2;

            Set<Integer> rowsToRepaint = new HashSet<Integer>();

            rowsToRepaint.add(natTable.getRowPositionByY(this.currentY - overlayExtent));
            rowsToRepaint.add(natTable.getRowPositionByY(this.currentY + overlayExtent));

            if (this.lastY >= 0) {
                rowsToRepaint.add(natTable.getRowPositionByY(this.lastY - overlayExtent));
                rowsToRepaint.add(natTable.getRowPositionByY(this.lastY + overlayExtent));
            }

            for (Integer rowToRepaint : rowsToRepaint) {
                natTable.repaintRow(rowToRepaint.intValue());
            }

            this.lastY = this.currentY;
        }
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        natTable.removeOverlayPainter(this.overlayPainter);
        updateRowHeight(natTable, event);
    }

    private void updateRowHeight(ILayer natLayer, MouseEvent e) {
        int dragHeight = e.y - this.startY;
        int newRowHeight = this.originalRowHeight + dragHeight;
        if (newRowHeight < getRowHeightMinimum()) {
            newRowHeight = getRowHeightMinimum();
        }
        natLayer.doCommand(
                new RowResizeCommand(
                        natLayer,
                        this.gridRowPositionToResize,
                        GUIHelper.convertHorizontalDpiToPixel(newRowHeight)));
    }

    // XXX: should ask the layer for its minimum row height
    public int getRowHeightMinimum() {
        return DEFAULT_ROW_HEIGHT_MINIMUM;
    }

    private class RowResizeOverlayPainter implements IOverlayPainter {

        static final int ROW_RESIZE_OVERLAY_HEIGHT = 2;

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            Color originalBackgroundColor = gc.getBackground();
            gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
            gc.fillRectangle(0, RowResizeDragMode.this.currentY - (ROW_RESIZE_OVERLAY_HEIGHT / 2),
                    layer.getWidth(), ROW_RESIZE_OVERLAY_HEIGHT);
            gc.setBackground(originalBackgroundColor);
        }
    }
}
