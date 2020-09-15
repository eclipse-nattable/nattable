/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.util.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Special implementation of FilterRowComboBoxDataProvider that performs
 * FilterRowComboUpdateEvents if the underlying list is changed.
 * <p>
 * This implementation is necessary for a special case. If a filter is applied
 * and a new row is added to the data model, the FilterList won't show the new
 * row because the current applied filter is not aware of the new values. This
 * is because of the inverse filter logic in then Excel like filter row. As the
 * FilterList doesn't show the new value, there is no ListEvent fired further,
 * so the FilterRowComboBoxDataProvider is not informed about the structural
 * change.
 * <p>
 * This implementation solves this issue by listening to the wrapped source
 * EventList of the FilterList instead of the NatTable IStructuralChangeEvent.
 */
public class GlazedListsFilterRowComboBoxDataProvider<T> extends
        FilterRowComboBoxDataProvider<T> implements ListEventListener<T> {

    private static final Logger LOG = LoggerFactory.getLogger(GlazedListsFilterRowComboBoxDataProvider.class);

    private static final Scheduler SCHEDULER = new Scheduler("GlazedListsFilterRowComboBoxDataProvider"); //$NON-NLS-1$

    private AtomicBoolean changeHandlingProcessing = new AtomicBoolean(false);

    private EventList<T> baseEventList;

    /**
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed to
     *            register ourself as listener for data changes.
     * @param baseCollection
     *            The base collection used to collect the unique values from.
     *            This need to be a collection that is not filtered, otherwise
     *            after modifications the content of the filter row combo boxes
     *            will only contain the current visible (not filtered) elements.
     * @param columnAccessor
     *            The IColumnAccessor to be able to read the values out of the
     *            base collection objects.
     */
    public GlazedListsFilterRowComboBoxDataProvider(
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> columnAccessor) {
        this(bodyLayer, baseCollection, columnAccessor, true);
    }

    /**
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed to
     *            register ourself as listener for data changes.
     * @param baseCollection
     *            The base collection used to collect the unique values from.
     *            This need to be a collection that is not filtered, otherwise
     *            after modifications the content of the filter row combo boxes
     *            will only contain the current visible (not filtered) elements.
     * @param columnAccessor
     *            The IColumnAccessor to be able to read the values out of the
     *            base collection objects.
     * @param lazy
     *            <code>true</code> to configure this
     *            {@link FilterRowComboBoxDataProvider} should load the combobox
     *            values lazily, <code>false</code> to pre-build the value
     *            cache.
     * @since 1.4
     */
    public GlazedListsFilterRowComboBoxDataProvider(
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> columnAccessor,
            boolean lazy) {
        super(bodyLayer, baseCollection, columnAccessor, lazy);

        if (baseCollection instanceof EventList) {
            this.baseEventList = ((EventList<T>) baseCollection);
            this.baseEventList.addListEventListener(this);
        } else {
            LOG.error("baseCollection is not of type EventList. List changes can not be tracked."); //$NON-NLS-1$
        }
    }

    @Override
    public void listChanged(ListEvent<T> listChanges) {
        if (!this.changeHandlingProcessing.getAndSet(true)) {
            // a new row was added or a row was deleted
            SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    List<FilterRowComboUpdateEvent> updateEvents = new ArrayList<FilterRowComboUpdateEvent>();
                    getValueCacheLock().writeLock().lock();
                    try {
                        // remember the cache before updating
                        Map<Integer, List<?>> cacheBefore = new HashMap<Integer, List<?>>(getValueCache());

                        // perform a refresh of the whole cache
                        getValueCache().clear();

                        if (!GlazedListsFilterRowComboBoxDataProvider.this.lazyLoading) {
                            buildValueCache();
                        } else {
                            // to determine the diff for the update event
                            // the current values need to be collected,
                            // otherwise on clear() - addAll() a full reset
                            // will be triggered since there are no cached
                            // values
                            for (Map.Entry<Integer, List<?>> entry : cacheBefore.entrySet()) {
                                getValueCache().put(entry.getKey(),
                                        collectValues(entry.getKey()));
                            }
                        }

                        // fire events for every column that has cached data
                        for (Map.Entry<Integer, List<?>> entry : cacheBefore.entrySet()) {
                            updateEvents.add(buildUpdateEvent(
                                    entry.getKey(),
                                    entry.getValue(),
                                    getValueCache().get(entry.getKey())));
                        }

                        GlazedListsFilterRowComboBoxDataProvider.this.changeHandlingProcessing.set(false);
                    } finally {
                        getValueCacheLock().writeLock().unlock();
                    }

                    if (isUpdateEventsEnabled()) {
                        for (FilterRowComboUpdateEvent event : updateEvents) {
                            fireCacheUpdateEvent(event);
                        }
                    }
                }
            }, 100);
        }
    }

    @Override
    public void handleLayerEvent(final ILayerEvent event) {
        // we only need to perform event handling if caching is enabled
        if (this.cachingEnabled) {
            if (event instanceof CellVisualChangeEvent) {
                SCHEDULER.schedule(new Runnable() {

                    @Override
                    public void run() {
                        // usually this is fired for data updates
                        // so we need to update the value cache for the updated
                        // column
                        getValueCacheLock().writeLock().lock();
                        try {
                            int column = ((CellVisualChangeEvent) event).getColumnPosition();

                            List<?> cacheBefore = getValueCache().get(column);

                            // only update the cache in case a cache was build
                            // already
                            if (!GlazedListsFilterRowComboBoxDataProvider.this.lazyLoading
                                    || cacheBefore != null) {
                                getValueCache().put(column, collectValues(column));
                            }

                            if (isUpdateEventsEnabled()) {
                                // get the diff and fire the event
                                fireCacheUpdateEvent(buildUpdateEvent(column, cacheBefore, getValueCache().get(column)));
                            }
                        } finally {
                            getValueCacheLock().writeLock().unlock();
                        }
                    }
                }, 0);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        SCHEDULER.shutdownNow();
    }

}
