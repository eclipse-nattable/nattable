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
import org.eclipse.nebula.widgets.nattable.command.AbstractMultiColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.command.AutoResizeColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeAutoResizeColumnsCommandHandler;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;

/**
 * Command indicating that all selected columns have to be auto resized i.e made
 * wide enough to just fit the widest cell. This should also take the column
 * header into account
 *
 * Note: The {@link InitializeAutoResizeColumnsCommand} has to be fired first
 * when autoresizing columns.
 *
 * @see AutoResizeColumnCommandHandler
 * @see InitializeAutoResizeColumnsCommand
 * @see InitializeAutoResizeColumnsCommandHandler
 */
public class AutoResizeColumnsCommand extends AbstractMultiColumnCommand {

    private final IConfigRegistry configRegistry;
    private final GCFactory gcFactory;
    private final boolean transformPositions;

    /**
     * Create a {@link AutoResizeColumnsCommand} from the given
     * {@link InitializeAutoResizeColumnsCommand}.
     *
     * @param initCommand
     *            The {@link InitializeAutoResizeColumnsCommand} from which this
     *            command should be created.
     */
    public AutoResizeColumnsCommand(InitializeAutoResizeColumnsCommand initCommand) {
        super(initCommand.getSourceLayer(), initCommand.getColumnPositions());
        this.configRegistry = initCommand.getConfigRegistry();
        this.gcFactory = initCommand.getGCFactory();
        this.transformPositions = true;
    }

    /**
     * Create a {@link AutoResizeColumnsCommand} for programmatic execution.
     *
     * @param natTable
     *            The {@link NatTable} instance in which the resize should be
     *            performed.
     * @param columnPositions
     *            The column positions that should be auto resized, based on the
     *            NatTable position.
     *
     * @since 1.6
     */
    public AutoResizeColumnsCommand(NatTable natTable, int... columnPositions) {
        this(natTable, false, columnPositions);
    }

    /**
     * Create a {@link AutoResizeColumnsCommand} for programmatic execution.
     *
     * @param natTable
     *            The {@link NatTable} instance in which the resize should be
     *            performed.
     * @param transformPositions
     *            <code>true</code> if the column positions should be back
     *            transformed (e.g. in case the positions are collected via
     *            SelectionLayer and therefore need to be back transformed to
     *            the GridLayer coordinates), <code>false</code> if the column
     *            positions should be treated based on the NatTable.
     * @param columnPositions
     *            The column positions that should be auto resized, based on the
     *            NatTable position.
     *
     * @since 1.6
     */
    public AutoResizeColumnsCommand(NatTable natTable, boolean transformPositions, int... columnPositions) {
        super(natTable, columnPositions);
        this.configRegistry = natTable.getConfigRegistry();
        this.gcFactory = new GCFactory(natTable);
        this.transformPositions = transformPositions;
    }

    /**
     * Create a {@link AutoResizeColumnsCommand} as a clone of the given command
     * instance.
     *
     * @param command
     *            The {@link AutoResizeColumnsCommand} that should be cloned.
     */
    protected AutoResizeColumnsCommand(AutoResizeColumnsCommand command) {
        super(command);
        this.configRegistry = command.configRegistry;
        this.gcFactory = command.gcFactory;
        this.transformPositions = command.transformPositions;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new AutoResizeColumnsCommand(this);
    }

    // Accessors

    /**
     *
     * @return The {@link GCFactory} needed to create a temporary GC for column
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
     * Return whether the command handler should transform the column positions
     * or not. If this command is created via
     * {@link InitializeAutoResizeColumnsCommand} the column positions need to
     * be transformed as by default the command handler is registered to the
     * GridLayer but the column positions are set based on the SelectionLayer. A
     * back transformation is therefore needed. If this command was not created
     * with the other constructor, a back transformation in the command handler
     * is not necessary.
     *
     * @return <code>true</code> if the command handler should transform the
     *         column positions, <code>false</code> if not.
     *
     * @since 1.6
     */
    public boolean doPositionTransformation() {
        return this.transformPositions;
    }
}
