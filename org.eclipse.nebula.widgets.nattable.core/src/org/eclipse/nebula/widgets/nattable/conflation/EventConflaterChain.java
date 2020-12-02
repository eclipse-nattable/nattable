/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.conflation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.util.Scheduler;

/**
 * A Chain of Conflaters. Every conflater in the chain is given the chance to
 * queue an event. When the chain runs every conflater in the chain can run its
 * own task to handle the events as it sees fit.
 */
public class EventConflaterChain implements IEventConflater {

    public static final long DEFAULT_INITIAL_DELAY = 100;
    public static final long DEFAULT_REFRESH_INTERVAL = 20;
    private static final Scheduler scheduler = new Scheduler("EventConflaterChain"); //$NON-NLS-1$

    private final List<IEventConflater> chain = new LinkedList<>();
    private ScheduledFuture<?> future;
    private boolean started;
    private final long refreshInterval;
    private final long initialDelay;

    public EventConflaterChain() {
        this(DEFAULT_REFRESH_INTERVAL, DEFAULT_INITIAL_DELAY);
    }

    /**
     *
     * @param refreshInterval
     *            the delay between the termination of one execution and the
     *            commencement of the next
     * @param initialDelay
     *            the time to delay first execution
     * @since 2.0
     */
    public EventConflaterChain(long refreshInterval, long initialDelay) {
        this.refreshInterval = refreshInterval;
        this.initialDelay = initialDelay;
    }

    public void add(IEventConflater conflater) {
        this.chain.add(conflater);
    }

    public void start() {
        if (!this.started) {
            this.future = scheduler.scheduleWithFixedDelay(
                    getConflaterTask(), this.initialDelay, this.refreshInterval);
            this.started = true;
        }
    }

    public void stop() {
        if (this.started) {
            scheduler.unschedule(this.future);
            this.started = false;
        }
    }

    @Override
    public void addEvent(ILayerEvent event) {
        for (IEventConflater eventConflater : this.chain) {
            eventConflater.addEvent(event);
        }
    }

    @Override
    public void clearQueue() {
        for (IEventConflater eventConflater : this.chain) {
            eventConflater.clearQueue();
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        for (IEventConflater eventConflater : this.chain) {
            count = count + eventConflater.getCount();
        }
        return count;
    }

    @Override
    public Runnable getConflaterTask() {
        return () -> {
            for (IEventConflater conflater : EventConflaterChain.this.chain) {
                conflater.getConflaterTask().run();
            }
        };
    }
}
