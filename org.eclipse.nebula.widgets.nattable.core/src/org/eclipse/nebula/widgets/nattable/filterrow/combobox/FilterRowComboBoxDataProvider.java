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
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

/**
 * IComboBoxDataProvider that provides items for a combobox in the filter row. These items are
 * calculated dynamically based on the content contained in the column it is connected to.
 * <p>
 * On creating this IComboBoxDataProvider, the possible values for all columns will be calculated
 * taking the whole data provided by the body IDataProvider into account. Therefore you shouldn't 
 * use this one if you show huge datasets at once.
 * <p>
 * As the values are cached in here, this IComboBoxDataProvider registers itself as ILayerListener
 * to the body DataLayer. If values are updated or rows get added/deleted, it will update the cache
 * accordingly.
 * 
 * @author Dirk Fauth
 *
 */
public class FilterRowComboBoxDataProvider implements IComboBoxDataProvider, ILayerListener {

	/**
	 * The IDataProvider of the body. Needed to read the available values for 
	 * the configured column.
	 */
	private final IDataProvider bodyDataProvider;
	/**
	 * The local cache for the values to show in the filter row combobox.
	 * This is needed because otherwise the calculation of the necessary values
	 * would happen everytime the combobox is opened and if a filter is applied
	 * using GlazedLists for example, the combobox would only contain the value
	 * which is currently used for filtering.
	 */
	private final Map<Integer, List<?>> valueCache =  new HashMap<Integer, List<?>>();
	
	/**
	 * @param bodyDataLayer The DataLayer of the body region.
	 */
	public FilterRowComboBoxDataProvider(DataLayer bodyDataLayer) {
		this.bodyDataProvider = bodyDataLayer.getDataProvider();
		
		//build the cache
		buildValueCache();
		
		bodyDataLayer.addLayerListener(this);
	}

	@Override
	public List<?> getValues(int columnIndex, int rowIndex) {
		return this.valueCache.get(columnIndex);
	}

	/**
	 * Builds the local value cache for all columns.
	 */
	protected void buildValueCache() {
		for (int i = 0; i < bodyDataProvider.getColumnCount(); i++) {
			this.valueCache.put(i, collectValues(i));
		}
	}
	
	/**
	 * This method returns the column indexes of the columns for which values
	 * was cached. Usually it will return all column indexes that are available
	 * in the table.
	 * @return The column indexes of the columns for which values was cached.
	 */
	public Collection<Integer> getCachedColumnIndexes() {
		return this.valueCache.keySet();
	}
	
	/**
	 * Iterates over all rows of the local body IDataProvider and collects the unique
	 * values for the given column index.
	 * @param columnIndex The column index for which the values should be collected
	 * @return List of all unique values that are contained in the body IDataProvider
	 * 			for the given column.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<?> collectValues(int columnIndex) {
		Set uniqueValues = new HashSet();
		
		for (int i = 0; i < this.bodyDataProvider.getRowCount(); i++) {
			uniqueValues.add(bodyDataProvider.getDataValue(columnIndex, i));
		}
		
		List result = new ArrayList(uniqueValues);
		if (!result.isEmpty() && result.get(0) instanceof Comparable) {
			Collections.sort(result);
		}
		
		return result;
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof CellVisualChangeEvent) {
			//usually this if fired for data updates
			//so we need to update the value cache for the updated column
			int column = ((CellVisualChangeEvent)event).getColumnPosition();
			this.valueCache.put(column, collectValues(column));
		}
		else if (event instanceof IStructuralChangeEvent
				&& ((IStructuralChangeEvent) event).isVerticalStructureChanged()) {
			//a new row was added or a row was deleted
			//perform a refresh of the whole cache
			this.valueCache.clear();
			buildValueCache();
		}
	}
}
