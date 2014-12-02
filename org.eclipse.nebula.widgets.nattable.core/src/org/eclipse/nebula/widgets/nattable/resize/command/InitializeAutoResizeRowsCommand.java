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

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;

/**
 * @see InitializeAutoResizeColumnsCommand
 */

public class InitializeAutoResizeRowsCommand extends AbstractRowCommand {

    private final IConfigRegistry configRegistry;
    private final GCFactory gcFactory;
    private final ILayer sourceLayer;
    private int[] selectedRowPositions = new int[0];

    public InitializeAutoResizeRowsCommand(ILayer layer, int rowPosition,
            IConfigRegistry configRegistry, GCFactory gcFactory) {
        super(layer, rowPosition);
        this.configRegistry = configRegistry;
        this.gcFactory = gcFactory;
        this.sourceLayer = layer;
    }

    protected InitializeAutoResizeRowsCommand(
            InitializeAutoResizeRowsCommand command) {
        super(command);
        this.configRegistry = command.configRegistry;
        this.gcFactory = command.gcFactory;
        this.sourceLayer = command.sourceLayer;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new InitializeAutoResizeRowsCommand(this);
    }

    // Accessors

    public GCFactory getGCFactory() {
        return this.gcFactory;
    }

    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    public ILayer getSourceLayer() {
        return this.sourceLayer;
    }

    public void setSelectedRowPositions(int[] selectedRowPositions) {
        this.selectedRowPositions = selectedRowPositions;
    }

    public int[] getRowPositions() {
        return this.selectedRowPositions;
    }
}
