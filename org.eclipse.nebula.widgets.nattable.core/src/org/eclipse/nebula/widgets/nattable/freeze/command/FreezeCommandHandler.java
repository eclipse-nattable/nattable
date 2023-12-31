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
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeHelper;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezeCommandHandler extends AbstractLayerCommandHandler<IFreezeCommand> {

    protected final FreezeLayer freezeLayer;

    protected final ViewportLayer viewportLayer;

    protected final SelectionLayer selectionLayer;

    public FreezeCommandHandler(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
        this.freezeLayer = freezeLayer;
        this.viewportLayer = viewportLayer;
        this.selectionLayer = selectionLayer;
    }

    @Override
    public Class<IFreezeCommand> getCommandClass() {
        return IFreezeCommand.class;
    }

    @Override
    public boolean doCommand(IFreezeCommand command) {
        // need to convert for correct handling in scrolled state
        // using ViewportLayer#getScrollableLayer() because the positions in the
        // further processing are taking from the scrollable layer, which does
        // not need to be the SelectionLayer in every case
        //
        // The client area check is used to handle the programmatic freeze
        // BEFORE the client area was initialized. Without this check a
        // programmatic freeze before the rendering completed is not working.
        if ((this.viewportLayer.getClientAreaWidth() == 0
                && this.viewportLayer.getClientAreaHeight() == 0)
                || command.convertToTargetLayer(this.viewportLayer.getScrollableLayer())) {
            if (command instanceof FreezeColumnCommand) {
                // freeze for a whole column
                FreezeColumnCommand freezeColumnCommand = (FreezeColumnCommand) command;
                IFreezeCoordinatesProvider coordinatesProvider = new FreezeColumnStrategy(
                        this.freezeLayer, this.viewportLayer, freezeColumnCommand.getColumnPosition());
                handleFreezeCommand(coordinatesProvider,
                        freezeColumnCommand.isToggle(), command.isOverrideFreeze());
                return true;
            } else if (command instanceof FreezeRowCommand) {
                // freeze for a whole row
                FreezeRowCommand freezeRowCommand = (FreezeRowCommand) command;
                IFreezeCoordinatesProvider coordinatesProvider = new FreezeRowStrategy(
                        this.freezeLayer, this.viewportLayer, freezeRowCommand.getRowPosition());
                handleFreezeCommand(coordinatesProvider,
                        freezeRowCommand.isToggle(), command.isOverrideFreeze());
                return true;
            } else if (command instanceof FreezePositionCommand) {
                // freeze for a given position
                FreezePositionCommand freezePositionCommand = (FreezePositionCommand) command;
                IFreezeCoordinatesProvider coordinatesProvider = new FreezePositionStrategy(
                        this.freezeLayer, this.viewportLayer,
                        freezePositionCommand.getColumnPosition(), freezePositionCommand.getRowPosition(),
                        freezePositionCommand.isInclude());
                handleFreezeCommand(coordinatesProvider,
                        freezePositionCommand.isToggle(),
                        command.isOverrideFreeze());
                return true;
            } else if (command instanceof FreezeSelectionCommand) {
                // freeze at the current selection anchor
                IFreezeCoordinatesProvider coordinatesProvider = new FreezeSelectionStrategy(
                        this.freezeLayer, this.viewportLayer, this.selectionLayer, ((FreezeSelectionCommand) command).isInclude());
                handleFreezeCommand(coordinatesProvider, command.isToggle(), command.isOverrideFreeze());
                return true;
            } else if (command instanceof UnFreezeGridCommand) {
                // unfreeze
                handleUnfreeze();
                return true;
            }
        }

        return false;
    }

    /**
     * Performs freeze actions dependent on the coordinates specified by the
     * given {@link IFreezeCoordinatesProvider} and the configuration flags. If
     * a freeze state is already active it is checked if this state should be
     * overriden or toggled. Otherwise the freeze state is applied.
     *
     * @param coordinatesProvider
     *            The {@link IFreezeCoordinatesProvider} to retrieve the freeze
     *            coordinates from
     * @param toggle
     *            whether to unfreeze if the freeze layer is already in a frozen
     *            state
     * @param override
     *            whether to override a current frozen state.
     */
    protected void handleFreezeCommand(
            IFreezeCoordinatesProvider coordinatesProvider, boolean toggle, boolean override) {

        if (!this.freezeLayer.isFrozen() || override) {
            // if we are in a frozen state and be configured to override, reset
            // the viewport first
            if (this.freezeLayer.isFrozen() && override) {
                FreezeHelper.resetViewport(this.freezeLayer, this.viewportLayer);
            }

            final PositionCoordinate topLeftPosition = coordinatesProvider.getTopLeftPosition();
            final PositionCoordinate bottomRightPosition = coordinatesProvider.getBottomRightPosition();

            FreezeHelper.freeze(this.freezeLayer, this.viewportLayer, topLeftPosition, bottomRightPosition);
        } else if (toggle) {
            // if frozen and toggle = true
            handleUnfreeze();
        }
    }

    /**
     * Unfreeze a current frozen state.
     */
    protected void handleUnfreeze() {
        FreezeHelper.unfreeze(this.freezeLayer, this.viewportLayer);
    }

}
