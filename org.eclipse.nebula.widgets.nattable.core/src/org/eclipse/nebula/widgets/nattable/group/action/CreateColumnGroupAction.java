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
package org.eclipse.nebula.widgets.nattable.group.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.command.OpenCreateColumnGroupDialog;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

public class CreateColumnGroupAction implements IKeyAction {

    private OpenCreateColumnGroupDialog dialogCommand;

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        if (this.dialogCommand == null) {
            // Create dialog
            this.dialogCommand = new OpenCreateColumnGroupDialog(natTable.getShell());
        }
        natTable.doCommand(this.dialogCommand);
    }

}
