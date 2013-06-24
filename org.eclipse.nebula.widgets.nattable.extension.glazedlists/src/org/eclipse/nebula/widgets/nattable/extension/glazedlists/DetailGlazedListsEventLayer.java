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
import java.util.concurrent.ScheduledFuture;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
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
 * 
 * Compared to the GlazedListsEventLayer, this layer does not conflate events and only
 * fire a single RowStructuralRefreshEvent for all events within 100ms. Instead it will
 * fire a corresponding NatTable event with the detail information for every {@link ListEvent}
 * fired by the GlazedLists immediately.
 * 
 * @param <T> Type of the bean in the backing list.
 * 
 * @author Dirk Fauth
 *
 */
public class DetailGlazedListsEventLayer<T> extends AbstractLayerTransform
		implements IUniqueIndexLayer, ListEventListener<T>, PropertyChangeListener {

	/**
	 * The scheduler needed to add cleanup tasks which are scheduled after handling
	 * glazedlists events.
	 */
	private static final Scheduler scheduler = new Scheduler(DetailGlazedListsEventLayer.class.getSimpleName());

	/**
	 * The underlying layer of type {@link IUniqueIndexLayer}
	 * This is necessary because {@link AbstractLayerTransform} only specifies {@link ILayer}
	 * as the type of the underlying layer. But as this event layer implements {@link IUniqueIndexLayer}
	 * the underlying layer needs to be of type {@link IUniqueIndexLayer} too so the necessary
	 * methods can delegate to it.
	 * Storing the underlying layer reference as {@link IUniqueIndexLayer} in here avoids casting
	 * operations at every access.
	 */
	private final IUniqueIndexLayer underlyingLayer;
	
	/**
	 * The {@link EventList} whose events this layer is processing.
	 * Needed here so it is possible to exchange the list at runtime.
	 */
	private EventList<T> eventList;
	
	/**
	 * The {@link ListEvent} that was handled before. Needed to ensure that the same event
	 * is not processed several times. Will be reset by the cleanup task 100ms after the handling.
	 */
	private ListEvent<T> lastFiredEvent;
	
	/**
	 * The current scheduled cleanup task which is stored to ensure that at most only one cleanup
	 * task is active at any time and it can be stopped if the NatTable itself is disposed to avoid
	 * still active background tasks that get never be done.
	 */
	private ScheduledFuture<?> cleanupFuture;
	
	/**
	 * Create a new {@link DetailGlazedListsEventLayer} which is in fact a {@link ListEventListener}
	 * that listens to GlazedLists events and translate them into events that are understandable
	 * by the NatTable.
	 * @param underlyingLayer The underlying layer of type {@link IUniqueIndexLayer}
	 * @param eventList The {@link EventList} this layer should be added as listener.
	 */
	public DetailGlazedListsEventLayer(IUniqueIndexLayer underlyingLayer, EventList<T> eventList) {
		super(underlyingLayer);
		this.underlyingLayer = underlyingLayer;
		
		//add ourself as listener to the EventList
		this.eventList = eventList;
		this.eventList.addListEventListener(this);
	}

	/* (non-Javadoc)
	 * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
	 */
	/**
	 * GlazedLists event handling.
	 */
	@Override
	public void listChanged(final ListEvent<T> event) {
		if (lastFiredEvent == null || !lastFiredEvent.equals(event)) {
			int deletedCount = 0;
			
			final List<Range> deleteRanges = new ArrayList<Range>();
			final List<Range> insertRanges = new ArrayList<Range>();
			while (event.next()) {
				int eventType = event.getType();
				if (eventType == ListEvent.DELETE) {
					int index = event.getIndex() + deletedCount;
					deleteRanges.add(new Range(index, index + 1));
					deletedCount++;
				}
				else if (eventType == ListEvent.INSERT) {
					insertRanges.add(new Range(event.getIndex(), event.getIndex() + 1));
				}
			}
			
			if (!deleteRanges.isEmpty() || !insertRanges.isEmpty()) {
				lastFiredEvent = event;
				
				scheduler.submit(new Runnable() {
					
					@Override
					public void run() {
						if (!deleteRanges.isEmpty()) {
							fireEventFromSWTDisplayThread(new RowDeleteEvent(getUnderlyingLayer(), deleteRanges));
						}
						
						if (!insertRanges.isEmpty()) {
							fireEventFromSWTDisplayThread(new RowInsertEvent(getUnderlyingLayer(), insertRanges));
						}
						
						//start cleanup task that will set the last fired event to null after 100 milliseconds
						if (cleanupFuture == null || cleanupFuture.isDone() || cleanupFuture.isCancelled()) {
							cleanupFuture = scheduler.schedule(new Runnable() {
								@Override
								public void run() {
									DetailGlazedListsEventLayer.this.lastFiredEvent = null;
								}
							}, 100L);
						}
					}
				});
			}
		}
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof DisposeResourcesCommand) {
			//ensure to kill a possible running cleanup task
			if (cleanupFuture != null) {
				scheduler.unschedule(cleanupFuture);
			}
		}
		return super.doCommand(command);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	/**
	 * Object property updated event
	 */
	@SuppressWarnings("unchecked")
	@Override
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
	 * @param event The ILayerEvent to fire from the SWT Display thread.
	 */
	protected void fireEventFromSWTDisplayThread(final ILayerEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				fireLayerEvent(event);
			}
		});
	}

	/**
	 * Change the underlying {@link EventList} this layer is listening to.
	 * @param newEventList the {@link EventList} to listen on.
	 */
	public void setEventList(EventList<T> newEventList){
		this.eventList.removeListEventListener(this);
		this.eventList = newEventList;
		this.eventList.addListEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer#getColumnPositionByIndex(int)
	 */
	@Override
	public int getColumnPositionByIndex(int columnIndex) {
		return underlyingLayer.getColumnPositionByIndex(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer#getRowPositionByIndex(int)
	 */
	@Override
	public int getRowPositionByIndex(int rowIndex) {
		return underlyingLayer.getRowPositionByIndex(rowIndex);
	}

}
