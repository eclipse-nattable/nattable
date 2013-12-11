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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Special implementation of FilterRowComboBoxDataProvider that performs FilterRowComboUpdateEvents
 * if the underlying list is changed.
 * <p>
 * This implementation is necessary for a special case. If a filter is applied and a new row is 
 * added to the data model, the FilterList won't show the new row because the current applied 
 * filter is not aware of the new values. This is because of the inverse filter logic in then Excel 
 * like filter row. As the FilterList doesn't show the new value, there is no ListEvent fired further,
 * so the FilterRowComboBoxDataProvider is not informed about the structural change.
 * <p>
 * This implementation solves this issue by listening to the wrapped source EventList of the FilterList
 * instead of the NatTable IStructuralChangeEvent.
 * 
 * @author Dirk Fauth
 *
 */
public class GlazedListsFilterRowComboBoxDataProvider<T> 
		extends	FilterRowComboBoxDataProvider<T> 
		implements ListEventListener<T> {

	private static final Log log = LogFactory.getLog(GlazedListsFilterRowComboBoxDataProvider.class);
	
	/**
	 * @param bodyLayer A layer in the body region. Usually the DataLayer or a layer that is responsible for list event handling.
	 * 			Needed to register ourself as listener for data changes.
	 * @param baseCollection The base collection used to collect the unique values from. This need to be a collection that
	 * 			is not filtered, otherwise after modifications the content of the filter row combo boxes will only
	 * 			contain the current visible (not filtered) elements.
	 * @param columnAccessor The IColumnAccessor to be able to read the values out of the base collection objects. 
	 */
	public GlazedListsFilterRowComboBoxDataProvider(ILayer bodyLayer,
			Collection<T> baseCollection, IColumnAccessor<T> columnAccessor) {
		super(bodyLayer, baseCollection, columnAccessor);
		
		if (baseCollection instanceof EventList) {
			((EventList<T>)baseCollection).addListEventListener(this);
		}
		else {
			log.error("baseCollection is not of type EventList. List changes can not be tracked."); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
	 */
	@Override
	public void listChanged(ListEvent<T> listChanges) {
		//a new row was added or a row was deleted
		
		//remember the cache before updating
		Map<Integer, List<?>> cacheBefore = new HashMap<Integer, List<?>>(getValueCache());
		
		//perform a refresh of the whole cache
		getValueCache().clear();
		buildValueCache();
		
		//fire events for every column
		for (Map.Entry<Integer, List<?>> entry : cacheBefore.entrySet()) {
			fireCacheUpdateEvent(buildUpdateEvent(entry.getKey(), entry.getValue(), getValueCache().get(entry.getKey())));
		}
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof CellVisualChangeEvent) {
			//usually this is fired for data updates
			//so we need to update the value cache for the updated column
			int column = ((CellVisualChangeEvent)event).getColumnPosition();
			
			List<?> cacheBefore = getValueCache().get(column);
			
			getValueCache().put(column, collectValues(column));
			
			//get the diff and fire the event
			fireCacheUpdateEvent(buildUpdateEvent(column, cacheBefore, getValueCache().get(column)));
		}
	}
}
