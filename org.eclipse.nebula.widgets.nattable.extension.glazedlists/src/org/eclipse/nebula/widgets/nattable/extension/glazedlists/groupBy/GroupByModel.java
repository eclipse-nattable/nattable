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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;


public class GroupByModel extends Observable {

	private List<Integer> groupByColumnIndexes = new ArrayList<Integer>();
	private Map<Integer, SortDirectionEnum> sortDirectionMap = new HashMap<Integer, SortDirectionEnum>();
	
	public boolean addGroupByColumnIndex(int columnIndex) {
		if (!groupByColumnIndexes.contains(columnIndex)) {
			groupByColumnIndexes.add(columnIndex);
			setChanged();
			notifyObservers();
			return true;
		} else {
			// unchanged
			return false;
		}
	}
	
	public boolean removeGroupByColumnIndex(int columnIndex) {
		if (groupByColumnIndexes.contains(columnIndex)) {
			groupByColumnIndexes.remove(Integer.valueOf(columnIndex));
			sortDirectionMap.remove(columnIndex);
			setChanged();
			notifyObservers();
			return true;
		} else {
			// unchanged;
			return false;
		}
	}
	
	public void clearGroupByColumnIndexes() {
		groupByColumnIndexes.clear();
		sortDirectionMap.clear();
		setChanged();
		notifyObservers();
	}

	public List<Integer> getGroupByColumnIndexes() {
		return groupByColumnIndexes;
	}
	
	public SortDirectionEnum getSortDirection(int columnIndex) {
		return sortDirectionMap.get(columnIndex);
	}
	
}
