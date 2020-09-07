/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.command.EditCellCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseClickAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * Action that will execute an {@link EditCellCommand}. It determines the cell
 * to edit by mouse pointer coordinates instead of using a SelectionLayer. So
 * this action is also working in NatTables that doesn't have a SelectionLayer
 * in its composition of layers.
 */
public class MouseEditAction implements IMouseClickAction {

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        int columnPosition = natTable.getColumnPositionByX(event.x);
        int rowPosition = natTable.getRowPositionByY(event.y);

        natTable.doCommand(
                new EditCellCommand(
                        natTable,
                        natTable.getConfigRegistry(),
                        natTable.getCellByPosition(columnPosition, rowPosition)));
    }

    @Override
    public boolean isExclusive() {
        return true;
    }
}
