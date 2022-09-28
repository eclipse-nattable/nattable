/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.MaxCellBoundsHelper;
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * This command is triggered by the {@link InitializeAutoResizeRowsCommand}. The
 * selected columns picked from the {@link SelectionLayer} by the above command.
 * This handler runs as a second step.
 * <p>
 * This handler assumes that the target layer is the NatTable itself on calling
 * doCommand()
 */
public class AutoResizeRowCommandHandler implements ILayerCommandHandler<AutoResizeRowsCommand> {

    /**
     * The layer on which the command should be fired. Usually this will be the
     * GridLayer
     */
    protected final ILayer commandLayer;
    /**
     * The layer to use for calculation of the row positions. Needs to be a
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
     *            The layer to use for calculation of the row positions. Needs
     *            to be a layer at a lower position in the layer composition.
     *            Typically the body layer stack.
     */
    public AutoResizeRowCommandHandler(ILayer commandLayer, ILayer positionLayer) {
        this.commandLayer = commandLayer;
        this.positionLayer = positionLayer;
    }

    /**
     *
     * @param gridLayer
     *            The {@link GridLayer} to which this command handler should be
     *            registered
     */
    public AutoResizeRowCommandHandler(GridLayer gridLayer) {
        this.commandLayer = gridLayer;
        this.positionLayer = gridLayer.getBodyLayer();
    }

    @Override
    public Class<AutoResizeRowsCommand> getCommandClass() {
        return AutoResizeRowsCommand.class;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, AutoResizeRowsCommand command) {
        // Need to resize selected rows even if they are outside the viewport
        targetLayer.doCommand(new TurnViewportOffCommand());

        int[] rowPositions = command.getRowPositionsArray();
        int[] gridRowPositions =
                command.doPositionTransformation() ? convertFromPositionToCommandLayer(rowPositions) : rowPositions;

        int[] gridRowHeights = MaxCellBoundsHelper.getPreferredRowHeights(
                command.getConfigRegistry(),
                command.getGCFactory(),
                this.commandLayer,
                gridRowPositions);

        // only perform further actions if the heights could be calculated
        // could fail and return null for example if the GCFactory fails
        if (gridRowHeights != null && gridRowHeights.length > 0) {
            this.commandLayer.doCommand(
                    new MultiRowResizeCommand(this.commandLayer, gridRowPositions, gridRowHeights, true));

        }

        targetLayer.doCommand(new TurnViewportOnCommand());

        return true;
    }

    /**
     * Translates the row positions the layer stack upwards as the resulting
     * {@link MultiRowResizeCommand} will be fired on the command layer which is
     * on top of the position layer.
     *
     * @param rowPositions
     *            The row positions to convert to the positions in the command
     *            layer
     * @return The translated row positions for the local command layer.
     */
    protected int[] convertFromPositionToCommandLayer(int[] rowPositions) {
        int[] commandLayerRowPositions = new int[rowPositions.length];

        for (int i = 0; i < rowPositions.length; i++) {
            commandLayerRowPositions[i] =
                    this.commandLayer.underlyingToLocalRowPosition(this.positionLayer, rowPositions[i]);
        }
        return commandLayerRowPositions;
    }
}
