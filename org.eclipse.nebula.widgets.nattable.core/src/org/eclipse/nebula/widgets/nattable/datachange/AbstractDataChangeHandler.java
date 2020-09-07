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

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;

/**
 * Abstract implementation of {@link DataChangeHandler} to handle data updates.
 *
 * @since 1.6
 */
public abstract class AbstractDataChangeHandler<T extends DataChange> implements DataChangeHandler {

    /**
     * The {@link DataChangeLayer} this handler is assigned to.
     */
    protected final DataChangeLayer layer;

    /**
     * The {@link CellKeyHandler} that is used to store dataChanges for a
     * specific key.
     */
    @SuppressWarnings("rawtypes")
    protected final CellKeyHandler keyHandler;

    /**
     * Collection of modified identifiers according to the used
     * {@link CellKeyHandler} and corresponding {@link DataChange}s that are
     * collected in this handler.
     */
    protected final Map<Object, T> dataChanges;

    /**
     * Flag that is used to temporarily disable event handling. Used to not
     * handle {@link DataUpdateEvent}s on discard.
     */
    protected boolean handleDataUpdate = true;

    /**
     *
     * @param layer
     *            The {@link DataChangeLayer} this handler should be assigned
     *            to.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to store data changes
     *            for a specific key.
     * @param dataChanges
     *            The map to track the data changes locally.
     */
    public AbstractDataChangeHandler(DataChangeLayer layer, CellKeyHandler<?> keyHandler, Map<Object, T> dataChanges) {
        this.layer = layer;
        this.keyHandler = keyHandler;
        this.dataChanges = dataChanges;
    }

    @Override
    public void disableTracking() {
        this.handleDataUpdate = false;
    }

    @Override
    public void enableTracking() {
        this.handleDataUpdate = true;
    }

    @Override
    public void clearDataChanges() {
        this.dataChanges.clear();
    }

    /**
     *
     * @return The {@link CellKeyHandler} that is used to store dataChanges for
     *         a specific key.
     */
    public CellKeyHandler<?> getKeyHandler() {
        return this.keyHandler;
    }

    /**
     *
     * @return Collection of modified identifiers according to the used
     *         {@link CellKeyHandler} and corresponding {@link DataChange}s that
     *         are collected in this handler.
     */
    public Map<Object, T> getDataChanges() {
        return this.dataChanges;
    }

}
