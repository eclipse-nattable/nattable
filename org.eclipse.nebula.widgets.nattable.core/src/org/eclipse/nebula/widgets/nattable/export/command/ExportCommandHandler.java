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

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.NatExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Shell;

public class ExportCommandHandler extends
        AbstractLayerCommandHandler<ExportCommand> {

    private final ILayer layer;

    public ExportCommandHandler(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public boolean doCommand(final ExportCommand command) {
        Shell shell = command.getShell();
        IConfigRegistry configRegistry = command.getConfigRegistry();

        new NatExporter(shell).exportSingleLayer(this.layer, configRegistry);

        return true;
    }

    @Override
    public Class<ExportCommand> getCommandClass() {
        return ExportCommand.class;
    }

}
