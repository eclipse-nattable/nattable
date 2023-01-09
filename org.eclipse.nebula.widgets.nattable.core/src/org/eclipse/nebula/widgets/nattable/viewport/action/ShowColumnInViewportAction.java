/*******************************************************************************
 * Copyright (c) 2023 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommand;
import org.eclipse.swt.events.MouseEvent;

/**
 * Action to move a cell into the viewport.
 *
 * @since 2.1
 */
public class ShowColumnInViewportAction implements IMouseAction {

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        // only perform the selection if the cursor is null
        if (natTable.getCursor() == null)
            natTable.doCommand(
                    new ShowColumnInViewportCommand(
                            natTable,
                            natTable.getColumnPositionByX(event.x)));
    }
}
