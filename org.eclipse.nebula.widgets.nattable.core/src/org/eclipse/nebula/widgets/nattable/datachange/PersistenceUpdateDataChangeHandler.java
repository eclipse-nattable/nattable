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
package org.eclipse.nebula.widgets.nattable.datachange;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;

/**
 * {@link DataChangeHandler} to handle {@link DataUpdateEvent}s for data changes
 * that where performed on the backing data. Creates {@link UpdateDataChange}s
 * to track data changes and to be able to revert those changes.
 *
 * @since 1.6
 */
public class PersistenceUpdateDataChangeHandler extends UpdateDataChangeHandler<PersistenceUpdateDataChange> implements ILayerEventHandler<DataUpdateEvent> {

    /**
     * Creates an {@link PersistenceUpdateDataChangeHandler} to handle
     * {@link DataUpdateEvent}s to be able to track and revert data changes.
     *
     * @param layer
     *            The {@link DataChangeLayer} this handler should be assigned
     *            to.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to store data changes
     *            for a specific key.
     */
    public PersistenceUpdateDataChangeHandler(DataChangeLayer layer, CellKeyHandler<?> keyHandler) {
        super(layer, keyHandler, new ConcurrentHashMap<>());
    }

    @Override
    public void handleLayerEvent(DataUpdateEvent event) {
        // avoid handling of DataUpdateEvents that are caused by restoring
        // the previous data states
        if (this.handleDataUpdate) {
            Object key = this.keyHandler.getKey(event.getColumnPosition(), event.getRowPosition());
            if (key != null) {
                synchronized (this.dataChanges) {
                    // store the change in the DataChangeLayer
                    this.layer.addDataChange(new PersistenceUpdateDataChange(key, event.getOldValue(), this.keyHandler));

                    // update the local storage of tracked changes
                    UpdateDataChange stored = this.dataChanges.get(key);
                    if (stored == null) {
                        // update the position tracking
                        this.changedColumns.add(event.getColumnPosition());
                        this.changedRows.add(event.getRowPosition());

                        // store the change locally
                        this.dataChanges.put(key, new PersistenceUpdateDataChange(key, event.getOldValue(), this.keyHandler));
                    } else if ((stored.getValue() != null && stored.getValue().equals(event.getNewValue())
                            || (stored.getValue() == null && event.getNewValue() == null))) {
                        // the value was changed back to the original value in
                        // the underlying layer simply remove the local storage
                        // to not showing the cell as dirty
                        this.dataChanges.remove(key);
                        // rebuild the position tracking
                        rebuildPositionCollections();
                    }
                }
            }
        }
    }

    @Override
    public Class<DataUpdateEvent> getLayerEventClass() {
        return DataUpdateEvent.class;
    }

}
