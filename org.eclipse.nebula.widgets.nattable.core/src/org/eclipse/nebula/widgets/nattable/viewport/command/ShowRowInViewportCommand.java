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

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to show a row position in the viewport.
 *
 * TODO make this an AbstractContextFreeCommand with the next major version
 */
public class ShowRowInViewportCommand extends AbstractRowCommand {

    private final int rowPosition;

    /**
     * @param rowPosition
     *            The row position in the underlying layer of the ViewportLayer
     *            that should be shown in the viewport.
     *
     * @since 1.6
     */
    public ShowRowInViewportCommand(int rowPosition) {
        super(null, rowPosition);
        this.rowPosition = rowPosition;
    }

    /**
     *
     * @param layer
     *            The layer to which the row position matches.<br>
     *            <b>Note: </b> This information is not used anymore.
     * @param rowPosition
     *            The row position in the underlying layer of the ViewportLayer
     *            that should be shown in the viewport.
     * @deprecated The layer parameter is not used anymore, use the constructor
     *             without the ILayer parameter.
     */
    @Deprecated
    public ShowRowInViewportCommand(ILayer layer, int rowPosition) {
        super(layer, rowPosition);
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
    protected ShowRowInViewportCommand(ShowRowInViewportCommand command) {
        super(command);
        this.rowPosition = command.rowPosition;
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
    public ShowRowInViewportCommand cloneCommand() {
        return this;
    }

}
