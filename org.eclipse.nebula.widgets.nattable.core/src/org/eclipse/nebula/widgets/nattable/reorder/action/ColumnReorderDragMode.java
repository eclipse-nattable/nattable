/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.ui.util.MouseEventHelper;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.AutoScrollDragMode;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the column
 * header. It does the following when invoked:
 * <ol>
 * <li>Fires a column reorder command, to move columns</li>
 * <li>Overlays a black line indicating the new column position</li>
 * </ol>
 */
public class ColumnReorderDragMode extends AutoScrollDragMode {

    protected NatTable natTable;
    protected MouseEvent initialEvent;
    protected MouseEvent currentEvent;
    protected int dragFromGridColumnPosition;

    protected ColumnReorderOverlayPainter targetOverlayPainter = new ColumnReorderOverlayPainter();

    public ColumnReorderDragMode() {
        super(true, false);
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        this.natTable = natTable;
        this.initialEvent = event;
        this.currentEvent = this.initialEvent;
        this.dragFromGridColumnPosition = getDragFromGridColumnPosition();

        natTable.addOverlayPainter(this.targetOverlayPainter);

        natTable.doCommand(new ClearAllSelectionsCommand());

        fireMoveStartCommand(natTable, this.dragFromGridColumnPosition);
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        super.mouseMove(natTable, event);

        this.currentEvent = event;

        natTable.redraw();
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        // Cancel any active viewport drag
        super.mouseUp(natTable, event);

        natTable.removeOverlayPainter(this.targetOverlayPainter);

        // only trigger column reordering in case there is a real drag operation
        if (!MouseEventHelper.treatAsClick(this.initialEvent, this.currentEvent)) {
            int dragToGridColumnPosition = getDragToGridColumnPosition(
                    getMoveDirection(event.x),
                    natTable.getColumnPositionByX(event.x));

            if (!isValidTargetColumnPosition(natTable, this.dragFromGridColumnPosition, dragToGridColumnPosition)) {
                dragToGridColumnPosition = -1;
            }

            fireMoveEndCommand(natTable, dragToGridColumnPosition);
        }

        natTable.redraw();
    }

    protected int getDragFromGridColumnPosition() {
        return this.natTable.getColumnPositionByX(this.initialEvent.x);
    }

    protected int getDragToGridColumnPosition(CellEdgeEnum moveDirection, int gridColumnPosition) {
        int dragToGridColumnPosition = -1;

        if (moveDirection != null) {
            switch (moveDirection) {
                case LEFT:
                    dragToGridColumnPosition = gridColumnPosition;
                    break;
                case RIGHT:
                    dragToGridColumnPosition = gridColumnPosition + 1;
                    break;
            }
        }

        return dragToGridColumnPosition;
    }

    protected CellEdgeEnum getMoveDirection(int x) {
        ILayerCell cell = getColumnCell(x);
        if (cell != null) {
            Rectangle selectedColumnHeaderRect = cell.getBounds();
            return CellEdgeDetectUtil.getHorizontalCellEdge(selectedColumnHeaderRect, new Point(x, this.initialEvent.y));
        }

        return null;
    }

    private ILayerCell getColumnCell(int x) {
        int gridColumnPosition = this.natTable.getColumnPositionByX(x);
        int gridRowPosition = this.natTable.getRowPositionByY(this.initialEvent.y);
        return this.natTable.getCellByPosition(gridColumnPosition, gridRowPosition);
    }

    protected boolean isValidTargetColumnPosition(ILayer natLayer, int dragFromGridColumnPosition, int dragToGridColumnPosition) {
        return dragFromGridColumnPosition >= 0 && dragToGridColumnPosition >= 0;
    }

    protected void fireMoveStartCommand(NatTable natTable, int dragFromGridColumnPosition) {
        natTable.doCommand(new ColumnReorderStartCommand(natTable, dragFromGridColumnPosition));
    }

    protected void fireMoveEndCommand(NatTable natTable, int dragToGridColumnPosition) {
        natTable.doCommand(new ColumnReorderEndCommand(natTable, dragToGridColumnPosition));
    }

    private class ColumnReorderOverlayPainter implements IOverlayPainter {

        @Override
        public void paintOverlay(GC gc, ILayer layer) {
            int dragFromGridColumnPosition = getDragFromGridColumnPosition();

            if (ColumnReorderDragMode.this.currentEvent.x > ColumnReorderDragMode.this.natTable.getWidth()) {
                return;
            }

            CellEdgeEnum moveDirection = getMoveDirection(ColumnReorderDragMode.this.currentEvent.x);
            int dragToGridColumnPosition = getDragToGridColumnPosition(
                    moveDirection,
                    ColumnReorderDragMode.this.natTable.getColumnPositionByX(ColumnReorderDragMode.this.currentEvent.x));

            if (isValidTargetColumnPosition(
                    ColumnReorderDragMode.this.natTable,
                    dragFromGridColumnPosition, dragToGridColumnPosition)) {
                int dragToColumnHandleX = -1;

                if (moveDirection != null) {
                    Rectangle selectedColumnHeaderRect = getColumnCell(ColumnReorderDragMode.this.currentEvent.x).getBounds();

                    switch (moveDirection) {
                        case LEFT:
                            dragToColumnHandleX = selectedColumnHeaderRect.x;
                            break;
                        case RIGHT:
                            dragToColumnHandleX = selectedColumnHeaderRect.x + selectedColumnHeaderRect.width;
                            break;
                    }
                }

                if (dragToColumnHandleX > 0) {
                    Color orgBgColor = gc.getBackground();
                    gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
                    gc.fillRectangle(dragToColumnHandleX - 1, 0, 2, layer.getHeight());
                    gc.setBackground(orgBgColor);
                }
            }
        }
    }
}
