/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.export.NatExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command handler to handle the {@link ExportCommand} and trigger an export.
 */
public class ExportCommandHandler extends AbstractLayerCommandHandler<ExportCommand> {

    private final ILayer layer;

    /**
     * Creates an {@link ExportCommandHandler}.
     *
     * @param layer
     *            The ILayer that should be exported. Typically a NatTable
     *            instance, but can also be a lower layer in the stack to avoid
     *            higher level modifications.
     */
    public ExportCommandHandler(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public boolean doCommand(final ExportCommand command) {
        new NatExporter(command.getShell(), command.isExecuteSynchronously())
                .exportSingleLayer(this.layer, command.getConfigRegistry());

        return true;
    }

    @Override
    public Class<ExportCommand> getCommandClass() {
        return ExportCommand.class;
    }

}
