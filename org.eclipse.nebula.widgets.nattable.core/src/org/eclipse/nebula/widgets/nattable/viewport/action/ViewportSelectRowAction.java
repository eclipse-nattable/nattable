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
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommand;
import org.eclipse.swt.events.MouseEvent;

/**
 * Event fired when the <i>ctrl</i> key is pressed and the row header is
 * clicked. Note: Fires command in NatTable coordinates.
 */
public class ViewportSelectRowAction implements IMouseAction {

    private final boolean withShiftMask;
    private final boolean withControlMask;

    public ViewportSelectRowAction(boolean withShiftMask, boolean withControlMask) {
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        // only perform the selection if the cursor is null
        if (natTable.getCursor() == null)
            natTable.doCommand(
                    new ViewportSelectRowCommand(
                            natTable,
                            natTable.getRowPositionByY(event.y),
                            this.withShiftMask,
                            this.withControlMask));
    }

}
