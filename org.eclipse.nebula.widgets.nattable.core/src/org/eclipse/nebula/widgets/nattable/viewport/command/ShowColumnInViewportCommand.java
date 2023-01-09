/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to show a column position in the viewport.
 */
public class ShowColumnInViewportCommand extends AbstractColumnCommand {

    private final int columnPosition;

    /**
     * Create a command with a fixed column position based on the underlying
     * layer of the ViewportLayer that does not get converted while processing.
     *
     * @param columnPosition
     *            The column position in the underlying layer of the
     *            ViewportLayer that should be shown in the viewport.
     *
     * @since 1.6
     */
    public ShowColumnInViewportCommand(int columnPosition) {
        super(null, columnPosition);
        this.columnPosition = columnPosition;
    }

    /**
     * Create a command with a ColumnPositionCoordinate that gets converted
     * while processing down the layer stack.
     *
     * @param layer
     *            The {@link ILayer} to which the column position correlate.
     * @param columnPosition
     *            The column position related to the given layer for which the
     *            command should be processed.
     *
     * @since 2.1
     */
    public ShowColumnInViewportCommand(ILayer layer, int columnPosition) {
        super(layer, columnPosition);
        this.columnPosition = -1;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     *
     * @since 2.1
     */
    protected ShowColumnInViewportCommand(ShowColumnInViewportCommand command) {
        super(command);
        this.columnPosition = command.columnPosition;
    }

    /**
     * @return The column position in the layer below the ViewportLayer to be
     *         shown or the column position related to the ILayer that was
     *         passed at command creation.
     */
    @Override
    public int getColumnPosition() {
        if (getLayer() != null) {
            return super.getColumnPosition();
        }
        return this.columnPosition;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        if (getLayer() != null) {
            return super.convertToTargetLayer(targetLayer);
        }
        return true;
    }

    @Override
    public ILayerCommand cloneCommand() {
        if (getLayer() != null) {
            return new ShowColumnInViewportCommand(this);
        }
        return this;
    }

}
