/*******************************************************************************
 * Copyright (c) 2012, 2025 Original authors and others.
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
        NatExporter natExporter = new NatExporter(
                command.getShell(),
                command.isExecuteSynchronously(),
                command.isUseProgressDialog(),
                command.isOpenResult(),
                command.getSuccessRunnable());

        if (command.getExporter() == null) {
            natExporter.exportSingleLayer(this.layer, command.getConfigRegistry());
        } else {
            natExporter.exportSingleLayer(command.getExporter(), this.layer, command.getConfigRegistry());
        }

        return true;
    }

    @Override
    public Class<ExportCommand> getCommandClass() {
        return ExportCommand.class;
    }

}
