/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.util.Scheduler;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * This layer acts as the event listener for:
 * <ol>
 * <li>GlazedLists events - {@link ListEvent}
 * <li>Bean updates - PropertyChangeEvent(s)
 * </ol>
 * GlazedLists events are conflated at a 100ms interval i.e a single
 * {@link RowStructuralRefreshEvent} is fired for any number of GlazedLists
 * events received during that interval.
 * <p>
 * PropertyChangeEvent(s) are propagated immediately as a
 * {@link PropertyUpdateEvent}.
 *
 * @param <T>
 *            Type of the bean in the backing list.
 */
public class GlazedListsEventLayer<T>
        extends AbstractLayerTransform
        implements IUniqueIndexLayer, ListEventListener<T>, PropertyChangeListener {

    private static final Scheduler scheduler = new Scheduler("GlazedListsEventLayer"); //$NON-NLS-1$
    private final IUniqueIndexLayer underlying;
    private final ScheduledFuture<?> future;
    private EventList<T> eventList;
    private boolean testMode = false;
    private boolean structuralChangeEventsToProcess = false;
    private AtomicBoolean eventsToProcess = new AtomicBoolean(false);
    private AtomicBoolean terminated = new AtomicBoolean(false);

    private boolean active = true;

    public GlazedListsEventLayer(IUniqueIndexLayer underlyingLayer, EventList<T> eventList) {
        super(underlyingLayer);
        this.underlying = underlyingLayer;
        this.eventList = eventList;

        this.eventList.addListEventListener(this);

        // Start the event conflation thread
        this.future = scheduler.scheduleAtFixedRate(getEventNotifier(), 0L, 100L);
    }

    /**
     *
     * @return The {@link Runnable} that is triggered all 100ms to fire a
     *         NatTable refresh event.
     */
    protected Runnable getEventNotifier() {
        return () -> {
            if (GlazedListsEventLayer.this.active && GlazedListsEventLayer.this.eventsToProcess.compareAndSet(true, false)) {
                ILayerEvent layerEvent;
                if (GlazedListsEventLayer.this.structuralChangeEventsToProcess) {
                    layerEvent = new RowStructuralRefreshEvent(getUnderlyingLayer());
                } else {
                    layerEvent = new VisualRefreshEvent(getUnderlyingLayer());
                }
                fireEventFromSWTDisplayThread(layerEvent);

                GlazedListsEventLayer.this.structuralChangeEventsToProcess = false;
            }
        };
    }

    // GlazedLists ListEventListener

    @Override
    public void listChanged(ListEvent<T> event) {
        while (event.next()) {
            int eventType = event.getType();
            if (eventType == ListEvent.DELETE || eventType == ListEvent.INSERT) {
                this.structuralChangeEventsToProcess = true;
            }
        }
        this.eventsToProcess.set(true);
    }

    // PropertyChangeListener

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent event) {
        // We can cast since we know that the EventList is of type T
        PropertyUpdateEvent<T> updateEvent = new PropertyUpdateEvent<>(
                this,
                (T) event.getSource(),
                event.getPropertyName(),
                event.getOldValue(),
                event.getNewValue());
        fireEventFromSWTDisplayThread(updateEvent);
    }

    /**
     * Fires the given {@link ILayerEvent} on the SWT Display thread in case
     * {@link #testMode} is <code>false</code>. Needed because the GlazedLists
     * list change handling is done in a background thread, but NatTable event
     * handling needs to be triggered in the UI thread to be able to trigger
     * repainting.
     *
     * @param event
     *            The event to fire
     */
    protected void fireEventFromSWTDisplayThread(final ILayerEvent event) {
        if (!this.testMode && Display.getCurrent() == null) {
            Display.getDefault().asyncExec(() -> fireLayerEvent(event));
        } else {
            fireLayerEvent(event);
        }
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof DisposeResourcesCommand && this.terminated.compareAndSet(false, true)) {
            scheduler.unschedule(this.future);
        }
        return super.doCommand(command);
    }

    /**
     *
     * @return <code>true</code> if this layer was terminated,
     *         <code>false</code> if it is still active.
     */
    public boolean isDisposed() {
        return this.terminated.get();
    }

    /**
     * @param newEventList
     *            the {@link EventList} to listen on.
     */
    public void setEventList(EventList<T> newEventList) {
        this.eventList.removeListEventListener(this);
        this.eventList = newEventList;
        this.eventList.addListEventListener(this);
    }

    /**
     * Activate the test mode, which is needed for unit testing. When enabling
     * the test mode, the events are not fired in the UI thread.
     *
     * @param testMode
     *            <code>true</code> to enable the test mode, <code>false</code>
     *            for real mode.
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * Activates the handling of GlazedLists events. By activating on receiving
     * GlazedLists change events, there will be NatTable events fired to
     * indicate that re-rendering is necessary.
     * <p>
     * This is usually necessary to perform huge updates of the data model to
     * avoid concurrency issues. By default the {@link GlazedListsEventLayer} is
     * activated. You can deactivate it prior performing bulk updates and
     * activate it again after the update is finished for a better event
     * handling.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the handling of GlazedLists events. By deactivating there
     * will be no NatTable events fired on GlazedLists change events.
     * <p>
     * This is usually necessary to perform huge updates of the data model to
     * avoid concurrency issues. By default the {@link GlazedListsEventLayer} is
     * activated. You can deactivate it prior performing bulk updates and
     * activate it again after the update is finished for a better event
     * handling.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * @return Whether this {@link GlazedListsEventLayer} will propagate
     *         {@link ListEvent}s into NatTable or not.
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
     * @since 1.6
     */
    public void discardEventsToProcess() {
        this.eventsToProcess.set(false);
        this.structuralChangeEventsToProcess = false;
    }

    // Columns

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.underlying.getColumnPositionByIndex(columnIndex);
    }

    // Rows

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.underlying.getRowPositionByIndex(rowIndex);
    }
}
