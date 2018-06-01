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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to show a column position in the viewport.
 *
 * TODO make this an AbstractContextFreeCommand with the next major version
 */
public class ShowColumnInViewportCommand extends AbstractColumnCommand {

    private final int columnPosition;

    /**
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
     *
     * @param layer
     *            The layer to which the column position matches.<br>
     *            <b>Note: </b> This information is not used anymore.
     * @param columnPosition
     *            The column position in the underlying layer of the
     *            ViewportLayer that should be shown in the viewport.
     * @deprecated The layer parameter is not used anymore, use the constructor
     *             without the ILayer parameter.
     */
    @Deprecated
    public ShowColumnInViewportCommand(ILayer layer, int columnPosition) {
        super(layer, columnPosition);
        this.columnPosition = columnPosition;
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
    protected ShowColumnInViewportCommand(ShowColumnInViewportCommand command) {
        super(command);
        this.columnPosition = command.columnPosition;
    }

    /**
     * @return The column position in the layer below the ViewportLayer to be
     *         shown.
     */
    @Override
    public int getColumnPosition() {
        return this.columnPosition;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

    @Override
    public ShowColumnInViewportCommand cloneCommand() {
        return this;
    }

}
