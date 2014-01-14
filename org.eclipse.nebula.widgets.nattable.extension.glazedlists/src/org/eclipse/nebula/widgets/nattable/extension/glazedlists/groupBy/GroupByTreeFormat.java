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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;

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
	 * To provide sorting functionality
	 */
	private ISortModel sortModel;

	/**
	 * 
	 * @param model The GroupByModel that carries the information about the groupBy states.
	 * @param columnAccessor The IColumnAccessor that is used to get the column value
	 *			for the columns that are grouped by. Needed for compare operations and
	 *			creating the path in the tree.
	 */
	public GroupByTreeFormat(GroupByModel model, IColumnAccessor<T> columnAccessor) {
		this.model = model;
		this.columnAccessor = columnAccessor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getPath(List<Object> path, Object element) {
		List<Integer> groupByColumns = model.getGroupByColumnIndexes();
		if (!groupByColumns.isEmpty()) {
			List<Entry<Integer, Object>> descriptor = new ArrayList<Entry<Integer, Object>>();		
			for (int columnIndex : groupByColumns) {
				// Build a unique descriptor for the group
				Object columnValue = columnAccessor.getDataValue((T) element, columnIndex);
				descriptor.add(new AbstractMap.SimpleEntry<Integer, Object>(columnIndex, columnValue));
				GroupByObject groupByObject = new GroupByObject(columnValue, new ArrayList<Entry<Integer, Object>>(descriptor));
				path.add(groupByObject);
			}
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

	public void setSortModel(ISortModel model) {
		sortModel = model;
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
				} else {
					Object columnValue1 = null;
					Object columnValue2 = null;
					int result = 0;
					if (o1 instanceof GroupByObject && o2 instanceof GroupByObject) {
						columnValue1 = o1;
						columnValue2 = o2;
						result = ((GroupByObject) o1).compareTo((GroupByObject) o2);

						if (result != 0) {
							if (sortModel != null) {
								// Compare aggregated columns
								for (int sortedColumnIndex : sortModel.getSortedColumnIndexes()) {
									if (o1 instanceof GroupByObject && o2 instanceof GroupByObject) {
										GroupByObject grp1 = (GroupByObject) o1;
										GroupByObject grp2 = (GroupByObject) o2;
										columnValue1 = columnAccessor.getDataValue((T) grp1, sortedColumnIndex);
										columnValue2 = columnAccessor.getDataValue((T) grp2, sortedColumnIndex);
										if (columnValue1 != null && columnValue2 != null) {
											int res = DefaultComparator.getInstance().compare(columnValue1,
													columnValue2);
											if (res == 0) {
												continue;
											}
											switch (sortModel.getSortDirection(sortedColumnIndex)) {
											case ASC:
												result = res;
												break;
											case DESC:
												result = res * -1;
											}
										}
									}
								}
							}
						}
					} else if (o1 instanceof GroupByObject && !(o2 instanceof GroupByObject)) {
						result = 1;
					} else if (!(o1 instanceof GroupByObject) && o2 instanceof GroupByObject) {
						result = -1;
					} else {
						columnValue1 = columnAccessor.getDataValue((T) o1, columnIndex);
						columnValue2 = columnAccessor.getDataValue((T) o2, columnIndex);
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
