/*******************************************************************************
 * Copyright (c) 2013, 2023 Dirk Fauth and others.
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
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
    private final ScheduledFuture<?> future;

    private AtomicBoolean eventsToProcess = new AtomicBoolean(false);

    private EventList<T> baseEventList;
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    private AtomicBoolean terminated = new AtomicBoolean(false);
    private boolean active = true;

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

        // Start the event conflation thread
        this.future = SCHEDULER.scheduleAtFixedRate(() -> {
            if (this.cachingEnabled
                    && this.active
                    && GlazedListsFilterRowComboBoxDataProvider.this.eventsToProcess.compareAndSet(true, false)) {
                clearCache(true);
            }
        }, 0L, 100L);
    }

    @Override
    public void listChanged(ListEvent<T> listChanges) {
        this.cacheLock.readLock().lock();
        try {
            // if the list is cleared, we drop the previous collection cache
            // state
            if (this.cachingEnabled && this.baseEventList.size() == 0) {
                setLastFilter(-1, null);
            }
        } finally {
            this.cacheLock.readLock().unlock();
        }

        this.eventsToProcess.set(true);
    }

    @Override
    public void handleLayerEvent(final ILayerEvent event) {
        // we only need to perform event handling if caching is enabled
        if (this.cachingEnabled
                && isEventFromBodyLayer(event)
                && event instanceof DataUpdateEvent) {
            SCHEDULER.schedule(() -> {
                // this is fired for data updates so we need to update
                // the value cache for the updated column
                updateCache(((DataUpdateEvent) event).getColumnPosition());
            }, 0);
        }

        if (event instanceof FilterAppliedEvent) {
            super.handleLayerEvent(event);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.terminated.compareAndSet(false, true)) {
            SCHEDULER.unschedule(this.future);
            SCHEDULER.shutdownNow();
        }
    }

    /**
     *
     * @return <code>true</code> if this layer was terminated,
     *         <code>false</code> if it is still active.
     * @since 2.1
     */
    public boolean isDisposed() {
        return this.terminated.get();
    }

    /**
     * Activates the handling of GlazedLists events. By activating on receiving
     * GlazedLists change events, there will be NatTable events fired to
     * indicate that re-rendering is necessary.
     * <p>
     * This is usually necessary to perform huge updates of the data model to
     * avoid concurrency issues. By default the
     * {@link GlazedListsFilterRowComboBoxDataProvider} is activated. You can
     * deactivate it prior performing bulk updates and activate it again after
     * the update is finished for a better event handling.
     *
     * @since 2.1
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the handling of GlazedLists events. By deactivating there
     * will be no NatTable events fired on GlazedLists change events.
     * <p>
     * This is usually necessary to perform huge updates of the data model to
     * avoid concurrency issues. By default the
     * {@link GlazedListsFilterRowComboBoxDataProvider} is activated. You can
     * deactivate it prior performing bulk updates and activate it again after
     * the update is finished for a better event handling.
     *
     * @since 2.1
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * @return Whether this {@link GlazedListsFilterRowComboBoxDataProvider}
     *         will propagate {@link ListEvent}s into NatTable or not.
     *
     * @since 2.1
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * This method can be used to discard event processing.
     * <p>
     * It is useful in cases scenarios where list changes are tracked while the
     * handling is deactivated. By default list changes are also tracked while
     * the handling is deactivated, so automatically a refresh is triggered on
     * activation. For cases where a custom event is fired for updates, it could
     * make sense to discard the events to process to avoid that a full refresh
     * event is triggered.
     * </p>
     *
     * @since 2.1
     */
    public void discardEventsToProcess() {
        this.eventsToProcess.set(false);
    }

    @Override
    protected List<?> collectValues(Collection<T> collection, int columnIndex) {
        if (collection instanceof EventList) {
            ca.odell.glazedlists.util.concurrent.ReadWriteLock collectionLock =
                    ((EventList<T>) collection).getReadWriteLock();
            collectionLock.readLock().lock();
            try {
                return super.collectValues(collection, columnIndex);
            } finally {
                collectionLock.readLock().unlock();
            }
        }
        return super.collectValues(collection, columnIndex);
    }
}
