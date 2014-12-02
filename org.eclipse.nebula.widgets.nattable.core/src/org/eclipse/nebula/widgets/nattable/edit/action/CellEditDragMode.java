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
package org.eclipse.nebula.widgets.nattable.edit.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.command.EditCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.action.CellSelectionDragMode;
import org.eclipse.swt.events.MouseEvent;

/**
 * Specialisation of CellSelectionDragMode that is used in the context of
 * editing. If a drag&amp;drop operation is executed on the same cell, the
 * corresponding editor will be activated, just as if you performed a click into
 * that cell.
 * <p>
 * This is needed to treat minimal (not intended) drag&amp;drop operations like
 * clicks. It sometimes happens that on performing a click, the mouse moves a
 * bit. So between mouseDown and mouseUp there is a movement registered, so it
 * is not interpreted as a click anymore, but as a drag&amp;drop operation. With
 * this implementation registered the described behaviour is avoided.
 */
public class CellEditDragMode extends CellSelectionDragMode {

    private int originalColumnPosition;
    private int originalRowPosition;

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        super.mouseDown(natTable, event);

        this.originalColumnPosition = natTable.getColumnPositionByX(event.x);
        this.originalRowPosition = natTable.getRowPositionByY(event.y);
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        super.mouseMove(natTable, event);

        int columnPosition = natTable.getColumnPositionByX(event.x);
        int rowPosition = natTable.getRowPositionByY(event.y);

        if (columnPosition != this.originalColumnPosition
                || rowPosition != this.originalRowPosition) {
            // Left original cell, cancel edit
            this.originalColumnPosition = -1;
            this.originalRowPosition = -1;
        }
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        super.mouseUp(natTable, event);

        int columnPosition = natTable.getColumnPositionByX(event.x);
        int rowPosition = natTable.getRowPositionByY(event.y);

        if (columnPosition == this.originalColumnPosition
                && rowPosition == this.originalRowPosition) {
            natTable.doCommand(new EditCellCommand(natTable, natTable
                    .getConfigRegistry(), natTable.getCellByPosition(
                    columnPosition, rowPosition)));
        }
    }

}
