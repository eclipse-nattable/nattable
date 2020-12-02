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
 */
public class DetailGlazedListsEventLayer<T>
        extends AbstractLayerTransform
        implements IUniqueIndexLayer, ListEventListener<T>, PropertyChangeListener {

    /**
     * The underlying layer of type {@link IUniqueIndexLayer} This is necessary
     * because {@link AbstractLayerTransform} only specifies {@link ILayer} as
     * the type of the underlying layer. But as this event layer implements
     * {@link IUniqueIndexLayer} the underlying layer needs to be of type
     * {@link IUniqueIndexLayer} too so the necessary methods can delegate to
     * it. Storing the underlying layer reference as {@link IUniqueIndexLayer}
     * in here avoids casting operations at every access.
     */
    private final IUniqueIndexLayer underlying;

    /**
     * The {@link EventList} whose events this layer is processing. Needed here
     * so it is possible to exchange the list at runtime.
     */
    private EventList<T> eventList;

    /**
     * Flag that indicates whether GlazedLists list change events are propagated
     * to the NatTable or not.
     */
    private boolean active = true;

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
    public DetailGlazedListsEventLayer(IUniqueIndexLayer underlyingLayer, EventList<T> eventList) {
        super(underlyingLayer);
        this.underlying = underlyingLayer;

        // add ourself as listener to the EventList
        this.eventList = eventList;
        this.eventList.addListEventListener(this);
    }

    // GlazedLists ListEventListener

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
        if (this.active) {
            try {
                this.eventList.getReadWriteLock().readLock().lock();

                int currentEventType = -1;

                // as the delete events in GlazedLists are containing indexes
                // that are related to prior deletes we need to ensure index
                // consistency within NatTable,
                // e.g. filtering so the complete list would be empty would
                // result in getting events that all tell that index 0 is
                // deleted
                int deleteCount = 0;

                final List<Range> deleteRanges = new ArrayList<>();
                final List<Range> insertRanges = new ArrayList<>();
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
                        insertRanges.add(new Range(event.getIndex(), event.getIndex() + 1));
                    }
                }

                internalFireEvents(deleteRanges, insertRanges);
            } finally {
                this.eventList.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /**
     * Create {@link RowDeleteEvent}s and {@link RowInsertEvent}s based on the
     * given information and fire them synchronously to the UI thread to update
     * and repaint the NatTable accordingly.
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
    private void internalFireEvents(final List<Range> deleteRanges, final List<Range> insertRanges) {
        if (!deleteRanges.isEmpty()) {
            Display.getDefault().syncExec(() -> fireLayerEvent(new RowDeleteEvent(getUnderlyingLayer(), deleteRanges)));
        }

        if (!insertRanges.isEmpty()) {
            Display.getDefault().syncExec(() -> fireLayerEvent(new RowInsertEvent(getUnderlyingLayer(), insertRanges)));
        }
    }

    // PropertyChangeListener

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // We can cast since we know that the EventList is of type T
        final PropertyUpdateEvent<T> updateEvent =
                new PropertyUpdateEvent<>(
                        this,
                        (T) event.getSource(),
                        event.getPropertyName(),
                        event.getOldValue(),
                        event.getNewValue());

        // The PropertyUpdateEvent will cause a repaint of the NatTable.
        // We need to fire the event from the SWT Display thread, otherwise
        // there will be an exception because painting can only be triggered
        // from the SWT Display thread.
        // As a property change doesn't indicate a structural change, the
        // event can be fired asynchronously.
        Display.getDefault().asyncExec(() -> fireLayerEvent(updateEvent));
    }

    /**
     * Activates the handling of GlazedLists events. By activating on receiving
     * GlazedLists change events, there will be NatTable events fired to
     * indicate that re-rendering is necessary.
     * <p>
     * This is usually necessary to perform huge updates of the data model to
     * avoid concurrency issues. By default the
     * {@link DetailGlazedListsEventLayer} is activated. You can deactivate it
     * prior performing bulk updates and activate it again after the update is
     * finished for a better event handling.
     * </p>
     * <p>
     * <b>Note:</b> When activating the list change handling again, there will
     * be no event fired in NatTable automatically. For bulk updates with
     * deactivated internal handling it is therefore necessary to fire a custom
     * event to trigger the NatTable refresh operation.
     * </p>
     *
     * @since 1.6
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
     * {@link DetailGlazedListsEventLayer} is activated. You can deactivate it
     * prior performing bulk updates and activate it again after the update is
     * finished for a better event handling.
     * </p>
     * <p>
     * <b>Note:</b> When activating the list change handling again, there will
     * be no event fired in NatTable automatically. For bulk updates with
     * deactivated internal handling it is therefore necessary to fire a custom
     * event to trigger the NatTable refresh operation.
     * </p>
     *
     * @since 1.6
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * @return Whether this {@link DetailGlazedListsEventLayer} will propagate
     *         {@link ListEvent}s into NatTable or not.
     *
     * @since 1.6
     */
    public boolean isActive() {
        return this.active;
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

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.underlying.getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.underlying.getRowPositionByIndex(rowIndex);
    }

}
