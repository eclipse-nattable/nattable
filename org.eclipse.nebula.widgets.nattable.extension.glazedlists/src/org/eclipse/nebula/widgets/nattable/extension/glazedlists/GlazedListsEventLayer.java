/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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
 *    <li>Glazed list events - {@link ListEvent}
 *    <li>Bean updates - PropertyChangeEvent(s)
 * </ol>
 * GlazedLists Events are conflated at a 100ms interval i.e a single {@link RowStructuralRefreshEvent}
 * is fired for any number of GlazedLists events received during that interval.
 * <p>
 * PropertyChangeEvent(s) are propagated immediately as a {@link PropertyUpdateEvent}.
 *
 * @param <T> Type of the bean in the backing list.
 */
public class GlazedListsEventLayer<T> extends AbstractLayerTransform implements IUniqueIndexLayer, ListEventListener<T>, PropertyChangeListener{

	private static final Scheduler scheduler = new Scheduler("GlazedListsEventLayer"); //$NON-NLS-1$
	private final IUniqueIndexLayer underlyingLayer;
	private final ScheduledFuture<?> future;
	private EventList<T> eventList;
	private boolean testMode = false;
    private boolean structuralChangeEventsToProcess = false;
	private boolean eventsToProcess = false;
	private boolean terminated;

	public GlazedListsEventLayer(IUniqueIndexLayer underlyingLayer, EventList<T> eventList) {
	    super(underlyingLayer);
	    this.underlyingLayer = underlyingLayer;
		this.eventList = eventList;

		this.eventList.addListEventListener(this);

		// Start the event conflation thread
		future = scheduler.scheduleAtFixedRate(getEventNotifier(),0L,100L);
	}
	
	/**
	 * Fires a NatTable refresh event, if any glazed list events have occurred.
	 */
    protected Runnable getEventNotifier() {
        return new Runnable() {
            public void run() {
                if (eventsToProcess) {
                    ILayerEvent layerEvent;
                    if (structuralChangeEventsToProcess) {
                        layerEvent = new RowStructuralRefreshEvent(getUnderlyingLayer());
                    } else {
                        layerEvent = new VisualRefreshEvent(getUnderlyingLayer());
                    }
                    fireEventFromSWTDisplayThread(layerEvent);
                }
                eventsToProcess = false;
                structuralChangeEventsToProcess = false;
            }
        };
    }
    
	/**
	 * Glazed list event handling.
	 */
	public void listChanged(ListEvent<T> event) {
        while (event.next()) {
            int eventType = event.getType();
            if (eventType == ListEvent.DELETE || eventType == ListEvent.INSERT) {
                structuralChangeEventsToProcess = true;
            }
        }
		eventsToProcess = true;
	}

	/**
	 * Object property updated event
	 */
	@SuppressWarnings("unchecked")
	public void propertyChange(PropertyChangeEvent event) {
		// We can cast since we know that the EventList is of type T
		PropertyUpdateEvent<T> updateEvent = new PropertyUpdateEvent<T>(this,
													  (T)event.getSource(),
	                                                  event.getPropertyName(),
	                                                  event.getOldValue(),
	                                                  event.getNewValue());
		fireEventFromSWTDisplayThread(updateEvent);
	}

	/**
	 * These update events are likely to cause a repaint on NatTable.
	 * If these are not thrown from the SWT Display thread, SWT
	 * will throw an Exception. Painting can only be triggered from the
	 * SWT Display thread.
	 */
	protected void fireEventFromSWTDisplayThread(final ILayerEvent event) {
		if (!testMode && Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
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
		if(!terminated && command instanceof DisposeResourcesCommand){
			terminated = true;
			scheduler.unschedule(future);
		}
		return super.doCommand(command);
	}

	public boolean isDisposed() {
		return terminated;
	}

	/**
	 * @param newEventList the {@link EventList} to listen on.
	 */
	public void setEventList(EventList<T> newEventList){
		eventList.removeListEventListener(this);
		eventList = newEventList;
		eventList.addListEventListener(this);
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	// Columns

	public int getColumnPositionByIndex(int columnIndex) {
		return underlyingLayer.getColumnPositionByIndex(columnIndex);
	}

	// Rows

	public int getRowPositionByIndex(int rowIndex) {
		return underlyingLayer.getRowPositionByIndex(rowIndex);
	}
}
