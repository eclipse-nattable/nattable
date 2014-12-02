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
import org.eclipse.swt.events.MouseEvent;

/**
 * Action executed when the user selects any row in the grid.
 */
public class SelectRowAction extends AbstractMouseSelectionAction {

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        super.run(natTable, event);
        natTable.doCommand(new SelectRowsCommand(natTable,
                getGridColumnPosition(), getGridRowPosition(),
                isWithShiftMask(), isWithControlMask()));
    }

}
