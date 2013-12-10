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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;

/**
 * This class is used to add tree items that are added to the tree path for grouping purposes. Contains the value that is used for grouping and the grouping index to ensure the correct ordering.
 */
public class GroupByObject implements Comparable<GroupByObject> {

	/** The columnIndex->value */
	private final List<Entry<Integer, Object>> descriptor;

	/**
	 * The value that is used for grouping.
	 */
	private final Object value;

	/**
	 * @param value
	 *            The value that is used for grouping.
	 * @param descriptor
	 *            The description of the grouping (Index->Value)
	 */
	public GroupByObject(Object value, List<Entry<Integer, Object>> descriptor) {
		this.value = value;
		this.descriptor = descriptor;
	}

	/**
	 * @return The value that is used for grouping.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return The description of the grouping (Index->Value)
	 */
	public Collection<Entry<Integer, Object>> getDescriptor() {
		return descriptor;
	}

	@Override
	public String toString() {
		// Without adjusting a lot of API and adding dependencies to the DataLayer and the ConfigRegistry
		// we can not get the IDataConverter here. It might be solvable with the next generation because
		// we can then inject the necessary values. Until then you should consider implementing toString()
		return value.toString();
	}

	@Override
	public int compareTo(GroupByObject o) {
		if (this.value.getClass().equals(o.value.getClass())) {
			return DefaultComparator.getInstance().compare(value, o.value);
		}
		return Integer.valueOf(this.descriptor.hashCode()).compareTo(o.descriptor.hashCode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
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
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
