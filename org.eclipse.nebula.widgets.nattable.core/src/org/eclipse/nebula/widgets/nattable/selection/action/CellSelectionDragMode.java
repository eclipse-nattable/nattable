/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.viewport.action.AutoScrollDragMode;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * Fires commands to select a range of cells when the mouse is dragged in the
 * viewport.
 */
public class CellSelectionDragMode extends AutoScrollDragMode {

    private Point lastDragInCellPosition = null;

    public CellSelectionDragMode() {
        super(true, true);
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        natTable.forceFocus();
        this.lastDragInCellPosition = new Point(
                natTable.getColumnPositionByX(event.x),
                natTable.getRowPositionByY(event.y));
    }

    @Override
    protected void performDragAction(
            NatTable natTable,
            int x, int y,
            MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {

        if (!MoveDirectionEnum.NONE.equals(horizontal)
                || !MoveDirectionEnum.NONE.equals(vertical)) {
            this.lastDragInCellPosition = null;
        }

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

    /**
     * Execute a command to trigger selection.
     *
     * @param natTable
     *            The NatTable to execute the command on.
     * @param columnPosition
     *            The column position of the cell to select.
     * @param rowPosition
     *            The row position of the cell to select.
     * @param shiftMask
     *            Flag to configure whether the SHIFT mask is activated or not.
     * @param controlMask
     *            Flag to configure whether the CTRL mask is activated or not.
     */
    public void fireSelectionCommand(
            NatTable natTable,
            int columnPosition, int rowPosition,
            boolean shiftMask, boolean controlMask) {

        boolean result = natTable.doCommand(
                new SelectCellCommand(natTable, columnPosition, rowPosition, shiftMask, controlMask));

        // If the command execution fails for cell coordinates pointing to
        // position 0 try again with increased positions. Needed in case of grid
        // compositions where position 0 in NatTable are typically the headers.
        if (!result && columnPosition == 0) {
            natTable.doCommand(
                    new SelectCellCommand(natTable, columnPosition + 1, rowPosition, shiftMask, controlMask));
        } else if (!result && rowPosition == 0) {
            natTable.doCommand(
                    new SelectCellCommand(natTable, columnPosition, rowPosition + 1, shiftMask, controlMask));
        }
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        // Cancel any active viewport drag
        super.mouseUp(natTable, event);

        this.lastDragInCellPosition = null;
    }

}
