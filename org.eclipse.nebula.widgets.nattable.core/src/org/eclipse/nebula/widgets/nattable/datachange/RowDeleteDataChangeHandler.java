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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowObjectDeleteEvent;

/**
 * {@link DataChangeHandler} to track row delete operations. Registers as
 * {@link ILayerEventHandler} for the {@link RowObjectDeleteEvent}. It is
 * intended to be used with a configuration that directly updates the backing
 * data. Temporary data storage is not supported. It therefore is able to
 * perform discard operations and will do nothing on save.
 *
 * @since 1.6
 */
public class RowDeleteDataChangeHandler extends AbstractDataChangeHandler<RowDeleteDataChange> implements ILayerEventHandler<RowObjectDeleteEvent> {

    /**
     *
     * @param layer
     *            The {@link DataChangeLayer} this handler should be assigned
     *            to.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to store dataChanges
     *            for a specific key.
     */
    public RowDeleteDataChangeHandler(DataChangeLayer layer, CellKeyHandler<?> keyHandler) {
        super(layer, keyHandler, new ConcurrentHashMap<Object, RowDeleteDataChange>());
    }

    @Override
    public void handleStructuralChange(IStructuralChangeEvent event) {
        // nothing to update on structural changes
    }

    @Override
    public boolean isColumnDirty(int columnPosition) {
        return !this.dataChanges.isEmpty();
    }

    @Override
    public boolean isRowDirty(int rowPosition) {
        // a deleted row is not shown and therefore not dirty
        return false;
    }

    @Override
    public boolean isCellDirty(int columnPosition, int rowPosition) {
        // there are no visible cells for deleted rows
        return false;
    }

    @Override
    public void handleLayerEvent(RowObjectDeleteEvent event) {
        if (this.handleDataUpdate) {
            synchronized (this.dataChanges) {
                // we need to ensure that the data changes are in reverse order
                // to ensure that inserting them back again insert in the
                // correct places
                Map<Integer, Object> map = new TreeMap<Integer, Object>(Collections.reverseOrder());
                map.putAll(event.getDeletedObjects());
                for (Map.Entry<Integer, Object> deleted : map.entrySet()) {
                    // store the change locally
                    this.dataChanges.put(deleted.getKey(), new RowDeleteDataChange(deleted.getKey(), deleted.getValue()));

                    // store the change in the DataChangeLayer
                    this.layer.addDataChange(new RowDeleteDataChange(deleted.getKey(), deleted.getValue()));
                }
            }
        }
    }

    @Override
    public Class<RowObjectDeleteEvent> getLayerEventClass() {
        return RowObjectDeleteEvent.class;
    }

}
