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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.ASC;
import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.DESC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;


public class SortModelFixture implements ISortModel {
	List<Integer> sortedColumnIndexes;
	List<Integer> sortOrder;
	List<SortDirectionEnum> sortDirection;
	Map<Integer, List<Comparator>> columnComparators = new HashMap<Integer, List<Comparator>>();

	public SortModelFixture() {
		this(Arrays.asList(0, 5, 6, 3),
				Arrays.asList(6, 5, 3, 0),
				Arrays.asList(ASC, DESC, ASC, DESC));
	}

	public SortModelFixture(List<Integer> sortedColumnIndexes,
								List<Integer> sortOrder,
								List<SortDirectionEnum> sortDirection) {
		this.sortedColumnIndexes = sortedColumnIndexes;
		this.sortOrder = sortOrder;
		this.sortDirection = sortDirection;
	}

	public static SortModelFixture getEmptyModel() {
		return new SortModelFixture(
				new ArrayList<Integer>(),
				new ArrayList<Integer>(),
				new ArrayList<SortDirectionEnum>());
	}

	public List<Integer> getSortedColumnIndexes() {
		return sortedColumnIndexes;
	}
	
	public boolean isColumnIndexSorted(int columnIndex) {
		return sortedColumnIndexes.contains(columnIndex);
	}

	public int getSortOrder(int columnIndex) {
		if (sortedColumnIndexes.contains(columnIndex)) {
			return sortOrder.indexOf(columnIndex);
		}
		return -1;
	}

	public SortDirectionEnum getSortDirection(int columnIndex) {
		if (sortedColumnIndexes.contains(columnIndex)) {
			return sortDirection.get(sortOrder.indexOf(columnIndex));
		}
		return SortDirectionEnum.NONE;
	}
	
	public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
		return columnComparators.get(columnIndex);
	}

	public void sort(int columnIndex, SortDirectionEnum direction, boolean accumulate) {
		sortedColumnIndexes.add(columnIndex);
		sortOrder.add(columnIndex);
		sortDirection.add(direction);
	}

	public void clear() {
		// No op
	}

}
