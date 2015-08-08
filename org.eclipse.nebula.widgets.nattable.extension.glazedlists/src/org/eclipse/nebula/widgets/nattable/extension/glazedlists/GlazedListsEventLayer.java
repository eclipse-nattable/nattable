/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledFuture;

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
public class GlazedListsEventLayer<T> extends AbstractLayerTransform implements
IUniqueIndexLayer, ListEventListener<T>, PropertyChangeListener {

    private static final Scheduler scheduler = new Scheduler("GlazedListsEventLayer"); //$NON-NLS-1$
    private final IUniqueIndexLayer underlyingLayer;
    private final ScheduledFuture<?> future;
    private EventList<T> eventList;
    private boolean testMode = false;
    private boolean structuralChangeEventsToProcess = false;
    private boolean eventsToProcess = false;
    private boolean terminated;

    private boolean active = true;

    public GlazedListsEventLayer(IUniqueIndexLayer underlyingLayer, EventList<T> eventList) {
        super(underlyingLayer);
        this.underlyingLayer = underlyingLayer;
        this.eventList = eventList;

        this.eventList.addListEventListener(this);

        // Start the event conflation thread
        this.future = scheduler.scheduleAtFixedRate(getEventNotifier(), 0L, 100L);
    }

    /**
     * Fires a NatTable refresh event, if any glazed list events have occurred.
     */
    protected Runnable getEventNotifier() {
        return new Runnable() {
            @Override
            public void run() {
                if (GlazedListsEventLayer.this.eventsToProcess && GlazedListsEventLayer.this.active) {
                    ILayerEvent layerEvent;
                    if (GlazedListsEventLayer.this.structuralChangeEventsToProcess) {
                        layerEvent = new RowStructuralRefreshEvent(getUnderlyingLayer());
                    } else {
                        layerEvent = new VisualRefreshEvent(getUnderlyingLayer());
                    }
                    fireEventFromSWTDisplayThread(layerEvent);

                    GlazedListsEventLayer.this.eventsToProcess = false;
                    GlazedListsEventLayer.this.structuralChangeEventsToProcess = false;
                }
            }
        };
    }

    /**
     * Glazed list event handling.
     */
    @Override
    public void listChanged(ListEvent<T> event) {
        while (event.next()) {
            int eventType = event.getType();
            if (eventType == ListEvent.DELETE || eventType == ListEvent.INSERT) {
                this.structuralChangeEventsToProcess = true;
            }
        }
        this.eventsToProcess = true;
    }

    /**
     * Object property updated event
     */
    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent event) {
        // We can cast since we know that the EventList is of type T
        PropertyUpdateEvent<T> updateEvent = new PropertyUpdateEvent<T>(
                this,
                (T) event.getSource(),
                event.getPropertyName(),
                event.getOldValue(),
                event.getNewValue());
        fireEventFromSWTDisplayThread(updateEvent);
    }

    /**
     * These update events are likely to cause a repaint on NatTable. If these
     * are not thrown from the SWT Display thread, SWT will throw an Exception.
     * Painting can only be triggered from the SWT Display thread.
     */
    protected void fireEventFromSWTDisplayThread(final ILayerEvent event) {
        if (!this.testMode && Display.getCurrent() == null) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    fireLayerEvent(event);
                }
            });
        } else {
            fireLayerEvent(event);
        }
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (!this.terminated && command instanceof DisposeResourcesCommand) {
            this.terminated = true;
            scheduler.unschedule(this.future);
        }
        return super.doCommand(command);
    }

    public boolean isDisposed() {
        return this.terminated;
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

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * Activates the handling of GlazedLists events. By activating on receiving
     * GlazedLists change events, there will be NatTable events fired to
     * indicate that re-rendering is necessary.
     * <p>
     * This is usually necessary to perform huge updates of the data model to
     * avoid concurrency issues. By default the GlazedListsEventLayer is
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
     * avoid concurrency issues. By default the GlazedListsEventLayer is
     * activated. You can deactivate it prior performing bulk updates and
     * activate it again after the update is finished for a better event
     * handling.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * @return Whether this GlazedListsEventLayer will propagate
     *         {@link ListEvent}s into NatTable or not.
     */
    public boolean isActive() {
        return this.active;
    }

    // Columns

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.underlyingLayer.getColumnPositionByIndex(columnIndex);
    }

    // Rows

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.underlyingLayer.getRowPositionByIndex(rowIndex);
    }
}
