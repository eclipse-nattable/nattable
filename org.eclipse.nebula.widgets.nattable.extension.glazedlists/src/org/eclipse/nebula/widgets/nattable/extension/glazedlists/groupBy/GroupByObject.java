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

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;

/**
 * This class is used to add tree items that are added to the tree path for grouping purposes.
 * Contains the value that is used for grouping and the grouping index to ensure the correct
 * ordering.
 */
public class GroupByObject implements Comparable<GroupByObject> {

	/**
	 * The order index of the grouping.
	 */
	private final Integer groupByIndex;
	/**
	 * The value that is used for grouping.
	 */
	private final Object value;
	
	/**
	 * @param groupByIndex The order index of the grouping.
	 * @param value The value that is used for grouping.
	 */
	public GroupByObject(int groupByIndex, Object value) {
		this.groupByIndex = groupByIndex;
		this.value = value;
	}

	/**
	 * @return The order index of the grouping.
	 */
	public int getGroupByIndex() {
		return groupByIndex;
	}
	
	/**
	 * @return The value that is used for grouping.
	 */
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		//Without adjusting a lot of API and adding dependencies to the DataLayer and the ConfigRegistry
		//we can not get the IDataConverter here. It might be solvable with the next generation because
		//we can then inject the necessary values. Until then you should consider implementing toString()
		return value.toString();
	}

	@Override
	public int compareTo(GroupByObject o) {
		//if we have several groupings, the comparison is performed on the group by order
		int result = this.groupByIndex.compareTo(o.groupByIndex);
		
		if (result == 0) {
			//if the datatypes are not the same here, comparison is not possible
			if (this.value.getClass().equals(o.value.getClass())) {
				result = DefaultComparator.getInstance().compare(value, o.value);
			}
		}

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupByIndex;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupByObject other = (GroupByObject) obj;
		if (groupByIndex != other.groupByIndex)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
