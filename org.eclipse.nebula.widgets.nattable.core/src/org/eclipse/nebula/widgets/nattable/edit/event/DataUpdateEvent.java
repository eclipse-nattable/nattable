/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.event;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;

/**
 * Specialization of {@link CellVisualChangeEvent} to inform about a data update
 * that is triggered via
 * {@link org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler}.
 *
 * @since 1.6
 */
public class DataUpdateEvent extends CellVisualChangeEvent {

    private final Object oldValue;
    private final Object newValue;

    /**
     * Create a new {@link DataUpdateEvent}.
     *
     * @param layer
     *            The layer to which the position values match.
     * @param columnPosition
     *            The column position of the cell whose data was updated.
     * @param rowPosition
     *            The row position of the cell whose data was updated.
     * @param oldValue
     *            The old value before the data modification.
     * @param newValue
     *            The new value after the data modification.
     */
    public DataUpdateEvent(ILayer layer, int columnPosition, int rowPosition,
            Object oldValue, Object newValue) {
        super(layer, columnPosition, rowPosition);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Constructor that is used to clone an even.
     *
     * @param event
     *            The event that should be cloned.
     */
    protected DataUpdateEvent(DataUpdateEvent event) {
        super(event);
        this.oldValue = event.oldValue;
        this.newValue = event.newValue;
    }

    @Override
    public DataUpdateEvent cloneEvent() {
        return new DataUpdateEvent(this);
    }

    /**
     *
     * @return The old value before the data modification.
     */
    public Object getOldValue() {
        return this.oldValue;
    }

    /**
     *
     * @return The new value after the data modification.
     */
    public Object getNewValue() {
        return this.newValue;
    }
}
