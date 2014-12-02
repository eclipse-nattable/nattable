/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * This layer acts as the event listener for:
 * <ol>
 * <li>Glazed list events - {@link ListEvent}
 * <li>Bean updates - PropertyChangeEvent(s)
 * </ol>
 *
 * Compared to the GlazedListsEventLayer, this layer does not conflate events
 * and only fire a single RowStructuralRefreshEvent for all events within 100ms.
 * Instead it will fire a corresponding NatTable event with the detail
 * information for every {@link ListEvent} fired by the GlazedLists immediately.
 *
 * @param <T>
 *            Type of the bean in the backing list.
 *
 * @author Dirk Fauth
 *
 */
public class DetailGlazedListsEventLayer<T> extends AbstractLayerTransform
        implements IUniqueIndexLayer, ListEventListener<T>,
        PropertyChangeListener {

    /**
     * The underlying layer of type {@link IUniqueIndexLayer} This is necessary
     * because {@link AbstractLayerTransform} only specifies {@link ILayer} as
     * the type of the underlying layer. But as this event layer implements
     * {@link IUniqueIndexLayer} the underlying layer needs to be of type
     * {@link IUniqueIndexLayer} too so the necessary methods can delegate to
     * it. Storing the underlying layer reference as {@link IUniqueIndexLayer}
     * in here avoids casting operations at every access.
     */
    private final IUniqueIndexLayer underlyingLayer;

    /**
     * The {@link EventList} whose events this layer is processing. Needed here
     * so it is possible to exchange the list at runtime.
     */
    private EventList<T> eventList;

    /**
     * Create a new {@link DetailGlazedListsEventLayer} which is in fact a
     * {@link ListEventListener} that listens to GlazedLists events and
     * translate them into events that are understandable by the NatTable.
     *
     * @param underlyingLayer
     *            The underlying layer of type {@link IUniqueIndexLayer}
     * @param eventList
     *            The {@link EventList} this layer should be added as listener.
     */
    public DetailGlazedListsEventLayer(IUniqueIndexLayer underlyingLayer,
            EventList<T> eventList) {
        super(underlyingLayer);
        this.underlyingLayer = underlyingLayer;

        // add ourself as listener to the EventList
        this.eventList = eventList;
        this.eventList.addListEventListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists
     * .event.ListEvent)
     */
    /**
     * GlazedLists event handling. Will transform received GlazedLists
     * ListEvents into corresponding NatTable RowStructuralChangeEvents. Ensures
     * that no other changes can be made to the GlazedLists instance until the
     * events are processed in NatTable itself. This is necessary to avoid
     * concurrent modifications which will lead to asynchronous states of
     * NatTable and GlazedLists.
     */
    @Override
    public void listChanged(final ListEvent<T> event) {
        try {
            this.eventList.getReadWriteLock().readLock().lock();

            int currentEventType = -1;

            // as the delete events in GlazedLists are containing indexes that
            // are related
            // to prior deletes we need to ensure index consistency within
            // NatTable,
            // e.g. filtering so the complete list would be empty would result
            // in getting
            // events that all tell that index 0 is deleted
            int deleteCount = 0;

            final List<Range> deleteRanges = new ArrayList<Range>();
            final List<Range> insertRanges = new ArrayList<Range>();
            while (event.next()) {
                int eventType = event.getType();

                // first event, go ahead
                if (currentEventType == -1) {
                    currentEventType = eventType;
                } else if (currentEventType != eventType) {
                    // there is a new event type, fire the collected events
                    internalFireEvents(deleteRanges, insertRanges);

                    // and clear for clean further processing
                    deleteRanges.clear();
                    deleteCount = 0;
                    insertRanges.clear();
                }

                if (eventType == ListEvent.DELETE) {
                    int index = event.getIndex() + deleteCount;
                    deleteRanges.add(new Range(index, index + 1));
                    deleteCount++;
                } else if (eventType == ListEvent.INSERT) {
                    insertRanges.add(new Range(event.getIndex(), event
                            .getIndex() + 1));
                }
            }

            internalFireEvents(deleteRanges, insertRanges);
        } finally {
            this.eventList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Fire events with detail informations to update the NatTable accordingly.
     * <p>
     * The RowStructuralChangeEvents will cause a repaint of the NatTable. We
     * need to fire the event from the SWT Display thread, otherwise there will
     * be an exception because painting can only be triggered from the SWT
     * Display thread.
     * </p>
     * <p>
     * As there is a structural change, there need to be some processing for
     * indexes and positions in layers above this one. Therefore we need to
     * ensure that the processing is handled synchronous, otherwise we would get
     * into an asynchronous state were we try to process events based on a
     * ListEvent, while the list itself has already changed again. e.g.
     * filtering: clear + apply
     * </p>
     *
     * @param deleteRanges
     *            The ranges that were deleted and should be fired in an event.
     * @param insertRanges
     *            The ranges that were inserted and should be fired in an event.
     */
    private void internalFireEvents(final List<Range> deleteRanges,
            final List<Range> insertRanges) {
        if (!deleteRanges.isEmpty()) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    fireLayerEvent(new RowDeleteEvent(getUnderlyingLayer(),
                            deleteRanges));
                }
            });
        }

        if (!insertRanges.isEmpty()) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    fireLayerEvent(new RowInsertEvent(getUnderlyingLayer(),
                            insertRanges));
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    /**
     * Object property updated event
     */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // We can cast since we know that the EventList is of type T
        final PropertyUpdateEvent<T> updateEvent = new PropertyUpdateEvent<T>(
                this, (T) event.getSource(), event.getPropertyName(),
                event.getOldValue(), event.getNewValue());

        // The PropertyUpdateEvent will cause a repaint of the NatTable.
        // We need to fire the event from the SWT Display thread, otherwise
        // there will be an exception because painting can only be triggered
        // from the SWT Display thread.
        // As a property change doesn't indicate a structural change, the
        // event can be fired asynchronously.
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                fireLayerEvent(updateEvent);
            }
        });
    }

    /**
     * Change the underlying {@link EventList} this layer is listening to.
     *
     * @param newEventList
     *            the {@link EventList} to listen on.
     */
    public void setEventList(EventList<T> newEventList) {
        this.eventList.removeListEventListener(this);
        this.eventList = newEventList;
        this.eventList.addListEventListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer#
     * getColumnPositionByIndex(int)
     */
    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.underlyingLayer.getColumnPositionByIndex(columnIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer#
     * getRowPositionByIndex(int)
     */
    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.underlyingLayer.getRowPositionByIndex(rowIndex);
    }

}
