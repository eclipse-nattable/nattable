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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

/**
 * {@link ILayerCommandHandler} that handles {@link UpdateDataCommand}s by
 * updating the data model. It is usually directly registered to the
 * {@link DataLayer} this command handler is associated with.
 */
public class UpdateDataCommandHandler extends AbstractLayerCommandHandler<UpdateDataCommand> {

    private static final Log LOG = LogFactory.getLog(UpdateDataCommandHandler.class);

    /**
     * The {@link DataLayer} on which the data model updates should be executed.
     */
    private final DataLayer dataLayer;

    /**
     * Flag to configure if the new value should be checked for equality with
     * the existing value. If set to <code>true</code> the check is performed
     * and the update operation will be skipped if the two values are equal. If
     * set to <code>false</code> the update is performed always.
     */
    private final boolean performEqualsCheck;

    /**
     * Creates an {@link UpdateDataCommandHandler} that performs an equals check
     * before performing a data update and does not perform an update if the
     * data value is equal to the one currently set.
     * 
     * @param dataLayer
     *            The {@link DataLayer} on which the data model updates should
     *            be executed.
     */
    public UpdateDataCommandHandler(DataLayer dataLayer) {
        this(dataLayer, true);
    }

    /**
     * @param dataLayer
     *            The {@link DataLayer} on which the data model updates should
     *            be executed.
     * @param performEqualsCheck
     *            Flag to configure if the new value should be checked for
     *            equality with the existing value. If set to <code>true</code>
     *            the check is performed and the update operation will be
     *            skipped if the two values are equal. If set to
     *            <code>false</code> the update is performed always.
     *
     * @since 1.6
     */
    public UpdateDataCommandHandler(DataLayer dataLayer, boolean performEqualsCheck) {
        this.dataLayer = dataLayer;
        this.performEqualsCheck = performEqualsCheck;
    }

    @Override
    public Class<UpdateDataCommand> getCommandClass() {
        return UpdateDataCommand.class;
    }

    @Override
    protected boolean doCommand(UpdateDataCommand command) {
        try {
            int columnPosition = command.getColumnPosition();
            int rowPosition = command.getRowPosition();

            Object currentValue = this.dataLayer.getDataValueByPosition(columnPosition, rowPosition);
            if ((!this.performEqualsCheck) ||
                    ((currentValue == null && command.getNewValue() != null)
                            || (command.getNewValue() == null && currentValue != null)
                            || (currentValue != null && command.getNewValue() != null && !currentValue.equals(command.getNewValue())))) {
                this.dataLayer.setDataValueByPosition(columnPosition, rowPosition, command.getNewValue());
                this.dataLayer.fireLayerEvent(
                        new DataUpdateEvent(this.dataLayer, columnPosition, rowPosition, currentValue, command.getNewValue()));
            }
            return true;
        } catch (Exception e) {
            LOG.error("Failed to update value to: " + command.getNewValue(), e); //$NON-NLS-1$
            return false;
        }
    }
}
