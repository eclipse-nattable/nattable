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
package org.eclipse.nebula.widgets.nattable.print.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.print.LayerPrinter;

/**
 * {@link ILayerCommandHandler} for handling the {@link PrintCommand}. Simply
 * delegates to the {@link LayerPrinter}
 */
public class PrintCommandHandler extends AbstractLayerCommandHandler<PrintCommand> {

    /**
     * @since 1.5
     */
    protected final ILayer layer;

    /**
     * @param layer
     *            The layer that should be printed. Usually the top most layer
     *            to print, e.g. the GridLayer.
     */
    public PrintCommandHandler(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public boolean doCommand(PrintCommand command) {
        new LayerPrinter(this.layer, command.getConfigRegistry()).print(command.getShell());
        return true;
    }

    @Override
    public Class<PrintCommand> getCommandClass() {
        return PrintCommand.class;
    }

}
