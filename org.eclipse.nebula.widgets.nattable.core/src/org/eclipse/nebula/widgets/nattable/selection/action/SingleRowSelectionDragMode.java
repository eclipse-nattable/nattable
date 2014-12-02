/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.swt.events.MouseEvent;

/**
 * Selects the entire row when the mouse is dragged on the body. Only a
 * <i>single</i> row is selected at a given time. This is the row the mouse is
 * over.
 *
 * @see RowOnlySelectionBindings
 */
public class SingleRowSelectionDragMode extends RowSelectionDragMode implements
        IDragMode {

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        natTable.doCommand(new ClearAllSelectionsCommand());

        if (event.x > natTable.getWidth()) {
            return;
        }
        int selectedColumnPosition = natTable.getColumnPositionByX(event.x);
        int selectedRowPosition = natTable.getRowPositionByY(event.y);

        if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
            fireSelectionCommand(natTable, selectedColumnPosition,
                    selectedRowPosition, false, false);
        }
    }
}
