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
package org.eclipse.nebula.widgets.nattable.filterrow;

import static org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.util.PersistenceUtils;


/**
 * Data provider for the filter row<br/>
 * - Stores filter strings
 * - Applies them to the {@link MatcherEditor} on the {@link FilterList}
 */
public class FilterRowDataProvider<T> implements IDataProvider, IPersistable {

	private final IFilterStrategy<T> filterStrategy;
	private final ILayer columnHeaderLayer;
	private final IDataProvider columnHeaderDataProvider;
	private final IConfigRegistry configRegistry;
	
	private Map<Integer, Object> filterIndexToObjectMap = new HashMap<Integer, Object>();
	private int rowCount = 1;
	
	public FilterRowDataProvider(IFilterStrategy<T> filterStrategy, ILayer columnHeaderLayer, IDataProvider columnHeaderDataProvider, IConfigRegistry configRegistry) {
		this.filterStrategy = filterStrategy;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnHeaderDataProvider = columnHeaderDataProvider;
		this.configRegistry = configRegistry;
	}
	
	public void setFilterIndexToObjectMap(Map<Integer, Object> filterIndexToObjectMap) {
		this.filterIndexToObjectMap = filterIndexToObjectMap;
	}

	public int getColumnCount() {
		return columnHeaderDataProvider.getColumnCount();
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		return filterIndexToObjectMap.get(columnIndex);
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		columnIndex = columnHeaderLayer.getColumnIndexByPosition(columnIndex);

		if (ObjectUtils.isNotNull(newValue)) {
			filterIndexToObjectMap.put(columnIndex, newValue);
		} else {
			filterIndexToObjectMap.remove(columnIndex);
		}

		filterStrategy.applyFilter(filterIndexToObjectMap);

		columnHeaderLayer.fireLayerEvent(new FilterAppliedEvent(columnHeaderLayer));
	}

	// Load/save state

	public void saveState(String prefix, Properties properties) {
		Map<Integer, String> filterTextByIndex = new HashMap<Integer, String>();
		for(Integer columnIndex : filterIndexToObjectMap.keySet()){
			final IDisplayConverter converter = configRegistry.getConfigAttribute(
					FILTER_DISPLAY_CONVERTER, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
			filterTextByIndex.put(columnIndex, (String) converter.canonicalToDisplayValue(filterIndexToObjectMap.get(columnIndex)));
		}
		
		String string = PersistenceUtils.mapAsString(filterTextByIndex);

		if (!isEmpty(string)) {
			properties.put(prefix + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS, string);
		}
	}
	
	public void loadState(String prefix, Properties properties) {
		filterIndexToObjectMap.clear();
		
		try {
			Object property = properties.get(prefix + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);
			Map<Integer, String> filterTextByIndex = PersistenceUtils.parseString(property);
			for (Integer columnIndex : filterTextByIndex.keySet()) {
				final IDisplayConverter converter = configRegistry.getConfigAttribute(
						FILTER_DISPLAY_CONVERTER, NORMAL, FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
				filterIndexToObjectMap.put(columnIndex, converter.displayToCanonicalValue(filterTextByIndex.get(columnIndex)));
			}
		} catch (Exception e) {
			System.err.println("Error while restoring filter row text: " + e.getMessage()); //$NON-NLS-1$
		}
		
		filterStrategy.applyFilter(filterIndexToObjectMap);
	}

	public void clearAllFilters() {
		filterIndexToObjectMap.clear();
		filterStrategy.applyFilter(filterIndexToObjectMap);
	}
	
}
