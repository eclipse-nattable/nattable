/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 462143
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommand;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Fires commands to select a range of cells when the mouse is dragged in the
 * viewport.
 */
public class CellSelectionDragMode implements IDragMode {

    private Point lastDragInCellPosition = null;

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        natTable.forceFocus();
        this.lastDragInCellPosition = new Point(
                natTable.getColumnPositionByX(event.x),
                natTable.getRowPositionByY(event.y));
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {

        Rectangle clientArea = natTable.getClientAreaProvider().getClientArea();

        int x = event.x;
        int y = event.y;

        MoveDirectionEnum horizontal = MoveDirectionEnum.NONE;
        if (event.x < 0) {
            horizontal = MoveDirectionEnum.LEFT;
            x = 0;
        }
        else if (event.x > clientArea.width) {
            horizontal = MoveDirectionEnum.RIGHT;
            x = clientArea.width;
        }

        MoveDirectionEnum vertical = MoveDirectionEnum.NONE;
        if (event.y < 0) {
            vertical = MoveDirectionEnum.UP;
            y = 0;
        }
        else if (event.y > clientArea.height) {
            vertical = MoveDirectionEnum.DOWN;
            y = clientArea.height;
        }

        if (!MoveDirectionEnum.NONE.equals(horizontal)
                || !MoveDirectionEnum.NONE.equals(vertical)) {
            this.lastDragInCellPosition = null;
        }

        if (natTable.doCommand(new ViewportDragCommand(horizontal, vertical))) {

            int selectedColumnPosition = natTable.getColumnPositionByX(x);
            int selectedRowPosition = natTable.getRowPositionByY(y);

            if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
                Point dragInCellPosition = new Point(selectedColumnPosition, selectedRowPosition);
                if (this.lastDragInCellPosition == null
                        || !dragInCellPosition.equals(this.lastDragInCellPosition)) {
                    this.lastDragInCellPosition = dragInCellPosition;

                    fireSelectionCommand(natTable, selectedColumnPosition, selectedRowPosition, true, false);
                }
            }
        }
    }

    public void fireSelectionCommand(NatTable natTable, int columnPosition, int rowPosition,
            boolean shiftMask, boolean controlMask) {

        natTable.doCommand(
                new SelectCellCommand(natTable, columnPosition, rowPosition, shiftMask, controlMask));
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        this.lastDragInCellPosition = null;
    }

}
