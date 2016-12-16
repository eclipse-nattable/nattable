/*******************************************************************************
 * Copyright (c) 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thanh Liem PHAN (ALL4TEC) <thanhliem.phan@all4tec.net> - Bug 509361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.NatExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Shell;

/**
 * Class to handle the {@link ExportTableCommand}.
 *
 * @since 1.5
 */
public class ExportTableCommandHandler extends AbstractLayerCommandHandler<ExportTableCommand> {

    private final ILayer layer;

    public ExportTableCommandHandler(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public boolean doCommand(ExportTableCommand command) {
        Shell shell = command.getShell();
        IConfigRegistry configRegistry = command.getConfigRegistry();

        new NatExporter(shell).exportSingleTable(this.layer, configRegistry);

        return true;
    }

    @Override
    public Class<ExportTableCommand> getCommandClass() {
        return ExportTableCommand.class;
    }

}
