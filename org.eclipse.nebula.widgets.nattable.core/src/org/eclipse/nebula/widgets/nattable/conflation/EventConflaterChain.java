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

	public static final int DEFAULT_INITIAL_DELAY = 100;
	public static final int DEFAULT_REFRESH_INTERVAL = 100;
	private static final Scheduler scheduler = new Scheduler("EventConflaterChain"); //$NON-NLS-1$

	private final List<IEventConflater> chain = new LinkedList<IEventConflater>();
	private ScheduledFuture<?> future;
	private boolean started;
	private final long refreshInterval;
	private final long initialDelay;

	public EventConflaterChain() {
		this(DEFAULT_REFRESH_INTERVAL, DEFAULT_INITIAL_DELAY);
	}

	public EventConflaterChain(int refreshInterval, int initialDelay) {
		this.refreshInterval = refreshInterval;
		this.initialDelay = initialDelay;
	}

	public void add(IEventConflater conflater) {
		chain.add(conflater);
	}

	public void start() {
		if (!started) {
			future = scheduler.scheduleWithFixedDelay(getConflaterTask(), initialDelay, refreshInterval);
			started = true;
		}
	}

	public void stop() {
		if (started) {
			scheduler.unschedule(future);
			started = false;
		}
	}

	public void addEvent(ILayerEvent event) {
		for (IEventConflater eventConflater : chain) {
			eventConflater.addEvent(event);
		}
	}

	public void clearQueue() {
		for (IEventConflater eventConflater : chain) {
			eventConflater.clearQueue();
		}
	}

	public int getCount() {
		int count = 0;
		for (IEventConflater eventConflater : chain) {
			count = count + eventConflater.getCount();
		}
		return count;
	}

	public Runnable getConflaterTask() {
		return new Runnable() {
			public void run() {
				for (IEventConflater conflater : chain) {
					conflater.getConflaterTask().run();
				}
			}
		};
	}
}
