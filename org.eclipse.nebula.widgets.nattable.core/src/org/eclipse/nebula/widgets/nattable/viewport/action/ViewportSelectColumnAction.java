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
package org.eclipse.nebula.widgets.nattable.viewport.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommand;
import org.eclipse.swt.events.MouseEvent;

/**
 * Action indicating that the user has specifically selected a column header.
 */
public class ViewportSelectColumnAction implements IMouseAction {

    private final boolean withShiftMask;
    private final boolean withControlMask;

    public ViewportSelectColumnAction(boolean withShiftMask, boolean withControlMask) {
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        // only perform the selection if the cursor is null
        if (natTable.getCursor() == null)
            natTable.doCommand(
                    new ViewportSelectColumnCommand(
                            natTable,
                            natTable.getColumnPositionByX(event.x),
                            this.withShiftMask,
                            this.withControlMask));
    }
}
