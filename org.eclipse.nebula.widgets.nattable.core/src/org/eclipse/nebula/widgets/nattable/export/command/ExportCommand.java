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
package org.eclipse.nebula.widgets.nattable.export.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.swt.widgets.Shell;

public class ExportCommand extends AbstractContextFreeCommand {

    private IConfigRegistry configRegistry;
    private final Shell shell;

    public ExportCommand(IConfigRegistry configRegistry, Shell shell) {
        this.configRegistry = configRegistry;
        this.shell = shell;
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    public Shell getShell() {
        return this.shell;
    }
}
