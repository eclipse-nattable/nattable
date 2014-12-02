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
    public void fireSelectionCommand(NatTable natTable, int columnPosition,
            int rowPosition, boolean shiftMask, boolean controlMask) {
        natTable.doCommand(new SelectRowsCommand(natTable, columnPosition,
                rowPosition, shiftMask, controlMask));
    }

}
