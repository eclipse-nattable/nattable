/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Extension of the {@link RowDeleteEvent} that additionally carries the deleted
 * objects per index. Used for example in combination with the DataChangeLayer
 * to be able to revert a delete operation.
 *
 * @since 1.6
 */
public class RowObjectDeleteEvent extends RowDeleteEvent {

    private Map<Integer, Object> deletedObjects = new HashMap<>();

    /**
     * Creates a {@link RowObjectDeleteEvent} for one deleted row object.
     *
     * @param layer
     *            The layer to which the row index is matching.
     * @param rowIndex
     *            The index of the row that was deleted.
     * @param deletedObject
     *            The deleted row object.
     */
    public RowObjectDeleteEvent(ILayer layer, int rowIndex, Object deletedObject) {
        super(layer, rowIndex);
        this.deletedObjects.put(rowIndex, deletedObject);
    }

    /**
     * Creates a {@link RowObjectDeleteEvent} for multiple deleted row objects.
     *
     * @param layer
     *            The layer to which the row indexes are matching.
     * @param deletedObjects
     *            The mapping from index to object of the deleted rows.
     */
    public RowObjectDeleteEvent(ILayer layer, Map<Integer, ?> deletedObjects) {
        super(layer, PositionUtil.getRanges(deletedObjects.keySet()));
        this.deletedObjects.putAll(deletedObjects);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected RowObjectDeleteEvent(RowObjectDeleteEvent event) {
        super(event);
        this.deletedObjects.putAll(event.deletedObjects);
    }

    /**
     *
     * @return The deleted rows mapped from row index to object.
     */
    public Map<Integer, Object> getDeletedObjects() {
        return this.deletedObjects;
    }

    @Override
    public RowObjectDeleteEvent cloneEvent() {
        return new RowObjectDeleteEvent(this);
    }
}
