/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to show a cell in the viewport.
 *
 * TODO make this an AbstractContextFreeCommand with the next major version
 */
public class ShowCellInViewportCommand extends AbstractPositionCommand {

    private final int columnPosition;
    private final int rowPosition;

    /**
     * @param columnPosition
     *            The column position in the underlying layer of the
     *            ViewportLayer that should be shown in the viewport.
     * @param rowPosition
     *            The row position in the underlying layer of the ViewportLayer
     *            that should be shown in the viewport.
     *
     * @since 1.6
     */
    public ShowCellInViewportCommand(int columnPosition, int rowPosition) {
        super(null, columnPosition, rowPosition);
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
    }

    /**
     *
     * @param layer
     *            The layer to which the row position matches.<br>
     *            <b>Note: </b> This information is not used anymore.
     * @param columnPosition
     *            The column position in the underlying layer of the
     *            ViewportLayer that should be shown in the viewport.
     * @param rowPosition
     *            The row position in the underlying layer of the ViewportLayer
     *            that should be shown in the viewport.
     * @deprecated The layer parameter is not used anymore, use the constructor
     *             without the ILayer parameter.
     */
    @Deprecated
    public ShowCellInViewportCommand(ILayer layer, int columnPosition, int rowPosition) {
        super(layer, columnPosition, rowPosition);
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     * @deprecated Since 1.6 this constructor is not needed anymore as the
     *             command is a context free command.
     */
    @Deprecated
    protected ShowCellInViewportCommand(ShowCellInViewportCommand command) {
        super(command);
        this.columnPosition = command.columnPosition;
        this.rowPosition = command.rowPosition;
    }

    /**
     * @return The column position in the layer below the ViewportLayer to be
     *         shown.
     */
    @Override
    public int getColumnPosition() {
        return this.columnPosition;
    }

    /**
     * @return The row position in the layer below the ViewportLayer to be
     *         shown.
     */
    @Override
    public int getRowPosition() {
        return this.rowPosition;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

    @Override
    public ShowCellInViewportCommand cloneCommand() {
        return this;
    }

}
