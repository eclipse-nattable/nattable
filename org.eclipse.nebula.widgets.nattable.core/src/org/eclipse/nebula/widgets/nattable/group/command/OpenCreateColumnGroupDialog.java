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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.group.gui.CreateColumnGroupDialog;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class OpenCreateColumnGroupDialog extends AbstractContextFreeCommand implements IColumnGroupCommand {

    private final CreateColumnGroupDialog dialog;
    private final MessageBox messageBox;

    public OpenCreateColumnGroupDialog(Shell parentShell) {
        this.dialog = CreateColumnGroupDialog.createColumnGroupDialog(parentShell);
        this.messageBox = new MessageBox(parentShell, SWT.INHERIT_DEFAULT | SWT.ICON_ERROR | SWT.OK);
    }

    public CreateColumnGroupDialog getDialog() {
        return this.dialog;
    }

    public void openDialog(ILayer contextLayer) {
        this.dialog.setContextLayer(contextLayer);
        this.dialog.open();
    }

    public void openErrorBox(String errMessage) {
        this.messageBox.setText(Messages.getString("ErrorDialog.title")); //$NON-NLS-1$
        this.messageBox.setMessage(errMessage);
        this.messageBox.open();
    }

}
