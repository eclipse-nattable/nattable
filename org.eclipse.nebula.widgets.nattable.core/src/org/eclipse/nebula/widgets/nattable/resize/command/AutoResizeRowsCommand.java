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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.command.AutoResizeRowCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeAutoResizeRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;

/**
 * Command to trigger auto resizing of rows in a grid.
 *
 * @see AutoResizeRowCommandHandler
 * @see InitializeAutoResizeRowsCommand
 * @see InitializeAutoResizeRowsCommandHandler
 */
public class AutoResizeRowsCommand extends AbstractMultiRowCommand {

    private final IConfigRegistry configRegistry;
    private final GCFactory gcFactory;
    private final boolean transformPositions;

    /**
     * Create a {@link AutoResizeRowsCommand} from the given
     * {@link InitializeAutoResizeRowsCommand}.
     *
     * @param initCommand
     *            The {@link InitializeAutoResizeRowsCommand} from which this
     *            command should be created.
     */
    public AutoResizeRowsCommand(InitializeAutoResizeRowsCommand initCommand) {
        super(initCommand.getSourceLayer(), initCommand.getRowPositions());
        this.configRegistry = initCommand.getConfigRegistry();
        this.gcFactory = initCommand.getGCFactory();
        this.transformPositions = true;
    }

    /**
     * Create a {@link AutoResizeRowsCommand} for programmatic execution.
     *
     * @param natTable
     *            The {@link NatTable} instance in which the resize should be
     *            performed.
     * @param rowPositions
     *            The row positions that should be auto resized, based on the
     *            NatTable position.
     *
     * @since 1.6
     */
    public AutoResizeRowsCommand(NatTable natTable, int... rowPositions) {
        this(natTable, false, rowPositions);
    }

    /**
     * Create a {@link AutoResizeRowsCommand} for programmatic execution.
     *
     * @param natTable
     *            The {@link NatTable} instance in which the resize should be
     *            performed.
     * @param transformPositions
     *            <code>true</code> if the row positions should be back
     *            transformed (e.g. in case the positions are collected via
     *            SelectionLayer and therefore need to be back transformed to
     *            the GridLayer coordinates), <code>false</code> if the row
     *            positions should be treated based on the NatTable.
     * @param rowPositions
     *            The row positions that should be auto resized, based on the
     *            NatTable position.
     *
     * @since 1.6
     */
    public AutoResizeRowsCommand(NatTable natTable, boolean transformPositions, int... rowPositions) {
        super(natTable, rowPositions);
        this.configRegistry = natTable.getConfigRegistry();
        this.gcFactory = new GCFactory(natTable);
        this.transformPositions = transformPositions;
    }

    /**
     * Create a {@link AutoResizeRowsCommand} as a clone of the given command
     * instance.
     *
     * @param command
     *            The {@link AutoResizeRowsCommand} that should be cloned.
     */
    protected AutoResizeRowsCommand(AutoResizeRowsCommand command) {
        super(command);
        this.configRegistry = command.configRegistry;
        this.gcFactory = command.gcFactory;
        this.transformPositions = command.transformPositions;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new AutoResizeRowsCommand(this);
    }

    // Accessors

    /**
     *
     * @return The {@link GCFactory} needed to create a temporary GC for row
     *         height calculation.
     */
    public GCFactory getGCFactory() {
        return this.gcFactory;
    }

    /**
     *
     * @return The {@link IConfigRegistry} needed for retrieval of style
     *         configurations.
     */
    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    /**
     * Return whether the command handler should transform the row positions or
     * not. If this command is created via
     * {@link InitializeAutoResizeRowsCommand} the row positions need to be
     * transformed as by default the command handler is registered to the
     * GridLayer but the row positions are set based on the SelectionLayer. A
     * back transformation is therefore needed. If this command was not created
     * with the other constructor, a back transformation in the command handler
     * is not necessary.
     *
     * @return <code>true</code> if the command handler should transform the row
     *         positions, <code>false</code> if not.
     *
     * @since 1.6
     */
    public boolean doPositionTransformation() {
        return this.transformPositions;
    }
}
