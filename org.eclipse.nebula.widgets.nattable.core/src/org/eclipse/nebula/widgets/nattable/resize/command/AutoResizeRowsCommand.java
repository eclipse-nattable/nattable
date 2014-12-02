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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;

/**
 * @see AutoResizeColumnsCommand
 */

public class AutoResizeRowsCommand extends AbstractMultiRowCommand {

    private final IConfigRegistry configRegistry;
    private final GCFactory gcFactory;

    public AutoResizeRowsCommand(InitializeAutoResizeRowsCommand initCommand) {
        super(initCommand.getSourceLayer(), initCommand.getRowPositions());
        this.configRegistry = initCommand.getConfigRegistry();
        this.gcFactory = initCommand.getGCFactory();
    }

    protected AutoResizeRowsCommand(AutoResizeRowsCommand command) {
        super(command);
        this.configRegistry = command.configRegistry;
        this.gcFactory = command.gcFactory;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new AutoResizeRowsCommand(this);
    }

    // Accessors

    public GCFactory getGCFactory() {
        return this.gcFactory;
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }
}
