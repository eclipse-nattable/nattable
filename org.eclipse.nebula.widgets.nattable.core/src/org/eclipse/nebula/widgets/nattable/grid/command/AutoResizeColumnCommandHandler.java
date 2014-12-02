/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.MaxCellBoundsHelper;
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * This command is triggered by the {@link InitializeAutoResizeColumnsCommand}.
 * The selected columns picked from the {@link SelectionLayer} by the above
 * command. This handler runs as a second step.
 * <p>
 * This handler assumes that the target layer is the NatTable itself on calling
 * doCommand()
 */
public class AutoResizeColumnCommandHandler implements
        ILayerCommandHandler<AutoResizeColumnsCommand> {

    /**
     * The layer on which the command should be fired. Usually this will be the
     * GridLayer
     */
    protected final ILayer commandLayer;
    /**
     * The layer to use for calculation of the column positions. Needs to be a
     * layer at a lower position in the layer composition. Typically the body
     * layer stack.
     */
    protected final ILayer positionLayer;

    /**
     *
     * @param commandLayer
     *            The layer on which the command should be fired. Usually this
     *            will be the GridLayer.
     * @param positionLayer
     *            The layer to use for calculation of the column positions.
     *            Needs to be a layer at a lower position in the layer
     *            composition. Typically the body layer stack.
     */
    public AutoResizeColumnCommandHandler(ILayer commandLayer,
            ILayer positionLayer) {
        this.commandLayer = commandLayer;
        this.positionLayer = positionLayer;
    }

    /**
     *
     * @param gridLayer
     *            The {@link GridLayer} to which this command handler should be
     *            registered
     */
    public AutoResizeColumnCommandHandler(GridLayer gridLayer) {
        this.commandLayer = gridLayer;
        this.positionLayer = gridLayer.getBodyLayer();
    }

    @Override
    public Class<AutoResizeColumnsCommand> getCommandClass() {
        return AutoResizeColumnsCommand.class;
    }

    @Override
    public boolean doCommand(ILayer targetLayer,
            AutoResizeColumnsCommand command) {
        // Need to resize selected columns even if they are outside the viewport
        // As this command is triggered by the InitialAutoResizeCommand we know
        // that the targetLayer is the
        // NatTable itself
        targetLayer.doCommand(new TurnViewportOffCommand());

        int[] columnPositions = ObjectUtils.asIntArray(command
                .getColumnPositions());
        int[] gridColumnPositions = convertFromPositionToCommandLayer(columnPositions);

        int[] gridColumnWidths = MaxCellBoundsHelper.getPreferredColumnWidths(
                command.getConfigRegistry(), command.getGCFactory(),
                this.commandLayer, gridColumnPositions);

        this.commandLayer.doCommand(new MultiColumnResizeCommand(this.commandLayer,
                gridColumnPositions, gridColumnWidths));
        targetLayer.doCommand(new TurnViewportOnCommand());

        return true;
    }

    /**
     * Translates the column positions the layer stack upwards as the resulting
     * {@link MultiColumnResizeCommand} will be fired on the command layer which
     * is on top of the position layer.
     *
     * @param columnPositions
     *            The column positions to convert to the positions in the
     *            command layer
     * @return The translated column positions for the local command layer.
     */
    protected int[] convertFromPositionToCommandLayer(int[] columnPositions) {
        int[] commandLayerColumnPositions = new int[columnPositions.length];

        for (int i = 0; i < columnPositions.length; i++) {
            commandLayerColumnPositions[i] = this.commandLayer
                    .underlyingToLocalColumnPosition(this.positionLayer,
                            columnPositions[i]);
        }
        return commandLayerColumnPositions;
    }

}
