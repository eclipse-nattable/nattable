/*****************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple command handler that opens a {@link MessageDialog} to verify that the
 * command handler is executed.
 */
public class MessageBoxCommandHandler {

    @Execute
    public void execute(Shell shell) {
        MessageDialog.openInformation(
                shell,
                "Information",
                "This dialog simply verifies that the handled command is executed.");
    }

}