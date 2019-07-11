/*****************************************************************************
 * Copyright (c) 2015, 2019 CEA LIST.
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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class TestHandler {

    @Execute
    public void execute(Shell parent) {
        MessageDialog.openInformation(parent, "Test", "Show a message to verify that Eclipse 4 menu integration is working");
    }

    @CanExecute
    public boolean canExecute(IEclipseContext context) {
        Boolean enabled = (Boolean) context.get("enabled");
        if (enabled == null) {
            enabled = Boolean.FALSE;
        }
        return enabled;
    }

}