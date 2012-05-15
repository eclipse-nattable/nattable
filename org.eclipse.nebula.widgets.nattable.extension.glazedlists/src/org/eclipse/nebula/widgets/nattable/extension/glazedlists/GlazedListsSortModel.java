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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyResolver;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;

public class GlazedListsSortModel<T> implements ISortModel {

	public static final String PERSISTENCE_KEY_GLAZEDLISTS_SORT_MODEL = ".glazedListsSortModel"; //$NON-NLS-1$

	private NatTableComparatorChooser<T> comparatorChooser;
	protected final SortedList<T> sortedList;
	protected final IColumnAccessor<T> columnAccessor;
	protected final IColumnPropertyResolver columnPropertyResolver;
	protected final IConfigRegistry configRegistry;
	protected final ILayer columnHeaderDataLayer;

	public GlazedListsSortModel(SortedList<T> sortedList, IColumnPropertyAccessor<T> columnPropertyAccessor, IConfigRegistry configRegistry, ILayer dataLayer) {
		this(sortedList, columnPropertyAccessor, columnPropertyAccessor, configRegistry, dataLayer);
	}

	public GlazedListsSortModel(SortedList<T> sortedList, IColumnAccessor<T> columnAccessor, IColumnPropertyResolver columnPropertyResolver, IConfigRegistry configRegistry, ILayer dataLayer) {
		this.sortedList = sortedList;
		this.columnAccessor = columnAccessor;
		this.columnPropertyResolver = columnPropertyResolver;
		this.configRegistry = configRegistry;
		this.columnHeaderDataLayer = dataLayer;
	}

	protected NatTableComparatorChooser<T> getComparatorChooser() {
		if (comparatorChooser == null) {
			comparatorChooser =
				new NatTableComparatorChooser<T>(
						sortedList,
						new NatColumnTableFormat<T>(columnAccessor, columnPropertyResolver, configRegistry, columnHeaderDataLayer)
				);
		}

		return comparatorChooser;
	}
	
	public List<Integer> getSortedColumnIndexes() {
		return getComparatorChooser().getSortingColumns();
	}

	public int getSortOrder(int columnIndex) {
		return getComparatorChooser().getClickSequence(columnIndex);
	}

	public SortDirectionEnum getSortDirection(int columnIndex) {
		return getComparatorChooser().getSortDirectionForColumnIndex(columnIndex);
	}

	public boolean isColumnIndexSorted(int columnIndex) {
		return getComparatorChooser().isColumnIndexSorted(columnIndex);
	}
	
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		return getComparatorChooser().getComparatorsForColumn(columnIndex);
	}

	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
		getComparatorChooser().sort(columnIndex, sortDirection, accumulate);
	}

	/**
	 * Restore state by leveraging {@link AbstractTableComparatorChooser}
	 */
	public void loadState(String prefix, Properties properties) {
		Object savedObject = properties.get(prefix + PERSISTENCE_KEY_GLAZEDLISTS_SORT_MODEL);
		if(savedObject == null) {
			return;
		}
		getComparatorChooser().fromString(savedObject.toString());
	}

	/**
	 * Save state by leveraging {@link AbstractTableComparatorChooser}
	 */
	public void saveState(String prefix, Properties properties) {
		properties.put(prefix + PERSISTENCE_KEY_GLAZEDLISTS_SORT_MODEL, getComparatorChooser().toString());
	}

	public void clear() {
		getComparatorChooser().clearComparator();
	}

}
