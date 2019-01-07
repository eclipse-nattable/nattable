/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.command.CreateColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.gui.ColumnGroupNameDialog;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action to trigger creation of a column group based on the currently fully
 * selected columns and the name provided by the user via dialog.
 *
 * @since 1.6
 */
public class CreateColumnGroupAction implements IKeyAction {

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        ColumnGroupNameDialog dialog = new ColumnGroupNameDialog(natTable.getShell());
        int result = dialog.open();
        if (result == IDialogConstants.OK_ID) {
            natTable.doCommand(new CreateColumnGroupCommand(dialog.getColumnGroupName()));
        }
    }

}
