/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.blink;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;

/**
 * Cache for the update events coming in.
 *
 * This cache is used by the {@link BlinkLayer} to check if updates are
 * available for a cell (hence, does it need to blink).
 *
 * @param <T>
 *            Type of the Bean in the backing list.
 */
public class UpdateEventsCache<T> {

    /** Initial startup delay for the expired event removal task */
    public static final int INITIAL_DELAY = 100;

    /** TTL for an event in the cache. The event is deleted when this expires */
    public static final int TIME_TO_LIVE = 500;

    private final IRowIdAccessor<T> rowIdAccessor;
    private final KeyStrategy keyStrategy;
    private final ScheduledExecutorService cleanupScheduler;

    private Map<String, TimeStampedEvent> updateEvents;
    private ScheduledFuture<?> scheduledFutureCleanup;

    public UpdateEventsCache(IRowIdAccessor<T> rowIdAccessor,
            KeyStrategy keyStrategy, ScheduledExecutorService cleanupScheduler) {
        this.rowIdAccessor = rowIdAccessor;
        this.keyStrategy = keyStrategy;
        this.cleanupScheduler = cleanupScheduler;
        this.updateEvents = new HashMap<String, TimeStampedEvent>();
    }

    /**
     * We are not interested in update events which are too old and need not be
     * blinked. This task cleans them up, by looking at the received time stamp.
     */
    private Runnable getStaleUpdatesCleanupTask() {
        return new Runnable() {

            @Override
            public void run() {
                Map<String, TimeStampedEvent> recentEvents = new HashMap<String, TimeStampedEvent>();
                Date recent = new Date(System.currentTimeMillis()
                        - TIME_TO_LIVE);

                for (Map.Entry<String, TimeStampedEvent> entry : UpdateEventsCache.this.updateEvents
                        .entrySet()) {
                    if (entry.getValue().timeRecieved.after(recent)) {
                        recentEvents.put(entry.getKey(), entry.getValue());
                    }
                }
                synchronized (UpdateEventsCache.this.updateEvents) {
                    UpdateEventsCache.this.updateEvents = recentEvents;
                    checkUpdateEvents();
                }
            }

        };
    }

    private void checkUpdateEvents() {
        if (this.updateEvents.isEmpty()) {
            if (this.scheduledFutureCleanup != null) {
                this.scheduledFutureCleanup.cancel(true);
                this.scheduledFutureCleanup = null;
            }
        } else {
            if (this.scheduledFutureCleanup == null) {
                this.scheduledFutureCleanup = this.cleanupScheduler.scheduleAtFixedRate(
                        getStaleUpdatesCleanupTask(), INITIAL_DELAY,
                        TIME_TO_LIVE, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void put(PropertyUpdateEvent<T> event) {
        String key = getKey(event);
        this.updateEvents.put(key, new TimeStampedEvent(event));
        checkUpdateEvents();
    }

    protected String getKey(PropertyUpdateEvent<T> event) {
        String rowId = this.rowIdAccessor.getRowId(event.getSourceBean()).toString();
        return getKey(event.getPropertyName(), rowId);
    }

    public String getKey(String columnProperty, String rowId) {
        return this.keyStrategy.getKey(columnProperty, rowId);
    }

    public PropertyUpdateEvent<T> getEvent(String key) {
        return this.updateEvents.get(key).event;
    }

    public int getCount() {
        return this.updateEvents.size();
    }

    public boolean contains(String columnProperty, String rowId) {
        return this.updateEvents.containsKey(getKey(columnProperty, rowId));
    }

    public boolean isUpdated(String key) {
        return this.updateEvents.containsKey(key);
    }

    public void clear() {
        this.updateEvents.clear();
        checkUpdateEvents();
    }

    public void remove(String key) {
        this.updateEvents.remove(key);
        checkUpdateEvents();
    }

    /**
     * Class to keep track of the time when an event was received
     */
    private class TimeStampedEvent {
        Date timeRecieved;
        PropertyUpdateEvent<T> event;

        public TimeStampedEvent(PropertyUpdateEvent<T> event) {
            this.event = event;
            this.timeRecieved = new Date();
        }
    }

}
