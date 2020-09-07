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
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;

/**
 * Selects the entire row when the mouse is dragged on the body. <i>Multiple</i>
 * rows are selected as the user drags.
 *
 * @see RowOnlySelectionBindings
 */
public class RowSelectionDragMode extends CellSelectionDragMode {

    @Override
    public void fireSelectionCommand(NatTable natTable, int columnPosition, int rowPosition, boolean shiftMask, boolean controlMask) {
        natTable.doCommand(new SelectRowsCommand(natTable, columnPosition, rowPosition, shiftMask, controlMask));
    }

}
