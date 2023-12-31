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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command that will trigger a data model update.
 */
public class UpdateDataCommand extends AbstractPositionCommand {

    /**
     * The value to update the data model to.
     */
    private Object newValue;

    /**
     * Create a new {@link UpdateDataCommand} based on the specified
     * information.
     *
     * @param layer
     *            The {@link ILayer} to which the columnPosition and rowPosition
     *            are resolved to. This is needed to support conversion of
     *            column and row positions from this layer to NatTable
     *            coordinates, which may be different e.g. in case of grid
     *            composition.
     * @param columnPosition
     *            The column position of the value to update.
     * @param rowPosition
     *            The row position of the value to update.
     * @param newValue
     *            The value to update the data model value to.
     */
    public UpdateDataCommand(ILayer layer, int columnPosition, int rowPosition, Object newValue) {
        super(layer, columnPosition, rowPosition);
        this.newValue = newValue;
    }

    /**
     * Create a new {@link UpdateDataCommand} based on the specified instance.
     * Mainly needed for cloning purposes.
     *
     * @param command
     *            The command to create a new instance from.
     */
    protected UpdateDataCommand(UpdateDataCommand command) {
        super(command);
        this.newValue = command.newValue;
    }

    /**
     * @return The value to update the data model to.
     */
    public Object getNewValue() {
        return this.newValue;
    }

    /**
     *
     * @param newValue
     *            The value to update the data model to.
     *
     * @since 2.1
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public UpdateDataCommand cloneCommand() {
        return new UpdateDataCommand(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " columnPosition = " + getColumnPosition() //$NON-NLS-1$
                + ", rowPosition = " + getRowPosition() //$NON-NLS-1$
                + ", newValue = " + this.newValue; //$NON-NLS-1$
    }

}
