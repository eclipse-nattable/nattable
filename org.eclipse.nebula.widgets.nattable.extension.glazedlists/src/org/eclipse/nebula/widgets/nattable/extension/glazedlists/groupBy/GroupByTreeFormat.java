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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;

import ca.odell.glazedlists.TreeList;

/**
 * The TreeList.Format that is used by the TreeList that is created and used by the
 * GroupByDataLayer. Note that the TreeList created by the GroupByDataLayer is generic for
 * Object because the groupBy functionality will add GroupByObjects to the path for creating
 * the grouping.
 * 
 * @param <T> The type of the base objects carried in the TreeList.
 */
public class GroupByTreeFormat<T> implements TreeList.Format<Object> {

	/**
	 * The GroupByModel that carries the information about the groupBy states.
	 */
	private final GroupByModel model;
	/**
	 * The IColumnAccessor that is used to get the column value for the columns that are grouped by. 
	 * Needed for compare operations and creating the path in the tree.
	 */
	private final IColumnAccessor<T> columnAccessor;
	/**
	 * Comparator that is used to sort the TreeList based on the groupBy information.
	 */
	private final GroupByComparator groupByComparator = new GroupByComparator();

	/**
	 * 
	 * @param model The GroupByModel that carries the information about the groupBy states.
	 * @param columnAccessor The IColumnAccessor that is used to get the column value 
	 * 			for the columns that are grouped by. Needed for compare operations and
	 * 			creating the path in the tree.
	 */
	public GroupByTreeFormat(GroupByModel model, IColumnAccessor<T> columnAccessor) {
		this.model = model;
		this.columnAccessor = columnAccessor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getPath(List<Object> path, Object element) {
		int groupByIndex = 0;
		for (int columnIndex : model.getGroupByColumnIndexes()) {
			//add a GroupByObject that contains the value contained in the column which is grouped
			Object columnValue = columnAccessor.getDataValue((T) element, columnIndex);
			path.add(new GroupByObject(groupByIndex++, columnValue));
		}

		path.add(element);
	}

	@Override
	public boolean allowsChildren(Object element) {
		return true;
	}

	@Override
	public Comparator<Object> getComparator(int depth) {
		//if there is no grouping we do not provide a comparator for the tree
		if (this.model.getGroupByColumnIndexes().isEmpty()) {
			return null;
		}

		return this.groupByComparator;
	}
	
	/**
	 * Comparator that is used to sort the TreeList based on the groupBy information.
	 * 
	 * @author Dirk Fauth
	 *
	 */
	class GroupByComparator implements Comparator<Object> {

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object o1, Object o2) {
			for (int columnIndex : model.getGroupByColumnIndexes()) {
				if (o1 == null) {
					if (o2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else if (o2 == null) {
					return 1;
				}
				else {
					Object columnValue1 = null;
					Object columnValue2 = null;
					int result = 0;
					if (o1 instanceof GroupByObject && o2 instanceof GroupByObject) {
						columnValue1 = o1;
						columnValue2 = o2;
						result = ((GroupByObject)o1).compareTo((GroupByObject)o2);
					}
					else if (o1 instanceof GroupByObject && !(o2 instanceof GroupByObject)) {
						result = 1;
					}
					else if (!(o1 instanceof GroupByObject) && o2 instanceof GroupByObject) {
						result = -1;
					}
					else {
						columnValue1 = columnAccessor.getDataValue((T)o1, columnIndex);
						columnValue2 = columnAccessor.getDataValue((T)o2, columnIndex);
						result = DefaultComparator.getInstance().compare(columnValue1, columnValue2);
					}
					
					if (result != 0) {
						return result;
					}
				}
			}			
			return 0;
		}
		
	}
}
